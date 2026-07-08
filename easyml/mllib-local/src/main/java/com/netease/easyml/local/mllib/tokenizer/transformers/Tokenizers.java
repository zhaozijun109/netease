package com.netease.easyml.local.mllib.tokenizer.transformers;

import com.netease.easyml.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

import static com.netease.easyml.common.util.StringUtil.isWhitespace;

/**
 * Created by linjiuning on 2019/8/22.
 */
public class Tokenizers {
    private static final Logger log = LoggerFactory.getLogger(Tokenizers.class);

    public static List<String> whitespaceTokenize(String text) {
        text = text.trim();
        if (text.isEmpty())
            return Collections.emptyList();
        List<String> tokens = new ArrayList<>();
        char[] chars = text.toCharArray();
        int i = 0;
        for (int j = 0; j < chars.length; j++) {
            char ch = chars[j];
            if (isWhitespace(ch)) {
                if (j > i) {
                    tokens.add(text.substring(i, j));
                }
                i = j + 1;
            }
        }
        if (i < chars.length) {
            tokens.add(text.substring(i, chars.length));
        }
        return tokens;
    }

    public static Map<String, Integer> loadVocab(String vocabPath) {
        log.info("Loading vocabulary from {}.", vocabPath);
        InputStream stream;
        if (IOUtil.exists(vocabPath)) {
            stream = IOUtil.getInputStream(vocabPath);
        } else {
            stream = IOUtil.getResourceAsStream(vocabPath);
        }
        Map<String, Integer> vocab = new HashMap<>();
        for (String line : IOUtil.readLines(stream)) {
            if (line.isEmpty())
                continue;
            vocab.put(line, vocab.size());
        }
        return vocab;
    }
}
