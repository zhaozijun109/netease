"""
方案 A: 全 PySpark Pipeline — 创作者贡献度计算

全流程在一个 Spark 作业中完成:
  1. 读取宽表 → 特征工程 → 用户-日聚合
  2. 抽样 → toPandas → 本地训练 DML (含诊断)
  3. 模型广播 → pandas_udf 全量预测 CATE
  4. CATE 分配到文章 → 聚合文章/创作者贡献度 → 写入 Hive 分区表

运行方式:
  spark-submit --master yarn --driver-memory 16g --executor-memory 8g \
    --executor-cores 4 --num-executors 30 \
    plan_a_spark_pipeline.py
"""

import os
import pickle
import warnings
from typing import Dict

import numpy as np
import pandas as pd
from pyspark.sql import SparkSession, Window
import pyspark.sql.functions as F
import pyspark.sql.types as T

warnings.filterwarnings("ignore")

# ===========================================================================
# 配置 (与 plan_b 保持一致)
# ===========================================================================

SOURCE_TABLE = "rec.tmp_creator_contribution_degree_input_wide_table"
MODEL_LOCAL_PATH = "/tmp/dml_models/"
SAMPLE_SIZE = 5_000_000  # DML 训练抽样量

# Hive 输出表
ARTICLE_TABLE = "rec.rec_boost_article_contribution_score"
CREATOR_TABLE = "rec.rec_boost_creator_contribution_score"

# 留存目标 (与 plan_b 一致: 只用 1d + 3d)
OUTCOME_COLS = {
    "retained_1d": {"col": "retain_1d", "type": "binary", "weight": 0.4},
    "retained_3d": {"col": "retain_3d", "type": "binary", "weight": 0.6},
}

# 用户特征 (Confounders / Controls)
USER_FEATURE_COLS = [
    "active_days_1d", "active_days_3d", "active_days_7d",
    "active_days_15d", "active_days_30d",
    "send_hot_30d", "send_comment_cnt_30d", "send_collect_cnt_30d",
]

# Effect Modifiers (影响 CATE 异质性)
EFFECT_MODIFIER_COLS = [
    "active_days_7d", "active_days_30d",
    "send_hot_30d", "send_comment_cnt_30d", "send_collect_cnt_30d",
    # 以下为聚合特征, 在 user_day 聚合时生成
    "read_article_count", "read_creator_count", "avg_article_hot",
]

# 全部 Confounders (W) = 用户特征 + 聚合特征
CONFOUNDER_COLS = USER_FEATURE_COLS + [
    "read_article_count", "read_creator_count", "avg_article_hot",
    "total_likes", "total_comments", "total_shares",
    "total_collects", "total_follows",
]

TREATMENT_COL = "total_engagement_score"


# ===========================================================================
# Step 1: 特征工程
# ===========================================================================
def create_spark():
    return (
        SparkSession.builder
        .appName("CreatorContribution_PlanA")
        .config("spark.sql.shuffle.partitions", "400")
        .config("spark.sql.adaptive.enabled", "true")
        .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
        .config("spark.driver.maxResultSize", "8g")
        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .config("spark.sql.sources.partitionOverwriteMode", "dynamic")
        .enableHiveSupport()
        .getOrCreate()
    )


def load_wide_table(spark):
    """读取源宽表"""
    df = spark.sql(f"SELECT * FROM {SOURCE_TABLE}")
    print(f"[Step1] Source table loaded: {SOURCE_TABLE}")
    return df


