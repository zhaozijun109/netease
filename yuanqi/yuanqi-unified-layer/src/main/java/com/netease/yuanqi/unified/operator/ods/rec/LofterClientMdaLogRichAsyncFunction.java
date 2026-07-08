package com.netease.yuanqi.unified.operator.ods.rec;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.utils.redis.RedisUtils;
import com.netease.yuanqi.unified.pojo.ItemEntry;
import com.netease.yuanqi.unified.pojo.RecParsedLogEvents;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.FlinkRuntimeException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LofterClientMdaLogRichAsyncFunction
        extends RichAsyncFunction<RecParsedLogEvents, RecParsedLogEvents> {
    private static final Logger LOG =
            LoggerFactory.getLogger(LofterClientMdaLogRichAsyncFunction.class);
    private ObjectMapper objectMapper;
    private RedisClusterClient redisClusterClient;
    private StatefulRedisClusterConnection<String, String> redisConnection;
    private RedisAdvancedClusterAsyncCommands<String, String> redisAsyncCommands;
    private AsyncLoadingCache<String, String> itemCache;
    private Set<String> itemTypeSet;

    @Override
    public void open(OpenContext openContext) throws Exception {
        redisClusterClient =
                RedisUtils.getClusterClient(
                        ClusterConfigOptions.getRedisHosts(
                                ClusterConfigOptions.RedisHostsEnum.COMMON),
                        ClusterConfigOptions.getRedisAuthUserAndPass(
                                        ClusterConfigOptions.RedisHostsEnum.COMMON)
                                .f1);
        // redisClusterClient.setDefaultTimeout(Duration.ofSeconds(5));
        redisClusterClient.setOptions(
                ClusterClientOptions.builder()
                        .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(5)))
                        .build());
        redisConnection = redisClusterClient.connect();
        redisAsyncCommands = redisConnection.async();
        itemCache =
                Caffeine.newBuilder()
                        .maximumSize(2000000)
                        .expireAfterAccess(Duration.ofHours(1))
                        .buildAsync(
                                new AsyncCacheLoader<String, String>() {
                                    @Override
                                    public @NonNull CompletableFuture<String> asyncLoad(
                                            @NonNull String s, @NonNull Executor executor) {
                                        return updateCacheFromRedis(Collections.singletonList(s))
                                                .thenApply(resultMap -> resultMap.get(s));
                                    }

                                    @Override
                                    public @NonNull CompletableFuture<
                                                    Map<@NonNull String, @NonNull String>>
                                            asyncLoadAll(
                                                    @NonNull
                                                            Iterable<? extends @NonNull String>
                                                                    keys,
                                                    @NonNull Executor executor) {
                                        return updateCacheFromRedis(keys);
                                    }
                                });
        itemTypeSet =
                new HashSet<>(
                        Arrays.asList(
                                "text",
                                "photo",
                                "audio",
                                "video",
                                "qa",
                                "article",
                                "answer",
                                "cos_answer",
                                "normal_comment_answer",
                                "line_comment_answer",
                                "photo_comment_answer",
                                "join_nominatesign_answer",
                                "join_score_answer",
                                "card"));
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    }

    @Override
    public void asyncInvoke(
            RecParsedLogEvents recParsedLogEvents, ResultFuture<RecParsedLogEvents> resultFuture)
            throws Exception {
        JsonNode root = objectMapper.readTree(recParsedLogEvents.getSource());
        ObjectNode attributes = root.get("attributes").deepCopy();
        String itemId = attributes.hasNonNull("itemId") ? attributes.get("itemId").asText() : "";
        String itemType =
                attributes.hasNonNull("itemType")
                        ? attributes.get("itemType").asText().toLowerCase()
                        : "";

        String extOption = attributes.hasNonNull("ext") ? attributes.get("ext").asText() : "";
        ObjectNode nested = objectMapper.createObjectNode();
        if (("a2-7".equals(recParsedLogEvents.getEventId()) && extOption.startsWith("{\""))
                || (!"a2-7".equals(recParsedLogEvents.getEventId()) && extOption.startsWith("{"))) {
            try {
                nested = objectMapper.readTree(extOption).deepCopy();
            } catch (JsonParseException e) {
                LOG.info("Parse ext json error: {}", recParsedLogEvents);
            }
        }

        String nestedItemId = nested.hasNonNull("itemId") ? nested.get("itemId").asText() : "";
        String nestedItemType =
                nested.hasNonNull("itemType") ? nested.get("itemType").asText().toLowerCase() : "";

        String extItemId =
                attributes.hasNonNull("ext_itemId") ? attributes.get("ext_itemId").asText() : "";
        String extItemType =
                attributes.hasNonNull("ext_itemType")
                        ? attributes.get("ext_itemType").asText().toLowerCase()
                        : "";
        String addItemId =
                attributes.hasNonNull("add_itemId") ? attributes.get("add_itemId").asText() : "";
        String addItemType =
                attributes.hasNonNull("add_itemType")
                        ? attributes.get("add_itemType").asText().toLowerCase()
                        : "";

        List<ItemEntry> itemEntries = new ArrayList<>();
        List<String> itemIds = new ArrayList<>();
        if (itemId != null
                && !itemId.isEmpty()
                && itemTypeSet.contains(itemType)
                && ((!"card".equals(itemType) && itemId.chars().allMatch(Character::isDigit))
                        || ("card".equals(itemType)
                                && !itemId.chars().allMatch(Character::isDigit)))) {
            String k1 =
                    !"card".equals(itemType)
                            ? "dp_" + Long.toHexString(Long.parseLong(itemId)).toLowerCase()
                            : "dc_" + itemId;
            itemIds.add(k1);
            itemEntries.add(
                    ItemEntry.builder()
                            .setItemId(k1)
                            .setItemType(itemType)
                            .setEntryType("itemId")
                            .build());
        }
        if (extItemId != null
                && !extItemId.isEmpty()
                && itemTypeSet.contains(extItemType)
                && ((!"card".equals(extItemType) && extItemId.chars().allMatch(Character::isDigit))
                        || ("card".equals(extItemType)
                                && !extItemId.chars().allMatch(Character::isDigit)))) {
            String k2 =
                    !"card".equals(extItemType)
                            ? "dp_" + Long.toHexString(Long.parseLong(extItemId)).toLowerCase()
                            : "dc_" + extItemId;
            itemIds.add(k2);
            itemEntries.add(
                    ItemEntry.builder()
                            .setItemId(k2)
                            .setItemType(extItemType)
                            .setEntryType("ext_itemId")
                            .build());
        }
        if (addItemId != null
                && !addItemId.isEmpty()
                && itemTypeSet.contains(addItemType)
                && ((!"card".equals(addItemType) && addItemId.chars().allMatch(Character::isDigit))
                        || ("card".equals(addItemType)
                                && !addItemId.chars().allMatch(Character::isDigit)))) {
            String k3 =
                    !"card".equals(addItemType)
                            ? "dp_" + Long.toHexString(Long.parseLong(addItemId)).toLowerCase()
                            : "dc_" + addItemId;
            itemIds.add(k3);
            itemEntries.add(
                    ItemEntry.builder()
                            .setItemId(k3)
                            .setItemType(addItemType)
                            .setEntryType("add_itemId")
                            .build());
        }
        if (nestedItemId != null
                && !nestedItemId.isEmpty()
                && itemTypeSet.contains(nestedItemType)
                && ((!"card".equals(nestedItemType)
                                && nestedItemId.chars().allMatch(Character::isDigit))
                        || ("card".equals(nestedItemType)
                                && !nestedItemId.chars().allMatch(Character::isDigit)))) {
            String k4 =
                    !"card".equals(nestedItemType)
                            ? "dp_" + Long.toHexString(Long.parseLong(nestedItemId)).toLowerCase()
                            : "dc_" + nestedItemId;
            itemIds.add(k4);
            itemEntries.add(
                    ItemEntry.builder()
                            .setItemId(k4)
                            .setItemType(nestedItemType)
                            .setEntryType("ext/itemId")
                            .build());
        }

        if (!itemIds.isEmpty()) {
            Map<String, String> itemMap = getCacheValues(itemIds);
            for (ItemEntry e : itemEntries) {
                String id = e.getItemId();
                String permalink = itemMap.get(id);
                if ("itemId".equals(e.getEntryType())
                        || "ext_itemId".equals(e.getEntryType())
                        || "add_itemId".equals(e.getEntryType())) {
                    attributes.put(e.getEntryType(), permalink);
                }
                if ("ext/itemId".equals(e.getEntryType())) {
                    nested.put("itemId", permalink);
                    try {
                        attributes.put("ext", objectMapper.writeValueAsString(nested));
                    } catch (JsonProcessingException ex) {
                        LOG.info("Parse nested json error: {}", recParsedLogEvents);
                    }
                }
            }

            ObjectNode resultSource = ((ObjectNode) root).without("attributes").deepCopy();
            resultSource.set("attributes", attributes);
            resultFuture.complete(
                    Collections.singletonList(
                            RecParsedLogEvents.builder()
                                    .setEventId(recParsedLogEvents.getEventId())
                                    .setDeviceUdid(recParsedLogEvents.getDeviceUdid())
                                    .setAppVersion(recParsedLogEvents.getAppVersion())
                                    .setSource(objectMapper.writeValueAsString(resultSource))
                                    .setAction(recParsedLogEvents.getAction())
                                    .build()));
        } else {
            resultFuture.complete(Collections.singletonList(recParsedLogEvents));
        }
    }

    @Override
    public void timeout(
            RecParsedLogEvents recParsedLogEvents, ResultFuture<RecParsedLogEvents> resultFuture)
            throws Exception {
        LOG.error(
                "Timeout when get permalinkId or activityId from redis, event: {}",
                recParsedLogEvents.toString());
        resultFuture.completeExceptionally(
                new FlinkRuntimeException("Timeout when get permalinkId or activityId from redis"));
    }

    private Map<String, String> getCacheValues(Iterable<? extends String> keys) {
        try {
            return itemCache.getAll(keys).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new FlinkRuntimeException(e);
        }
    }

    private CompletableFuture<Map<String, String>> updateCacheFromRedis(
            Iterable<? extends String> keys) {
        List<String> keyList = new ArrayList<>();
        keys.iterator().forEachRemaining(keyList::add);
        return redisAsyncCommands
                .mget(keyList.toArray(new String[0]))
                .thenApply(
                        keyValues -> {
                            Map<String, String> resultMap = new HashMap<>(keyValues.size());
                            keyValues.forEach(
                                    kv -> {
                                        try {
                                            resultMap.put(kv.getKey(), kv.getValue());
                                        } catch (NoSuchElementException e) {
                                            resultMap.put(kv.getKey(), "");
                                        }
                                    });
                            return resultMap;
                        })
                .toCompletableFuture();
    }

    @Override
    public void close() throws Exception {
        redisConnection.close();
        redisClusterClient.close();
    }
}
