package com.netease.wm.udf.bitmap;
import org.roaringbitmap.Util;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BitmapValue {
    private Roaring64NavigableMap bitmap;

    public BitmapValue() {
        bitmap = new Roaring64NavigableMap();
    }

    public void add(int value) {
        add(Util.toUnsignedLong(value));
    }

    public void add(long value) {
        bitmap.addLong(value);
    }

    public boolean contains(int value) {
        return contains(Util.toUnsignedLong(value));
    }

    public boolean contains(long value) {
        return bitmap.contains(value);
    }

    public long cardinality() {
        return bitmap.getLongCardinality();
    }

    public void serialize(DataOutput output) throws IOException {
        bitmap.serialize(output);
    }

    public void deserialize(DataInput input) throws IOException {
        clear();
        bitmap = bitmap == null ? new Roaring64NavigableMap() : bitmap;
        bitmap.deserialize(input);
    }

    // In-place bitwise AND (intersection) operation. The current bitmap is modified.
    public void and(BitmapValue other) {
        this.bitmap.and(other.bitmap);
    }

    // In-place bitwise OR (union) operation. The current bitmap is modified.
    public void or(BitmapValue other) {
        this.bitmap.or(other.bitmap);
    }

    public void remove(long value) {
        this.bitmap.removeLong(value);
    }

    //In-place bitwise ANDNOT (difference) operation. The current bitmap is modified
    public void not(BitmapValue other) {
        this.bitmap.andNot(other.bitmap);
    }

    //In-place bitwise XOR (symmetric difference) operation. The current bitmap is modified
    public void xor(BitmapValue other) {
        this.bitmap.xor(other.bitmap);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof BitmapValue)) {
            return false;
        }
        return bitmap.equals(((BitmapValue) other).bitmap);
    }

    public long[] toArray() {
        return this.bitmap.toArray();
    }

    @Override
    public String toString() {
        return this.bitmap.toString();
    }

    public void clear() {
        this.bitmap = null;
    }
}
