package com.netease.util;

import com.netease.dts.common.DTSException;
import com.netease.dts.common.subscribe.DefaultSubscribeEventSerializer;
import com.netease.dts.common.subscribe.SubscribeEvent;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

/** Ndc binlog source deserialize util. */
public class BinlogSubscribeEventSchemaUtils implements DeserializationSchema<SubscribeEvent> {
    private final DefaultSubscribeEventSerializer serializer =
            new DefaultSubscribeEventSerializer();

    @Override
    public SubscribeEvent deserialize(byte[] bytes) throws IOException {
        try {
            return serializer.deserialize(bytes);
        } catch (DTSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEndOfStream(SubscribeEvent subscribeEvent) {
        return false;
    }

    @Override
    public TypeInformation<SubscribeEvent> getProducedType() {
        return TypeInformation.of(SubscribeEvent.class);
    }
}
