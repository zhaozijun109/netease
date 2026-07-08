package com.netease.yuanqi.common.serialization.protobuf.schema;

import com.netease.yuanqi.common.pojo.proto.ods.WebMdaLogProtoBuilder;

public class WebMdaLogProtoSerializationSchema
        extends ProtobufSerializationSchema<WebMdaLogProtoBuilder.WebMdaLogProto> {
    @Override
    public void open(InitializationContext context) throws Exception {
        super.open(context);
    }

    @Override
    public byte[] serialize(WebMdaLogProtoBuilder.WebMdaLogProto webMdaLogProto) {
        return webMdaLogProto.toByteArray();
    }
}
