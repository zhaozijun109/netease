package com.netease.easyml.launcher.command;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.launcher.Archive;
import com.netease.easyml.launcher.EasyMLContext;
import com.netease.easyml.ml.trainer.Trainer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

import static com.netease.easyml.launcher.Constant.*;

/**
 * Created by linjiuning on 2020/7/6.
 */
public class Evaluate implements SubCommand {
    private String archiveFile;
    private String validation;

    private Params params;

    @Override
    public void addOptions(Options args) {
        Option option = new Option("a", ARCHIVE_FILE, true,
                "the archived model to make predictions with");
        option.setRequired(true);
        args.addOption(option);

        args.addOption("v", VALIDATION_PATH, true,
                "path or table of the validation");
    }

    @Override
    public void parse(CommandLine args) {
        archiveFile = args.getOptionValue(ARCHIVE_FILE).trim();

        params = Params.fromFile(IOUtil.join(archiveFile, CONFIG_NAME));

        validation = args.getOptionValue(VALIDATION_PATH, params.get(VALIDATION_PATH, "")).trim();
        if (validation.isEmpty()) {
            throw new IllegalArgumentException("Must specify validation path in args or config");
        }
    }

    @Override
    public void run(EasyMLContext context) throws Exception {
        SparkSession spark = context.getSpark();
        Archive archive = Archive.loadArchive(archiveFile);

        Trainer trainer = archive.trainer;
        Dataset<Row> dataset = archive.readValidation(spark, validation);
        Params params = trainer.evaluate(dataset);
        System.out.println("VALIDATION METRICS: " + params.toJson(true));
    }

    @Override
    public Map<String, String> env() {
        return env(params);
    }
}
