package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.GetLoadStateResponse;
import io.milvus.grpc.LoadState;
import io.milvus.param.R;
import io.milvus.param.collection.GetLoadStateParam;
import org.apache.hadoop.hive.ql.exec.UDF;

public class GetLoadStateUDF extends UDF {

    public String evaluate(String collectionName) throws Exception {
        MilvusServiceClient milvusClient = MilvusUtil.getOrCreateClient();

        GetLoadStateParam param = GetLoadStateParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        R<GetLoadStateResponse> response = milvusClient.getLoadState(param);
        MilvusUtil.checkStatus(response);
        milvusClient.close();
        return response.getData().getState().toString();
    }
}