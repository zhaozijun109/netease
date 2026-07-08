package org.apache.flink.connector.file.sink;

import com.netease.yuanqi.common.pojo.config.KerberosConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flink.runtime.fs.hdfs.HadoopFileSystem;
import org.apache.flink.shaded.guava31.com.google.common.io.ByteStreams;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.concurrent.ExecutorThreadFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class SlothHadoopFileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(SlothHadoopFileSystem.class);
    private static final String LOCAL_ENV_SLOTH_FILE_NAME = "sloth_test_file_sink";
    private static final String CLASSPATH_SLOTH_FILE = "/zz_sloth/";
    private static KerberosConfig kerberosConfig;

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(
                    1, new ExecutorThreadFactory("sloth-refresh-kerberos"));

    private static String slothFilePath = null;

    public static org.apache.flink.core.fs.FileSystem getSlothHadoopFileSystem(
            KerberosConfig config) throws IOException {
        kerberosConfig = config;
        org.apache.hadoop.fs.FileSystem hdfs = initFileSystem();
        return new SlothHadoopFileSystemUtil(hdfs);
    }

    private static org.apache.hadoop.fs.FileSystem initFileSystem() throws IOException {
        LOG.info("Trying to get hadoop env, kerberos config = {}", kerberosConfig.toString());
        initSlothWorkDir();
        Configuration conf = new Configuration(false);
        initHadoopConf(conf);
        UserGroupInformation ugi =
                kerberosConfig.getRelatedMammoth() ? initMammutEnv(conf) : initKerberosEnv(conf);

        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        reLoginExpiringKeytabUser(ugi);
                    } catch (Exception e) {
                        LOG.info(
                                "sloth refresh kerberos error: {}",
                                ExceptionUtils.getStackTrace(e));
                    }
                },
                0L,
                10L,
                TimeUnit.MINUTES);

        return ugi.doAs(
                (PrivilegedAction<org.apache.hadoop.fs.FileSystem>)
                        () -> {
                            try {
                                return org.apache.hadoop.fs.FileSystem.get(
                                        new URI("hdfs://hz-cluster10/user/da_lofter"), conf);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    public static void initHadoopConf(Configuration conf) throws IOException {
        Preconditions.checkNotNull(conf);
        if (kerberosConfig.getCoreSitePath().contains(".xml")) {
            conf.addResource(new Path(copyPath(kerberosConfig.getCoreSitePath())));
        } else {
            conf.addResource(
                    new Path(
                            copyPath(
                                    kerberosConfig.getCoreSitePath(),
                                    "sloth-mammut-core-site.xml")));
        }

        if (kerberosConfig.getHdfsSitePath().contains(".xml")) {
            conf.addResource(new Path(copyPath(kerberosConfig.getHdfsSitePath())));
        } else {
            conf.addResource(
                    new Path(
                            copyPath(
                                    kerberosConfig.getHdfsSitePath(),
                                    "sloth-mammut-hdfs-site.xml")));
        }
    }

    public static UserGroupInformation initKerberosEnv(Configuration conf) throws IOException {
        System.setProperty(
                "java.security.krb5.conf",
                kerberosConfig.getKrb5ConfPath().contains(".conf")
                        ? copyPath(kerberosConfig.getKrb5ConfPath())
                        : copyPath(kerberosConfig.getKrb5ConfPath(), "sloth-mammut-krb5.conf"));

        try {
            UserGroupInformation.setConfiguration(conf);
            return UserGroupInformation.loginUserFromKeytabAndReturnUGI(
                    kerberosConfig.getKeyTabLoginUser(),
                    kerberosConfig.getKeyTabPath().contains(".keytab")
                            ? copyPath(kerberosConfig.getKeyTabPath())
                            : copyPath(kerberosConfig.getKeyTabPath(), "sloth-mammut.keytab"));
        } catch (Exception e) {
            LOG.error("Init sloth kerberos env error: {}", ExceptionUtils.getStackTrace(e));
            throw new IOException(ExceptionUtils.getStackTrace(e));
        }
    }

    public static UserGroupInformation initMammutEnv(Configuration conf) throws IOException {
        LOG.info("start to init mammut env");
        String krb5ConfPath = copyPath("/zz_sloth/sloth-mammut-krb5.conf");
        LOG.info("sloth-mammut-krb5.conf  {}", krb5ConfPath);
        System.setProperty("java.security.krb5.conf", krb5ConfPath);

        String loginUser;
        try {
            loginUser =
                    IOUtils.toString(
                            Objects.requireNonNull(
                                    SlothHadoopFileSystem.class.getResourceAsStream(
                                            "/zz_sloth/sloth-mammut-principal")));
            if (loginUser == null) {
                LOG.info("sloth-mammut-principal null");
                throw new IOException("sloth-mammut-principal is null");
            }
        } catch (IOException e) {
            throw new IOException("Failed to read sloth-mammut-principal", e);
        }

        try {
            UserGroupInformation.setConfiguration(conf);
            return UserGroupInformation.loginUserFromKeytabAndReturnUGI(
                    loginUser, copyPath("/zz_sloth/sloth-mammut.keytab"));
        } catch (Exception e) {
            LOG.error("initKerberosEnv error. error: {}", ExceptionUtils.getStackTrace(e));
            throw new IOException(ExceptionUtils.getStackTrace(e));
        }
    }

    private static String copyPath(String source) throws IOException {
        return copyAndGetPath(source, slothFilePath, getPathFileName(source));
    }

    private static String copyPath(String source, String dist) throws IOException {
        return copyAndGetPath(source, slothFilePath, dist);
    }

    private static void initSlothWorkDir() throws IOException {
        String localDirs = System.getenv("LOCAL_DIRS");
        if (localDirs == null) {
            localDirs =
                    System.getProperty("java.io.tmpdir")
                            + File.separator
                            + LOCAL_ENV_SLOTH_FILE_NAME;
            System.out.println("is not cluster env, will init at local path: " + localDirs);
            File file = new File(localDirs);
            if (!file.exists()) {
                file.mkdir();
            }
        }
        if (StringUtils.isBlank(localDirs)) {
            throw new IOException("not find LOCAL_DIRS");
        } else {
            String localDir = localDirs.split(",")[0];
            slothFilePath = localDir + File.separator + "slothFile";
            File file = new File(slothFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            LOG.info("initSlothWorkDir success: {}", slothFilePath);
        }
    }

    private static String getPathFileName(String filePath) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            throw new IOException("not find file path: " + filePath);
        }
        String[] filePaths = filePath.split(Matcher.quoteReplacement(File.separator));
        return filePaths[filePaths.length - 1];
    }

    private static void reLoginExpiringKeytabUser(UserGroupInformation ugi) {
        try {
            if (ugi.isFromKeytab()) {
                ugi.checkTGTAndReloginFromKeytab();
                LOG.info("refresh keytab login success.");
            }
        } catch (IOException e) {
            LOG.info("Error doing re-login using keytab {}", ExceptionUtils.getStackTrace(e));
        }
    }

    private static void copyResourceToPath(String resourceName, String targetPath) {
        try (FileOutputStream outputStream = new FileOutputStream(targetPath)) {
            ByteStreams.copy(
                    Objects.requireNonNull(
                            SlothHadoopFileSystem.class.getResourceAsStream(resourceName)),
                    outputStream);
        } catch (Exception e) {
            throw new RuntimeException(
                    "failed to pull resource ["
                            + resourceName
                            + "] out of class to dest ["
                            + targetPath
                            + "]. ex: "
                            + e.getMessage(),
                    e);
        }
    }

    private static String copyAndGetPath(String source, String distRootPath, String distFileName) {
        File dir = new File(distRootPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String targetPath = distRootPath + "/" + distFileName;
        File file = new File(targetPath);
        if (file.exists()) {
            return targetPath;
        } else {
            copyResourceToPath(source, targetPath);
            return targetPath;
        }
    }

    private static class SlothHadoopFileSystemUtil extends HadoopFileSystem {
        public SlothHadoopFileSystemUtil(org.apache.hadoop.fs.FileSystem hadoopFileSystem) {
            super(hadoopFileSystem);
        }
    }
}
