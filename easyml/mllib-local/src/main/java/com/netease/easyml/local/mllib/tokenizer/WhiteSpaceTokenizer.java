package com.netease.easyml.local.mllib.tokenizer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by linjiuning on 2020/4/1.
 */
public class WhiteSpaceTokenizer implements Tokenizer {
    private String sep;
    private boolean lowercaseCharacters;
    private int maxLength;
    private List<Object> startTokens;
    private List<Object> endTokens;

    public WhiteSpaceTokenizer(String sep, boolean lowercaseCharacters, int maxLength, List<Object> startTokens, List<Object> endTokens) {
        this.sep = sep;
        this.lowercaseCharacters = lowercaseCharacters;
        this.maxLength = maxLength;
        this.startTokens = startTokens;
        this.endTokens = endTokens;
    }

    @Override
    public List<Token> tokenize(String text) {
        if (lowercaseCharacters) {
            text = text.toLowerCase();
        }
        List<Token> tokens = new ArrayList<>();
        for (String wd : text.split(sep, -1)) {
            if (maxLength > 0 && tokens.size() >= maxLength) {
                break;
            }
            Token token = new Token();
            token.text = wd;
            tokens.add(token);
        }

        for (Object startToken : startTokens) {
            Token token = new Token();
            if (startToken instanceof Integer) {
                token.textId = (Integer) startToken;
                token.idx = 0;
            } else {
                token.text = (String) startToken;
                token.idx = 0;
            }
            tokens.add(0, token);
        }

        for (Object endToken : endTokens) {
            Token token = new Token();
            if (endToken instanceof Integer) {
                token.textId = (Integer) endToken;
                token.idx = 0;
            } else {
                token.text = (String) endToken;
                token.idx = 0;
            }
            tokens.add(token);
        }
        return tokens;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sep = " ";
        private boolean lowercaseCharacters = false;
        private int maxLength;
        private List<Object> startTokens;
        private List<Object> endTokens;

        public String getSep() {
            return sep;
        }

        public Builder setSep(String sep) {
            this.sep = sep;
            return this;
        }

        public boolean isLowercaseCharacters() {
            return lowercaseCharacters;
        }

        public Builder setLowercaseCharacters(boolean lowercaseCharacters) {
            this.lowercaseCharacters = lowercaseCharacters;
            return this;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public Builder setMaxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public List<Object> getStartTokens() {
            return startTokens;
        }

        public Builder setStartTokens(List<Object> startTokens) {
            this.startTokens = startTokens;
            return this;
        }

        public List<Object> getEndTokens() {
            return endTokens;
        }

        public Builder setEndTokens(List<Object> endTokens) {
            this.endTokens = endTokens;
            return this;
        }

        public WhiteSpaceTokenizer build() {
            if (startTokens == null) {
                startTokens = new ArrayList<>();
            }
            Collections.reverse(startTokens);

            if (endTokens == null) {
                endTokens = new ArrayList<>();
            }

            if (maxLength > 0) {
                maxLength -= startTokens.size() + endTokens.size();
                if (maxLength <= 0) {
                    throw new IllegalArgumentException("max length must > 0");
                }
            }
            return new WhiteSpaceTokenizer(sep, lowercaseCharacters, maxLength, startTokens, endTokens);
        }
    }
}
