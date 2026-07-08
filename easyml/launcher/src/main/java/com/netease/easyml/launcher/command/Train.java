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

import static com.netease.easyml.launcher.Constant.CONFIG;

/**
 * Created by linjiuning on 2020/7/6.
 */
public class Train implements SubCommand {
    private String config;
    private String outputDir;
    private boolean force;

    private Params params;

    @Override
    public void addOptions(Options args) {
        Option option = new Option("c", CONFIG, true,
                "path to config file describing the model to be trained");
        option.setRequired(true);
        args.addOption(option);

        option = new Option("o", "output-dir", true,
                "directory in which to save the model");
        option.setRequired(true);
        args.addOption(option);

        args.addOption("f", "force", false,
                "overwrite the output directory if it exists");
    }

    @Override
    public void parse(CommandLine args) {
        config = args.getOptionValue(CONFIG).trim();
        outputDir = args.getOptionValue("output-dir").trim();
        force = args.hasOption("force");

        if (IOUtil.exists(outputDir) && !force) {
            throw new IllegalArgumentException(String.format("output dir %s already exist.", outputDir));
        }
        params = Params.fromFile(config);
    }

    @Override
    public void run(EasyMLContext context) throws Exception {
        SparkSession spark = context.getSpark();
        Archive archive = Archive.loadArchiveFromParams(params);
        Trainer trainer = archive.trainer;

        Dataset<Row> dataset = archive.readTrain(spark);
        Dataset<Row> validDataset = archive.readValidation(spark);

        trainer.fit(dataset);
        if (validDataset != null) {
            Params params = trainer.evaluate(validDataset);
            System.out.println("VALIDATION METRICS: " + params.toJson(true));
        }

        if (IOUtil.exists(outputDir)) {
            IOUtil.delete(outputDir);
        }

        archive.save(outputDir);
    }

    @Override
    public Map<String, String> env() {
        return env(params);
    }
}
