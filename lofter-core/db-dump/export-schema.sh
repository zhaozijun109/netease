#!/usr/bin/env bash

tables=""

function getSchema {
  dbConnect=$1
  pattern=$2
  for tbl in `$dbConnect -e "show tables" --table|sed '1,3d'|sed '$d'|awk -F '|' '{print $2}'`;do
    columns=`$dbConnect -e "desc $tbl" --table|sed '1,3d'|sed '$d'|sed 's/[ \t\n]*|[ \t\n]*/|/g'|sed 's/\\\\$//'|sed 's/"/\\\\"/g'|awk 'BEGIN{FS="|"; ORS=","} NF > 3 {print "{\"columnName\":\"" $2 "\",\"columnType\":\"" $3 "\",\"isPrimaryKey\":" ( length($4) == 0 ? "false" : "true" ) ",\"comment\":\"" $8 "\"}" }'|sed s'/.$//'`

    if [[ "$tbl" =~ ^"$pattern" ]]; then
    	if [ -n "$tables" ]; then
      		printf ","
    	fi
    	tables="{\"tableName\":\"$tbl\",\"columns\":[$columns]}"
	echo -n "$tables"
    fi
  done
}

printf "["
getSchema "mysql -h 10.59.186.164 -P 6000 -u lofter_bi_gy --password=Q8@BJ5wh_ lofter-mirror-gz"
getSchema "mysql -h lofter-rds-statis-jd-34893.rds.cn-gz-p1.internal. -P 3306 -u lofter_bi --password=qKsbCbRpM comic_statis"
getSchema "mysql -h lofter-rds-common-recomment-mirror-gz-34729.rds.cn-gz-p1.internal. -P 3331 -u lofter_bi_gy --password=q4W0Kf_@I recomment"
getSchema "mysql -h lofter-rds-public-online-gz-34591.rds.cn-gz-p1.internal. -P 3331 -u lofter_bi_gy --password=@2X1oKN_h dc_ab"
getSchema "mysql -h 10.59.186.122 -P 6000 -u lofter_bi_gy --password=w4W9F_A@q lofter-yaolu-online"
getSchema "mysql -h lofter-rds-flow-control-online-34888.rds.cn-gz-p1.internal. -P 3306 -u lofter_bi_gy --password=WjQ3@hE@1 flow_control"
printf "]"
