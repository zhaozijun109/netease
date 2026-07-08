package com.netease.easyml.local.mllib;

import com.github.jfasttext.JFastText;
import com.google.common.io.Files;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2019/6/4.
 * 若存在linux上中文编码问题，index设为true，转成id
 * 支持读取hdfs路径
 * example格式: __label__[label] token1 token2 ...
 */
public class FastText {
    private static final Logger log = LoggerFactory.getLogger(FastText.class);
    private static final String TOKEN_ID = "token2id.txt";
    private static final String LABEL_ID = "label2id.txt";
    private static final String PAD = "__PAD__";
    private static final String UNK = "__UNK__";
    private static final String LABEL = "__label__";

    public static final String[] DEF_CLS_CMD = new String[]{
            "supervised",
            "-dim", "100",
            "-epoch", "5",
            "-minCount", "1",
            "-wordNgrams", "1",
            "-bucket", "2000000",
            "-minn", "0",
            "-maxn", "0"
    };

    private JFastText jFastText;

    private Map<String, Integer> token2id;
    private Map<String, Integer> label2id;

    private Map<Integer, String> id2label;

    private FastText(JFastText jFastText) {
        this.jFastText = jFastText;
    }

    private FastText(JFastText jFastText, Map<String, Integer> token2id, Map<String, Integer> label2id) {
        this.jFastText = jFastText;
        this.label2id = label2id;
        this.token2id = token2id;

        id2label = new HashMap<>();
        label2id.forEach((k, v) -> id2label.put(v, k));
    }

    public FastText() {
    }

    public static void dumpIds(Map<String, Integer> token2id, String outPath) {
        List<String> outLines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : token2id.entrySet()) {
            outLines.add(entry.getKey() + "\t" + entry.getValue().toString());
        }
        IOUtil.writeLines(outPath, outLines);
    }

    public static Map<String, Integer> loadIds(String path) {
        Map<String, Integer> ids = new HashMap<>();
        for (String line : IOUtil.readLines(path)) {
            String[] split = line.split("\t");
            ids.put(split[0], Integer.parseInt(split[1]));
        }
        return ids;
    }

    private String index(String path) {
        String tmpDir = Files.createTempDir().getAbsolutePath();
        String tmpPath = IOUtil.join(tmpDir, IOUtil.baseName(path));
        label2id = new HashMap<>();
        token2id = new HashMap<>();
        token2id.put(PAD, token2id.size());
        token2id.put(UNK, token2id.size());
        List<String> inLines = IOUtil.readLines(path);
        for (String line : inLines) {
            int i = line.indexOf(" ");
            String label = line.substring(0, i).substring(getLabelPrefix().length());
            if (!label2id.containsKey(label))
                label2id.put(label, label2id.size());
            String text = line.substring(i + 1);
            for (String s : text.split(" ")) {
                if (s.isEmpty())
                    continue;
                if (!token2id.containsKey(s))
                    token2id.put(s, token2id.size());
            }
        }
        List<String> outLines = new ArrayList<>();
        for (String line : inLines) {
            int i = line.indexOf(" ");
            String label = line.substring(0, i).substring(getLabelPrefix().length());
            String text = line.substring(i + 1);
            label = label2id.get(label).toString();
            List<String> pTokens = new ArrayList<>();
            for (String s : text.split(" ")) {
                if (s.isEmpty())
                    continue;
                String id = token2id.get(s).toString();
                pTokens.add(id);
            }
            text = StringUtil.join(pTokens, " ");
            outLines.add(getLabelPrefix() + label + " " + text);
        }
        log.debug("Save tmp file to: " + tmpPath);
        IOUtil.writeLines(tmpPath, outLines);
        return tmpPath;
    }

    public void train(String[] cmd, String input, String output) {
        train(cmd, input, output, false);
    }

    public void train(String[] cmd, String input, String output, boolean index) {
        String[] fullCmd = new String[cmd.length + 4];
        int i = 0;
        for (; i < cmd.length; i++) {
            fullCmd[i] = cmd[i];
        }
        if (index) {
            input = index(input);
        }
        fullCmd[i++] = "-input";
        fullCmd[i++] = input;
        fullCmd[i++] = "-output";
        fullCmd[i] = output;

        JFastText jFastText = new JFastText();
        jFastText.runCmd(fullCmd);

        if (index) {
            String dir = IOUtil.join(IOUtil.parentName(output), "ids");
            IOUtil.mkdirs(dir);
            dumpIds(token2id, IOUtil.join(dir, TOKEN_ID));
            dumpIds(label2id, IOUtil.join(dir, LABEL_ID));

            String tmpDir = IOUtil.parentName(input);
            log.debug("Remove tmp dir: " + tmpDir);
            IOUtil.delete(tmpDir);
        }
        this.jFastText = jFastText;
    }

    private String convert(List<String> tokens) {
        if (token2id != null) {
            return tokens.stream()
                    .map(it -> token2id.getOrDefault(it, 1).toString())
                    .collect(Collectors.joining(" "));
        } else {
            return String.join(" ", tokens);
        }
    }

    public JFastText.ProbLabel predictProba(String text) {
        return predictProba(Arrays.asList(text.split(" ")));
    }

    public List<JFastText.ProbLabel> predictProba(String text, int k) {
        return predictProba(Arrays.asList(text.split(" ")), k);
    }

    public String getLabelPrefix() {
        return jFastText == null ? LABEL : jFastText.getLabelPrefix();
    }

    public JFastText.ProbLabel predictProba(List<String> tokens) {
        String text = convert(tokens);
        JFastText.ProbLabel probLabel = jFastText.predictProba(text);
        probLabel = convertLabel(probLabel);
        return probLabel;
    }

    public List<JFastText.ProbLabel> predictProba(List<String> tokens, int k) {
        String text = convert(tokens);
        List<JFastText.ProbLabel> probLabels = jFastText.predictProba(text, k);
        return probLabels.stream().map(this::convertLabel).collect(Collectors.toList());
    }

    private JFastText.ProbLabel convertLabel(JFastText.ProbLabel probLabel) {
        if (probLabel == null)
            return null;
        String label = probLabel.label.substring(getLabelPrefix().length());
        if (id2label != null) {
            label = id2label.get(Integer.parseInt(label));
        }
        return new JFastText.ProbLabel(probLabel.logProb, label);
    }

    public int getNLabels() {
        return jFastText.getNLabels();
    }

    public static FastText loadModel(String modelFile) {
        return loadModel(modelFile, false);
    }

    public static FastText loadModel(String modelFile, boolean index) {
        String idsDir = null;
        if (index) {
            idsDir = IOUtil.join(IOUtil.parentName(modelFile), "ids");
        }
        return loadModel(modelFile, idsDir);
    }

    public static FastText loadModel(String modelFile, String idsDir) {
        JFastText jFastText = new JFastText();

        modelFile = IOUtil.mayCopyHdfsToLocal(modelFile);
        jFastText.loadModel(modelFile);
        if (idsDir != null) {
            Map<String, Integer> token2id = loadIds(IOUtil.join(idsDir, TOKEN_ID));
            Map<String, Integer> label2id = loadIds(IOUtil.join(idsDir, LABEL_ID));
            return new FastText(jFastText, token2id, label2id);
        } else {
            return new FastText(jFastText);
        }
    }
}
