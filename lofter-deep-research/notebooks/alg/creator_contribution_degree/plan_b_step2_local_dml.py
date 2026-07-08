"""
方案 B: PySpark 导出 + 本地 Python 建模 — 创作者贡献度计算

分为 3 个独立步骤, 可分步运行:

  Step 1 (PySpark): 特征工程 + 用户-日聚合 + 导出本地 Parquet
    spark-submit plan_b_step1_export.py

  Step 2 (本地 Python): DML 训练 + 全量预测 CATE
    python plan_b_step2_local_dml.py

  Step 3 (PySpark): 读回 CATE + 贡献度聚合
    spark-submit plan_b_step3_aggregate.py

本文件是 Step 2: 本地 Python 建模
"""

import argparse
import os
import pickle
import warnings
from typing import Dict

import numpy as np
import pandas as pd
from lightgbm import LGBMClassifier, LGBMRegressor

warnings.filterwarnings("ignore")

# ===========================================================================
# 配置 (与方案 A 保持一致)
# ===========================================================================

OUTCOME_COLS = {
    "retained_1d": {"col": "retain_1d", "type": "binary", "weight": 0.4},
    "retained_3d": {"col": "retain_3d", "type": "binary", "weight": 0.6},
    # "active_days_7d": {"col": "retain_active_days_7d", "type": "continuous", "weight": 0.3},
}

USER_FEATURE_COLS = [
    "active_days_1d", "active_days_3d", "active_days_7d",
    "active_days_15d", "active_days_30d",
    "send_hot_30d", "send_comment_cnt_30d", "send_collect_cnt_30d",
]

EFFECT_MODIFIER_COLS = [
    "active_days_7d", "active_days_30d",
    "send_hot_30d", "send_comment_cnt_30d", "send_collect_cnt_30d",
    "read_article_count", "read_creator_count", "avg_article_hot",
]

CONFOUNDER_COLS = USER_FEATURE_COLS + [
    "read_article_count", "read_creator_count", "avg_article_hot",
    "total_likes", "total_comments", "total_shares",
    "total_collects", "total_follows",
]

TREATMENT_COL = "total_engagement_score"


def parse_args():
    parser = argparse.ArgumentParser(description="Plan B Step2: Local DML")
    parser.add_argument("--data_dir", type=str, default="./contribution_export/",
                        help="Step1 导出的 Parquet 目录")
    parser.add_argument("--output_dir", type=str, default="./contribution_export/cate/",
                        help="CATE 输出目录")
    parser.add_argument("--model_dir", type=str, default="./dml_models/",
                        help="模型保存目录")
    parser.add_argument("--sample_size", type=int, default=5_000_000)
    parser.add_argument("--batch_size", type=int, default=500_000)
    parser.add_argument("--n_estimators", type=int, default=200)
    parser.add_argument("--max_depth", type=int, default=8)
    return parser.parse_args()


# ===========================================================================
# 1. 加载数据
# ===========================================================================
def load_user_day_data(data_dir: str) -> pd.DataFrame:
    """加载 Step1 导出的用户-日聚合 Parquet"""
    path = os.path.join(data_dir, "user_day_agg")
    df = pd.read_parquet(path)
    print(f"[Load] User-day data: {df.shape}")
    print(f"[Load] Columns: {list(df.columns)}")
    return df