def add_engagement_score(df):
    """
    计算每条 user-article 交互的 engagement_score

    duration 归一化: log 变换映射到 [0, 5]
    公式: 5 * log(clip(duration, 5, 600) / 5) / log(120)
    5s→0, 15s→1.15, 30s→1.87, 60s→2.57, 120s→3.27, 300s→4.18, 600s→5.0

    各因子得分上限:
      duration_norm: [0, 5]   — 时长(权重最大)
      like:          2 × pv   — 点赞
      comment:       3 × pv   — 评论
      share:         2 × pv   — 分享
      collect:       2 × pv   — 收藏
      follow:        5 × 0/1  — 关注创作者(单次)
    """
    df = df.withColumn(
        "duration_clipped",
        F.least(F.greatest(F.col("duration").cast("float"), F.lit(5.0)), F.lit(600.0))
    ).withColumn(
        "duration_norm",
        F.lit(5.0) * F.log(F.col("duration_clipped") / F.lit(5.0)) / F.log(F.lit(120.0))
    ).withColumn(
        "engagement_score",
        F.col("duration_norm")
        + 2.0 * F.coalesce(F.col("like_recommend_pv").cast("float"), F.lit(0.0))
        + 3.0 * F.coalesce(F.col("comment_pv").cast("float"), F.lit(0.0))
        + 2.0 * F.coalesce(F.col("share_pv").cast("float"), F.lit(0.0))
        + 2.0 * F.coalesce(F.col("collect_pv").cast("float"), F.lit(0.0))
        + 5.0 * F.coalesce(F.col("is_follow").cast("float"), F.lit(0.0))
    ).drop("duration_clipped")
    return df


def compute_user_weights(df):
    """
    计算用户权重 (在用户-文章粒度, 每个用户所有行权重相同)
    核心互动(评论多) 2.5 | 活跃互动(点赞/收藏多) 2.0 | 高活跃 1.5 | 普通 1.0
    """
    percentiles = df.select(
        F.expr("percentile_approx(send_comment_cnt_30d, 0.75)").alias("comment_p75"),
        F.expr("percentile_approx(send_hot_30d, 0.75)").alias("hot_p75"),
        F.expr("percentile_approx(send_collect_cnt_30d, 0.75)").alias("collect_p75"),
    ).collect()[0]

    comment_p75 = float(percentiles["comment_p75"] or 0)
    hot_p75 = float(percentiles["hot_p75"] or 0)
    collect_p75 = float(percentiles["collect_p75"] or 0)

    print(f"[Step1] P75 thresholds: comment={comment_p75}, hot={hot_p75}, collect={collect_p75}")

    df = df.withColumn(
        "user_weight",
        F.greatest(
            F.when(F.col("send_comment_cnt_30d") > comment_p75, 2.5).otherwise(1.0),
            F.when(
                (F.col("send_hot_30d") > hot_p75) | (F.col("send_collect_cnt_30d") > collect_p75),
                2.0
            ).otherwise(1.0),
            F.when(F.col("active_days_30d") >= 20, 1.5).otherwise(1.0),
        )
    )
    return df


def aggregate_user_day(df):
    """
    从用户-文章粒度聚合到用户-日粒度
    DML 需要的粒度: 每个用户每天一行, T=总交互, Y=留存, X=用户特征
    """
    user_day = df.groupBy("userid", "dt").agg(
        # Treatment
        F.sum("engagement_score").alias("total_engagement_score"),

        # 内容侧聚合特征
        F.count("postid").alias("read_article_count"),
        F.countDistinct("blog_id").alias("read_creator_count"),
        F.avg("hot").alias("avg_article_hot"),

        # 交互汇总
        F.sum("like_recommend_pv").alias("total_likes"),
        F.sum("comment_pv").alias("total_comments"),
        F.sum("share_pv").alias("total_shares"),
        F.sum("collect_pv").alias("total_collects"),
        F.sum("is_follow").alias("total_follows"),

        # 用户特征 (同一用户同一天值相同, 取 first)
        F.first("active_days_1d").alias("active_days_1d"),
        F.first("active_days_3d").alias("active_days_3d"),
        F.first("active_days_7d").alias("active_days_7d"),
        F.first("active_days_15d").alias("active_days_15d"),
        F.first("active_days_30d").alias("active_days_30d"),
        F.first("send_hot_30d").alias("send_hot_30d"),
        F.first("send_comment_cnt_30d").alias("send_comment_cnt_30d"),
        F.first("send_collect_cnt_30d").alias("send_collect_cnt_30d"),

        # 留存标签 (同一用户同一天值相同)
        F.first("retain_1d").alias("retain_1d"),
        F.first("retain_3d").alias("retain_3d"),
        # F.first("retain_active_days_7d").alias("retain_active_days_7d"),

        # 用户权重
        F.first("user_weight").alias("user_weight"),
    )

    # 填充 null 为 0
    fill_cols = (CONFOUNDER_COLS + [TREATMENT_COL]
                 + [c["col"] for c in OUTCOME_COLS.values()])
    user_day = user_day.fillna(0, subset=fill_cols)

    return user_day


