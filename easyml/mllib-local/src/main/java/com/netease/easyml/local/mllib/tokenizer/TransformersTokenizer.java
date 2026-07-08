package com.netease.easyml.local.mllib.tokenizer;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.local.mllib.tokenizer.transformers.BertTokenizer;
import com.netease.easyml.local.mllib.tokenizer.transformers.PreTrainedTokenizer;
import com.netease.easyml.local.mllib.tokenizer.transformers.TransformersUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linjiuning on 2020/2/13.
 */
public class TransformersTokenizer implements Tokenizer {

    private static final Pattern GROUP_SEP = Pattern.compile("^(.*) \\|\\|\\| (.*)$");

    private PreTrainedTokenizer tokenizer;

    public TransformersTokenizer(PreTrainedTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public TransformersTokenizer() {
        tokenizer = BertTokenizer.builder().build();
    }

    public Map<String, Integer> getVocab() {
        return tokenizer.vocab();
    }

    public List<Token> tokenize(String textA, String textB) {
        Params encodedTokens = tokenizer.encodePlus(textA, textB);
        int[] inputIds = encodedTokens.get(TransformersUtil.IndexKeys.INPUT_IDS, int[].class);
        int[] tokenTypeIds = encodedTokens.get(TransformersUtil.IndexKeys.TOKEN_TYPE_IDS, int[].class);
        return createTokens(inputIds, tokenTypeIds);
    }

    private List<Token> createTokens(int[] inputIds, int[] tokenTypeIds) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < inputIds.length; i++) {
            int tokenId = inputIds[i];
            int tokenTypeId = tokenTypeIds[i];
            String tokenStr = tokenizer.convertIdToToken(tokenId);
            boolean wordPieceStart = TransformersUtil.isWordPieceStart(tokenStr);
            Token token = new Token();
            token.text = tokenStr;
            token.textId = tokenId;
            token.typeId = tokenTypeId;
            token.wordPieceStart = wordPieceStart;
            tokens.add(token);
        }
        return tokens;
    }

    @Override
    public List<Token> tokenize(String text) {
        String textA;
        String textB = null;
        Matcher m = GROUP_SEP.matcher(text);
        if (m.find()) {
            textA = m.group(1);
            textB = m.group(2);
        } else {
            textA = text;
        }

        return tokenize(textA, textB);
    }

    public int padTokenId() {
        return tokenizer.padTokenId();
    }

    public int padTokenTypeId() {
        return tokenizer.padTokenTypeId();
    }

    public boolean paddingOnLeft() {
        return tokenizer.paddingOnLeft();
    }
}
