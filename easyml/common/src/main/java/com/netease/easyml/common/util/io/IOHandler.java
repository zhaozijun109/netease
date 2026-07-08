package com.netease.easyml.common.util.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by linjiuning on 2020/6/22.
 */
public interface IOHandler {
    boolean exists(String path);

    boolean isFile(String path);

    boolean isDirectory(String path);

    long lastModified(String path);

    boolean mkdirs(String path);

    boolean delete(String path);

    boolean rename(String path, String path1);

    String baseName(String path);

    String parentName(String path);

    String join(String first, String... more);

    List<String> listAllFile(String path, Predicate<String> predicate);

    List<String> listAllDirectory(String path, Predicate<String> predicate);

    default List<String> listAllFile(String path) {
        return listAllFile(path, null);
    }

    default List<String> listAllDirectory(String path) {
        return listAllDirectory(path, null);
    }

    List<String> listFile(String path, Predicate<String> predicate);

    List<String> listDirectory(String path, Predicate<String> predicate);

    default List<String> listFile(String path) {
        return listFile(path, null);
    }

    default List<String> listDirectory(String path) {
        return listDirectory(path, null);
    }

    InputStream getInputStream(String path);

    OutputStream getOutputStream(String path);

    String HDFS_PREFIX = "hdfs://";
}
