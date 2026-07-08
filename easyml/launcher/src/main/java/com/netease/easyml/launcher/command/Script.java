package com.netease.easyml.launcher.command;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.common.collection.Tuple;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.launcher.Constant;
import com.netease.easyml.launcher.EasyMLContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.spark.SparkConf;
import org.apache.spark.repl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.netease.easyml.launcher.Constant.*;

/**
 * Created by linjiuning on 2020/9/4.
 */
public class Script implements SubCommand {
    private static final Logger log = LoggerFactory.getLogger(Script.class);
    private static final String SEP = ",";
    private static final String STATUS = "__status__";

    private static final Pattern REMOVE_PT = Pattern.compile("^\\s*package\\s");
    private static final Pattern CLASS_NAME_PT = Pattern.compile("(?<![^\\s])(?<type>object)\\s+(?<name>[a-zA-Z_][a-zA-Z_0-9]*(UDScript|UDS))(?![a-zA-Z_0-9])");
    private Params params;
    private String config;
    private String script;
    private String args;

    public Script setScript(String script) {
        this.script = script;
        return this;
    }

    public Script setArgs(String args) {
        this.args = args;
        return this;
    }

    @Override
    public void addOptions(Options args) {
        Option option = new Option("s", SCRIPT, true,
                "scripts to run, separated by ','");
        option.setRequired(true);
        args.addOption(option);

        args.addOption("a", ARGS, true,
                "arguments of script, separated by ','");

        args.addOption("c", CONFIG, true,
                "path to config file describing the easyml env");

        args.addOption("e", ENV, true,
                "easyml env, format key=val, each pair is separated by ','");
    }

    @Override
    public void parse(CommandLine args) {
        if (args.hasOption(CONFIG)) {
            config = args.getOptionValue(CONFIG).trim();
            params = Params.fromFile(config);
        } else {
            params = new Params();
        }
        if (args.hasOption(ENV)) {
            Map<String, String> env = params.get(ENV, new HashMap<>());
            for (String pair : StringUtil.splitTrimNoEmpty(args.getOptionValue(ENV), SEP)) {
                String[] split = pair.split("=", 2);
                if (split.length != 2) {
                    log.warn("Skip env: " + pair);
                    continue;
                }
                log.info("Set env: " + pair);
                env.put(split[0], split[1]);
            }
            params.put(ENV, env);
        }
        script = args.getOptionValue(SCRIPT).trim();
        this.args = args.hasOption(ARGS) ? args.getOptionValue(ARGS) : "";
    }

    private String initCode(SparkConf conf) {
        List<String> code = new ArrayList<>();
        boolean bigdlEnable = StringUtil.parseBoolean(conf.get(Constant.EASYML_BIGDL_ENABLE, "true"), false);
        if (bigdlEnable) {
            code.add("com.netease.easyml.launcher.EasyMLContext.initBigDL(sc.getConf)");
        }
        if (!StringUtil.isEmpty(args)) {
            code.add("val args = \"" + args + "\".split(\"" + SEP + "\")");
        } else {
            code.add("val args = Array.empty[String]");
        }
        code.add("$intp.isettings.maxPrintString = 0");
        return StringUtil.join(code, "\n");
    }

    public static String getStackTrace(Throwable throwable) {
        if (throwable.getCause() != null) {
            return getStackTrace(throwable.getCause());
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    private String statusTemplate(String code) {
        return String.format("val %s = try {\n", STATUS) +
                String.format("      %s\n", code) +
                "      \"\"\n" +
                "    } catch {\n" +
                "      case ex: Throwable => com.netease.easyml.launcher.command.Script.getStackTrace(ex)\n" +
                "    }\n";
    }

    public static String getStatus(String output) {
        Pattern pt = Pattern.compile(STATUS + ": String =[ \\n](?<status>(.*?))(?=scala>)", Pattern.DOTALL);
        Matcher m = pt.matcher(output);
        String status = "";
        if (m.find()) {
            status = m.group("status").trim();
            if (status.replaceAll("[\n\\s\"]", "").isEmpty()) {
                status = "";
            }
        }
        return status;
    }

    public static String getConsoleError(String output) {
        Pattern pt = Pattern.compile("<console>:\\d+: error:[ \\n](?<error>(.*?))(?=scala>)", Pattern.DOTALL);
        Matcher m = pt.matcher(output);
        String error = "";
        if (m.find()) {
            error = m.group("error").trim();
            if (error.replaceAll("[\n\\s\"]", "").isEmpty()) {
                error = "";
            }
        }
        return error;
    }

    private String scriptToCode(String script) {
        InputStream stream = IOUtil.getResourceAsStream(script);
        if (stream == null) {
            stream = IOUtil.getInputStream(script);
        }
        List<String> lines = IOUtil.readLines(stream);
        List<String> fLines = new ArrayList<>();
        for (String line : lines) {
            Matcher m = REMOVE_PT.matcher(line);
            if (m.find()) {
                continue;
            }
            fLines.add(line);
        }
        lines = fLines;
        List<Tuple<String, String>> clazzes = new ArrayList<>();
        for (String line : lines) {
            Matcher m = CLASS_NAME_PT.matcher(line);
            if (m.find()) {
                clazzes.add(Tuple.tuple(m.group("type"), m.group("name")));
            }
        }
        if (clazzes.isEmpty()) {
            String main = StringUtil.join(lines, "\n");
            return statusTemplate(main);
        } else {
            for (Tuple<String, String> clazz : clazzes) {
                String code;
                String type = clazz.v1();
                String name = clazz.v2();
                if (type.equals("class")) { // only support object type for now
                    code = statusTemplate(String.format("new %s().run(spark, args)", name));
                } else {
                    code = statusTemplate(name + ".run(spark, args)");
                }
                lines.add(code);
            }
            return StringUtil.join(lines, "\n");
        }
    }

    @Override
    public void run(EasyMLContext context) throws Exception {
        List<String> codes = new ArrayList<>();
        SparkConf conf = context.getConf();

        codes.add(initCode(conf));
        for (String file : StringUtil.splitTrimNoEmpty(script, SEP)) {
            String code = scriptToCode(file);
            codes.add(code);
        }
        String mergeCodes = StringUtil.join(codes, "\n");
        String output = Utils.runInterpreter(conf, mergeCodes);
        System.out.println("Script output is: \n" + output);
        String status = getStatus(output);
        if (status.isEmpty()) {
            status = getConsoleError(output);
        }
        if (!status.isEmpty()) {
            throw new RuntimeException(status + "\n" + "Checkout driver's stdout logs for more information.");
        }
    }

    @Override
    public Map<String, String> env() {
        if (params != null) {
            return env(params);
        } else {
            return Collections.emptyMap();
        }
    }
}
