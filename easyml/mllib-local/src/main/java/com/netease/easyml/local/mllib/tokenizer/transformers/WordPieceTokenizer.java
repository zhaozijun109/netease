package com.netease.easyml.local.mllib.tokenizer.transformers;

import com.netease.easyml.local.mllib.tokenizer.Token;
import com.netease.easyml.local.mllib.tokenizer.Tokenizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.local.mllib.tokenizer.transformers.Tokenizers.whitespaceTokenize;

/**
 * Created by linjiuning on 2019/8/22.
 */
public class WordPieceTokenizer implements Tokenizer, Serializable {
    private Map<String, Integer> vocab;
    private String unkToken;
    private int maxInputCharsPerWord;

    public WordPieceTokenizer(Map<String, Integer> vocab, String unkToken, int maxInputCharsPerWord) {
        this.vocab = vocab;
        this.unkToken = unkToken;
        this.maxInputCharsPerWord = maxInputCharsPerWord;
    }

    public WordPieceTokenizer(Map<String, Integer> vocab, String unkToken) {
        this(vocab, unkToken, 100);
    }

    public List<String> _tokenize(String text) {
        List<String> outputTokens = new ArrayList<>();
        for (String token : whitespaceTokenize(text)) {
            if (token.length() > maxInputCharsPerWord) {
                outputTokens.add(token);
                continue;
            }

            boolean isBad = false;
            int start = 0;
            List<String> subTokens = new ArrayList<>();
            while (start < token.length()) {
                int end = token.length();
                String curSubstr = "";
                while (start < end) {
                    String substr = token.substring(start, end);
                    if (start > 0)
                        substr = "##" + substr;
                    if (vocab.containsKey(substr)) {
                        curSubstr = substr;
                        break;
                    }
                    end -= 1;
                }
                if (curSubstr.isEmpty()) {
                    isBad = true;
                    break;
                }
                subTokens.add(curSubstr);
                start = end;
            }
            if (isBad)
                outputTokens.add(unkToken);
            else
                outputTokens.addAll(subTokens);
        }
        return outputTokens;
    }


    @Override
    public List<Token> tokenize(String text) {
        List<String> words = _tokenize(text);
        List<Token> tokens = new ArrayList<>();
        for (String word : words) {
            Token token = new Token();
            token.text = word;
            tokens.add(token);
        }
        return tokens;
    }
}
