package com.netease.easyml.local.mllib.keyword;

import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.SortUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.Graph;
import com.netease.easyml.local.mllib.PageRank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.netease.easyml.local.mllib.keyword.Keywords.genCandidate;

/**
 * Created by eddielin on 2018/8/30.
 */
public class TextRankKeyword implements IKeyword {
    protected Set<String> stopWord;
    protected int windowSize;
    protected PageRank.Params params;

    public TextRankKeyword(Set<String> stopWord, int windowSize, PageRank.Params params) {
        this.stopWord = stopWord;
        this.windowSize = windowSize;
        this.params = params;
    }

    protected Graph<String> buildGraph(List<String> text) {
        Graph<String> graph = new Graph<>();
        text.forEach(graph::addNode);

//        text = text.stream().map(String::toLowerCase).collect(Collectors.toList());
        for (int i = 1; i < text.size(); i++) {
            for (int j = Math.max(i - windowSize + 1, 0); j < i; j++) {
                graph.addEdge(text.get(i), text.get(j), 1);
                graph.addEdge(text.get(j), text.get(i), 1);
            }
        }
        return graph;
    }

    @Override
    public Map<String, Double> transform(List<Term> text) {
        List<String> candidateWords = genCandidate(stopWord, text);
        Graph<String> graph = buildGraph(candidateWords);
        Map<String, Double> score = PageRank.fit(params, graph);
        return SortUtil.sortByValueDesc(score);
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private Set<String> stopWords;
        private int windowSize = 2;

        private PageRank.Params params = new PageRank.Params();

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String stopWordsPath;

        public Builder setStopWords(String stopWords) {
            this.stopWordsPath = stopWords;
            return this;
        }

        public TextRankKeyword build() {
            if (!StringUtil.isEmpty(stopWordsPath)) {
                stopWords = new HashSet<>(IOUtil.readLines(stopWordsPath));
            }
            return new TextRankKeyword(stopWords, windowSize, params);
        }
    }
}
