package com.netease.easyml.local.mllib.bean;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Created by eddielin on 2019/3/1.
 */
@Data
public class Sentence {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id {
        private int offset = -1;
        private int paraId = -1;
        private int sentId = -1;

        public int getId() {
            return paraId * 1000 + sentId;
        }

        public static Id id(int paraId, int sentId) {
            return new Id(-1, paraId, sentId);
        }

        public static Id id(int offset, int paraId, int sentId) {
            return new Id(offset, paraId, sentId);
        }
    }

    private String origin;
    private List<Term> terms = new ArrayList<>();
    private String clean; // cleaned sentence
    private List<Term> cleanTerms = null;
    private SentenceType type = SentenceType.TEXT;
    private Map<HTMLInfo, Object> htmlInfos = new HashMap<>();

    // for concat
    private List<Id> id = new ArrayList<>();
    private List<Integer> size = new ArrayList<>();
    private List<Integer> cleanSize = new ArrayList<>();

    // for vector
    private double[] vector; // vector of sentence
    private int[] indices; //use for sparse vector

    public void setId(List<Id> id) {
        this.id = id;
    }

    public void setId(Id id) {
        this.id.add(id);
    }

    public Id getStartId() {
        return id.isEmpty() ? new Id() : id.get(0);
    }

    public Id getEndId() {
        return id.isEmpty() ? new Id() : id.get(id.size() - 1);
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
        this.size.add(terms.size());
    }

    public void setCleanTerms(List<Term> cleanTerms) {
        this.cleanTerms = cleanTerms;
        if (cleanSize == null) {
            cleanSize = new ArrayList<>();
        }
        this.cleanSize.add(cleanTerms.size());
    }

    public String getClean() {
        return StringUtil.isEmpty(clean) ? origin : clean;
    }

    public List<Term> getCleanTerms() {
        return cleanTerms == null ? terms : cleanTerms;
    }

    public List<Integer> getCleanSize() {
        return cleanSize == null ? size : cleanSize;
    }

    public int length() {
        return getOrigin().length();
    }

    public int size() {
        return getTerms().size();
    }

    public void setOrigin(String origin) {
        List<Term> terms = setAndPad(this.origin, origin, this.terms);
        this.origin = origin;
        this.terms = terms;
    }

    public void setClean(String clean) {
        List<Term> terms = setAndPad(this.clean, clean, this.cleanTerms);
        this.clean = clean;
        this.cleanTerms = terms;
    }

    public static Sentence concat(Sentence before, Sentence... after) {
        StringBuilder origin = new StringBuilder(before.getOrigin());
        List<Term> terms = new ArrayList<>(before.getTerms());
        StringBuilder clean = new StringBuilder(before.getClean());
        List<Term> cleanTerms = new ArrayList<>(before.getCleanTerms());
        List<Id> id = new ArrayList<>(before.getId());
        List<Integer> originSize = new ArrayList<>(before.getSize());
        List<Integer> cleanSize = new ArrayList<>(before.getCleanSize());
        Map<HTMLInfo, Object> infos = new HashMap<>(before.getHtmlInfos());
        for (Sentence o : after) {
            origin.append(o.getOrigin());
            terms.addAll(o.getTerms());
            clean.append(o.getClean());
            cleanTerms.addAll(o.getCleanTerms());
            originSize.addAll(o.getSize());
            cleanSize.addAll(o.getCleanSize());
            infos.putAll(o.getHtmlInfos());
            id.addAll(o.getId());
        }

        Sentence newSent = new Sentence();
        newSent.setOrigin(origin.toString());
        newSent.setTerms(terms);
        newSent.setClean(clean.toString());
        newSent.setCleanTerms(cleanTerms);
        newSent.setId(id);
        newSent.setSize(originSize);
        newSent.setCleanSize(cleanSize);
        newSent.setHtmlInfos(infos);

        // use before type
        newSent.setType(before.getType());
        return newSent;
    }

    public static List<Term> setAndPad(String oStr, String nStr, List<Term> oTerms) {
        if (nStr.isEmpty())
            return Collections.emptyList();
        if (oTerms.isEmpty() || oStr.equals(nStr))
            return oTerms;
        if (!oStr.startsWith(nStr))
            return HanLP.segment(nStr);
        int i = 0;
        int len = 0;
        List<Term> nTerms = new ArrayList<>();
        while (i < oTerms.size()) {
            if (len >= nStr.length())
                break;
            Term term = oTerms.get(i);

            int tmp = len + term.word.length();
            if (tmp > nStr.length()) {
                term = new Term(nStr.substring(len), term.nature);
            }
            len = tmp;
            nTerms.add(term);
            i++;
        }
        return nTerms;
    }

    @Override
    public String toString() {
        return origin;
    }
}
