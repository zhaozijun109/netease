package com.netease.jobs.ads.revenue;

import com.netease.operator.ads.revenue.AdsUserOrderDetailsRichFlatMapFunction;
import com.netease.pojo.revenue.UserOrderDetails;
import com.netease.sink.DatabaseSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/** The duplicate data of user order. */
public class AdsUserOrderDetailsJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsUserOrderDetailsJob.class);

    private static void adsUserOrderDetailsJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        Properties kafkaProducerProps = new Properties();

        env.fromSource(
                        new KafkaConsumerSource(
                                        params.getRequired("kafka.bootstrap.servers"),
                                        params.getRequired("kafka.lofter.dwd_binlog_ndc_topic"),
                                        "AdsUserOrderDetailsJobTest",
                                        kafkaConsumerProps)
                                .createEarliestKafkaSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DwdBinlogNdcSource")
                .setParallelism(6)
                .name("AdsUserOrderDetailsSource")
                .uid("AdsUserOrderDetailsSource")
                .flatMap(new AdsUserOrderDetailsRichFlatMapFunction())
                .setParallelism(6)
                .name("AdsUserOrderDetailsRichFlatMap")
                .uid("AdsUserOrderDetailsRichFlatMap")
                .addSink(
                        new DatabaseSink<UserOrderDetails>(
                                        params.getRequired("clickhouse.jdbc.driver"),
                                        params.getRequired("clickhouse.data.jdbc.url"),
                                        params.getRequired("clickhouse.data.jdbc.user"),
                                        params.getRequired("clickhouse.data.jdbc.password"))
                                .createClickhouseSink(
                                        "insert into dwd_user_revenue_trade_local(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money, createTime, finishTime, opTime) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                        new JdbcStatementBuilder<UserOrderDetails>() {
                                            @Override
                                            public void accept(
                                                    PreparedStatement preparedStatement,
                                                    UserOrderDetails userOrderDetails)
                                                    throws SQLException {
                                                preparedStatement.setLong(
                                                        1, userOrderDetails.getOrderId());
                                                preparedStatement.setString(
                                                        2, userOrderDetails.getOrderType());
                                                preparedStatement.setLong(
                                                        3, userOrderDetails.getUserId());
                                                preparedStatement.setLong(
                                                        4, userOrderDetails.getProductId());
                                                preparedStatement.setInt(
                                                        5, userOrderDetails.getProductNum());
                                                preparedStatement.setLong(
                                                        6, userOrderDetails.getBlogId());
                                                preparedStatement.setLong(
                                                        7, userOrderDetails.getPostId());
                                                preparedStatement.setLong(
                                                        8, userOrderDetails.getGiftId());
                                                preparedStatement.setInt(
                                                        9, userOrderDetails.getStatus());
                                                preparedStatement.setDouble(
                                                        10,
                                                        Double.parseDouble(
                                                                String.valueOf(
                                                                        userOrderDetails
                                                                                .getOrderAmount())));
                                                preparedStatement.setLong(
                                                        11, userOrderDetails.getOrderTime());
                                                preparedStatement.setLong(
                                                        12, userOrderDetails.getPayTime());
                                                preparedStatement.setLong(
                                                        13, userOrderDetails.getOrderTime());
                                            }
                                        },
                                        JdbcExecutionOptions.builder()
                                                .withBatchSize(100)
                                                .withBatchIntervalMs(5000)
                                                .build()))
                .setParallelism(6)
                .name("AdsUserOrderDetailsSink")
                .uid("AdsUserOrderDetailsSink");

        env.execute("AdsUserOrderDetailsJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        adsUserOrderDetailsJob(params);
    }
}
