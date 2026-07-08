package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.R;
import io.milvus.param.collection.ShowCollectionsParam;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.List;

public class ShowCollectionsUDF extends UDF {

    public List<String> evaluate() throws Exception {
        MilvusServiceClient milvusClient = MilvusUtil.getOrCreateClient();
        R<ShowCollectionsResponse> r = milvusClient.showCollections(ShowCollectionsParam.newBuilder().build());
        milvusClient.close();
        MilvusUtil.checkStatus(r);
        List<String> collectionNames = new ArrayList<>();
        ShowCollectionsResponse data = r.getData();
        for (int i = 0; i < data.getCollectionIdsCount(); i++) {
            collectionNames.add(data.getCollectionNames(i));
        }
        return collectionNames;
    }
}