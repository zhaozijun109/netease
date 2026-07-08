package com.netease.easyudf.udf.util;


import org.apache.commons.codec.digest.MurmurHash3;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashStringToIntUDF extends UDF {

    public static Long hash(String s, String algo, int nbytes) throws NoSuchAlgorithmException {
        assert nbytes <= 8;
        byte[] sBytes = s.getBytes();
        if (algo.equalsIgnoreCase("murmur")) {
            assert nbytes == 8 || nbytes == 4;
            if (nbytes == 4) {
                return (long) MurmurHash3.hash32(sBytes, 0, sBytes.length);
            }
            return MurmurHash3.hash64(sBytes, 0, sBytes.length);
        }

        MessageDigest md = MessageDigest.getInstance(algo);
        byte[] hashBytes = md.digest(sBytes);
        byte[] bytes = new byte[8];
        System.arraycopy(hashBytes, 0, bytes, 8 - nbytes, nbytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, 8);
        return buffer.getLong();
    }

    public static Long hash(String s) throws NoSuchAlgorithmException {
        return hash(s, 8);
    }

    public static Long hash(String s, int nbytes) throws NoSuchAlgorithmException {
        return hash(s, "SHA-256", nbytes);
    }

    public Long evaluate(String s) throws NoSuchAlgorithmException {
        return hash(s);
    }

    public Long evaluate(String s, int nbytes) throws NoSuchAlgorithmException {
        return hash(s, "SHA-256", nbytes);
    }

    public Long evaluate(String s, String algo, int nbytes) throws NoSuchAlgorithmException {
        return hash(s, algo, nbytes);
    }

}
