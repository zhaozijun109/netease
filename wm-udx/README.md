#### 概述
#### 使用示例
```sql
add jar hdfs://gy-cluster8/user/da_lofter/lib/wm-udx-assembly-0.2.0.jar;

create temporary function html_images as 'com.netease.wm.udf.HtmlImages';
create temporary function html_text_length as 'com.netease.wm.udf.HtmlTextLength';
create temporary function gcd as 'com.netease.wm.udf.Gcd';
create temporary function regex_count as 'com.netease.wm.udf.RegexCount';
create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp';
create temporary function decode_url as 'com.netease.wm.udf.DecodeUrl';
create temporary function parse_array as 'com.netease.wm.udf.ParseArrayJson';
```
