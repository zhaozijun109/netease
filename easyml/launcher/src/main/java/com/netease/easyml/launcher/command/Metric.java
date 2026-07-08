package com.netease.easyml.launcher.command;

import com.netease.easyml.common.collection.Params;
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
import static com.netease.easyml.launcher.Constant.RESULT_PATH;

/**
 * Created by linjiuning on 2020/7/6.
 */
public class Metric implements SubCommand {
    private String config;
    private String result;

    private Params params;

    @Override
    public void addOptions(Options args) {
        Option option = new Option("c", CONFIG, true,
                "path to config file describing the model to be trained");
        option.setRequired(true);
        args.addOption(option);

        option = new Option("r", RESULT_PATH, true,
                "path or table of the prediction result");
        option.setRequired(true);
        args.addOption(option);
    }

    @Override
    public void parse(CommandLine args) {
        config = args.getOptionValue(CONFIG).trim();

        params = Params.fromFile(config);

        result = args.getOptionValue(RESULT_PATH, "").trim();
    }

    @Override
    public void run(EasyMLContext context) throws Exception {
        SparkSession spark = context.getSpark();
        Archive archive = Archive.loadArchiveFromParams(params);

        Trainer trainer = archive.trainer;
        Dataset<Row> dataset = archive.readResult(spark, result);
        Params params = trainer.metric(dataset);
        System.out.println("METRICS: " + params.toJson(true));
    }

    @Override
    public Map<String, String> env() {
        return env(params);
    }
}
