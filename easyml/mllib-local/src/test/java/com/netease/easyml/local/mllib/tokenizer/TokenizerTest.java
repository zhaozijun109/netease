package com.netease.easyml.local.mllib.tokenizer;

import com.netease.easyml.local.mllib.tokenizer.transformers.BasicTokenizer;
import org.junit.Test;

import java.util.List;

/**
 * Created by linjiuning on 2020/7/29.
 */
public class TokenizerTest {

    @Test
    public void hanlp() {
        String text = "这是hanlp分词器";
        List<Token> tokens = new HanLPTokenizer()
                .tokenize(text);
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    @Test
    public void bert() {
        TransformersTokenizer tokenizer = new TransformersTokenizer();
        String text = "我爱中国, i like playing basketball.";
        List<Token> tokens = tokenizer.tokenize(text);
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    @Test
    public void basicTokenize() {
        BasicTokenizer tokenizer = new BasicTokenizer();
        String text = "我爱中国, i like playing basketball.";
        List<Token> tokens = tokenizer.tokenize(text);
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    @Test
    public void whitespace() {
        WhiteSpaceTokenizer tokenizer = WhiteSpaceTokenizer.builder().build();
        String text = "我爱中国, i like playing basketball.";
        List<Token> tokens = tokenizer.tokenize(text);
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}