# ===========================================================================
# Step 2: DML 训练 (含诊断, 与 plan_b 对齐)
# ===========================================================================
def sample_and_train(user_day_df, sample_size=SAMPLE_SIZE):
    """
    从 Spark DF 抽样到 Pandas, 训练 DML 模型
    """
    from econml.dml import CausalForestDML
    from lightgbm import LGBMRegressor

    total_count = user_day_df.count()
    sample_ratio = min(1.0, sample_size / total_count)
    print(f"[Step2] Total user-day rows: {total_count}, sample ratio: {sample_ratio:.4f}")

    sampled_pdf = user_day_df.sample(fraction=sample_ratio, seed=42).toPandas()
    print(f"[Step2] Sampled {len(sampled_pdf)} rows to Pandas")

    # 准备特征矩阵
    W = sampled_pdf[CONFOUNDER_COLS].fillna(0).values.astype(np.float32)
    X = sampled_pdf[EFFECT_MODIFIER_COLS].fillna(0).values.astype(np.float32)
    T_raw = sampled_pdf[TREATMENT_COL].fillna(0).values.astype(np.float32)

    # Treatment 预处理: log(1+T) 压缩极端值, 让效应估计更稳定
    T_arr = np.log1p(T_raw).astype(np.float32)
    print(f"[Step2] Treatment stats: raw_mean={T_raw.mean():.2f}, raw_std={T_raw.std():.2f}, "
          f"log_mean={T_arr.mean():.2f}, log_std={T_arr.std():.2f}")

    models = {}
    diagnostics = {}

    for outcome_name, config in OUTCOME_COLS.items():
        print(f"\n[Step2] Training DML for: {outcome_name}")
        Y = sampled_pdf[config["col"]].fillna(0).values.astype(np.float32)

        print(f"  Y stats: mean={Y.mean():.4f}, std={Y.std():.4f}, positive_rate={Y.mean():.4f}")

        lgbm_params = dict(
            n_estimators=200, max_depth=8, learning_rate=0.05,
            num_leaves=127, subsample=0.8, colsample_bytree=0.8,
            min_child_samples=30, n_jobs=-1, verbose=-1,
        )
        # EconML CausalForestDML 对 model_y 调用 .predict(), 所以 binary Y 也用 Regressor
        model_y = LGBMRegressor(**lgbm_params)
        model_t = LGBMRegressor(**lgbm_params)

        dml = CausalForestDML(
            model_y=model_y, model_t=model_t,
            n_estimators=200, max_depth=8,
            min_samples_leaf=20,             # 允许更细粒度的分裂
            min_impurity_decrease=1e-8,      # 不阻止微小效应的分裂
            min_balancedness_tol=0.3,        # 允许更灵活的分裂
            cv=3, random_state=42, n_jobs=-1,
        )

        print(f"  Fitting: W={W.shape}, X={X.shape}, T={T_arr.shape}, Y={Y.shape}")
        dml.fit(Y=Y, T=T_arr, X=X, W=W)

        # 诊断
        ate = dml.ate(X=X)
        cate_train = dml.effect(X=X).flatten()
        diag = {
            "ate": float(ate),
            "cate_mean": float(np.mean(cate_train)),
            "cate_std": float(np.std(cate_train)),
            "cate_p10": float(np.percentile(cate_train, 10)),
            "cate_p50": float(np.percentile(cate_train, 50)),
            "cate_p90": float(np.percentile(cate_train, 90)),
        }
        diagnostics[outcome_name] = diag

        try:
            ate_inf = dml.ate_inference(X=X)
            ci = ate_inf.conf_int_mean()
            diag["ate_ci_lower"] = float(ci[0][0])
            diag["ate_ci_upper"] = float(ci[1][0])
        except Exception:
            pass

        _print_diagnostics(outcome_name, diag)
        models[outcome_name] = dml

    # 保存模型到本地
    os.makedirs(MODEL_LOCAL_PATH, exist_ok=True)
    model_file = os.path.join(MODEL_LOCAL_PATH, "dml_models.pkl")
    with open(model_file, "wb") as f:
        pickle.dump({"models": models, "diagnostics": diagnostics}, f)
    print(f"\n[Step2] Models saved to {model_file}")

    return models


