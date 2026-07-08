package com.netease.yuanqi.common.serialization.protobuf.schema;

import com.netease.yuanqi.common.pojo.proto.ods.WapMdaLogProtoBuilder;

public class WapMdaLogProtoSerializationSchema
        extends ProtobufSerializationSchema<WapMdaLogProtoBuilder.WapMdaLogProto> {
    @Override
    public void open(InitializationContext context) throws Exception {
        super.open(context);
    }

    @Override
    public byte[] serialize(WapMdaLogProtoBuilder.WapMdaLogProto wapMdaLogProto) {
        return wapMdaLogProto.toByteArray();
    }
}
