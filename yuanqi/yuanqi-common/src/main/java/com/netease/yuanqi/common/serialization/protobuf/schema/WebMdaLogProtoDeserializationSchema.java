package com.netease.yuanqi.common.serialization.protobuf.schema;

import com.netease.yuanqi.common.pojo.proto.ods.WebMdaLogProtoBuilder;
import java.io.IOException;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;

public class WebMdaLogProtoDeserializationSchema
        extends ProtobufDeserializationSchema<WebMdaLogProtoBuilder.WebMdaLogProto> {
    @Override
    public void open(InitializationContext context) throws Exception {
        super.open(context);
    }

    @Override
    public WebMdaLogProtoBuilder.WebMdaLogProto deserialize(byte[] bytes) throws IOException {
        // WebMdaLogProto webMdaLogProto = new WebMdaLogProto();
        // webMdaLogProto.parseFrom(bytes);
        // return webMdaLogProto;
        return WebMdaLogProtoBuilder.WebMdaLogProto.parseFrom(bytes);
    }

    @Override
    public void deserialize(byte[] message, Collector<WebMdaLogProtoBuilder.WebMdaLogProto> out)
            throws IOException {
        super.deserialize(message, out);
    }

    @Override
    public boolean isEndOfStream(WebMdaLogProtoBuilder.WebMdaLogProto webMdaLog) {
        return false;
    }

    @Override
    public TypeInformation<WebMdaLogProtoBuilder.WebMdaLogProto> getProducedType() {
        return TypeInformation.of(WebMdaLogProtoBuilder.WebMdaLogProto.class);
    }
}
