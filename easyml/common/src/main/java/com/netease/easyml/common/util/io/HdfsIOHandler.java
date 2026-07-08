package com.netease.easyml.common.util.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class HdfsIOHandler implements IOHandler {
    private static final Logger logger = LoggerFactory.getLogger(HdfsIOHandler.class);
    public static final HdfsIOHandler handler = new HdfsIOHandler();

    public static final char separatorChar = '/';
    public static final int prefixLength = HDFS_PREFIX.length();

    private static Configuration configuration = new Configuration();

    public static void setConfiguration(String confPath) {
        configuration.addResource(new Path(confPath));
    }

    public static void setConfiguration(Configuration conf) {
        configuration = conf;
    }

    public static String getHdfsClusterName(String filePath) {
        if (filePath.startsWith(HDFS_PREFIX)) {
            int pos = filePath.indexOf(separatorChar, prefixLength);
            if (pos > 0)
                return filePath.substring(0, pos);
        }
        return null;
    }

    private static FileSystem getHdfsFileSystem(String filePath) {
        return getHdfsFileSystem(filePath, configuration);
    }

    private static FileSystem getHdfsFileSystem(String filePath, Configuration configuration) {
        String clusterName = getHdfsClusterName(filePath);
        if (clusterName == null) {
            logger.error("hdfs cluster name parse error...");
            return null;
        }
        try {
            return FileSystem.get(new URI(clusterName), configuration);
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException: " + e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return null;
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException: " + e.getMessage());
            return null;
        }

    }

    @Override
    public boolean exists(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return false;

        try {
            Path term = new Path(path);
            return fs.exists(term);
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isFile(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return false;

        try {
            Path term = new Path(path);
            return fs.isFile(term);
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isDirectory(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return false;

        try {
            Path term = new Path(path);
            return fs.isDirectory(term);
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public long lastModified(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return 0;

        try {
            Path term = new Path(path);
            return fs.getFileStatus(term).getModificationTime();
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean mkdirs(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return false;

        try {
            Path term = new Path(path);
            return fs.mkdirs(term);
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return false;

        try {
            Path term = new Path(path);
            return fs.delete(term, true);
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rename(String path, String path1) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return false;

        try {
            Path term = new Path(path);
            Path term1 = new Path(path1);
            return fs.rename(term, term1);
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String baseName(String path) {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) return path.substring(prefixLength);
        return path.substring(index + 1);
    }

    @Override
    public String parentName(String path) {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) {
            if ((prefixLength > 0) && (path.length() > prefixLength))
                return path.substring(0, prefixLength);
            return null;
        }
        return path.substring(0, index);
    }

    @Override
    public String join(String first, String... more) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        for (String m : more)
            sb.append("/").append(m);
        return sb.toString().replaceAll("\\\\", "/").replaceAll("(?<!:)/+", "/");
    }

    @Override
    public List<String> listAllFile(String path, Predicate<String> predicate) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return Collections.emptyList();

        try {
            Path term = new Path(path);
            List<String> filePaths = new ArrayList<>();
            RemoteIterator<LocatedFileStatus> iter = fs.listFiles(term, true);
            while (iter.hasNext()) {
                Path path_ = iter.next().getPath();
                if (fs.isFile(path_))
                    filePaths.add(join(getHdfsClusterName(path), path_.toUri().getRawPath()));
            }
            if (predicate != null)
                filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
            return filePaths;
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> listFile(String path, Predicate<String> predicate) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return Collections.emptyList();

        try {
            Path term = new Path(path);
            List<String> filePaths = new ArrayList<>();
            RemoteIterator<LocatedFileStatus> iter = fs.listFiles(term, false);
            while (iter.hasNext()) {
                Path path_ = iter.next().getPath();
                if (fs.isFile(path_))
                    filePaths.add(join(getHdfsClusterName(path), path_.toUri().getRawPath()));
            }
            if (predicate != null)
                filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
            return filePaths;
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> listAllDirectory(String path, Predicate<String> predicate) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return Collections.emptyList();

        try {
            Path term = new Path(path);
            List<String> filePaths = new ArrayList<>();
            RemoteIterator<LocatedFileStatus> iter = fs.listLocatedStatus(term);
            while (iter.hasNext()) {
                Path path_ = iter.next().getPath();
                if (fs.isDirectory(path_)) {
                    String curPath = join(getHdfsClusterName(path), path_.toUri().getRawPath());
                    filePaths.add(curPath);
                    filePaths.addAll(listAllDirectory(curPath, predicate));
                }
            }
            if (predicate != null)
                filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
            return filePaths;
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> listDirectory(String path, Predicate<String> predicate) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return Collections.emptyList();

        try {
            Path term = new Path(path);
            List<String> filePaths = new ArrayList<>();
            RemoteIterator<LocatedFileStatus> iter = fs.listLocatedStatus(term);
            while (iter.hasNext()) {
                Path path_ = iter.next().getPath();
                if (fs.isDirectory(path_))
                    filePaths.add(join(getHdfsClusterName(path), path_.toUri().getRawPath()));
            }
            if (predicate != null)
                filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
            return filePaths;
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public InputStream getInputStream(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return null;
        try {
            Path term = new Path(path);
            if (!fs.exists(term)) {
                logger.error(path + " is not exists...");
                return null;
            }
            return fs.open(term);
        } catch (IOException e) {
            logger.warn("IOException: " + e.getMessage());
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(String path) {
        FileSystem fs = getHdfsFileSystem(path);
        if (fs == null)
            return null;

        try {
            Path term = new Path(path);
            return fs.create(term, true);
        } catch (IOException e) {
            logger.warn("IOException: " + e.getMessage());
            return null;
        }
    }
}
