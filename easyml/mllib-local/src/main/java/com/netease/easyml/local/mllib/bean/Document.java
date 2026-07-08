package com.netease.easyml.local.mllib.bean;

import com.hankcs.hanlp.seg.common.Term;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by eddielin on 2019/3/1.
 */
@NoArgsConstructor
@Data
public class Document {
    private String author = "";
    protected Sentence title;
    protected List<Sentence> sentences = new ArrayList<>();
    private int[] chunk; // 分组边界
    private Map<String, Double> keywords;

    public Document(Sentence title, List<Sentence> sentences) {
        this.title = title;
        this.sentences = sentences;
    }

    public Document(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public String getOrigin() {
        StringBuilder sb = new StringBuilder();
        if (title != null)
            sb.append(title.getOrigin());
        for (Sentence sentence : getTextSentences()) {
            sb.append(sentence.getOrigin());
        }
        return sb.toString();
    }

    public List<Term> getTerms() {
        List<Term> terms = new ArrayList<>(title.getTerms());
        for (Sentence sentence : getTextSentences()) {
            terms.addAll(sentence.getTerms());
        }
        return terms;
    }

    public String getClean() {
        StringBuilder sb = new StringBuilder();
        sb.append(title.getClean());
        for (Sentence sentence : getTextSentences()) {
            sb.append(sentence.getClean());
        }
        return sb.toString();
    }

    public List<Term> getCleanTerms() {
        List<Term> terms = new ArrayList<>();
        if (title != null)
            terms.addAll(title.getCleanTerms());
        for (Sentence sentence : getTextSentences()) {
            terms.addAll(sentence.getCleanTerms());
        }
        return terms;
    }

    public List<Sentence> getChunks() {
        if (chunk == null)
            return sentences;
        List<Sentence> newSents = new ArrayList<>();
        int left = 0;
        for (int right : chunk) {
            Sentence sentence = null;
            for (int i = left; i < right; i++) {
                Sentence cur = sentences.get(i);
                sentence = sentence == null ? cur : Sentence.concat(sentence, cur);
            }
            left = right;
            newSents.add(sentence);
        }
        if (left < sentences.size()) {
            Sentence sentence = null;
            for (int i = left; i < sentences.size(); i++) {
                Sentence cur = sentences.get(i);
                sentence = sentence == null ? cur : Sentence.concat(sentence, cur);
            }
            newSents.add(sentence);
        }
        return newSents;
    }

    public List<Sentence> getTextSentences() {
        return sentences.stream()
                .filter(it -> it.getType().equals(SentenceType.TEXT))
                .collect(Collectors.toList());
    }

    public int size() {
        return sentences.size();
    }

    @Override
    public String toString() {
        return "Document{" +
                "title=" + title +
                ", sentences=" + sentences +
                '}';
    }
}