def _print_diagnostics(outcome_name: str, diag: dict):
    """自动分析并打印模型诊断结论"""
    ate = diag["ate"]
    mean = diag["cate_mean"]
    std = diag["cate_std"]
    p10, p50, p90 = diag["cate_p10"], diag["cate_p50"], diag["cate_p90"]

    print(f"\n  ── {outcome_name} 诊断报告 ──")

    ci_lower = diag.get("ate_ci_lower")
    ci_upper = diag.get("ate_ci_upper")
    if ci_lower is not None:
        print(f"  ATE = {ate:.6f}  95% CI = [{ci_lower:.6f}, {ci_upper:.6f}]")
        if ci_lower > 0:
            print(f"  ✅ ATE 显著为正 (CI不含0), 交互对留存有因果效应")
        elif ci_upper < 0:
            print(f"  🔴 ATE 显著为负, 模型或数据可能有问题!")
        else:
            print(f"  🟡 ATE 不显著 (CI包含0), 因果效应可能较弱")
    else:
        print(f"  ATE = {ate:.6f}  (CI 未计算)")
        if ate < 0:
            print(f"  🔴 ATE 为负, 不符合预期!")
        elif ate < 0.0005:
            print(f"  🟡 ATE 接近0, 因果效应可能很弱")
        elif ate > 0.05:
            print(f"  🟡 ATE 偏大 (>{0.05}), 可能存在残余混淆")
        else:
            print(f"  ✅ ATE 在合理区间")

    print(f"  CATE: mean={mean:.6f}, std={std:.6f}")
    print(f"        p10={p10:.6f}, p50={p50:.6f}, p90={p90:.6f}")

    if abs(ate) > 1e-8 and abs(mean - ate) / abs(ate) > 0.2:
        print(f"  🟡 CATE mean 与 ATE 偏差 > 20%, 预测可能有偏")
    else:
        print(f"  ✅ CATE mean ≈ ATE, 预测无偏")

    if abs(mean) > 1e-8:
        cv = std / abs(mean)
        if cv < 0.1:
            print(f"  🟡 异质性很小 (CV={cv:.2f}), 所有用户效应接近, DML价值有限")
        elif cv > 5.0:
            print(f"  🟡 异质性过大 (CV={cv:.2f}), CATE分布可能受噪音影响")
        else:
            print(f"  ✅ 异质性合理 (CV={cv:.2f}), 不同用户效应有差异")
    else:
        print(f"  🟡 CATE均值接近0, 无法评估异质性")

    if p10 < 0 and p50 > 0:
        print(f"  🟡 约10%~用户的CATE为负, 可检查低活跃用户子群")
    elif p50 < 0:
        print(f"  🔴 超过50%用户CATE为负, 模型结果不合理!")
    else:
        print(f"  ✅ CATE几乎全为正值, 符合预期")

    if p10 > 0:
        ratio = p90 / p10
        print(f"  📊 效应范围: 高效应用户(p90)是低效应用户(p10)的 {ratio:.1f} 倍")
    print()


