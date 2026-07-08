package com.netease.easyml.local.mllib.keyword;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.local.mllib.bean.Document;

import java.util.List;
import java.util.Map;

import static com.netease.easyml.local.mllib.keyword.Keywords.getTopK;

/**
 * Modified by eddielin on 2018/8/2.
 */
public interface IKeyword {

    Map<String, Double> transform(List<Term> termList);

    default Map<String, Double> transform(List<Term> termList, int topK) {
        return getTopK(transform(termList), topK);
    }

    default Map<String, Double> transform(String text) {
        return transform(HanLP.segment(text));
    }

    default Map<String, Double> transform(String text, int topK) {
        return getTopK(transform(text), topK);
    }

    default Map<String, Double> transform(Document document) {
        return transform(document.getCleanTerms());
    }

    default Map<String, Double> transform(Document document, int topK) {
        return getTopK(transform(document), topK);
    }
}
