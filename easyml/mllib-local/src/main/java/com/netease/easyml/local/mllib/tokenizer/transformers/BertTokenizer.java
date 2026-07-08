package com.netease.easyml.local.mllib.tokenizer.transformers;

import com.netease.easyml.common.util.IOUtil;

import java.util.*;

/**
 * Created by linjiuning on 2020/2/15.
 */
public class BertTokenizer extends PreTrainedTokenizer {
    private String vocabFile;
    private int maxLength;
    private boolean doLowerCase;
    private String truncationStrategy;
    private boolean doBasicTokenize;
    private Set<String> neverSplit;
    private String unkToken;
    private String sepToken;
    private String padToken;
    private String clsToken;
    private String maskToken;
    private boolean tokenizeChineseChars;

    private Map<String, Integer> vocab;
    private Map<Integer, String> idsToToken;

    private BasicTokenizer basicTokenizer;
    private WordPieceTokenizer wordPieceTokenizer;

    public BertTokenizer(String vocabFile, int maxLength, boolean doLowerCase, String truncationStrategy, boolean doBasicTokenize, Set<String> neverSplit, String unkToken, String sepToken, String padToken, String clsToken, String maskToken, boolean tokenizeChineseChars) {
        this.vocabFile = vocabFile;
        this.maxLength = maxLength;
        this.doLowerCase = doLowerCase;
        this.truncationStrategy = truncationStrategy;
        this.doBasicTokenize = doBasicTokenize;
        this.neverSplit = neverSplit;
        this.unkToken = unkToken;
        this.sepToken = sepToken;
        this.padToken = padToken;
        this.clsToken = clsToken;
        this.maskToken = maskToken;
        this.tokenizeChineseChars = tokenizeChineseChars;
        build();
    }

    public void build() {
        params.setUnkToken(unkToken);
        params.setSepToken(sepToken);
        params.setPadToken(padToken);
        params.setClsToken(clsToken);
        params.setMaskToken(maskToken);
        params.setMaxLen(maxLength);
        params.setTruncationStrategy(truncationStrategy);

        addSpecialToken(unkToken);
        addSpecialToken(sepToken);
        addSpecialToken(padToken);
        addSpecialToken(clsToken);
        addSpecialToken(maskToken);

        if (IOUtil.isDirectory(vocabFile)) {
            vocabFile = IOUtil.join(vocabFile, "vocab.txt");
        }

        vocab = Tokenizers.loadVocab(vocabFile);
        idsToToken = new HashMap<>();
        for (Map.Entry<String, Integer> entry : vocab.entrySet()) {
            idsToToken.put(entry.getValue(), entry.getKey());
        }

        if (doBasicTokenize) {
            basicTokenizer = new BasicTokenizer(doLowerCase, neverSplit, tokenizeChineseChars);
        }
        wordPieceTokenizer = new WordPieceTokenizer(vocab, unkToken);
    }

    @Override
    public int convertTokenToId(String token) {
        return vocab.getOrDefault(token, vocab.get(unkToken()));
    }

    @Override
    public String convertIdToToken(int index) {
        return idsToToken.getOrDefault(index, unkToken());
    }

    @Override
    protected List<String> _tokenize(String text) {
        List<String> splitTokens = new ArrayList<>();
        if (doBasicTokenize) {
            for (String token : basicTokenizer.tokenize(text, specialTokens())) {
                splitTokens.addAll(wordPieceTokenizer._tokenize(token));
            }
        } else {
            splitTokens.addAll(wordPieceTokenizer._tokenize(text));
        }
        return splitTokens;
    }

    @Override
    public Map<String, Integer> vocab() {
        return vocab;
    }

    @Override
    public int vocabSize() {
        return vocab.size();
    }

    /**
     * Build model inputs from a sequence or a pair of sequence for sequence classification tasks
     * by concatenating and adding special tokens.
     * A BERT sequence has the following format:
     * single sequence: [CLS] X [SEP]
     * pair of sequences: [CLS] A [SEP] B [SEP]
     * "
     */
    @Override
    public int[] buildInputsWithSpecialTokens(int[] ids, int[] pairIds) {
        if (pairIds == null) {
            int[] newIds = new int[ids.length + 2];
            newIds[0] = clsTokenId();
            newIds[newIds.length - 1] = sepTokenId();
            System.arraycopy(ids, 0, newIds, 1, ids.length);
            return newIds;
        }

        int[] newIds = new int[ids.length + pairIds.length + 3];
        newIds[0] = clsTokenId();
        newIds[newIds.length - 1] = sepTokenId();
        newIds[ids.length + 1] = sepTokenId();
        System.arraycopy(ids, 0, newIds, 1, ids.length);
        System.arraycopy(pairIds, 0, newIds, ids.length + 2, pairIds.length);
        return newIds;
    }

