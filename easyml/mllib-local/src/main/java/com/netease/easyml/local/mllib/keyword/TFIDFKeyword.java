package com.netease.easyml.local.mllib.keyword;

import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

import static com.netease.easyml.local.mllib.keyword.Keywords.genCandidate;

/**
 * Created by eddielin on 2018/8/30.
 */
@Slf4j
public class TFIDFKeyword implements IKeyword {
    private Set<String> stopWord;
    private Map<String, Float> idfWeight;
    private float defaultIdf;

    public TFIDFKeyword(Set<String> stopWord, Map<String, Float> idfWeight, float defaultIdf) {
        this.stopWord = stopWord;
        this.idfWeight = idfWeight;
        this.defaultIdf = defaultIdf;
    }

    @Override
    public Map<String, Double> transform(List<Term> text) {
        List<String> candidateWords = genCandidate(stopWord, text);
        Map<String, Double> result = new HashMap<>();

        for (String wd : candidateWords) {
            String word = wd.toLowerCase();
            result.put(word, result.getOrDefault(word, 0.0)
                    + idfWeight.getOrDefault(word, defaultIdf));
        }
        return SortUtil.sortByValueDesc(result);
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private Set<String> stopWords = Constant.STOP_WORD;
        private Map<String, Float> idfWeight;
        private float defaultIdf = 5.0f;

        private String kvDelimiter = " ";

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String stopWordsPath;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String idfWeightPath;

        public Builder setStopWords(String stopWords) {
            this.stopWordsPath = stopWords;
            return this;
        }

        public Builder setIdfWeight(String idfWeight) {
            this.idfWeightPath = idfWeight;
            return this;
        }

        public TFIDFKeyword build() {
            if (!StringUtil.isEmpty(stopWordsPath)) {
                stopWords = new HashSet<>(IOUtil.readLines(stopWordsPath));
            }
            if (!StringUtil.isEmpty(idfWeightPath)) {
                try {
                    idfWeight = ResourceUtil.loadWordWeight(idfWeightPath, kvDelimiter);
                } catch (IOException e) {
                    log.error(String.format("Failed to load idf weight from %s", idfWeightPath));
                }
            }
            return new TFIDFKeyword(stopWords, idfWeight, defaultIdf);
        }
    }
}
