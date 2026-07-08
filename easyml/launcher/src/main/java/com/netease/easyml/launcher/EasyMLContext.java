package com.netease.easyml.launcher;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.JavaUtil;
import com.netease.easyml.common.util.SparkUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.InputStream;
import java.util.*;

import static com.netease.easyml.launcher.Constant.ENV;

/**
 * Created by linjiuning on 2020/7/9.
 */
public class EasyMLContext {
    private static final Logger log = LoggerFactory.getLogger(EasyMLContext.class);
    private static EasyMLContext INSTANCE = null;

    private SparkConf conf;
    private SparkSession spark;

    public SparkConf getConf() {
        return spark == null ? conf : spark.sparkContext().conf();
    }

    public EasyMLContext setConf(SparkConf conf) {
        this.conf = conf;
        return this;
    }

    public synchronized SparkSession getSpark() {
        if (spark == null && conf != null) {
            spark = SparkSession.builder().config(conf).getOrCreate();
        }
        return spark;
    }

    public EasyMLContext setSpark(SparkSession spark) {
        this.spark = spark;
        return this;
    }

    public void stop() {
        if (spark != null) {
            spark.stop();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean enableHive;
        private SparkConf conf = new SparkConf();

        public Builder set(String key, String value) {
            conf.set(key, value);
            return this;
        }

        public Builder set(String config) {
            Params params = Params.fromFile(config);
            Map<String, Object> env = params.get(ENV, new HashMap<>());
            for (Map.Entry<String, Object> entry : env.entrySet()) {
                set(entry.getKey(), entry.getValue().toString());
            }
            return this;
        }

        public Builder appName(String name) {
            set("spark.app.name", name);
            return this;
        }

        public Builder master(String master) {
            set("spark.master", master);
            return this;
        }

        public Builder enableHiveSupport() {
            this.enableHive = true;
            return this;
        }

        private void environment() {
            InputStream resource = null;
            try {
                resource = IOUtil.getResourceAsStream(Constant.EASYML_PROPERTIES);
                if (resource == null) {
                    resource = IOUtil.getResourceAsStream(Constant.EASYML_DEFAULT_PROPERTIES);
                }
            } catch (Exception ignored) {
            }
            if (resource == null) {
                log.warn("Can't read easyml properties from classpath.");
                return;
            }
            Properties properties = IOUtil.readProperties(resource);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();

                if (conf.contains(key) || key.equals("fs.defaultFS")) {
                    if (!key.equals("fs.defaultFS") || !IOUtil.exists(IOUtil.join(value, "/"))) {
                        log.info(String.format("Skip env: %s = %s", key, value));
                        continue;
                    }
                }
                log.info(String.format("READ env: %s = %s", key, value));
                set(key, value);
            }
        }

        public EasyMLContext getOrCreate() {
            synchronized (EasyMLContext.class) {
                if (INSTANCE == null) {
                    environment();

                    for (Tuple2<String, String> tuple : conf.getAllWithPrefix(Constant.EASYML_PREFIX)) {
                        String key = Constant.EASYML_PREFIX + tuple._1();
                        log.info(String.format("SET: %s = %s", key, tuple._2()));
                        set(key, tuple._2());
                    }

                    String jarDir = conf.get(Constant.EASYML_JARS_DIR);
                    String excludeJar = conf.get(Constant.EASYML_JARS_SYS_EXCLUDE, "");
                    excludeJar += ";" + conf.get(Constant.EASYML_JARS_EXCLUDE, "");
                    String xgbVersion = conf.get(Constant.EASYML_XGB_VERSION, Constant.EASYML_XGB_DEFAULT_VERSION);
                    String distJars = conf.get(Constant.EASYML_DIST_JARS, "");
                    boolean hanlpPortable = StringUtil.parseBoolean(conf.get(Constant.EASYML_HANLP_PORTABLE, "true"), true);
                    boolean bigdlEnable = StringUtil.parseBoolean(conf.get(Constant.EASYML_BIGDL_ENABLE, "false"), false);
                    boolean angelEnable = StringUtil.parseBoolean(conf.get(Constant.EASYML_ANGEL_ENABLE, "false"), false);
                    List<String> excludeJars = StringUtil.splitTrimNoEmpty(excludeJar, ";");
                    List<String> jars = new ArrayList<>();
                    if (jarDir != null && IOUtil.exists(jarDir)) {
                        jarDir = IOUtil.mayCopyHdfsToLocal(jarDir);
                        for (String path : IOUtil.listAllFile(jarDir, (path) -> path.endsWith(".jar"))) {
                            if (excludeJars.stream().anyMatch(path::contains)) {
                                continue;
                            }
                            String jarName = IOUtil.baseName(path);
                            if (jarName.startsWith("hanlp")) {
                                if (hanlpPortable != jarName.contains("portable")) {
                                    continue;
                                }
                            }
                            if (jarName.startsWith("xgboost4j") && !jarName.contains(xgbVersion)) {
                                continue;
                            }
                            jars.add(path);
                        }
                    }
                    for (String jar : distJars.split(";")) {
                        jar = IOUtil.mayCopyHdfsToLocal(jar);
                        jars.add(jar);
                    }

                    String sparkJars = String.join(",", jars);
                    conf.set("spark.jars", sparkJars);
                    JavaUtil.addJarsToClassPath(sparkJars);

                    List<String> files = new ArrayList<>();
                    if (!hanlpPortable) {
                        String outPath = initHanLP(jarDir);
                        if (!outPath.isEmpty()) {
                            files.add(outPath);
                        }
                    }
                    String sparkFiles = String.join(",", files);
                    conf.set("spark.files", sparkFiles);
                    JavaUtil.addJarsToClassPath(sparkFiles);

                    if (bigdlEnable) {
                        initBigDL(conf);
                    }

                    if (angelEnable) {
                        initAngel(conf);
                    }

                    SparkSession.Builder builder = SparkSession.builder();
                    builder.config(conf);

                    if (enableHive) {
                        builder = builder.enableHiveSupport();
                    }

                    conf = SparkUtil.getSparkConf(builder);

                    INSTANCE = new EasyMLContext();
                    INSTANCE.setConf(conf);

                    List<String> packages = StringUtil.splitTrimNoEmpty(conf.get(Constant.EASYML_PKG_PREFIX, ""), ",");
                    for (String pkg : packages) {
                        RegisterManager.addPackagePrefix(pkg);
                    }
                    RegisterManager.register();
                }
                return INSTANCE;
            }
        }
    }

    private static String initHanLP(String tmpClassPath) {
        String outPath = "";
        InputStream stream = IOUtil.getResourceAsStream("hanlp.properties");
        log.info("Can't read hanlp properties from classpath, use default properties instead.");
        if (stream == null) {
            stream = IOUtil.getResourceAsStream("hanlp-default.properties");
            try {
                outPath = IOUtil.join(tmpClassPath, "hanlp.properties");
                IOUtil.writeLines(outPath, IOUtil.readLines(stream));
            } catch (Exception ignored) {
                log.warn("Can't read default hanlp properties from classpath.");
                return "";
            }
        }
        return outPath;
    }

    public static void initBigDL(SparkConf conf) {
        EasyMLUtils.initBigDL(conf);
    }

    public static void initAngel(SparkConf conf) {
        EasyMLUtils.initAngel(conf);
    }
}
