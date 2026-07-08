package com.netease.easyml.angel.psf.get;

import com.netease.easyml.angel.psf.result.IntGetIntsResult;
import com.netease.easyml.angel.psf.result.IntPartGeneralGetResult;
import com.netease.easyml.angel.utils.GeneralGetUtils;
import com.tencent.angel.graph.common.conf.Constents;
import com.tencent.angel.graph.common.psf.param.IntKeysGetParam;
import com.tencent.angel.ml.matrix.psf.get.base.GetFunc;
import com.tencent.angel.ml.matrix.psf.get.base.GetResult;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetParam;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetResult;
import com.tencent.angel.ps.storage.vector.element.IElement;
import com.tencent.angel.ps.storage.vector.element.IntArrayElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.List;

/**
 * Sample the neighbor
 */
public class IntGetIntNeighbor extends GetFunc {

    public IntGetIntNeighbor(IntKeysGetParam param) {
        super(param);
    }

    public IntGetIntNeighbor() {
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
            resultSize += ((IntPartGeneralGetResult) result).getNodeIds().length;
        }

        Int2ObjectOpenHashMap<int[]> nodeIdToNeighbors = new Int2ObjectOpenHashMap<>(resultSize);

        for (PartitionGetResult result : partResults) {
            IntPartGeneralGetResult partResult = (IntPartGeneralGetResult) result;
            int[] nodeIds = partResult.getNodeIds();
            IElement[] neighbors = partResult.getData();
            for (int i = 0; i < nodeIds.length; i++) {
                if (neighbors[i] != null) {
                    nodeIdToNeighbors.put(nodeIds[i], ((IntArrayElement) neighbors[i]).getData());
                } else {
                    nodeIdToNeighbors.put(nodeIds[i], Constents.emptyInts);
                }
            }
        }

        return new IntGetIntsResult(nodeIdToNeighbors);
    }
}