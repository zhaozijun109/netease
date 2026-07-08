#!/bin/bash
#
# 查找 Doris 数据库中使用了 AUTO PARTITION 的表
# 用法: bash find_auto_partition_tables.sh <database_name> [database_name2 ...]
# 示例: bash find_auto_partition_tables.sh lofter lofter_dm lofter_db_dump
#

set -euo pipefail

# ========== Doris 连接信息（固定） ==========
DORIS_HOST="music-doris-das.service.gy.ntes"
DORIS_PORT="6040"
DORIS_USER="doris_rw"
DORIS_PASS="Fkhike8A"

# ========== 参数校验 ==========
if [ $# -lt 1 ]; then
    echo "用法: $0 <database_name> [database_name2 ...]"
    echo "示例: $0 lofter lofter_dm lofter_db_dump"
    exit 1
fi

MYSQL_CMD="mysql -h${DORIS_HOST} -P${DORIS_PORT} -u${DORIS_USER} -p'${DORIS_PASS}' --batch --skip-column-names"

# ========== 主逻辑 ==========
total_count=0

for DB_NAME in "$@"; do
    echo "=============================================="
    echo "正在扫描数据库: ${DB_NAME}"
    echo "=============================================="

    # 获取该数据库所有表名
    tables=$(eval "${MYSQL_CMD} -e \"SHOW TABLES FROM \\\`${DB_NAME}\\\`;\"" 2>/dev/null)

    if [ -z "$tables" ]; then
        echo "  [WARN] 数据库 ${DB_NAME} 中没有找到表，或数据库不存在。"
        echo ""
        continue
    fi

    table_count=$(echo "$tables" | wc -l | tr -d ' ')
    echo "  共发现 ${table_count} 张表，开始逐表检查..."
    echo ""

    db_hit_count=0

    while IFS= read -r table_name; do
        # 跳过空行
        [ -z "$table_name" ] && continue

        # 获取建表语句
        create_sql=$(eval "${MYSQL_CMD} -e \"SHOW CREATE TABLE \\\`${DB_NAME}\\\`.\\\`${table_name}\\\`;\"" 2>/dev/null || true)

        if [ -z "$create_sql" ]; then
            echo "  [WARN] 无法获取表 ${table_name} 的建表语句，跳过。"
            continue
        fi

        # 检查是否包含 AUTO PARTITION（不区分大小写）
        if echo "$create_sql" | grep -qi "AUTO PARTITION"; then
            echo "  [HIT] ${DB_NAME}.${table_name}"
            db_hit_count=$((db_hit_count + 1))
            total_count=$((total_count + 1))
        fi
    done <<< "$tables"

    if [ "$db_hit_count" -eq 0 ]; then
        echo "  该数据库中未发现使用 AUTO PARTITION 的表。"
    else
        echo ""
        echo "  数据库 ${DB_NAME} 中共有 ${db_hit_count} 张表使用了 AUTO PARTITION。"
    fi
    echo ""
done

echo "=============================================="
echo "扫描完成！共发现 ${total_count} 张表使用了 AUTO PARTITION。"
echo "=============================================="
