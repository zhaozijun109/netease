
package com.netease.easyml.angel.psf.result;

import com.netease.easyml.angel.utils.GeneralGetUtils;
import com.tencent.angel.graph.common.psf.param.IntKeysGetParam;
import com.tencent.angel.graph.model.general.get.PartGeneralGetResult;
import com.tencent.angel.ml.matrix.psf.get.base.GetFunc;
import com.tencent.angel.ml.matrix.psf.get.base.GetResult;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetParam;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetResult;
import com.tencent.angel.ps.storage.vector.element.ByteArrayElement;
import com.tencent.angel.ps.storage.vector.element.IElement;
import com.twitter.chill.ScalaKryoInstantiator;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.List;

/**
 * Sample the neighbor
 */
public class IntGetByteNeighbor extends GetFunc {

    public static final int[] emptyInts = new int[0];

    public IntGetByteNeighbor(IntKeysGetParam param) {
        super(param);
    }

    public IntGetByteNeighbor() {
        this(null);
    }

    @Override
    public PartitionGetResult partitionGet(PartitionGetParam partParam) {
        return GeneralGetUtils.partitionIntGet(psContext, partParam);
    }

    @Override
    public GetResult merge(List<PartitionGetResult> partResults) {
        int resultSize = 0;
        for (PartitionGetResult result : partResults) {
            resultSize += ((PartGeneralGetResult) result).getNodeIds().length;
        }

        Int2ObjectOpenHashMap<int[]> nodeIdToNeighbors = new Int2ObjectOpenHashMap<>(resultSize);

        for (PartitionGetResult result : partResults) {
            IntPartGeneralGetResult partResult = (IntPartGeneralGetResult) result;
            int[] nodeIds = partResult.getNodeIds();
            IElement[] data = partResult.getData();
            for (int i = 0; i < nodeIds.length; i++) {
                if (data[i] != null) {
                    byte[] serializedNeighbors = ((ByteArrayElement) data[i]).getData();
                    if (serializedNeighbors.length > 0) {
                        nodeIdToNeighbors.put(nodeIds[i],
                                ScalaKryoInstantiator.defaultPool().fromBytes(serializedNeighbors, int[].class));
                    } else {
                        nodeIdToNeighbors.put(nodeIds[i], emptyInts);
                    }
                } else {
                    nodeIdToNeighbors.put(nodeIds[i], emptyInts);
                }
            }
        }

        return new IntGetIntsResult(nodeIdToNeighbors);
    }
}
