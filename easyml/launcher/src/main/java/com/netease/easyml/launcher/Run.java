package com.netease.easyml.launcher;

import com.netease.easyml.launcher.command.*;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linjiuning on 2020/7/6.
 */
public class Run {
    private static final Logger log = LoggerFactory.getLogger(Run.class);
    private static Map<String, SubCommand> SUB_COMMANDS = new HashMap<>();

    static {
        SUB_COMMANDS.put("fit", new Train());
        SUB_COMMANDS.put("train", new Train());
        SUB_COMMANDS.put("evaluate", new Evaluate());
        SUB_COMMANDS.put("transform", new Predict());
        SUB_COMMANDS.put("predict", new Predict());
        SUB_COMMANDS.put("metric", new Metric());
        SUB_COMMANDS.put("script", new Script());
    }

    public static void run(String cmd, String[] args) throws Exception {
        SubCommand subcommand = SUB_COMMANDS.get(cmd);
        Options options = new Options();

        subcommand.addOptions(options);

        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(90);
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine parse = parser.parse(options, args);
            EasyMLContext.Builder builder = EasyMLContext.builder()
                    .appName(Run.class.getSimpleName());
            builder.enableHiveSupport(); // TODO: based on config

            subcommand.parse(parse);

            Map<String, String> env = subcommand.env();
            for (Map.Entry<String, String> entry : env.entrySet()) {
                builder.set(entry.getKey(), entry.getValue());
            }

            EasyMLContext context = builder.getOrCreate();

            subcommand.run(context);
            context.stop();
        } catch (ParseException e) {
            hf.printHelp("easyml", options, true);
        }
    }

    public static void main(String[] args) throws Exception {
        String cmd = args[0];
        String[] nArgs = new String[args.length - 1];
        System.arraycopy(args, 1, nArgs, 0, args.length - 1);

        run(cmd, nArgs);
    }
}
