package com.netease.yuanqi.common.serialization.protobuf.schema;

import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;
import java.io.IOException;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;

public class ClientMdaLogProtoDeserializationSchema
        extends ProtobufDeserializationSchema<ClientMdaLogProtoBuilder.ClientMdaLogProto> {

    @Override
    public void open(InitializationContext context) throws Exception {
        super.open(context);
    }

    @Override
    public ClientMdaLogProtoBuilder.ClientMdaLogProto deserialize(byte[] bytes) throws IOException {
        // ClientMdaLogProto clientMdaLogProto = new ClientMdaLogProto();
        // clientMdaLogProto.parseFrom(bytes);
        // return clientMdaLogProto;
        return ClientMdaLogProtoBuilder.ClientMdaLogProto.parseFrom(bytes);
    }

    @Override
    public void deserialize(
            byte[] message, Collector<ClientMdaLogProtoBuilder.ClientMdaLogProto> out)
            throws IOException {
        super.deserialize(message, out);
    }

    @Override
    public boolean isEndOfStream(ClientMdaLogProtoBuilder.ClientMdaLogProto clientMdaLogProto) {
        return false;
    }

    @Override
    public TypeInformation<ClientMdaLogProtoBuilder.ClientMdaLogProto> getProducedType() {
        return TypeInformation.of(ClientMdaLogProtoBuilder.ClientMdaLogProto.class);
    }
}
