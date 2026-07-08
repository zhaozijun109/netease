package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.GetLoadStateResponse;
import io.milvus.grpc.LoadState;
import io.milvus.param.R;
import io.milvus.param.collection.GetLoadStateParam;
import io.milvus.param.collection.LoadCollectionParam;
import org.apache.hadoop.hive.ql.exec.UDF;

public class LoadCollectionUDF extends UDF {

    public boolean evaluate(String collectionName) throws Exception {
        return evaluate(collectionName, 1);
    }

    public boolean evaluate(String collectionName, Integer replicaNumber) throws Exception {
        MilvusServiceClient milvusClient = MilvusUtil.getOrCreateClient();
        MilvusUtil.checkStatus(milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withReplicaNumber(replicaNumber)
                        .build()
        ));

        GetLoadStateParam param = GetLoadStateParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        R<GetLoadStateResponse> response = milvusClient.getLoadState(param);
        MilvusUtil.checkStatus(response);
        milvusClient.close();
        return response.getData().getState().equals(LoadState.LoadStateLoaded);
    }
}