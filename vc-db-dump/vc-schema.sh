#!/usr/bin/env bash

tables=""

function getSchema {
  dbConnect=$1
  for tbl in `$dbConnect -e "show tables" --table|sed '1,3d'|sed '$d'|awk -F '|' '{print $2}'`;do
    columns=`$dbConnect -e "show full columns from $tbl" --table|sed '1,3d'|sed '$d'|sed 's/[ \t\n]*|[ \t\n]*/|/g'|sed 's/\\\\$//'|sed 's/"/\\\\"/g'|awk 'BEGIN{FS="|"; ORS=","} NF > 3 {print "{\"columnName\":\"" $2 "\",\"columnType\":\"" $3 "\",\"isPrimaryKey\":" ( $6 == "PRI" ? "true" : "false" ) ",\"comment\":\"" $10 "\"}" }'|sed s'/.$//'`
    if [ -n "$tables" ]; then
      printf ","
    fi
    tables="{\"tableName\":\"$tbl\",\"columns\":[$columns]}"
    echo -n "$tables"
  done
}

printf "["
getSchema "mysql -h vcharacter-mysql-rw-online.db.gy.ntes -P 4331 -u online_algorithm_r_user --password=_FN68@jDm vcharacter"
printf "]"
