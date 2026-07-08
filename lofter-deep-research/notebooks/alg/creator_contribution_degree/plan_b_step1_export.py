"""
方案 B — Step 1: PySpark 特征工程 + 导出本地 Parquet

将宽表做特征工程后导出两份数据:
  1. user_day_agg: 用户-日聚合表 (DML 训练 + 预测用)
  2. user_article_detail: 用户-文章明细 (贡献度分配用, 含 engagement_score)

运行方式:
  spark-submit --master yarn --driver-memory 16g --executor-memory 8g \
    --num-executors 30 plan_b_step1_export.py \
    --export_dir /tmp/contribution_export/
"""

import argparse
from pyspark.sql import SparkSession
import pyspark.sql.functions as F

SOURCE_TABLE = "rec.tmp_0324_creator_contribution_degree_input_wide_table"


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--export_dir", type=str, default="hdfs://gy-cluster8/user/rec/zyp/rec_items_pool/blog_contribution/")
    parser.add_argument("--hdfs_export", type=str, default="",
                        help="如果指定, 也导出到 HDFS (方案B Step3从这里读)")
    return parser.parse_args()


def create_spark():
    return (
        SparkSession.builder
        .appName("CreatorContribution_PlanB_Step1")
        .config("spark.sql.shuffle.partitions", "400")
        .config("spark.sql.adaptive.enabled", "true")
        .enableHiveSupport()
        .getOrCreate()
    )


def main():
    args = parse_args()
    spark = create_spark()

    # 1. 读取宽表
    df = spark.sql(f"SELECT * FROM {SOURCE_TABLE}")
    print(f"[Step1] Loaded: {SOURCE_TABLE}")

    # 2. 特征工程: engagement_score
    # duration 归一化: log 变换映射到 [0, 5]
    # 公式: 5 * log(clip(duration, 5, 600) / 5) / log(120)
    # 5s→0, 15s→1.15, 30s→1.87, 60s→2.57, 120s→3.27, 300s→4.18, 600s→5.0
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

    # 3. 用户权重
    percentiles = df.select(
        F.expr("percentile_approx(send_comment_cnt_30d, 0.75)").alias("comment_p75"),
        F.expr("percentile_approx(send_hot_30d, 0.75)").alias("hot_p75"),
        F.expr("percentile_approx(send_collect_cnt_30d, 0.75)").alias("collect_p75"),
    ).collect()[0]

    comment_p75 = float(percentiles["comment_p75"] or 0)
    hot_p75 = float(percentiles["hot_p75"] or 0)
    collect_p75 = float(percentiles["collect_p75"] or 0)

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

    # 4. 导出用户-文章明细
    article_detail = df.select(
        "userid", "dt", "postid", "blog_id",
        "engagement_score", "user_weight",
    )
    article_path = f"{args.export_dir}/user_article_detail"
    article_detail.coalesce(1).write.mode("overwrite").parquet(article_path)
    print(f"[Step1] User-article detail exported: {article_path}")

    if args.hdfs_export:
        hdfs_article = f"{args.hdfs_export}/user_article_detail"
        article_detail.write.mode("overwrite").parquet(hdfs_article)
        print(f"[Step1] Also exported to HDFS: {hdfs_article}")

    # 5. 用户-日聚合
    user_day = df.groupBy("userid", "dt").agg(
        F.sum("engagement_score").alias("total_engagement_score"),
        F.count("postid").alias("read_article_count"),
        F.countDistinct("blog_id").alias("read_creator_count"),
        F.avg("hot").alias("avg_article_hot"),
        F.sum("like_recommend_pv").alias("total_likes"),
        F.sum("comment_pv").alias("total_comments"),
        F.sum("share_pv").alias("total_shares"),
        F.sum("collect_pv").alias("total_collects"),
        F.sum("is_follow").alias("total_follows"),
        F.first("active_days_1d").alias("active_days_1d"),
        F.first("active_days_3d").alias("active_days_3d"),
        F.first("active_days_7d").alias("active_days_7d"),
        F.first("active_days_15d").alias("active_days_15d"),
        F.first("active_days_30d").alias("active_days_30d"),
        F.first("send_hot_30d").alias("send_hot_30d"),
        F.first("send_comment_cnt_30d").alias("send_comment_cnt_30d"),
        F.first("send_collect_cnt_30d").alias("send_collect_cnt_30d"),
        F.first("retain_1d").alias("retain_1d"),
        F.first("retain_3d").alias("retain_3d"),
        F.first("retain_active_days_7d").alias("retain_active_days_7d"),
        F.first("user_weight").alias("user_weight"),
    ).fillna(0)

    user_day_path = f"{args.export_dir}/user_day_agg"
    user_day.write.mode("overwrite").parquet(user_day_path)
    row_count = user_day.count()
    print(f"[Step1] User-day agg exported: {user_day_path} ({row_count} rows)")

    if args.hdfs_export:
        hdfs_ud = f"{args.hdfs_export}/user_day_agg"
        user_day.write.mode("overwrite").parquet(hdfs_ud)
        print(f"[Step1] Also exported to HDFS: {hdfs_ud}")

    spark.stop()
    print("[Step1] Done!")


if __name__ == "__main__":
    main()
