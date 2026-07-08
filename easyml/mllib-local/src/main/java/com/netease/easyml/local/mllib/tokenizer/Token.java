package com.netease.easyml.local.mllib.tokenizer;

/**
 * Created by eddielin on 2020/2/11.
 */
public class Token {
    public String text;
    public Integer idx;
    public String lemma_;
    public String pos_;
    public String tag_;
    public String dep_;
    public String entType_;
    public Integer textId;
    public Integer typeId;
    public boolean wordPieceStart = true;

    @Override
    public String toString() {
        return text;
    }
}