    /**
     * Creates a mask from the two sequences passed to be used in a sequence-pair classification task.
     * A BERT sequence pair mask has the following format:
     * 0 0 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1
     * | first sequence    | second sequence
     * <p>
     * if pairIds is None, only returns the first portion of the mask (0's).
     **/
    @Override
    protected int[] createTokenTypeIdsFromSequences(int[] ids, int[] pairIds) {
        if (pairIds == null) {
            return new int[ids.length + 2];
        }
        int[] typeIds = new int[ids.length + pairIds.length + 3];
        for (int i = 2 + ids.length; i < typeIds.length; i++) {
            typeIds[i] = 1;
        }
        return typeIds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String vocabFile = "pretrained/bert-base-chinese-vocab.txt";
        private int maxLength = 512;
        private boolean doLowerCase = true;
        private String truncationStrategy = TruncateStrategy.LONGEST_FIRST;
        private boolean doBasicTokenize = true;
        private Set<String> neverSplit = new HashSet<>();
        private String unkToken = SpecialTokens.UNK_TOKEN;
        private String sepToken = SpecialTokens.SEP_TOKEN;
        private String padToken = SpecialTokens.PAD_TOKEN;
        private String clsToken = SpecialTokens.CLS_TOKEN;
        private String maskToken = SpecialTokens.MASK_TOKEN;
        private boolean tokenizeChineseChars = true;

        public String getVocabFile() {
            return vocabFile;
        }

        public Builder setVocabFile(String vocabFile) {
            this.vocabFile = vocabFile;
            return this;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public Builder setMaxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public boolean isDoLowerCase() {
            return doLowerCase;
        }

        public Builder setDoLowerCase(boolean doLowerCase) {
            this.doLowerCase = doLowerCase;
            return this;
        }

        public String getTruncationStrategy() {
            return truncationStrategy;
        }

        public Builder setTruncationStrategy(String truncationStrategy) {
            this.truncationStrategy = truncationStrategy;
            return this;
        }

        public boolean isDoBasicTokenize() {
            return doBasicTokenize;
        }

        public Builder setDoBasicTokenize(boolean doBasicTokenize) {
            this.doBasicTokenize = doBasicTokenize;
            return this;
        }

        public Set<String> getNeverSplit() {
            return neverSplit;
        }

        public Builder setNeverSplit(Set<String> neverSplit) {
            this.neverSplit = neverSplit;
            return this;
        }

        public String getUnkToken() {
            return unkToken;
        }

        public Builder setUnkToken(String unkToken) {
            this.unkToken = unkToken;
            return this;
        }

        public String getSepToken() {
            return sepToken;
        }

        public Builder setSepToken(String sepToken) {
            this.sepToken = sepToken;
            return this;
        }

        public String getPadToken() {
            return padToken;
        }

        public Builder setPadToken(String padToken) {
            this.padToken = padToken;
            return this;
        }

        public String getClsToken() {
            return clsToken;
        }

        public Builder setClsToken(String clsToken) {
            this.clsToken = clsToken;
            return this;
        }

        public String getMaskToken() {
            return maskToken;
        }

        public Builder setMaskToken(String maskToken) {
            this.maskToken = maskToken;
            return this;
        }

        public boolean isTokenizeChineseChars() {
            return tokenizeChineseChars;
        }

        public Builder setTokenizeChineseChars(boolean tokenizeChineseChars) {
            this.tokenizeChineseChars = tokenizeChineseChars;
            return this;
        }

        public BertTokenizer build() {
            return new BertTokenizer(vocabFile, maxLength, doLowerCase, truncationStrategy, doBasicTokenize,
                    neverSplit, unkToken, sepToken, padToken, clsToken, maskToken, tokenizeChineseChars);
        }
    }
}