# ===========================================================================
# Step 3: 全量 CATE 预测 (Spark pandas_udf)
# ===========================================================================
def predict_cate_spark(spark, user_day_df, models):
    """
    使用 pandas_udf 将 DML 模型应用到全量 user-day 数据
    """

    # 序列化模型并广播
    model_bytes = pickle.dumps(models)
    bc_model = spark.sparkContext.broadcast(model_bytes)

    # 输出 schema (只含实际训练的 outcome)
    output_fields = [
        T.StructField("userid", T.LongType()),
        T.StructField("dt", T.StringType()),
        T.StructField("user_weight", T.FloatType()),
        T.StructField("total_engagement_score", T.FloatType()),
    ]
    for outcome_name in OUTCOME_COLS:
        output_fields.append(T.StructField(f"cate_{outcome_name}", T.FloatType()))
    output_fields.append(T.StructField("cate_combined", T.FloatType()))
    output_schema = T.StructType(output_fields)

    effect_mod_cols = EFFECT_MODIFIER_COLS
    outcome_cols_cfg = OUTCOME_COLS

    @F.pandas_udf(output_schema, F.PandasUDFType.GROUPED_MAP)
    def predict_cate_udf(pdf):
        """对每个分区的 Pandas DF 预测 CATE"""
        mdls = pickle.loads(bc_model.value)
        X_arr = pdf[effect_mod_cols].fillna(0).values.astype(np.float32)

        result = pdf[["userid", "dt", "user_weight", "total_engagement_score"]].copy()
        result["user_weight"] = result["user_weight"].astype(np.float32)
        result["total_engagement_score"] = result["total_engagement_score"].astype(np.float32)

        weighted_cate = np.zeros(len(pdf), dtype=np.float32)
        for outcome_name, config in outcome_cols_cfg.items():
            dml = mdls[outcome_name]
            cate = dml.effect(X=X_arr).flatten().astype(np.float32)
            result[f"cate_{outcome_name}"] = cate

            # 归一化到 [0,1] 后加权
            p1, p99 = np.percentile(cate, 1), np.percentile(cate, 99)
            clipped = np.clip(cate, p1, p99)
            mn, mx = clipped.min(), clipped.max()
            norm = (clipped - mn) / (mx - mn + 1e-10)
            weighted_cate += config["weight"] * norm

        result["cate_combined"] = weighted_cate
        return result

    # 按 dt 分组, 保证每个分区数据量适中
    cate_df = user_day_df.groupBy("dt").apply(predict_cate_udf)
    print("[Step3] CATE prediction done via pandas_udf.")
    return cate_df


