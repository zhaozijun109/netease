# notebooks/alg/

算法模型相关项目，包括因果推断、推荐算法、用户建模等算法方向的探索与实现。

每个子目录为一个独立的算法项目，包含需求文档（proposal）、技术方案（technical_design）、代码实现和模型产出。

## 目录规范

```
alg/
├── <项目名>/
│   ├── proposal.md           # 需求文档（核心）
│   ├── technical_design.md   # 技术方案（核心）
│   ├── archive.md            # 存档文档（核心）
│   ├── *.py                  # 代码实现
│   ├── requirements.txt      # Python 依赖
│   └── ...
└── ...
```

## 已有子目录

| 目录 | 简介 |
|------|------|
| `creator_contribution_degree/` | 创作者贡献度计算 — 基于 DML 因果推断模型，估计每个创作者对社区用户留存的因果贡献度，输出文章级和创作者级的贡献度得分与排名 |
| `creator_grouping/` | 创作者圈层分群 — 在 `creator_id × ip` 维度对创作者打标，输出 `stability_tag`（发文稳定性）、`vitality_tag`（圈层活人感）及质量过程指标（`quality_tag` 已废弃），日更落表 `lofter_dm.ads_creator_ip_grouping_dd` |
