
package com.netease.easyml.angel.psf.update;

import com.tencent.angel.graph.common.psf.param.IntKeysUpdateParam;
import com.tencent.angel.graph.utils.GraphMatrixUtils;
import com.tencent.angel.ml.matrix.psf.update.base.GeneralPartUpdateParam;
import com.tencent.angel.ml.matrix.psf.update.base.PartitionUpdateParam;
import com.tencent.angel.ml.matrix.psf.update.base.UpdateFunc;
import com.tencent.angel.ps.storage.vector.ServerIntAnyRow;
import com.tencent.angel.ps.storage.vector.element.IElement;
import com.tencent.angel.psagent.matrix.transport.router.operator.IIntKeyAnyValuePartOp;

/**
 * Init node neighbors for int type node id
 */
public class IntInitNeighbors extends UpdateFunc {

    /**
     * Create a new UpdateParam
     */
    public IntInitNeighbors(IntKeysUpdateParam param) {
        super(param);
    }

    public IntInitNeighbors() {
        this(null);
    }

    @Override
    public void partitionUpdate(PartitionUpdateParam partParam) {
        GeneralPartUpdateParam initParam = (GeneralPartUpdateParam) partParam;
        ServerIntAnyRow row = GraphMatrixUtils.getPSIntKeyRow(psContext, initParam);

        // Get nodes and features
        IIntKeyAnyValuePartOp split = (IIntKeyAnyValuePartOp) initParam.getKeyValuePart();
        int[] nodeIds = split.getKeys();
        IElement[] neighbors = split.getValues();

        row.startWrite();
        try {
            for (int i = 0; i < nodeIds.length; i++) {
                row.set(nodeIds[i], neighbors[i]);
            }
        } finally {
            row.endWrite();
        }
    }
}
