package com.netease.easyml.launcher.command;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.launcher.Archive;
import com.netease.easyml.launcher.EasyMLContext;
import com.netease.easyml.ml.trainer.Trainer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

import static com.netease.easyml.launcher.Constant.*;

/**
 * Created by linjiuning on 2020/7/6.
 */
public class Predict implements SubCommand {
    private String config;
    private String archiveFile;
    private String test;
    private String result;

    private Params params;

    @Override
    public void addOptions(Options args) {
        args.addOption("a", ARCHIVE_FILE, true,
                "the archived model to make predictions with");

        args.addOption("c", CONFIG, true,
                "path to config file describing the model to be trained");

        args.addOption("t", TEST_PATH, true,
                "path or table of the input");

        args.addOption("r", RESULT_PATH, true,
                "path or table of the prediction result");
    }

    @Override
    public void parse(CommandLine args) {
        if (!(args.hasOption(CONFIG) || args.hasOption(ARCHIVE_FILE))) {
            throw new IllegalArgumentException("Either set config or archive-file.");
        }
        if (args.hasOption(CONFIG)) {
            config = args.getOptionValue(CONFIG).trim();
        }

        if (args.hasOption(ARCHIVE_FILE)) {
            archiveFile = args.getOptionValue(ARCHIVE_FILE).trim();
            if (!args.hasOption(CONFIG)) {
                if (IOUtil.isDirectory(archiveFile)) {
                    config = IOUtil.join(archiveFile, CONFIG_NAME);
                } else {
                    config = archiveFile;
                }
            }
        }

        params = Params.fromFile(config);
        test = args.getOptionValue(TEST_PATH, params.get(TEST_PATH, "")).trim();
        if (test.isEmpty()) {
            throw new IllegalArgumentException("Must specify test path in args or config");
        }

        result = args.getOptionValue(RESULT_PATH, params.get(RESULT_PATH, "")).trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Must specify result path in args or config");
        }
    }

    @Override
    public void run(EasyMLContext context) throws Exception {
        SparkSession spark = context.getSpark();
        Archive archive;
        if (!StringUtil.isEmpty(archiveFile)) {
            archive = Archive.loadArchive(archiveFile, params);
        } else {
            archive = Archive.loadArchiveFromParams(params);
        }

        if (archive.writer == null) {
            throw new IllegalArgumentException("Must specify writer in config");
        }

        Trainer trainer = archive.trainer;
        Dataset<Row> dataset = archive.readTest(spark, test);
        dataset = trainer.transform(dataset);
        if (result != null) {
            archive.save(dataset, result);
        } else {
            archive.save(dataset);
        }
    }

    @Override
    public Map<String, String> env() {
        return env(params);
    }
}
