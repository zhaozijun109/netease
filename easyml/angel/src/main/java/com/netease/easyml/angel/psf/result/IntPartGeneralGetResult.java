package com.netease.easyml.angel.psf.result;

import com.tencent.angel.common.ByteBufSerdeUtils;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetResult;
import com.tencent.angel.ps.storage.vector.element.IElement;
import io.netty.buffer.ByteBuf;

public class IntPartGeneralGetResult extends PartitionGetResult {

    private transient Class<? extends IElement> dataClass;
    private int[] nodeIds;
    private IElement[] data;

    public IntPartGeneralGetResult(Class<? extends IElement> dataClass, int[] nodeIds, IElement[] data) {
        this.dataClass = dataClass;
        this.nodeIds = nodeIds;
        this.data = data;
    }

    public IntPartGeneralGetResult() {

    }

    @Override
    public void serialize(ByteBuf output) {
        ByteBufSerdeUtils.serializeInts(output, nodeIds);
        ByteBufSerdeUtils.serializeObjects(output, dataClass, data);
    }

    @Override
    public void deserialize(ByteBuf input) {
        nodeIds = ByteBufSerdeUtils.deserializeInts(input);
        data = ByteBufSerdeUtils.deserializeObjects(input);
    }

    @Override
    public int bufferLen() {
        return ByteBufSerdeUtils.serializedIntsLen(nodeIds) + ByteBufSerdeUtils
                .serializedObjectsLen(dataClass, data);
    }

    public int[] getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(int[] nodeIds) {
        this.nodeIds = nodeIds;
    }

    public IElement[] getData() {
        return data;
    }

    public void setData(IElement[] data) {
        this.data = data;
    }
}