# ===========================================================================
# 2. DML 训练
# ===========================================================================
def train_dml_models(
    df: pd.DataFrame,
    sample_size: int = 5_000_000,
    n_estimators: int = 200,
    max_depth: int = 8,
) -> Dict:
    """分层抽样 + 训练 DML"""
    from econml.dml import CausalForestDML

    # 抽样
    if len(df) > sample_size:
        sample_df = df.sample(n=sample_size, random_state=42)
        print(f"[Train] Sampled {len(sample_df)} from {len(df)} rows")
    else:
        sample_df = df
        print(f"[Train] Using all {len(df)} rows (no sampling needed)")

    W = sample_df[CONFOUNDER_COLS].fillna(0).values.astype(np.float32)
    X = sample_df[EFFECT_MODIFIER_COLS].fillna(0).values.astype(np.float32)
    T_raw = sample_df[TREATMENT_COL].fillna(0).values.astype(np.float32)

    # Treatment 预处理: log(1+T) 压缩极端值, 让效应估计更稳定
    # 原始 T 范围可能 [0, 100+], log 后变为 [0, ~5]
    T_arr = np.log1p(T_raw).astype(np.float32)
    print(f"[Train] Treatment stats: raw_mean={T_raw.mean():.2f}, raw_std={T_raw.std():.2f}, "
          f"log_mean={T_arr.mean():.2f}, log_std={T_arr.std():.2f}")

    models = {}
    diagnostics = {}

    for outcome_name, config in OUTCOME_COLS.items():
        print(f"\n[Train] ====== {outcome_name} ======")
        Y = sample_df[config["col"]].fillna(0).values.astype(np.float32)

        print(f"  Y stats: mean={Y.mean():.4f}, std={Y.std():.4f}, "
              f"positive_rate={Y.mean():.4f}")

        lgbm_params = dict(
            n_estimators=200, max_depth=8, learning_rate=0.05,
            num_leaves=127, subsample=0.8, colsample_bytree=0.8,
            min_child_samples=30, n_jobs=-1, verbose=-1,
        )
        # 注意: EconML 的 CausalForestDML 对 model_y 调用 .predict() 而非 .predict_proba(),
        # 所以即使 Y 是 binary, 也必须用 Regressor (输出值在 [0,1] 区间, 等效概率估计)
        model_y = LGBMRegressor(**lgbm_params)
        model_t = LGBMRegressor(**lgbm_params)

        dml = CausalForestDML(
            model_y=model_y, model_t=model_t,
            n_estimators=n_estimators, max_depth=max_depth,
            min_samples_leaf=20,             # 降低: 允许更细粒度的分裂
            min_impurity_decrease=1e-8,      # 降低: 不再阻止微小效应的分裂
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

        # 自动诊断
        _print_diagnostics(outcome_name, diag)

        models[outcome_name] = dml

    return models, diagnostics


def _print_diagnostics(outcome_name: str, diag: dict):
    """自动分析并打印模型诊断结论"""
    ate = diag["ate"]
    mean = diag["cate_mean"]
    std = diag["cate_std"]
    p10, p50, p90 = diag["cate_p10"], diag["cate_p50"], diag["cate_p90"]

    print(f"\n  ── {outcome_name} 诊断报告 ──")

    # ATE + CI
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

    # CATE 分布
    print(f"  CATE: mean={mean:.6f}, std={std:.6f}")
    print(f"        p10={p10:.6f}, p50={p50:.6f}, p90={p90:.6f}")

    # mean vs ATE 一致性
    if abs(ate) > 1e-8 and abs(mean - ate) / abs(ate) > 0.2:
        print(f"  🟡 CATE mean 与 ATE 偏差 > 20%, 预测可能有偏")
    else:
        print(f"  ✅ CATE mean ≈ ATE, 预测无偏")

    # 异质性
    if abs(mean) > 1e-8:
        cv = std / abs(mean)  # 变异系数
        if cv < 0.1:
            print(f"  🟡 异质性很小 (CV={cv:.2f}), 所有用户效应接近, DML价值有限")
        elif cv > 5.0:
            print(f"  🟡 异质性过大 (CV={cv:.2f}), CATE分布可能受噪音影响")
        else:
            print(f"  ✅ 异质性合理 (CV={cv:.2f}), 不同用户效应有差异")
    else:
        print(f"  🟡 CATE均值接近0, 无法评估异质性")

    # p10 符号
    if p10 < 0 and p50 > 0:
        neg_pct = "约10%~"
        print(f"  🟡 {neg_pct}用户的CATE为负 (看文章反而降低留存), 可检查低活跃用户子群")
    elif p50 < 0:
        print(f"  🔴 超过50%用户CATE为负, 模型结果不合理!")
    else:
        print(f"  ✅ CATE几乎全为正值, 符合预期")

    # p90/p10 比值 (异质性范围)
    if p10 > 0:
        ratio = p90 / p10
        print(f"  📊 效应范围: 高效应用户(p90)是低效应用户(p10)的 {ratio:.1f} 倍")
    print()


# ===========================================================================
# 3. 全量 CATE 预测 (分批)
# ===========================================================================
def predict_cate_full(
    df: pd.DataFrame,
    models: Dict,
    batch_size: int = 500_000,
) -> pd.DataFrame:
    """对全量用户-日数据分批预测 CATE"""

    n_total = len(df)
    results = []

    for start in range(0, n_total, batch_size):
        end = min(start + batch_size, n_total)
        batch = df.iloc[start:end]

        X_batch = batch[EFFECT_MODIFIER_COLS].fillna(0).values.astype(np.float32)
        row = batch[["userid", "dt", "user_weight", TREATMENT_COL]].copy()

        weighted_cate = np.zeros(len(batch), dtype=np.float32)

        for outcome_name, config in OUTCOME_COLS.items():
            dml = models[outcome_name]
            cate = dml.effect(X=X_batch).flatten().astype(np.float32)
            row[f"cate_{outcome_name}"] = cate

            # 归一化
            p1, p99 = np.percentile(cate, 1), np.percentile(cate, 99)
            clipped = np.clip(cate, p1, p99)
            mn, mx = clipped.min(), clipped.max()
            norm = (clipped - mn) / (mx - mn + 1e-10)
            weighted_cate += config["weight"] * norm

        row["cate_combined"] = weighted_cate
        results.append(row)
        print(f"  [Predict] Batch {start//batch_size+1}: rows {start}-{end}")

    cate_df = pd.concat(results, ignore_index=True)
    print(f"[Predict] Done. Total rows: {len(cate_df)}")
    return cate_df


# ===========================================================================
# Main
# ===========================================================================
def main():
    args = parse_args()
    os.makedirs(args.output_dir, exist_ok=True)
    os.makedirs(args.model_dir, exist_ok=True)

    # 1. 加载数据
    print("\n[Step2] ====== Loading Data ======")
    df = load_user_day_data(args.data_dir)

    # 2. 训练
    print("\n[Step2] ====== Training DML ======")
    models, diagnostics = train_dml_models(
        df, sample_size=args.sample_size,
        n_estimators=args.n_estimators, max_depth=args.max_depth,
    )

    # 保存模型
    model_file = os.path.join(args.model_dir, "dml_models.pkl")
    with open(model_file, "wb") as f:
        pickle.dump({"models": models, "diagnostics": diagnostics}, f)
    print(f"\n[Step2] Models saved: {model_file}")

    # 3. 全量预测
    print("\n[Step2] ====== Full CATE Prediction ======")
    cate_df = predict_cate_full(df, models, batch_size=args.batch_size)

    # 4. 保存 CATE 结果
    output_file = os.path.join(args.output_dir, "user_day_cate.parquet")
    cate_df.to_parquet(output_file, index=False)
    print(f"[Step2] CATE saved: {output_file} ({len(cate_df)} rows)")

    # 打印诊断
    print("\n[Step2] ====== Model Diagnostics ======")
    for name, diag in diagnostics.items():
        print(f"  {name}: {diag}")

    print("\n[Step2] Done!")


if __name__ == "__main__":
    main()
