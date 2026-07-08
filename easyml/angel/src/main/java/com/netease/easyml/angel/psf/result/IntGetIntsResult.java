
package com.netease.easyml.angel.psf.result;

import com.tencent.angel.ml.matrix.psf.get.base.GetResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class IntGetIntsResult extends GetResult {

    private final Int2ObjectOpenHashMap<int[]> data;

    public IntGetIntsResult(Int2ObjectOpenHashMap<int[]> data) {
        this.data = data;
    }

    public Int2ObjectOpenHashMap<int[]> getData() {
        return data;
    }
}