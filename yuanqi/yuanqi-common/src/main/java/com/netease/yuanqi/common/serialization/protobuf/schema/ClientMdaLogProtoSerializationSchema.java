package com.netease.yuanqi.common.serialization.protobuf.schema;

import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;

public class ClientMdaLogProtoSerializationSchema
        extends ProtobufSerializationSchema<ClientMdaLogProtoBuilder.ClientMdaLogProto> {
    private static final long serialVersionUID = 1L;

    @Override
    public void open(InitializationContext context) throws Exception {
        super.open(context);
    }

    @Override
    public byte[] serialize(ClientMdaLogProtoBuilder.ClientMdaLogProto clientMdaLogProto) {
        return clientMdaLogProto.toByteArray();
    }
}
