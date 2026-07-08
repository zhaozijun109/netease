package com.netease.easyml.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by linjiuning on 2020/8/24.
 */
public class ResourceUtil {
    private static final Logger log = LoggerFactory.getLogger(ResourceUtil.class);

    public static Map<String, float[]> loadWordVec(String path) throws IOException {
        return loadWordVec(path, " ");
    }

    public static Map<String, float[]> loadWordVec(String path, String delimiter) throws IOException {
        return loadWordVec(path, delimiter, delimiter);
    }

    public static Map<String, float[]> loadWordVec(String path, String kvDelimiter, String vDelimiter) throws IOException {
        List<String> files = new ArrayList<>();
        if (IOUtil.isDirectory(path)) {
            files = IOUtil.listAllFile(path);
        } else {
            files.add(path);
        }
        Map<String, float[]> dict = new HashMap<>();
        int dim = -1;
        for (String file : files) {
            try (BufferedReader reader = IOUtil.getBufferedReader(file)) {
                String line = "";
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    line = line.replaceAll("\n$", "");
                    int index = line.indexOf(kvDelimiter, 1);
                    if (index < 0) {
                        if (files.size() > 1) {
                            log.warn(String.format("Error in split file %s, %d line:\t%s", file, lineNum, line));
                        } else {
                            log.warn(String.format("Error in split %d line:\t%s", lineNum, line));
                        }
                        continue;
                    }
                    String key = line.substring(0, index);
                    String[] items = line.substring(index + 1).split(vDelimiter);
                    if (items.length > 1 && (dim < 0 || items.length == dim)) {
                        if (dim < 0) {
                            dim = items.length;
                        }
                        float[] value = new float[dim];
                        for (int i = 0; i < items.length; i++)
                            value[i] = Float.parseFloat(items[i]);
                        dict.put(key, value);
                    } else if (items.length == 1 && lineNum == 1 && files.size() == 1) {
                        dim = Integer.parseInt(items[0]);
                        log.info(String.format("WordVec vocabSize: %s\tdim: %d", key, dim));
                    } else {
                        if (files.size() > 1) {
                            log.warn(String.format("Error in split file %s, %d line:\t%s", file, lineNum, line));
                        } else {
                            log.warn(String.format("Error in split %d line:\t%s", lineNum, line));
                        }
                    }
                }
            }
        }
        return dict;
    }

    public static void saveWordVec(String path, Map<String, float[]> wordVectors) throws IOException {
        saveWordVec(path, wordVectors, " ");
    }

    public static void saveWordVec(String path, Map<String, float[]> wordVectors, String delimiter) throws IOException {
        try (BufferedWriter writer = IOUtil.getBufferedWriter(path)) {
            int cnt = wordVectors.size();
            int dim = wordVectors.values().iterator().next().length;
            writer.write(cnt + delimiter + dim + "\n");
            for (Map.Entry<String, float[]> entry : wordVectors.entrySet()) {
                String word = entry.getKey();
                String vector = StringUtil.join(entry.getValue(), delimiter);
                writer.write(word + delimiter + vector + "\n");
            }
        }
    }

    public static Map<String, Float> loadWordWeight(String path) throws IOException {
        return loadWordWeight(path, "\t");
    }

    public static Map<String, Float> loadWordWeight(String path, String delimiter) throws IOException {
        List<String> files = new ArrayList<>();
        if (IOUtil.isDirectory(path)) {
            files = IOUtil.listAllFile(path);
        } else {
            files.add(path);
        }
        Map<String, Float> dict = new HashMap<>();
        for (String file : files) {
            try (BufferedReader reader = IOUtil.getBufferedReader(file)) {
                String line = "";
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    line = line.replaceAll("\n$", "");
                    int index = line.lastIndexOf(delimiter);
                    if (index < 1) {
                        if (files.size() > 1) {
                            log.warn(String.format("Error in split file %s, %d line:\t%s", file, lineNum, line));
                        } else {
                            log.warn(String.format("Error in split %d line:\t%s", lineNum, line));
                        }
                        continue;
                    }
                    String key = line.substring(0, index);
                    String value = line.substring(index + 1);
                    dict.put(key, Float.parseFloat(value));
                }
            }
        }
        return dict;
    }

    public static void saveWordWeight(String path, Map<String, Float> wordWeights) throws IOException {
        saveWordWeight(path, wordWeights, "\t");
    }

    public static void saveWordWeight(String path, Map<String, Float> wordWeights, String delimiter) throws IOException {
        try (BufferedWriter writer = IOUtil.getBufferedWriter(path)) {
            for (Map.Entry<String, Float> entry : wordWeights.entrySet()) {
                String word = entry.getKey();
                Float weight = entry.getValue();
                writer.write(word + delimiter + weight + "\n");
            }
        }
    }
}
