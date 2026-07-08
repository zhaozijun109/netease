package com.netease.yuanqi.lofter.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.utils.es.EsUtils;
import com.netease.yuanqi.lofter.pojo.DimIpPost;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
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

public class DimIpPostRichAsyncFunction extends RichAsyncFunction<DimIpPost, DimIpPost> {
    private static final Logger LOG = LoggerFactory.getLogger(DimIpPostRichAsyncFunction.class);
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
    public void asyncInvoke(DimIpPost dimIpPost, ResultFuture<DimIpPost> resultFuture)
            throws Exception {
        DimIpPost.DimIpPostBuilder dimIpPostBuilder =
                DimIpPost.builder()
                        .setPostId(dimIpPost.getPostId())
                        .setBlogId(dimIpPost.getBlogId())
                        .setPublishTime(dimIpPost.getPublishTime())
                        .setTag(dimIpPost.getTag())
                        .setBlogLevel("")
                        .setIp("")
                        .setIsInRecommendPool(0);
        try {
            if (dimIpPost.getTag() != null && !dimIpPost.getTag().isEmpty()) {
                CompletableFuture<String> ipFuture = findIp(dimIpPost.getTag());
                dimIpPostBuilder.setIp(ipFuture.get());
            }
            CompletableFuture<String> blogLevelFuture = findBlogLevel(dimIpPost.getBlogId());
            dimIpPostBuilder.setBlogLevel(blogLevelFuture.get());
            resultFuture.complete(Collections.singletonList(dimIpPostBuilder.build()));
        } catch (Exception e) {
            resultFuture.completeExceptionally(e);
        }
    }

    @Override
    public void timeout(DimIpPost dimIpPost, ResultFuture<DimIpPost> resultFuture)
            throws Exception {
        LOG.error(
                "Timeout when resolve tag ip from es index, event: {}",
                objectMapper.writeValueAsString(dimIpPost));
        resultFuture.completeExceptionally(
                new FlinkRuntimeException("Timeout when resolve tag ip from es index"));
    }

    private CompletableFuture<String> findIp(String tag) {
        CompletableFuture<String> ipFuture = new CompletableFuture<>();
        SearchRequest searchRequest = new SearchRequest("lofter_tag_ip_mapping");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(QueryBuilders.termsQuery("ip", tag));
        SearchSourceBuilder searchSourceBuilder =
                new SearchSourceBuilder()
                        .query(boolQueryBuilder)
                        .fetchSource(false)
                        .docValueField("tag", "use_field_mapping")
                        .docValueField("ip", "use_field_mapping");
        searchRequest.source(searchSourceBuilder);
        esClient.searchAsync(
                searchRequest,
                RequestOptions.DEFAULT,
                new ActionListener<SearchResponse>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        SearchHit[] hits = searchResponse.getHits().getHits();
                        if (hits.length > 0) {
                            ipFuture.complete(hits[0].field("ip").getValue().toString());
                        } else {
                            ipFuture.complete("_small_ip");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        LOG.error("Error occurred when query from es index: lofter_tag_ip_mapping");
                        ipFuture.completeExceptionally(
                                new FlinkRuntimeException(
                                        "Error occurred when query from es index: lofter_tag_ip_mapping"));
                    }
                });
        return ipFuture;
    }

    private CompletableFuture<String> findBlogLevel(Long blogId) {
        CompletableFuture<String> blogLevelFuture = new CompletableFuture<>();
        SearchRequest searchRequest = new SearchRequest("lofter_blog_level");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(QueryBuilders.termQuery("blogId", blogId));
        SearchSourceBuilder searchSourceBuilder =
                new SearchSourceBuilder()
                        .query(boolQueryBuilder)
                        .fetchSource(false)
                        .docValueField("blogId", "use_field_mapping")
                        .docValueField("level", "use_field_mapping");
        searchRequest.source(searchSourceBuilder);
        esClient.searchAsync(
                searchRequest,
                RequestOptions.DEFAULT,
                new ActionListener<SearchResponse>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        SearchHit[] hits = searchResponse.getHits().getHits();
                        if (hits.length > 0) {
                            blogLevelFuture.complete(hits[0].field("level").getValue().toString());
                        } else {
                            blogLevelFuture.complete("");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        LOG.error("Error occurred when query from es index: lofter_blog_level");
                        blogLevelFuture.completeExceptionally(
                                new FlinkRuntimeException(
                                        "Error occurred when query from es index: lofter_blog_level"));
                    }
                });
        return blogLevelFuture;
    }
}
