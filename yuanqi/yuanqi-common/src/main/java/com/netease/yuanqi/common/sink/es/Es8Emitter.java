package com.netease.yuanqi.common.sink.es;

import co.elastic.clients.elasticsearch.core.bulk.BulkOperationVariant;
import java.io.Serializable;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.base.sink.writer.ElementConverter;

/**
 * Elasticsearch 8 sink 的元素转换器抽象类，模仿原 {@code ElasticsearchEmitter} 风格.
 *
 * <ul>
 *   <li>子类实现 {@link #open()} 用于初始化资源（例如 ObjectMapper）
 *   <li>子类实现 {@link #emit(Object)} 将单条流元素转换为 {@link BulkOperationVariant}
 * </ul>
 *
 * <p>checked exception 仍由用户在 {@link #emit(Object)} 中以 try-catch 包裹后抛出 RuntimeException 处理。
 *
 * @param <T> 输入元素类型
 */
public abstract class Es8Emitter<T>
        implements ElementConverter<T, BulkOperationVariant>, Serializable {
    private static final long serialVersionUID = 1L;
    private transient boolean opened;

    /** 子类可以覆写以初始化资源（例如 ObjectMapper）。Sink 第一次处理元素前会调用一次. */
    public void open() throws Exception {}

    /**
     * 将单条流元素转换为 ES8 客户端的 {@link BulkOperationVariant}（例如 {@code IndexOperation} / {@code
     * UpdateOperation} / {@code DeleteOperation}）.
     */
    public abstract BulkOperationVariant emit(T element);

    @Override
    public final BulkOperationVariant apply(T element, SinkWriter.Context context) {
        if (!opened) {
            try {
                open();
            } catch (Exception e) {
                throw new RuntimeException("Failed to open Es8Emitter", e);
            }
            opened = true;
        }
        return emit(element);
    }
}
