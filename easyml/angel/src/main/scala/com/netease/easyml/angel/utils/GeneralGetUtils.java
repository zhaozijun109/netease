package com.netease.easyml.angel.utils;

import com.netease.easyml.angel.psf.result.IntPartGeneralGetResult;
import com.tencent.angel.exception.AngelException;
import com.tencent.angel.graph.utils.GraphMatrixUtils;
import com.tencent.angel.ml.matrix.MatrixMeta;
import com.tencent.angel.ml.matrix.psf.get.base.GeneralPartGetParam;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetParam;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetResult;
import com.tencent.angel.ps.PSContext;
import com.tencent.angel.ps.storage.vector.ServerIntAnyRow;
import com.tencent.angel.ps.storage.vector.element.IElement;
import com.tencent.angel.psagent.matrix.transport.router.KeyPart;
import com.tencent.angel.psagent.matrix.transport.router.operator.IIntKeyPartOp;

public class GeneralGetUtils {
    public static PartitionGetResult partitionIntGet(PSContext psContext, PartitionGetParam partParam) {
        GeneralPartGetParam param = (GeneralPartGetParam) partParam;
        KeyPart keyPart = param.getIndicesPart();

        // Long type node id
        int[] nodeIds = ((IIntKeyPartOp) keyPart).getKeys();
        ServerIntAnyRow row = GraphMatrixUtils.getPSIntKeyRow(psContext, param);

        // Get data
        IElement[] data = new IElement[nodeIds.length];
        for (int i = 0; i < nodeIds.length; i++) {
            data[i] = row.get(nodeIds[i]);
        }

        MatrixMeta meta = psContext.getMatrixMetaManager().getMatrixMeta(param.getMatrixId());

        try {
            return new IntPartGeneralGetResult(meta.getValueClass(), nodeIds, data);
        } catch (ClassNotFoundException e) {
            throw new AngelException("Can not get value class ");
        }
    }
}
