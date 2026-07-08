package com.netease.easyml.common.util;

import com.alibaba.fastjson.JSON;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.netease.easyml.common.util.io.HdfsIOHandler;
import com.netease.easyml.common.util.io.LocalIOHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import static com.netease.easyml.common.util.io.IOHandler.HDFS_PREFIX;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class IOUtil {
    private static final Logger log = LoggerFactory.getLogger(IOUtil.class);
    private static final Pattern VARIABLE = Pattern.compile("\\$\\{.*?}");
    private static final String FILE_SEP = "/";

    public static boolean exists(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.exists(path);
        else
            return LocalIOHandler.handler.exists(path);
    }

    public static boolean isFile(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.isFile(path);
        else
            return LocalIOHandler.handler.isFile(path);
    }

    public static boolean isDirectory(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.isDirectory(path);
        else
            return LocalIOHandler.handler.isDirectory(path);
    }

    public static long lastModified(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.lastModified(path);
        else
            return LocalIOHandler.handler.lastModified(path);
    }

    public static boolean mkdirs(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.mkdirs(path);
        else
            return LocalIOHandler.handler.mkdirs(path);
    }

    public static boolean mkParentDirs(String path) {
        String parentName = IOUtil.parentName(path);
        if (!IOUtil.exists(parentName)) {
            return IOUtil.mkdirs(parentName);
        }
        return true;
    }

    public static boolean delete(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.delete(path);
        else
            return LocalIOHandler.handler.delete(path);
    }

    public static boolean rename(String path, String path1) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.rename(path, path1);
        else
            return LocalIOHandler.handler.rename(path, path1);
    }

    public static String baseName(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.baseName(path);
        else
            return LocalIOHandler.handler.baseName(path);
    }

    public static String parentName(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.parentName(path);
        else
            return LocalIOHandler.handler.parentName(path);
    }

    public static String join(String first, String... more) {
        if (isHdfs(first))
            return HdfsIOHandler.handler.join(first, more);
        else
            return LocalIOHandler.handler.join(first, more);
    }

    public static List<String> listFile(String path) {
        return listFile(path, null);
    }

    public static List<String> listFile(String path, Predicate<String> predicate) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.listFile(path, predicate);
        else
            return LocalIOHandler.handler.listFile(path, predicate);
    }

    public static List<String> listAllFile(String path) {
        return listAllFile(path, null);
    }

    public static List<String> listAllFile(String path, Predicate<String> predicate) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.listAllFile(path, predicate);
        else
            return LocalIOHandler.handler.listAllFile(path, predicate);
    }

    public static List<String> listDirectory(String path) {
        return listDirectory(path, null);
    }

    public static List<String> listDirectory(String path, Predicate<String> predicate) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.listDirectory(path, predicate);
        else
            return LocalIOHandler.handler.listDirectory(path, predicate);
    }

    public static List<String> listAllDirectory(String path) {
        return listAllDirectory(path, null);
    }

    public static List<String> listAllDirectory(String path, Predicate<String> predicate) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.listAllDirectory(path, predicate);
        else
            return LocalIOHandler.handler.listAllDirectory(path, predicate);
    }

    public static InputStream getInputStream(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.getInputStream(path);
        else
            return LocalIOHandler.handler.getInputStream(path);
    }

    public static OutputStream getOutputStream(String path) {
        if (isHdfs(path))
            return HdfsIOHandler.handler.getOutputStream(path);
        else
            return LocalIOHandler.handler.getOutputStream(path);
    }

    public static BufferedReader getBufferedReader(String path) {
        return getBufferedReader(getInputStream(path));
    }

    public static BufferedReader getBufferedReader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public static BufferedWriter getBufferedWriter(String path) {
        return getBufferedWriter(getOutputStream(path));
    }

    public static BufferedWriter getBufferedWriter(OutputStream stream) {
        return new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
    }

    public static DataInputStream getDataStreamReader(String path) {
        return getDataStreamReader(getInputStream(path));
    }

    public static DataInputStream getDataStreamReader(InputStream stream) {
        return new DataInputStream(new BufferedInputStream(stream));
    }

    public static DataOutputStream getDataStreamWriter(String path) {
        return getDataStreamWriter(getOutputStream(path));
    }

    public static DataOutputStream getDataStreamWriter(OutputStream stream) {
        return new DataOutputStream(new BufferedOutputStream(stream));
    }

    public static InputStream getResourceAsStream(String filePath) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return classloader.getResourceAsStream(filePath);
    }

    public static InputStream getResourceAsStream(String filePath, Class clazz) {
        return clazz.getResourceAsStream(filePath);
    }

    public static List<String> getResource(InputStream stream) {
        List<String> res = new ArrayList<>();
        try (BufferedReader reader = getBufferedReader(stream)) {
            for (String line; (line = reader.readLine()) != null; ) {
                res.add(line.replaceAll("\n$", ""));
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return res;
    }

    public static List<String> getResource(String filePath) {
        return getResource(getResourceAsStream(filePath));
    }

    public static InputStream stringToInputStream(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String inputStreamToString(InputStream inputStream) {
        String result = "";
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            result = CharStreams.toString(reader);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return result;
    }

    public static List<String> readLines(InputStream stream, boolean skipEmpty) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = getBufferedReader(stream)) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\n$", "");
                if (skipEmpty && line.isEmpty())
                    continue;
                lines.add(line);
            }
        } catch (IOException ex) {
            log.error("IOException: " + ex.getMessage());
        }
        return lines;
    }

    public static List<String> readLines(String path, boolean skipEmpty) {
        return readLines(getInputStream(path), skipEmpty);
    }

    /**
     * read all lines from file and skip empty lines
     */
    public static List<String> readLines(String path) {
        return readLines(path, true);
    }

    public static List<String> readLines(InputStream stream) {
        return readLines(stream, true);
    }

    public static void writeLines(OutputStream stream, Collection<String> content) {
        try (BufferedWriter writer = getBufferedWriter(stream)) {
            int i = 0;
            for (String text : content) {
                writer.write(text);
                if (i < content.size() - 1)
                    writer.write("\n");
            }
        } catch (IOException ex) {
            log.error("IOException: " + ex.getMessage());
        }
    }

    public static void writeLines(String path, Collection<String> content) {
        writeLines(getOutputStream(path), content);
    }

    public static List<String> readLinesBinary(InputStream stream) {
        List<String> lines = new ArrayList<>();

        try (DataInputStream reader = getDataStreamReader(stream)) {
            int size = reader.readInt();
            while (size-- > 0) {
                int lineLen = reader.readInt();
                byte[] bytes = new byte[lineLen];
                int sz = reader.read(bytes);
                if (sz != lineLen)
                    return Collections.emptyList();
                lines.add(new String(bytes, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return lines;
    }

    public static List<String> readLinesBinary(String path) {
        return readLinesBinary(getInputStream(path));
    }

    public static void writeLinesBinary(OutputStream stream, Collection<String> content) {
        try (DataOutputStream writer = getDataStreamWriter(stream)) {
            writer.writeInt(content.size());
            for (String text : content) {
                byte[] bytes = text.getBytes();
                writer.writeInt(bytes.length);
                writer.write(bytes);
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
    }

    public static void writeLinesBinary(String path, Collection<String> content) {
        writeLinesBinary(getOutputStream(path), content);
    }

    public static List<String> readLinesBinaryUTF(InputStream stream) {
        List<String> lines = new ArrayList<>();

        try (DataInputStream reader = getDataStreamReader(stream)) {
            int size = reader.readInt();
            while (size-- > 0)
                lines.add(reader.readUTF());
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return lines;
    }

    public static List<String> readLinesBinaryUTF(String path) {
        return readLinesBinaryUTF(getInputStream(path));
    }

    private static String truncate(String str) {
        int strlen = str.length();
        int utflen = 0;
        int c;

        /* use charAt instead of copying String to char array */
        int i = 0;
        for (; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
            if (utflen > 65535)
                break;
        }
        return str.substring(0, i);
    }

    public static void writeLinesBinaryUTF(OutputStream stream, Collection<String> content) {
        try (DataOutputStream writer = getDataStreamWriter(stream)) {
            writer.writeInt(content.size());
            for (String text : content) {
                try {
                    writer.writeUTF(text);
                } catch (UTFDataFormatException ex) {
                    log.warn("String is truncated caused by UTFDataFormatException: " + ex.getMessage());
                    writer.writeUTF(truncate(text));
                }
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
    }

    public static void writeLinesBinaryUTF(String path, Collection<String> content) {
        writeLinesBinary(getOutputStream(path), content);
    }

    public static Properties readProperties(InputStream stream) {
        return readProperties(stream, false);
    }

    public static Properties readProperties(String path) {
        return readProperties(path, false);
    }

    public static Properties readProperties(InputStream stream, boolean render) {
        Properties properties = new Properties();
        try (BufferedReader reader = getBufferedReader(stream)) {
            properties.load(reader);
            if (render) {
                properties = resolveDependency(properties);
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    value = replaceFileSep(value, FILE_SEP);
                    properties.put(key, value);
                }
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return properties;
    }

    public static Properties readProperties(String path, boolean render) {
        return readProperties(getInputStream(path), render);
    }

    public static void writeProperties(OutputStream stream, Properties properties) {
        try (BufferedWriter writer = getBufferedWriter(stream)) {
            properties.store(writer, "");
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
    }

    public static void writeProperties(String path, Properties properties) {
        writeProperties(getOutputStream(path), properties);
    }

    // 替换${}占位符
    private static Properties resolveDependency(Properties properties) {
        Map<String, String> variables = new LinkedHashMap<>();
        Map<String, Set<String>> dep = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            Set<String> deps = new HashSet<>();
            Matcher m = VARIABLE.matcher(value);
            while (m.find()) {
                String var = m.group();
                var = StringUtil.strip(var, Pattern.quote("${}")).trim();
                if (properties.containsKey(var))
                    deps.add(var);
            }
            if (!deps.isEmpty())
                dep.put(key, deps);
            else
                variables.put(key, value);
        }
        while (variables.size() < properties.size()) {
            boolean update = false;
            for (Map.Entry<String, Set<String>> entry : dep.entrySet()) {
                if (variables.containsKey(entry.getKey()))
                    continue;
                if (entry.getValue().stream().allMatch(variables::containsKey)) {
                    String value = (String) properties.get(entry.getKey());
                    for (String it : entry.getValue()) {
                        value = replacePlaceholder(value, it, variables.get(it));
                    }
                    variables.put(entry.getKey(), value);
                    update = true;
                }
            }
            if (!update) {
                log.warn(String.format("Can't resolve all dependency of properties: %s, keep default value...", properties));
                break;
            }
        }
        Properties res = new Properties();
        res.putAll(variables);
        return res;
    }

    public static String getResourcePath(ClassLoader classloader, String name) {
        URL url = classloader.getResource(name);
        if (url == null)
            return "";
        return new File(url.getPath()).getAbsolutePath();
    }

    public static String getResourcePath(String name) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return getResourcePath(classloader, name);
    }

    public static byte[] readAllBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return null;
    }

    public static byte[] readAllBytes(String path) {
        try {
            InputStream stream = getInputStream(path);
            return ByteStreams.toByteArray(stream);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return null;
    }

    public static void writeBytes(String path, byte[] bytes) {
        try {
            OutputStream outputStream = getOutputStream(path);
            outputStream.write(bytes);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
    }

    // todo 去除对StringUtils的依赖
    public static String replaceFileSep(String path, String placeholder) {
        String fileSep = File.separator;
        if (isHdfs(path))
            fileSep = "/";

        if (fileSep.equals("\\"))
            fileSep = "\\\\";
        if (placeholder.equals(fileSep))
            return path;
        String newPath = StringUtil.replaceAll(path, placeholder, fileSep);
        try {
            File f = new File(path);
            f.getCanonicalPath();
        } catch (Exception e) {
            return path;
        }
        return newPath;
    }

    public static String replaceFileSep(String path) {
        return replaceFileSep(path, FILE_SEP);
    }

    public static String replacePlaceholder(String text, String placeholder, String value) {
        String regex = String.format("\\$\\{\\s*%s\\s*}", placeholder);
        return StringUtil.replaceAll(text, regex, value);
    }

    public static boolean isURL(String path) {
        try {
            File f = new File(path);
            f.getCanonicalPath();
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    public static String fileSep(String path) {
        if (isHdfs(path)) {
            return "/";
        }
        return File.separator;
    }

    public static long copy(String srcPath, String dstPath) throws IOException {
        InputStream inputStream = getInputStream(srcPath);
        OutputStream outputStream = getOutputStream(dstPath);
        return copy(inputStream, outputStream);
    }

    public static long copy(InputStream src, File dstFile) throws IOException {
        FileOutputStream dst = new FileOutputStream(dstFile);
        return copy(src, dst);
    }

    public static long copy(InputStream src, OutputStream dst) throws IOException {
        try {
            byte[] buffer = new byte[1 << 20]; // 1MB
            long ret = 0;
            int n = 0;
            while ((n = src.read(buffer)) >= 0) {
                dst.write(buffer, 0, n);
                ret += n;
            }
            return ret;
        } finally {
            dst.close();
            src.close();
        }
    }

    public static long copyDirectory(String srcDir, String dstDir) throws IOException {
        return copyDirectory(srcDir, dstDir, null);
    }

    public static long copyDirectory(String srcDir, String dstDir, Predicate<String> predicate) throws IOException {
        List<String> files = listFile(srcDir, predicate);
        if (!exists(dstDir))
            mkdirs(dstDir);

        long ret = 0;
        int preLen = srcDir.length();
        for (String file : files) {
            if (file.length() > preLen) {
                String relPath = file.substring(preLen);
                String outPath = join(dstDir, relPath);
                ret += copy(file, outPath);
            }
        }

        List<String> dirs = listAllDirectory(srcDir, predicate);
        for (String dir : dirs) {
            if (dir.length() > preLen) {
                String relDir = dir.substring(preLen);
                String outDir = join(dstDir, relDir);
                ret += copyDirectory(dir, outDir, predicate);
            }
        }
        return ret;
    }

    public static File createTemporaryDirectory() {
        return createTemporaryDirectory("tmp");
    }

    public static File createTemporaryDirectory(String filePrefix) {
        File baseDirectory = new File(System.getProperty("java.io.tmpdir"));
        String directoryName = filePrefix + "-" + System.currentTimeMillis() + "-";
        for (int attempt = 0; attempt < 1000; attempt++) {
            File temporaryDirectory = new File(baseDirectory, directoryName + attempt);
            if (temporaryDirectory.mkdir()) {
                return temporaryDirectory;
            }
        }
        throw new IllegalStateException(
                "Could not create a temporary directory (tried to make "
                        + directoryName
                        + "*)");
    }

    public static boolean isHdfs(String path) {
        return path.startsWith(HDFS_PREFIX);
    }

    public static String mayCopyHdfsToLocal(String path) {
        if (isHdfs(path)) {
            try {
                File tempPath = createTemporaryDirectory();
                // Deletions are in the reverse order of requests, so we need to request that the directory be
                // deleted first, so that it is empty when the request is fulfilled.
                tempPath.deleteOnExit();
                String tempDirectory = tempPath.getCanonicalPath();
                String name = baseName(path);
                String dstPath = join(tempDirectory, name);
                if (isDirectory(path)) {
                    copyDirectory(path, dstPath);
                } else {
                    copy(path, dstPath);
                }
                log.info(String.format("Copy from %s to %s", path, dstPath));
                path = dstPath;
            } catch (IOException e) {
                log.error("IOException: " + e.getMessage());
            }
        }
        return path;
    }

    public static String copyToLocal(InputStream stream) {
        try {
            File tempPath = createTemporaryDirectory();
            // Deletions are in the reverse order of requests, so we need to request that the directory be
            // deleted first, so that it is empty when the request is fulfilled.
            tempPath.deleteOnExit();
            String tempDirectory = tempPath.getCanonicalPath();
            String dstPath = join(tempDirectory, "tests");
            copy(stream, new File(dstPath));
            return dstPath;
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return null;
    }

    public static byte[] gzip(byte[] data) {
        byte[] ret = null;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length)) {
            try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
                zipStream.write(data, 0, data.length);
                zipStream.finish();
                zipStream.flush();
                byteStream.flush();
                ret = byteStream.toByteArray();
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
//        System.out.println(String.format("%d\t%d", data.length, ret.length));
        return ret;
    }

    public static List<String> smartReadLines(String path) {
        if (IOUtil.exists(path)) {
            return IOUtil.readLines(path);
        } else {
            return IOUtil.getResource(path);
        }
    }

    public static <T> T smartReadConfig(String path, Class<T> clazz) {
        String config = String.join("\n", smartReadLines(path));
        if (path.endsWith(".json")) {
            return JSON.parseObject(config, clazz);
        } else if (path.endsWith(".yaml")) {
            Yaml yaml = new Yaml(new Constructor(clazz));
            return yaml.load(config);
        } else {
            throw new IllegalArgumentException("only support json and yaml file");
        }
    }
}
