package com.netease.easyml.local.mllib.tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2020/2/11.
 */
public interface Tokenizer {
    default List<List<Token>> batchTokenize(List<String> texts) {
        List<List<Token>> batchTokens = new ArrayList<>();
        for (String text : texts) {
            List<Token> tokens = tokenize(text);
            batchTokens.add(tokens);
        }
        return batchTokens;
    }

    List<Token> tokenize(String text);
}
