package com.netease.wm.udf.bitmap;

import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BitmapValueUtil {
    public static byte[] serializeToBytes(BitmapValue bitmapValue) throws IOException {
        Roaring64NavigableMap.SERIALIZATION_MODE = Roaring64NavigableMap.SERIALIZATION_MODE_PORTABLE;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        bitmapValue.serialize(dos);
        dos.close();
        return bos.toByteArray();
    }

    public static BitmapValue deserializeToBitmap(byte[] bytes) throws IOException {
        Roaring64NavigableMap.SERIALIZATION_MODE = Roaring64NavigableMap.SERIALIZATION_MODE_PORTABLE;
        BitmapValue bitmapValue = new BitmapValue();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        bitmapValue.deserialize(in);
        in.close();
        return bitmapValue;
    }
}