# ===========================================================================
# Step 4: 贡献度聚合 + Hive 输出
# ===========================================================================
def create_hive_tables(spark):
    """如果 Hive 表不存在则创建"""

    spark.sql(f"""
        CREATE TABLE IF NOT EXISTS {ARTICLE_TABLE} (
            postid            BIGINT      COMMENT '文章ID',
            blog_id           STRING      COMMENT '创作者ID',
            contribution_1d   DOUBLE      COMMENT '次日留存贡献度',
            contribution_3d   DOUBLE      COMMENT '3日留存贡献度',
            contribution_final DOUBLE     COMMENT '综合贡献度',
            reader_count      BIGINT      COMMENT '阅读用户数',
            weighted_reader_count DOUBLE  COMMENT '加权阅读用户数',
            contribution_rank  BIGINT     COMMENT '贡献度排名',
            contribution_percentile DOUBLE COMMENT '贡献度百分位'
        )
        PARTITIONED BY (dt STRING COMMENT '日期分区')
        STORED AS PARQUET
    """)

    spark.sql(f"""
        CREATE TABLE IF NOT EXISTS {CREATOR_TABLE} (
            blog_id           STRING      COMMENT '创作者ID',
            article_count     BIGINT      COMMENT '文章数',
            total_reader_count BIGINT     COMMENT '总阅读用户数',
            contribution_1d   DOUBLE      COMMENT '次日留存贡献度',
            contribution_3d   DOUBLE      COMMENT '3日留存贡献度',
            contribution_final DOUBLE     COMMENT '综合贡献度',
            avg_article_contribution DOUBLE COMMENT '篇均贡献度',
            contribution_rank  BIGINT     COMMENT '贡献度排名',
            contribution_percentile DOUBLE COMMENT '贡献度百分位',
            contribution_level STRING     COMMENT '贡献等级 S/A/B/C/D'
        )
        PARTITIONED BY (dt STRING COMMENT '日期分区')
        STORED AS PARQUET
    """)

    print(f"[Step4] Hive tables ready: {ARTICLE_TABLE}, {CREATOR_TABLE}")


def distribute_and_aggregate(spark, wide_df, cate_df):
    """
    将 CATE 分配回文章级, 聚合到文章/创作者, 按 dt 分区
    """

    # 1. 文章明细 JOIN CATE
    article_detail = wide_df.select(
        "userid", "dt", "postid", "blog_id",
        "engagement_score",
    )

    cate_slim = cate_df.select(
        "userid", "dt", "user_weight",
        "cate_retained_1d", "cate_retained_3d",
        "cate_combined",
    )

    joined = article_detail.join(cate_slim, on=["userid", "dt"], how="inner")

    # 2. 文章级因果贡献 = CATE × engagement_score × user_weight
    joined = (
        joined
        .withColumn("contrib_1d",
                     F.col("cate_retained_1d") * F.col("engagement_score") * F.col("user_weight"))
        .withColumn("contrib_3d",
                     F.col("cate_retained_3d") * F.col("engagement_score") * F.col("user_weight"))
        .withColumn("contrib_combined",
                     F.col("cate_combined") * F.col("engagement_score") * F.col("user_weight"))
    )

    # 3. 聚合到文章粒度 (按 dt 分组)
    article_contrib = joined.groupBy("dt", "postid", "blog_id").agg(
        F.sum("contrib_1d").alias("contribution_1d"),
        F.sum("contrib_3d").alias("contribution_3d"),
        F.sum("contrib_combined").alias("contribution_final"),
        F.count("userid").alias("reader_count"),
        F.sum("user_weight").alias("weighted_reader_count"),
    )

    # 排名 (按 dt 分区内排名)
    w_art = Window.partitionBy("dt").orderBy(F.desc("contribution_final"))
    w_art_pct = Window.partitionBy("dt").orderBy("contribution_final")
    article_contrib = (
        article_contrib
        .withColumn("contribution_rank", F.row_number().over(w_art))
        .withColumn("contribution_percentile", F.percent_rank().over(w_art_pct) * 100)
    )

    # 4. 聚合到创作者粒度 (按 dt 分组)
    creator_contrib = article_contrib.groupBy("dt", "blog_id").agg(
        F.count("postid").alias("article_count"),
        F.sum("reader_count").alias("total_reader_count"),
        F.sum("contribution_1d").alias("contribution_1d"),
        F.sum("contribution_3d").alias("contribution_3d"),
        F.sum("contribution_final").alias("contribution_final"),
        F.avg("contribution_final").alias("avg_article_contribution"),
    )

    w_cr = Window.partitionBy("dt").orderBy(F.desc("contribution_final"))
    w_cr_pct = Window.partitionBy("dt").orderBy("contribution_final")
    creator_contrib = (
        creator_contrib
        .withColumn("contribution_rank", F.row_number().over(w_cr))
        .withColumn("contribution_percentile", F.percent_rank().over(w_cr_pct) * 100)
        .withColumn("contribution_level",
                     F.when(F.col("contribution_percentile") >= 95, "S")
                     .when(F.col("contribution_percentile") >= 85, "A")
                     .when(F.col("contribution_percentile") >= 70, "B")
                     .when(F.col("contribution_percentile") >= 50, "C")
                     .otherwise("D"))
    )

    return article_contrib, creator_contrib


