package com.netease.easyml.common.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class LocalIOHandler implements IOHandler {
    private static final Logger logger = LoggerFactory.getLogger(LocalIOHandler.class);
    public static final LocalIOHandler handler = new LocalIOHandler();

    @Override
    public boolean exists(String path) {
        try {
            return Paths.get(path).toFile().exists();
        } catch (InvalidPathException ex) {
            return false;
        }
    }

    @Override
    public boolean isFile(String path) {
        try {
            File file = Paths.get(path).toFile();
            return file.isFile();
        } catch (InvalidPathException ex) {
            return false;
        }
    }

    @Override
    public boolean isDirectory(String path) {
        try {
            File file = Paths.get(path).toFile();
            return file.isDirectory();
        } catch (InvalidPathException ex) {
            return false;
        }
    }

    @Override
    public long lastModified(String path) {
        try {
            File file = Paths.get(path).toFile();
            return file.lastModified();
        } catch (InvalidPathException ex) {
            return 0;
        }
    }

    @Override
    public boolean mkdirs(String path) {
        File file = Paths.get(path).toFile();
        return file.mkdirs();
    }

    @Override
    public boolean delete(String path) {
        boolean flag = true;
        try {
            List<File> files = Files.walk(Paths.get(path))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile).collect(Collectors.toList());
            for (File file : files)
                flag &= file.delete();
        } catch (IOException ex) {
            return false;
        }
        return flag;
    }

    @Override
    public boolean rename(String path, String path1) {
        File file = Paths.get(path).toFile();
        File dest = Paths.get(path1).toFile();
        return file.renameTo(dest);
    }

    @Override
    public String baseName(String path) {
        File file = Paths.get(path).toFile();
        return file.getName();
    }

    @Override
    public String parentName(String path) {
        File file = Paths.get(path).toFile();
        return file.getParent();
    }

    @Override
    public String join(String first, String... more) {
        return Paths.get(first, more).toFile().getAbsolutePath();
    }

    @Override
    public List<String> listAllFile(String path, Predicate<String> predicate) {
        List<String> filePaths = new ArrayList<>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return filePaths;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                filePaths.add(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                filePaths.addAll(listAllFile(file.getAbsolutePath(), predicate));
            }
        }
        if (predicate != null)
            filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
        return filePaths;
    }

    @Override
    public List<String> listFile(String path, Predicate<String> predicate) {
        List<String> filePaths = new ArrayList<>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return filePaths;
        for (File file : listOfFiles) {
            if (file.isFile())
                filePaths.add(file.getAbsolutePath());
        }

        if (predicate != null)
            filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
        return filePaths;
    }

    @Override
    public List<String> listAllDirectory(String path, Predicate<String> predicate) {
        List<String> filePaths = new ArrayList<>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return filePaths;
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                filePaths.add(file.getAbsolutePath());
                filePaths.addAll(listAllDirectory(file.getAbsolutePath(), predicate));
            }
        }

        if (predicate != null)
            filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
        return filePaths;
    }

    @Override
    public List<String> listDirectory(String path, Predicate<String> predicate) {
        List<String> filePaths = new ArrayList<>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return filePaths;
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                filePaths.add(file.getAbsolutePath());
            }
        }
        if (predicate != null)
            filePaths = filePaths.stream().filter(predicate::test).collect(Collectors.toList());
        return filePaths;
    }

    @Override
    public InputStream getInputStream(String path) {
        try {
            return new FileInputStream(new File(path));
        } catch (FileNotFoundException ex) {
            logger.error("FileNotFoundException: " + ex.getMessage());
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(String path) {
        try {
            return new FileOutputStream(new File(path));
        } catch (FileNotFoundException ex) {
            logger.error("FileNotFoundException: " + ex.getMessage());
            return null;
        }
    }
}
