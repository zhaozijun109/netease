package com.netease.wm.udf.common;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Description(
        name = "base64_aes",
        value = "_FUNC_(str, operation)",
        extended = "operation: encrypt/解密/en, decrypt/解密/de"
)

public class Base64AesUDF extends GenericUDF {

    // todo  加密key后面改成获取，不能写死
    private String key = "WV8ERjv3cKMtKB0xUqZznA==";

    public static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES_NAME = "AES";

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {

        if (arguments.length != 2) {
            throw new UDFArgumentException("Requires exactly two arguments");
        }
        return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {

        String data = arguments[0].get().toString();
        String operation = arguments[1].get().toString();

        if (data == null || operation == null || data.isEmpty()) {
            return null;
        }
        try {
            if (Arrays.asList("en", "encrypt", "加密").contains(operation)){
                return encrypt(key,data);
            }else if (Arrays.asList("de", "decrypt", "解密").contains(operation)){
                return decrypt(key,data);
            }else {
                return null;
            }
        } catch (Exception e) {
            return null;
//            throw new HiveException("AES operation failed", e);
        }
    }

    public static String encrypt(String base64AesKey, String plainText) {
        byte[] byteKey = Base64.decodeBase64(base64AesKey);
        return encrypt(byteKey, byteKey, plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static String encrypt(byte[] key, byte[] iv, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key, AES_NAME);
            IvParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec);
            byte[] result = cipher.doFinal(data);
            return Base64.encodeBase64String(result);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String base64Key, String base64Data) {
        byte[] byteKey = Base64.decodeBase64(base64Key);
        return decrypt(byteKey, byteKey, base64Data);
    }

    public static String decrypt(byte[] key, byte[] iv, String base64Data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key, AES_NAME);
            IvParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            byte[] decrypted = cipher.doFinal(Base64.decodeBase64(base64Data));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDisplayString(String[] children) {
        return "base64_aes";
    }
}
