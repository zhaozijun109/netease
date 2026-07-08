package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.param.R;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import org.apache.hadoop.hive.ql.exec.UDF;

public class GetCollectionStatisticsUDF extends UDF {

    public String evaluate(String collectionName) throws Exception {
        MilvusServiceClient milvusClient = MilvusUtil.getOrCreateClient();
        R<GetCollectionStatisticsResponse> collectionStatistics = milvusClient.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder().withCollectionName(collectionName).build());
        MilvusUtil.checkStatus(collectionStatistics);
        return collectionStatistics.toString();
    }

}