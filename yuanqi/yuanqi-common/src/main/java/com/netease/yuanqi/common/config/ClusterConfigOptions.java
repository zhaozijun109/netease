package com.netease.yuanqi.common.config;

import com.netease.yuanqi.common.pojo.config.KerberosConfig;
import java.util.Properties;
import org.apache.flink.api.java.tuple.Tuple2;

public class ClusterConfigOptions {
    // ------------------------------------------------------------------------
    //  BootStrapServers for kafka cluster
    // ------------------------------------------------------------------------
    public static KerberosConfig getKerberosConfig(KerberosConfigEnum kerberosConfigEnum) {
        switch (kerberosConfigEnum) {
            case LOFTER:
                return KerberosConfig.builder()
                        .withCoreSitePath("/lofter/hadoop_conf/core-site.xml")
                        .withHdfsSitePath("/lofter/hadoop_conf/hdfs-site.xml")
                        .withKrb5ConfPath("/lofter/kerberos_conf/krb5.conf")
                        .withKeyTabPath("/lofter/kerberos_conf/da_lofter.keytab")
                        .withKeyTabLoginUser("da_lofter/dev")
                        .build();
            case YCY:
            case VC:
            default:
                throw new IllegalArgumentException(
                        "Unsupported kerberos config of hdfs cluster: " + kerberosConfigEnum);
        }
    }

    // ------------------------------------------------------------------------
    //  BootStrapServers for kafka cluster
    // ------------------------------------------------------------------------
    public static String getKafkaBootStrapServers(
            KafkaBootstrapServersEnum kafkaBootstrapServersEnum) {
        switch (kafkaBootstrapServersEnum) {
            case COMMON:
            case LOFTER_DATA:
                return "lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092,lofter-kafka-bi-risk4.gy.ntes:9092,"
                        + "lofter-kafka-bi-risk5.gy.ntes:9092,lofter-kafka-bi-risk6.gy.ntes:9092,lofter-kafka-bi-risk7.gy.ntes:9092,lofter-kafka-bi-risk8.gy.ntes:9092";
            case LOFTER_RECOMMEND:
                return "lofter-kafka-recommend1.gy.ntes:9092,lofter-kafka-recommend2.gy.ntes:9092,lofter-kafka-recommend3.gy.ntes:9092,lofter-kafka-recommend4.gy.ntes:9092,lofter-kafka-recommend5.gy.ntes:9092";
            case LOFTER_RECOMMEND_TEST:
                return "10.59.187.156:9092,10.59.187.156:9093,10.59.187.156:9094";
            case LOFTER_BACKEND:
                return "lofter-kafka-dc1.gy.ntes:9092,lofter-kafka-dc2.gy.ntes:9092,lofter-kafka-dc3.gy.ntes:9092";
            case LOFTER_BACKEND_TEST:
                return "10.59.187.60:9092,10.59.187.61:9092,10.59.187.62:9092";
            case YCY_GUIANSERVER:
                return "guianserver01.brokers.canal.netease.com:9093";
            case LOCAL_TEST:
                return "10.104.122.11:9092,10.104.122.12:9092,10.104.122.13:9092,10.104.122.25:9092,10.104.122.26:9092,10.31.6.46:9092,10.31.6.47:9092,10.31.6.48:9092";
            case MUSIC_AD:
                return "10.48.2.16:9092,10.48.2.17:9092,10.48.2.18:9092,10.48.2.19:9092,10.48.2.20:9092,10.48.2.21:9092,10.48.2.22:9092,10.48.2.23:9092,10.48.2.24:9092,10.48.2.25:9092,10.48.2.26:9092,10.48.2.27:9092,10.48.2.28:9092,10.48.2.29:9092,10.48.2.30:9092";
            case VC:
                return "vcharacter-kafka-prod3.gy.ntes:9092,vcharacter-kafka-prod1.gy.ntes:9092,vcharacter-kafka-prod2.gy.ntes:9092";
            case YCY_GRAND:
                return "grand.brokers.canal.netease.com:9093";
            case YCY_TEST:
                return "test01.brokers.canal.netease.com:9093";
            default:
                throw new IllegalArgumentException(
                        "Unsupported bootstrap servers of kafka cluster: "
                                + kafkaBootstrapServersEnum);
        }
    }

