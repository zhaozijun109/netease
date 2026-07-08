package com.netease.easyml.common.collection;

/**
 * Created by linjiuning on 2018/6/12.
 */
public class Triple <V1, V2, V3> {

    public static <V1, V2, V3> Triple<V1, V2, V3> triple(V1 v1, V2 v2, V3 v3) {
        return new Triple<>(v1, v2, v3);
    }

    private final V1 v1;
    private final V2 v2;
    private final V3 v3;

    public Triple(V1 v1, V2 v2, V3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public V1 v1() {
        return v1;
    }

    public V2 v2() {
        return v2;
    }

    public V3 v3() {
        return v3;
    }

    @Override
    public String toString() {
        return "Triple{" +
                "v1=" + v1 +
                ", v2=" + v2 +
                ", v3=" + v3 +
                '}';
    }
}