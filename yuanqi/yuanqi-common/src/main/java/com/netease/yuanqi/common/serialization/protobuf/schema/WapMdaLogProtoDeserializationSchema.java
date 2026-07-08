package com.netease.yuanqi.common.serialization.protobuf.schema;

import com.netease.yuanqi.common.pojo.proto.ods.WapMdaLogProtoBuilder;
import java.io.IOException;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;

public class WapMdaLogProtoDeserializationSchema
        extends ProtobufDeserializationSchema<WapMdaLogProtoBuilder.WapMdaLogProto> {
    @Override
    public void open(InitializationContext context) throws Exception {
        super.open(context);
    }

    @Override
    public WapMdaLogProtoBuilder.WapMdaLogProto deserialize(byte[] bytes) throws IOException {
        // WapMdaLogProto wapMdaLogProto = new WapMdaLogProto();
        // wapMdaLogProto.parseFrom(bytes);
        // return wapMdaLogProto;
        return WapMdaLogProtoBuilder.WapMdaLogProto.parseFrom(bytes);
    }

    @Override
    public void deserialize(byte[] message, Collector<WapMdaLogProtoBuilder.WapMdaLogProto> out)
            throws IOException {
        super.deserialize(message, out);
    }

    @Override
    public boolean isEndOfStream(WapMdaLogProtoBuilder.WapMdaLogProto wapMdaLogProto) {
        return false;
    }

    @Override
    public TypeInformation<WapMdaLogProtoBuilder.WapMdaLogProto> getProducedType() {
        return TypeInformation.of(WapMdaLogProtoBuilder.WapMdaLogProto.class);
    }
}
