package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.param.R;
import io.milvus.param.collection.DescribeCollectionParam;
import io.milvus.response.DescCollResponseWrapper;
import org.apache.hadoop.hive.ql.exec.UDF;

public class DescribeCollectionUDF extends UDF {

    public String evaluate(String collectionName) throws Exception {
        MilvusServiceClient milvusClient = MilvusUtil.getOrCreateClient();
        R<DescribeCollectionResponse> respDescribeCollection = milvusClient.describeCollection(
                DescribeCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
        DescCollResponseWrapper wrapperDescribeCollection = new DescCollResponseWrapper(respDescribeCollection.getData());
        return wrapperDescribeCollection.toString();
    }

}