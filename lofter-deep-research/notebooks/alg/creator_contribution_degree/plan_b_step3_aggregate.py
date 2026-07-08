"""
方案 B — Step 3: PySpark 贡献度聚合

读回 Step2 输出的 CATE 结果, JOIN 用户-文章明细, 聚合到文章/创作者
输出到 Hive 表, 按日期 dt 分区

运行方式:
  spark-submit --master yarn --driver-memory 8g --executor-memory 8g \
    --num-executors 20 plan_b_step3_aggregate.py \
    --data_dir ./contribution_export/
"""

import argparse
from pyspark.sql import SparkSession, Window
import pyspark.sql.functions as F


ARTICLE_TABLE = "rec.rec_boost_article_contribution_score"
CREATOR_TABLE = "rec.rec_boost_creator_contribution_score"


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--data_dir", type=str, default="hdfs://gy-cluster8/user/rec/zyp/rec_items_pool/blog_contribution/",
                        help="Step1 导出目录 (含 user_article_detail)")
    parser.add_argument("--cate_dir", type=str, default="hdfs://gy-cluster8/user/rec/zyp/rec_items_pool/blog_contribution/cate/",
                        help="Step2 输出的 CATE 目录")
    parser.add_argument("--article_table", type=str, default=ARTICLE_TABLE,
                        help="文章贡献度 Hive 输出表")
    parser.add_argument("--creator_table", type=str, default=CREATOR_TABLE,
                        help="创作者贡献度 Hive 输出表")
    return parser.parse_args()


def create_spark():
    return (
        SparkSession.builder
        .appName("CreatorContribution_PlanB_Step3")
        .config("spark.sql.shuffle.partitions", "200")
        .config("spark.sql.adaptive.enabled", "true")
        .config("spark.sql.sources.partitionOverwriteMode", "dynamic")
        .enableHiveSupport()
        .getOrCreate()
    )


def create_hive_tables(spark, article_table, creator_table):
    """如果 Hive 表不存在则创建"""

    spark.sql(f"""
        CREATE TABLE IF NOT EXISTS {article_table} (
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
        CREATE TABLE IF NOT EXISTS {creator_table} (
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

    print(f"[Step3] Hive tables ready: {article_table}, {creator_table}")


def main():
    args = parse_args()
    spark = create_spark()

    # 0. 建表
    create_hive_tables(spark, args.article_table, args.creator_table)

    # 1. 加载数据
    cate_df = spark.read.parquet(f"{args.cate_dir}/user_day_cate.parquet")
    article_detail = spark.read.parquet(f"{args.data_dir}/user_article_detail")
    print(f"[Step3] CATE loaded: {cate_df.count()} rows")
    print(f"[Step3] Article detail loaded: {article_detail.count()} rows")

    # 查看 CATE 有哪些列, 适配 Step2 实际输出
    cate_columns = cate_df.columns
    print(f"[Step3] CATE columns: {cate_columns}")

    has_1d = "cate_retained_1d" in cate_columns
    has_3d = "cate_retained_3d" in cate_columns

    # 2. JOIN: 文章明细 ← CATE
    cate_select_cols = [
        F.col("userid").cast("long").alias("userid"),
        "dt", "user_weight", "cate_combined",
    ]
    if has_1d:
        cate_select_cols.append("cate_retained_1d")
    if has_3d:
        cate_select_cols.append("cate_retained_3d")

    cate_slim = cate_df.select(*cate_select_cols)

    joined = article_detail.join(cate_slim, on=["userid", "dt"], how="inner")

    # user_weight 可能在两边都有, 用 CATE 侧的
    if "user_weight" in article_detail.columns:
        joined = joined.drop(article_detail["user_weight"])

    # 3. 文章级贡献 = CATE × engagement_score × user_weight
    joined = joined.withColumn(
        "contrib_combined",
        F.col("cate_combined") * F.col("engagement_score") * F.col("user_weight")
    )
    if has_1d:
        joined = joined.withColumn(
            "contrib_1d",
            F.col("cate_retained_1d") * F.col("engagement_score") * F.col("user_weight")
        )
    if has_3d:
        joined = joined.withColumn(
            "contrib_3d",
            F.col("cate_retained_3d") * F.col("engagement_score") * F.col("user_weight")
        )

    # 4. 聚合到文章粒度 (按 dt 分组, 支持多日数据)
    agg_exprs = [
        F.sum("contrib_combined").alias("contribution_final"),
        F.count("userid").alias("reader_count"),
        F.sum("user_weight").alias("weighted_reader_count"),
    ]
    if has_1d:
        agg_exprs.append(F.sum("contrib_1d").alias("contribution_1d"))
    if has_3d:
        agg_exprs.append(F.sum("contrib_3d").alias("contribution_3d"))

    article_contrib = joined.groupBy("dt", "postid", "blog_id").agg(*agg_exprs)

    # 补齐可能缺失的列
    if not has_1d:
        article_contrib = article_contrib.withColumn("contribution_1d", F.lit(None).cast("double"))
    if not has_3d:
        article_contrib = article_contrib.withColumn("contribution_3d", F.lit(None).cast("double"))

    # 排名 (按 dt 分区内排名)
    w_art = Window.partitionBy("dt").orderBy(F.desc("contribution_final"))
    w_art_pct = Window.partitionBy("dt").orderBy("contribution_final")
    article_contrib = (
        article_contrib
        .withColumn("contribution_rank", F.row_number().over(w_art))
        .withColumn("contribution_percentile", F.percent_rank().over(w_art_pct) * 100)
    )

    # 5. 聚合到创作者粒度 (按 dt 分组)
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

    # 6. 质量检查
    print("\n[QC] 文章贡献度:")
    article_contrib.select(
        F.count("*").alias("total"),
        F.mean("contribution_final").alias("avg"),
        F.expr("percentile_approx(contribution_final, 0.5)").alias("median"),
    ).show(truncate=False)

    print("[QC] 创作者等级:")
    creator_contrib.groupBy("contribution_level").agg(
        F.count("*").alias("count"),
        F.mean("contribution_final").alias("avg_contribution"),
    ).orderBy("contribution_level").show(truncate=False)

    # 7. 写入 Hive 分区表 (按 dt 动态分区)
    # 文章贡献度表: dt 作为分区字段需放到最后
    article_output = article_contrib.select(
        "postid", "blog_id",
        "contribution_1d", "contribution_3d", "contribution_final",
        "reader_count", "weighted_reader_count",
        "contribution_rank", "contribution_percentile",
        "dt",  # 分区字段放最后
    )
    article_output.write.mode("overwrite").insertInto(args.article_table, overwrite=True)
    print(f"[Step3] Article contributions written to {args.article_table}")

    # 创作者贡献度表
    creator_output = creator_contrib.select(
        "blog_id",
        "article_count", "total_reader_count",
        "contribution_1d", "contribution_3d", "contribution_final",
        "avg_article_contribution",
        "contribution_rank", "contribution_percentile", "contribution_level",
        "dt",  # 分区字段放最后
    )
    creator_output.write.mode("overwrite").insertInto(args.creator_table, overwrite=True)
    print(f"[Step3] Creator contributions written to {args.creator_table}")

    # 打印分区信息
    dts = article_contrib.select("dt").distinct().collect()
    print(f"[Step3] Written partitions: {[r['dt'] for r in dts]}")

    spark.stop()
    print("[Step3] Done!")


if __name__ == "__main__":
    main()