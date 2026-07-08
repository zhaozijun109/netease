package com.netease.easyml.local.mllib.tokenizer;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2020/11/12.
 */
public class JiebaTokenizer implements Tokenizer {
    private JiebaSegmenter.SegMode mode;
    private final JiebaSegmenter segment;

    public JiebaTokenizer(JiebaSegmenter.SegMode mode) {
        this.mode = mode;
        segment = new JiebaSegmenter();
    }

    public JiebaTokenizer() {
        this(JiebaSegmenter.SegMode.SEARCH);
    }

    public JiebaTokenizer setMode(JiebaSegmenter.SegMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public List<Token> tokenize(String text) {
        List<SegToken> terms = segment.process(text, mode);

        List<Token> tokens = new ArrayList<>();
        for (SegToken term : terms) {
            Token token = new Token();
            token.text = term.word;
            token.idx = term.startOffset;
            tokens.add(token);
        }
        return tokens;
    }
}