    // ------------------------------------------------------------------------
    //  Property for kafka cluster
    // ------------------------------------------------------------------------
    public static Properties getKafkaSecurityProperty(
            KafkaBootstrapServersEnum kafkaBootstrapServersEnum) {
        // Properties props = new Properties();
        switch (kafkaBootstrapServersEnum) {
            case YCY_GUIANSERVER:
            case YCY_GRAND:
            case YCY_TEST:
                Properties props = new Properties();
                props.setProperty("security.protocol", "SASL_PLAINTEXT");
                props.setProperty("sasl.mechanism", "PLAIN");
                props.setProperty(
                        "sasl.jaas.config",
                        "org.apache.kafka.common.security.plain.PlainLoginModule required "
                                + "username=\"bu_a13_bigdata_writer\" "
                                + "password=\"206e9e77fb254ddaae65\";");
                return props;
            default:
                return new Properties();
        }
    }

    // ------------------------------------------------------------------------
    //  Host for elasticsearch cluster
    // ------------------------------------------------------------------------
    public static String getEsHosts(EsHostsEnum esHostsEnum) {
        switch (esHostsEnum) {
            case COMMON:
            case LOFTER_DATA:
                return "lofter-data-common1.gy.ntes:7000,lofter-data-common2.gy.ntes:7000,lofter-data-common3.gy.ntes:7000,lofter-data-common4.gy.ntes:7000";
            case LOFTER_BACKEND:
                return "lofter-online-es1.gy.ntes:7000,lofter-online-es2.gy.ntes:7000,lofter-online-es3.gy.ntes:7000,lofter-online-es4.gy.ntes:7000";
            case VC:
                return "vcharacter-es-1.gy.ntes:7000,vcharacter-es-2.gy.ntes:7000,vcharacter-es-3.gy.ntes:7000";
            default:
                throw new IllegalArgumentException(
                        "Unsupported hosts of elasticsearch cluster: " + esHostsEnum);
        }
    }

    public static Tuple2<String, String> getEsAuthUserAndPass(EsHostsEnum esHostsEnum) {
        switch (esHostsEnum) {
            case COMMON:
            case LOFTER_DATA:
                return Tuple2.of("data-analysis-gz", "Dvzo@vdwTs");
            case LOFTER_BACKEND:
                return Tuple2.of("lofter_online", "Yi3DLCppSI");
            case VC:
                return Tuple2.of("vcharacter-online", "fL4Rd4iMtG");
            default:
                throw new IllegalArgumentException(
                        "Unsupported auth of elasticsearch cluster: " + esHostsEnum);
        }
    }

    // ------------------------------------------------------------------------
    //  Connection for mysql
    // ------------------------------------------------------------------------
    public static String getMysqlConnection(MysqlConnectionEnum mysqlConnectionEnum) {
        switch (mysqlConnectionEnum) {
            case LOFTER_RECOMMEND:
                return "jdbc:mysql://lofter-rds-common-recomment-mirror-gz-34729.rds.cn-gz-p1.internal.:3331/recomment?useUnicode=true&characterEncoding=UTF-8&user=lofter_bi_gy&password=q4W0Kf_@I&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000&autoReconnect=true";
            case LOFTER_BACKEND:
                return "jdbc:mysql://10.59.186.164:6000/lofter-mirror-gz?user=lofter_bi_gy&password=Q8@BJ5wh_&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000&autoReconnect=true";
            case LOFTER_BACKEND_TEST:
                return "jdbc:mysql://10.57.60.11:6000,10.57.60.12:6000/public?user=public_test&password=eThNXWMVirg&connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8&autoReconnect=true";
            case VC:
                return "jdbc:mysql://vcharacter-mysql-rw-online.db.gy.ntes:4331/vcharacter?user=online_algorithm_ro&password=8oOD@C2_c&characterEncoding=utf-8&useSSL=false&autoReconnect=true&useAffectedRows=true&useLegacyDatetimeCode=false&useTimezone=true&serverTimezone=Asia/Shanghai&connectTimeout=5000&socketTimeout=1800000";
            default:
                throw new IllegalArgumentException(
                        "Unsupported mysql connection: " + mysqlConnectionEnum);
        }
    }

