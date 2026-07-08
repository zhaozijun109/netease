package com.netease.easyml.launcher;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.ml.dataset.DatasetReader;
import com.netease.easyml.ml.dataset.DatasetWriter;
import com.netease.easyml.ml.metric.Metric;
import com.netease.easyml.ml.trainer.BasicTrainer;
import com.netease.easyml.ml.trainer.Trainer;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.Evaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.launcher.Constant.*;

/**
 * Created by linjiuning on 2020/7/7.
 */
public class Archive {
    private static final Logger log = LoggerFactory.getLogger(Archive.class);

    public DatasetReader trainReader;
    public DatasetReader validReader;
    public DatasetReader testReader;
    public DatasetReader resultReader;

    public DatasetWriter writer;

    public Trainer trainer;
    public Params config;

    public Archive(DatasetReader trainReader, DatasetReader validReader, DatasetReader testReader, DatasetReader resultReader,
                   DatasetWriter writer, Trainer trainer, Params config) {
        this.trainReader = trainReader;
        this.validReader = validReader;
        this.testReader = testReader;
        this.resultReader = resultReader;
        this.writer = writer;
        this.trainer = trainer;
        this.config = config;
    }

    public Params dataParams(String key) {
        if (!config.containsKey(key)) {
            return null;
        }
        String o = config.get(key, String.class);

        Params p = new Params();
        p.put(PATH, o);
        return p;
    }

    public Dataset<Row> readTrain(SparkSession spark) {
        Params params = dataParams(TRAIN_PATH);
        return params == null ? null : trainReader.read(spark, params);
    }

    public Dataset<Row> readValidation(SparkSession spark) {
        Params params = dataParams(VALIDATION_PATH);
        return params == null ? null : validReader.read(spark, params);
    }

    public Dataset<Row> readValidation(SparkSession spark, String path) {
        Params params = dataParams(VALIDATION_PATH);
        if (params == null) {
            params = new Params();
        }
        params.put(PATH, path);
        return validReader.read(spark, params);
    }

    public Dataset<Row> readTest(SparkSession spark) {
        Params params = dataParams(TEST_PATH);
        return params == null ? null : testReader.read(spark, params);
    }

    public Dataset<Row> readTest(SparkSession spark, String path) {
        Params params = dataParams(TEST_PATH);
        if (params == null) {
            params = new Params();
        }
        params.put(PATH, path);
        return testReader.read(spark, params);
    }

    public Dataset<Row> readResult(SparkSession spark, String path) {
        Params params = new Params();
        params.put(PATH, path);
        return resultReader.read(spark, params);
    }

    public void save(Dataset<Row> dataset, String path) {
        Params p = new Params();
        p.put(PATH, path);
        writer.write(dataset, p);
    }

    public void save(Dataset<Row> dataset) {
        Params p = dataParams(RESULT_PATH);
        writer.write(dataset, p);
    }

    public void save(String path) {
        if (!IOUtil.exists(path)) {
            IOUtil.mkdirs(path);
        }
        String config = IOUtil.join(path, CONFIG_NAME);
        IOUtil.writeLines(config, Collections.singletonList(this.config.toJson(true)));

        String data = IOUtil.join(path, DATA);
        trainer.save(data);
    }

    public static boolean isReadable(Class<?> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("load", String.class);
            return clazz.isAssignableFrom(method.getReturnType());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static <T> Class<?> getModelClass(Class<T> clazz) {
        Method method;
        try {
            method = clazz.getDeclaredMethod("fit", Dataset.class);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getDeclaredMethod("train", Dataset.class);
            } catch (NoSuchMethodException noSuchMethodException) {
                return null;
            }
        }
        return method.getReturnType();
    }

    public static Archive loadArchive(String serializationDir) throws Exception {
        Params config = Params.fromFile(IOUtil.join(serializationDir, CONFIG_NAME));
        return loadArchive(serializationDir, config);
    }

    public static Archive loadArchive(String serializationDir, Params config) throws Exception {
        Archive archive = loadArchiveFromParams(config);
        String dataPath = IOUtil.join(serializationDir, DATA);
        if (IOUtil.exists(dataPath)) {
            archive.trainer.load(dataPath);
        }
        return archive;
    }

    public static Archive loadArchiveFromParams(Params params) throws IllegalAccessException {
        Params duplicate = params.duplicate();
        Params reader = params.get(READER, Params.class);
        DatasetReader trainReader = FromParams.fromParams(DatasetReader.class, reader);
        DatasetReader validReader = trainReader;
        DatasetReader testReader = trainReader;
        DatasetReader resultReader = trainReader;

        if (params.containsKey(TRAIN_READER)) {
            reader = params.get(TRAIN_READER, Params.class);
            trainReader = FromParams.fromParams(DatasetReader.class, reader);
        }

        if (params.containsKey(VALIDATION_READER)) {
            reader = params.get(VALIDATION_READER, Params.class);
            validReader = FromParams.fromParams(DatasetReader.class, reader);
        }

        if (params.containsKey(TEST_READER)) {
            reader = params.get(TEST_READER, Params.class);
            testReader = FromParams.fromParams(DatasetReader.class, reader);
        }

        if (params.containsKey(RESULT_READER)) {
            reader = params.get(RESULT_READER, Params.class);
            resultReader = FromParams.fromParams(DatasetReader.class, reader);
        }

        DatasetWriter datasetWriter = null;
        if (params.containsKey(WRITER)) {
            Params writer = params.get(WRITER, Params.class);
            datasetWriter = FromParams.fromParams(DatasetWriter.class, writer);
        }

        Params components = params.get(COMPONENT, Params.class);

        PipelineStage pipelineStage = FromParams.fromParams(PipelineStage.class, components);

        List<Metric> metrics = new ArrayList<>();
        if (params.containsKey(METRIC)) {
            Object[] array = params.get(METRIC, Object[].class);
            for (Object o : array) {
                Metric metric = (Metric) FromParams.fromParams(Evaluator.class, new Params((Map) o));
                metrics.add(metric);
            }
        }

        Trainer trainer;
        if (params.containsKey(TRAINER)) {
            Params trainerParams = params.get(TRAINER, Params.class);
            trainer = FromParams.fromParams(Trainer.class, trainerParams);
        } else {
            trainer = new BasicTrainer();
        }
        if (trainer != null) {
            trainer.setComponent(pipelineStage);
            trainer.setMetrics(metrics.toArray(new Metric[0]));
        }

        return new Archive(trainReader, validReader, testReader, resultReader,
                datasetWriter, trainer, duplicate);
    }
}
