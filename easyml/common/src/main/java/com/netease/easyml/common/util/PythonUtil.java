package com.netease.easyml.common.util;

import com.google.common.io.ByteStreams;
import org.jpmml.python.CompressedInputStreamStorage;
import org.jpmml.python.InputStreamStorage;
import org.jpmml.python.PickleUtil;
import org.jpmml.python.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by linjiuning on 2020/8/4.
 */
public class PythonUtil {
    private static final Logger log = LoggerFactory.getLogger(PythonUtil.class);

    static {
        PickleUtil.init("python2pmml.properties");
    }

    public static Object unpickle(String path) throws IOException {
        return unpickle(IOUtil.getInputStream(path));
    }

    public static Object unpickle(InputStream stream) throws IOException {
        byte[] bytes;

        try {
            bytes = ByteStreams.toByteArray(stream);
        } finally {
            stream.close();
        }

        return unpickle(bytes);
    }

    public static Object unpickle(byte[] bytes) throws IOException {
        Storage storage;

        try {
            storage = new CompressedInputStreamStorage(new ByteArrayInputStream(bytes));
        } catch (IOException ioe) {
            storage = new InputStreamStorage(new ByteArrayInputStream(bytes));
        }

        return PickleUtil.unpickle(storage);
    }
}
