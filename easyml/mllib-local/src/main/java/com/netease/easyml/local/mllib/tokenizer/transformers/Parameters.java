package com.netease.easyml.local.mllib.tokenizer.transformers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by linjiuning on 2020/2/15.
 */
public class Parameters implements Serializable {
    private String bosToken;
    private String eosToken;
    private String unkToken;
    private String sepToken;
    private String padToken;
    private String clsToken;
    private String maskToken;
    private int padTokenTypeId = 0;
    private Set<String> specialTokens = new HashSet<>();

    private int maxLen = (int) 1e12;

    private String paddingSide = PaddingSide.RIGHT;

    private Map<String, Integer> addedTokensEncoder = new HashMap<>();
    private Map<Integer, String> addedTokensDecoder = new HashMap<>();

    // encode parameter
    private boolean addSpecialTokens = true;
    private int stride = 0;
    private String truncationStrategy = TruncateStrategy.LONGEST_FIRST;
    private boolean padToMaxLength = false;

    private boolean returnTokenTypeIds = true;
    private boolean returnAttentionMask = true;

    public String getBosToken() {
        return bosToken;
    }

    public Parameters setBosToken(String bosToken) {
        this.bosToken = bosToken;
        return this;
    }

    public String getEosToken() {
        return eosToken;
    }

    public Parameters setEosToken(String eosToken) {
        this.eosToken = eosToken;
        return this;
    }

    public String getUnkToken() {
        return unkToken;
    }

    public Parameters setUnkToken(String unkToken) {
        this.unkToken = unkToken;
        return this;
    }

    public String getSepToken() {
        return sepToken;
    }

    public Parameters setSepToken(String sepToken) {
        this.sepToken = sepToken;
        return this;
    }

    public String getPadToken() {
        return padToken;
    }

    public Parameters setPadToken(String padToken) {
        this.padToken = padToken;
        return this;
    }

    public String getClsToken() {
        return clsToken;
    }

    public Parameters setClsToken(String clsToken) {
        this.clsToken = clsToken;
        return this;
    }

    public String getMaskToken() {
        return maskToken;
    }

    public Parameters setMaskToken(String maskToken) {
        this.maskToken = maskToken;
        return this;
    }

    public int getPadTokenTypeId() {
        return padTokenTypeId;
    }

    public Parameters setPadTokenTypeId(int padTokenTypeId) {
        this.padTokenTypeId = padTokenTypeId;
        return this;
    }

    public Set<String> getSpecialTokens() {
        return specialTokens;
    }

    public Parameters setSpecialTokens(Set<String> specialTokens) {
        this.specialTokens = specialTokens;
        return this;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public Parameters setMaxLen(int maxLen) {
        this.maxLen = maxLen;
        return this;
    }

    public String getPaddingSide() {
        return paddingSide;
    }

    public Parameters setPaddingSide(String paddingSide) {
        this.paddingSide = paddingSide;
        return this;
    }

    public Map<String, Integer> getAddedTokensEncoder() {
        return addedTokensEncoder;
    }

    public Parameters setAddedTokensEncoder(Map<String, Integer> addedTokensEncoder) {
        this.addedTokensEncoder = addedTokensEncoder;
        return this;
    }

    public Map<Integer, String> getAddedTokensDecoder() {
        return addedTokensDecoder;
    }

    public Parameters setAddedTokensDecoder(Map<Integer, String> addedTokensDecoder) {
        this.addedTokensDecoder = addedTokensDecoder;
        return this;
    }

    public boolean isAddSpecialTokens() {
        return addSpecialTokens;
    }

    public Parameters setAddSpecialTokens(boolean addSpecialTokens) {
        this.addSpecialTokens = addSpecialTokens;
        return this;
    }

    public int getStride() {
        return stride;
    }

    public Parameters setStride(int stride) {
        this.stride = stride;
        return this;
    }

    public String getTruncationStrategy() {
        return truncationStrategy;
    }

    public Parameters setTruncationStrategy(String truncationStrategy) {
        this.truncationStrategy = truncationStrategy;
        return this;
    }

    public boolean isPadToMaxLength() {
        return padToMaxLength;
    }

    public Parameters setPadToMaxLength(boolean padToMaxLength) {
        this.padToMaxLength = padToMaxLength;
        return this;
    }

    public boolean isReturnTokenTypeIds() {
        return returnTokenTypeIds;
    }

    public Parameters setReturnTokenTypeIds(boolean returnTokenTypeIds) {
        this.returnTokenTypeIds = returnTokenTypeIds;
        return this;
    }

    public boolean isReturnAttentionMask() {
        return returnAttentionMask;
    }

    public Parameters setReturnAttentionMask(boolean returnAttentionMask) {
        this.returnAttentionMask = returnAttentionMask;
        return this;
    }
}
