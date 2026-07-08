package com.netease.easyml.local.mllib.tokenizer;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2020/7/29.
 */
public class HanLPTokenizer implements Tokenizer {
    private Segment segment;

    public HanLPTokenizer(Segment segment) {
        this.segment = segment;
    }

    public HanLPTokenizer() {
    }

    @Override
    public List<Token> tokenize(String text) {
        List<Term> terms;
        if (segment == null) {
            terms = HanLP.segment(text);
        } else {
            terms = segment.seg(text);
        }
        List<Token> tokens = new ArrayList<>();
        for (Term term : terms) {
            Token token = new Token();
            token.text = term.word;
            token.pos_ = term.nature.toString();
            token.idx = term.offset;
            tokens.add(token);
        }
        return tokens;
    }
}
