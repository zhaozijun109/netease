package com.netease.yuanqi.lofter.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.utils.es.EsUtils;
import com.netease.yuanqi.lofter.pojo.DimBenefitCategory;
import java.util.Collections;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.FlinkRuntimeException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimBenefitProductCategoryRichAsyncFunction
        extends RichAsyncFunction<DimBenefitCategory, DimBenefitCategory> {
    private static final Logger LOG =
            LoggerFactory.getLogger(DimBenefitProductCategoryRichAsyncFunction.class);
    private ObjectMapper objectMapper;
    private RestHighLevelClient esClient;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
        esClient =
                new RestHighLevelClient(
                        RestClient.builder(
                                        EsUtils.parseEsHosts(
                                                ClusterConfigOptions.getEsHosts(
                                                        ClusterConfigOptions.EsHostsEnum.COMMON)))
                                .setHttpClientConfigCallback(
                                        new RestClientBuilder.HttpClientConfigCallback() {
                                            @Override
                                            public HttpAsyncClientBuilder customizeHttpClient(
                                                    HttpAsyncClientBuilder httpAsyncClientBuilder) {
                                                Tuple2<String, String> userCredential =
                                                        ClusterConfigOptions.getEsAuthUserAndPass(
                                                                ClusterConfigOptions.EsHostsEnum
                                                                        .COMMON);
                                                CredentialsProvider credentialsProvider =
                                                        new BasicCredentialsProvider();
                                                credentialsProvider.setCredentials(
                                                        AuthScope.ANY,
                                                        new UsernamePasswordCredentials(
                                                                userCredential.f0,
                                                                userCredential.f1));
                                                return httpAsyncClientBuilder
                                                        .setDefaultCredentialsProvider(
                                                                credentialsProvider);
                                            }
                                        }));
    }

    @Override
    public void asyncInvoke(
            DimBenefitCategory dimBenefitCategory, ResultFuture<DimBenefitCategory> resultFuture)
            throws Exception {
        boolean isComplete = true;
        if (dimBenefitCategory.getLevel() == 2) {
            isComplete =
                    dimBenefitCategory.getCategory1() != null
                            && dimBenefitCategory.getCategory1Name() != null;
        }
        if (dimBenefitCategory.getLevel() == 3) {
            isComplete =
                    dimBenefitCategory.getCategory1() != null
                            && dimBenefitCategory.getCategory1Name() != null
                            && dimBenefitCategory.getCategory2() != null
                            && dimBenefitCategory.getCategory2Name() != null;
        }
        if (isComplete
                || (dimBenefitCategory.getParentId() != null
                        && dimBenefitCategory.getParentId() == 0)) {
            resultFuture.complete(Collections.singletonList(dimBenefitCategory));
        } else {
            SearchRequest searchRequest = new SearchRequest("lofter_dim_benefit_product_category");
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.should(
                    QueryBuilders.termQuery("categoryId", dimBenefitCategory.getCategoryId()));
            SearchSourceBuilder searchSourceBuilder =
                    new SearchSourceBuilder()
                            .query(queryBuilder)
                            .fetchSource(false)
                            .docValueField("category1", "use_field_mapping")
                            .docValueField("category1_name", "use_field_mapping")
                            .docValueField("category2", "use_field_mapping")
                            .docValueField("category2_name", "use_field_mapping");
            searchRequest.source(searchSourceBuilder);
            esClient.searchAsync(
                    searchRequest,
                    RequestOptions.DEFAULT,
                    new ActionListener<SearchResponse>() {
                        @Override
                        public void onResponse(SearchResponse searchResponse) {
                            SearchHit[] hits = searchResponse.getHits().getHits();
                            if (hits.length > 0
                                    && hits[0].field("category1").getValue().toString() != null) {
                                Long category1 =
                                        Long.parseLong(
                                                hits[0].field("category1").getValue().toString());
                                Long category2 =
                                        Long.parseLong(
                                                hits[0].field("category2").getValue().toString());
                                String category1Name =
                                        hits[0].field("category1_name").getValue().toString();
                                String category2Name =
                                        hits[0].field("category2_name").getValue().toString();
                                DimBenefitCategory dimBenefitCategoryNew =
                                        DimBenefitCategory.builder()
                                                .setCategoryId(dimBenefitCategory.getCategoryId())
                                                .setName(dimBenefitCategory.getName())
                                                .setLevel(dimBenefitCategory.getLevel())
                                                .setParentId(dimBenefitCategory.getParentId())
                                                .setCategory1(category1)
                                                .setCategory1Name(category1Name)
                                                .setCategory2(category2)
                                                .setCategory2Name(category2Name)
                                                .setCategory3(dimBenefitCategory.getCategory3())
                                                .setCategory3Name(
                                                        dimBenefitCategory.getCategory3Name())
                                                .build();
                                resultFuture.complete(
                                        Collections.singletonList(dimBenefitCategoryNew));
                            } else {
                                LOG.error(
                                        "Though categoryId: {}, can't find category when query from es index: lofter_dim_benefit_product_category",
                                        dimBenefitCategory.getCategoryId());
                                resultFuture.completeExceptionally(
                                        new FlinkRuntimeException(
                                                String.format(
                                                        "Though categoryId: %s, can't find category when query from es index: lofter_dim_benefit_product_category",
                                                        dimBenefitCategory
                                                                .getCategoryId()
                                                                .toString())));
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            LOG.error(
                                    "Error occurred when query from es index: lofter_dim_benefit_product_category");
                            resultFuture.completeExceptionally(
                                    new FlinkRuntimeException(
                                            "Error occurred when query from es index: lofter_dim_benefit_product_category"));
                        }
                    });
        }
    }

    @Override
    public void timeout(
            DimBenefitCategory dimBenefitCategory, ResultFuture<DimBenefitCategory> resultFuture)
            throws Exception {
        LOG.error(
                "Timeout when resolve product category from es index, event: {}",
                objectMapper.writeValueAsString(dimBenefitCategory));
        resultFuture.completeExceptionally(
                new FlinkRuntimeException("Timeout when resolve product category from es index"));
    }
}