    public static String getMysqlDriverClassName(MysqlConnectionEnum mysqlConnectionEnum) {
        switch (mysqlConnectionEnum) {
            case LOFTER_RECOMMEND:
            case LOFTER_BACKEND:
            case LOFTER_BACKEND_TEST:
            case VC:
                return "com.mysql.cj.jdbc.Driver";
            default:
                throw new IllegalArgumentException(
                        "Unsupported mysql driver class: " + mysqlConnectionEnum);
        }
    }

    // ------------------------------------------------------------------------
    //  Connection for clickhouse
    // ------------------------------------------------------------------------
    public static String getClickhouseHosts(ClickhouseHostsEnum clickhouseHostsEnum) {
        switch (clickhouseHostsEnum) {
            case COMMON:
            case LOFTER_DATA:
                return "lofter-data-common5.gy.ntes,lofter-data-common6.gy.ntes,lofter-data-common7.gy.ntes,lofter-data-common8.gy.ntes,lofter-data-common9.gy.ntes";
            default:
                throw new IllegalArgumentException(
                        "Unsupported hosts of clickhouse cluster: " + clickhouseHostsEnum);
        }
    }

    // ------------------------------------------------------------------------
    //  Connection for redis
    // ------------------------------------------------------------------------
    public static String getRedisHosts(RedisHostsEnum redisHostsEnum) {
        switch (redisHostsEnum) {
            case COMMON:
            case LOFTER_DATA:
                return "10.31.6.61:6101,10.31.6.61:6102,10.31.6.58:6137,10.31.6.58:6138,10.31.6.60:6114,10.31.6.60:6115,10.31.6.59:6151,10.31.6.59:6152";
            default:
                throw new IllegalArgumentException(
                        "Unsupported hosts of redis cluster: " + redisHostsEnum);
        }
    }

    public static Tuple2<String, String> getRedisAuthUserAndPass(RedisHostsEnum redisHostsEnum) {
        switch (redisHostsEnum) {
            case COMMON:
            case LOFTER_DATA:
                return Tuple2.of("", "6bf1c5bc41da");
            default:
                throw new IllegalArgumentException(
                        "Unsupported auth of redis cluster: " + redisHostsEnum);
        }
    }

    public enum KerberosConfigEnum {
        LOFTER,
        YCY,
        VC,
    }

    public enum KafkaBootstrapServersEnum {
        COMMON,
        LOFTER_DATA,
        LOFTER_RECOMMEND,
        LOFTER_RECOMMEND_TEST,
        LOFTER_BACKEND,
        LOFTER_BACKEND_TEST,
        YCY_GUIANSERVER,
        LOCAL_TEST,
        MUSIC_AD,
        VC,
        YCY_GRAND,
        YCY_TEST
    }

    public enum EsHostsEnum {
        COMMON,
        LOFTER_DATA,
        LOFTER_BACKEND,
        VC,
    }

    public enum MysqlConnectionEnum {
        LOFTER_RECOMMEND,
        LOFTER_BACKEND,
        LOFTER_BACKEND_TEST,
        VC,
    }

    public enum ClickhouseHostsEnum {
        COMMON,
        LOFTER_DATA,
    }

    public enum DorisHostsEnum {
        COMMON,
        LOFTER_DATA,
    }

    public enum RedisHostsEnum {
        COMMON,
        LOFTER_DATA,
        LOFTER_RECOMMEND,
    }
}
