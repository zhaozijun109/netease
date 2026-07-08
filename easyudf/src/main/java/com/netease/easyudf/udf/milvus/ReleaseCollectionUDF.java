package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.ReleaseCollectionParam;
import org.apache.hadoop.hive.ql.exec.UDF;

public class ReleaseCollectionUDF extends UDF {

    public boolean evaluate(String collectionName) throws Exception {
        MilvusServiceClient milvusClient = MilvusUtil.getOrCreateClient();
        MilvusUtil.checkStatus(milvusClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        ));
        milvusClient.close();
        return true;
    }
}