def save_to_hive(article_contrib, creator_contrib):
    """写入 Hive 分区表 (按 dt 动态分区)"""

    # 文章贡献度表: dt 作为分区字段放最后
    article_output = article_contrib.select(
        "postid", "blog_id",
        "contribution_1d", "contribution_3d", "contribution_final",
        "reader_count", "weighted_reader_count",
        "contribution_rank", "contribution_percentile",
        "dt",
    )
    article_output.write.mode("overwrite").insertInto(ARTICLE_TABLE, overwrite=True)
    print(f"[Step4] Article contributions written to {ARTICLE_TABLE}")

    # 创作者贡献度表
    creator_output = creator_contrib.select(
        "blog_id",
        "article_count", "total_reader_count",
        "contribution_1d", "contribution_3d", "contribution_final",
        "avg_article_contribution",
        "contribution_rank", "contribution_percentile", "contribution_level",
        "dt",
    )
    creator_output.write.mode("overwrite").insertInto(CREATOR_TABLE, overwrite=True)
    print(f"[Step4] Creator contributions written to {CREATOR_TABLE}")

    # 打印分区信息
    dts = article_contrib.select("dt").distinct().collect()
    print(f"[Step4] Written partitions: {[r['dt'] for r in dts]}")


def quality_check(article_contrib, creator_contrib):
    """数据质量检查"""
    print("\n" + "=" * 60)
    print("[QC] 文章贡献度分布:")
    article_contrib.select(
        F.count("*").alias("total"),
        F.mean("contribution_final").alias("avg"),
        F.expr("percentile_approx(contribution_final, 0.5)").alias("median"),
        F.stddev("contribution_final").alias("std"),
    ).show(truncate=False)

    print("[QC] 创作者等级分布:")
    creator_contrib.groupBy("contribution_level").agg(
        F.count("*").alias("count"),
        F.mean("contribution_final").alias("avg_contribution"),
        F.mean("article_count").alias("avg_articles"),
    ).orderBy("contribution_level").show(truncate=False)
    print("=" * 60)


# ===========================================================================
# Main
# ===========================================================================
def main():
    spark = create_spark()

    # Step 1: 特征工程
    print("\n[Pipeline] ====== Step 1: Feature Engineering ======")
    wide_df = load_wide_table(spark)
    wide_df = add_engagement_score(wide_df)
    wide_df = compute_user_weights(wide_df)
    wide_df.cache()

    user_day_df = aggregate_user_day(wide_df)
    user_day_df.cache()
    print(f"[Step1] User-day aggregation done. Rows: {user_day_df.count()}")

    # Step 2: DML 训练
    print("\n[Pipeline] ====== Step 2: DML Training ======")
    models = sample_and_train(user_day_df)

    # Step 3: 全量 CATE 预测
    print("\n[Pipeline] ====== Step 3: CATE Prediction ======")
    cate_df = predict_cate_spark(spark, user_day_df, models)
    cate_df.cache()

    # Step 4: 贡献度聚合 + Hive 输出
    print("\n[Pipeline] ====== Step 4: Contribution Aggregation ======")
    create_hive_tables(spark)
    article_contrib, creator_contrib = distribute_and_aggregate(spark, wide_df, cate_df)

    # 质量检查
    quality_check(article_contrib, creator_contrib)

    # 写入 Hive
    save_to_hive(article_contrib, creator_contrib)

    spark.stop()
    print("\n[Pipeline] Done!")


if __name__ == "__main__":
    main()