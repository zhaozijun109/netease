package com.netease.easyml.local.mllib.keyword;

import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.Constant;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.ResourceUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by showcai on 2018/9/17.
 * Modified by eddielin on 2019/3/20.
 * Keyword extraction from a single document using word co-occurrence statistical information
 */
@Slf4j
public class ChiSquareKeyword implements IKeyword {
    private static final Pattern STOP_MASK_PATTERN = Pattern.compile("([，,；;。！﹗!？﹖?])+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final double JS_LOWER_BOUND = 0.95 * Math.log(2.0);
    private static final float FREQ_RATIO = 0.3f;

    private Set<String> stopWord;
    private int mMaxFreqNum;
    private float mFreqRate;
    private int mFreqLowerBound;
    private Map<String, Float> mWordIDFMap;
    private float defaultIdf;

    public static class CoMatrix {
        double[][] coMatrix;
        double[] nw;
        double[] pg;
        double[] tf;

        public CoMatrix(int rowDim, int colDim) {
            coMatrix = new double[rowDim][colDim];
            nw = new double[rowDim];
            pg = new double[colDim];
            tf = new double[rowDim];
        }
    }

    public ChiSquareKeyword(Set<String> stopWord, int mMaxFreqNum, float mFreqRate, int mFreqLowerBound,
                            Map<String, Float> mWordIDFMap, float defaultIdf) {
        this.stopWord = stopWord;
        this.mMaxFreqNum = mMaxFreqNum;
        this.mFreqRate = mFreqRate;
        this.mFreqLowerBound = mFreqLowerBound;
        this.mWordIDFMap = mWordIDFMap;
        this.defaultIdf = defaultIdf;
    }

    private boolean isLegalWord(String w, String nature) {
        if (stopWord != null && stopWord.contains(w)) {
            return false;
        }
        Matcher match = STOP_MASK_PATTERN.matcher(w);
        if (match.matches() || Keywords.mStopPos.contains(nature) || Keywords.mBannedCandidatePos.contains(nature) || w.length() < 2) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Double> transform(Document document) {
        List<Term> cleanTerms = document.getCleanTerms();
        String[] freqWords = getFreqWords(cleanTerms);

        Map<String, Integer> freqWordsIdxMap = new HashMap<>();
        for (int i = 0; i < freqWords.length; i++) {
            freqWordsIdxMap.put(freqWords[i], i);
        }

        Map<String, Integer> wordIdxMap = getWordIdxMap(cleanTerms);

        String[] wordArr = new String[wordIdxMap.size()];

        for (Map.Entry<String, Integer> entry : wordIdxMap.entrySet()) {
            wordArr[entry.getValue()] = entry.getKey();
        }

        CoMatrix coMatrix = buildCoMatrix(document, wordIdxMap, freqWordsIdxMap);

        List<Pair<String, Double>> allRst = computeChiSquares(coMatrix, wordArr);
        Map<String, Double> scores = new LinkedHashMap<>();
        for (Pair<String, Double> pair : allRst) {
            scores.put(pair.getValue0(), pair.getValue1());
        }
        return scores;
    }

    @Override
    public Map<String, Double> transform(List<Term> text) {
        Sentence sentence = new Sentence();
        sentence.setTerms(text);
        Document doc = new Document();
        doc.setSentences(Collections.singletonList(sentence));
        return transform(doc);
    }

    private void updateWordTF(List<Term> terms, Map<String, Integer> wordTFMap) {
        for (Term term : terms) {
            String nature = term.nature.toString();
            String word = term.word;
            if (isLegalWord(word, nature)) {
                wordTFMap.put(word, wordTFMap.getOrDefault(word, 0) + 1);
            }
        }
    }

    private String[] getFreqWords(List<Term> cleanTerms) {
        HashMap<String, Integer> wordTFMap = new HashMap<>();
        updateWordTF(cleanTerms, wordTFMap);

        List<Pair<String, Integer>> tfPairs = new ArrayList<>(wordTFMap.size());
        for (Map.Entry<String, Integer> entry : wordTFMap.entrySet()) {
            Pair<String, Integer> pair = new Pair<>(entry.getKey(), entry.getValue());
            tfPairs.add(pair);
        }
        tfPairs.sort((it1, it2) -> it2.getValue1().compareTo(it1.getValue1()));
        int freqNum = Math.min(mMaxFreqNum, (int) Math.ceil(tfPairs.size() * mFreqRate));
        List<String> freqWords = new ArrayList<>(freqNum);
        for (int i = 0; i < freqNum; i++) {
            if (tfPairs.get(i).getValue1() >= mFreqLowerBound) {
                freqWords.add(tfPairs.get(i).getValue0());
            } else {
                break;
            }
        }
        String[] rst = new String[freqWords.size()];
        freqWords.toArray(rst);
        return rst;
    }

    private double computeJSDist(double[] vec1, double[] vec2) {
        double dist = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            if (vec1[i] != vec2[i]) {
                double prob1 = vec1[i] == 0 ? 1e-8 : vec1[i];
                double prob2 = vec2[i] == 0 ? 1e-8 : vec2[i];
                dist += prob1 * Math.log(prob1 / prob2);
                dist += prob2 * Math.log(prob2 / prob1);
            }
        }
        if (dist == 0) {
            dist = -100;
        }
        return dist / 2;
    }

    private CoMatrix buildCoMatrix(Document doc, Map<String, Integer> wordIdxMap, Map<String, Integer> freqWordsIdxMap) {
        int totalCnt = 0;
        CoMatrix coMatrix = new CoMatrix(wordIdxMap.size(), freqWordsIdxMap.size());
        List<Term> terms;
        int size = doc.getTextSentences().size();
        for (int i = 0; i < size + 1; i++) {
            if (i == size) {
                terms = doc.getTitle() == null ? null : doc.getTitle().getCleanTerms();
            } else {
                terms = doc.getTextSentences().get(i).getCleanTerms();
            }
            if (terms == null || terms.isEmpty()) {
                continue;
            }
            List<Integer> wBasket = new ArrayList<>();
            Set<Integer> freqBasket = new HashSet<>();

            for (Term term : terms) {
                String w = term.word;
                String nature = term.nature.toString();
                Matcher match = STOP_MASK_PATTERN.matcher(w);

                // sentence level
                if (match.matches()) {
                    updateParams(wBasket, freqBasket, coMatrix);
                    totalCnt += wBasket.size();
                    wBasket.clear();
                    freqBasket.clear();
                } else {
                    if (isLegalWord(w, nature)) {
                        if (i == size) {
                            // boost title
                            coMatrix.tf[wordIdxMap.get(w)] += 2;
                        } else {
                            // boost front sentence
                            coMatrix.tf[wordIdxMap.get(w)] += 1 * Math.pow(0.9, i % size);
                        }
                        wBasket.add(wordIdxMap.get(w));
                        if (freqWordsIdxMap.containsKey(w)) {
                            freqBasket.add(freqWordsIdxMap.get(w));
                        }
                    }
                }
            }
            if (!wBasket.isEmpty()) {
                updateParams(wBasket, freqBasket, coMatrix);
                totalCnt += wBasket.size();
            }
        }
        // normalize pg
        if (totalCnt != 0) {
            for (int i = 0; i < freqWordsIdxMap.size(); i++) {
                coMatrix.pg[i] /= totalCnt;
            }
        }
        return coMatrix;
    }

    private Map<String, Integer> freqWordsClustering(CoMatrix coMatrix, Map<String, Integer> freqWordIdxMap) {

        Map<String, Integer> newFreqWordIdxMap = new HashMap<>();
        String[] freqWordArr = new String[freqWordIdxMap.size()];

        for (Map.Entry<String, Integer> entry : freqWordIdxMap.entrySet()) {
            freqWordArr[entry.getValue()] = entry.getKey();
        }

        double[][] matrix = coMatrix.coMatrix;
        boolean hasChange = false;
        if (matrix.length != 0) {
            int dim = matrix.length;
            double[][] freqProbMatrix = new double[matrix[0].length][dim];
            double[] freqCntVec = new double[matrix[0].length];
            int freqWordSize = matrix[0].length;
            Map<Integer, Integer> clusterMap = new HashMap<>();
            // generate prob matrix ...
            for (int i = 0; i < freqWordSize; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    freqProbMatrix[i][j] = matrix[j][i];
                    freqCntVec[i] += matrix[j][i];
                }
                for (int j = 0; j < matrix.length; j++) {
                    freqProbMatrix[i][j] /= freqCntVec[i];
                }
                clusterMap.put(i, i);
            }
            Set<Pair<Integer, Integer>> clusterWidxs = new HashSet<>();
            for (int i = 0; i < freqWordSize; i++) {
                int mainCmpIdx = clusterMap.get(i);
                double[] mainFreqVec = freqProbMatrix[mainCmpIdx];

                for (int j = i + 1; j < freqWordSize; j++) {
                    int cmpIdx = clusterMap.get(j);
                    double[] cmpFreVec = freqProbMatrix[cmpIdx];
                    double jsDist = computeJSDist(mainFreqVec, cmpFreVec);
//                    System.out.println(freqWordArr[i] + "#" + freqWordArr[j] + ":" + jsDist);

                    if (jsDist >= 0 && jsDist < JS_LOWER_BOUND) {
                        clusterWidxs.add(new Pair<>(j, cmpIdx));
                    }
                }

                // 如果发现跟高频词相似分布的词，则将这些词的分布与 该高频词合并，生成新的分布。
                if (!clusterWidxs.isEmpty()) {
                    hasChange = true;
                    System.out.println("merge ...." + clusterWidxs + " " + freqWordArr[mainCmpIdx]);
                    for (Pair<Integer, Integer> pair : clusterWidxs) {
                        System.out.println("merge words = " + freqWordArr[pair.getValue0()] + "\t" + coMatrix.pg[pair.getValue1()]);
                    }
                    for (int j = 0; j < dim; j++) {
                        mainFreqVec[j] *= freqCntVec[mainCmpIdx];
                    }
                    for (Pair<Integer, Integer> pair : clusterWidxs) {
                        int clusterIdx = pair.getValue1();
                        freqCntVec[mainCmpIdx] += freqCntVec[clusterIdx];
                        for (int j = 0; j < dim; j++) {
                            mainFreqVec[j] += freqProbMatrix[clusterIdx][j] * freqCntVec[clusterIdx];
                            freqProbMatrix[clusterIdx][j] = 0;
                        }
                        freqCntVec[clusterIdx] = 0;
                        for (int j = 0; j < dim; j++) {
                            mainFreqVec[j] /= freqCntVec[mainCmpIdx];
                        }
                        clusterMap.put(pair.getValue0(), mainCmpIdx);
                        List<Integer> changeKey = new ArrayList<>();
                        for (Map.Entry<Integer, Integer> entry : clusterMap.entrySet()) {
                            if (entry.getValue() == clusterIdx) {
                                changeKey.add(entry.getKey());
                            }
                        }
                        for (Integer key : changeKey) {
                            clusterMap.put(key, mainCmpIdx);
                        }
                    }
                }
                clusterWidxs.clear();
            }
            if (hasChange) {
                Collection<Integer> values = clusterMap.values();
                Set<Integer> clusterValueSet = new HashSet<>();
                clusterValueSet.addAll(values);
                Map<Integer, Integer> cluster2newIdxMap = new HashMap<>();
                int idx = 0;
                for (int clusterIdx : clusterValueSet) {
                    cluster2newIdxMap.put(clusterIdx, idx);
                    idx += 1;
                }
                for (Map.Entry<Integer, Integer> entry : clusterMap.entrySet()) {
                    newFreqWordIdxMap.put(freqWordArr[entry.getKey()],
                            cluster2newIdxMap.get(entry.getValue()));
                }
                return newFreqWordIdxMap;
            } else {
                return freqWordIdxMap;
            }

        } else {
            return freqWordIdxMap;
        }
    }

    private Map<String, Integer> getWordIdxMap(List<Term> terms) {
        Map<String, Integer> wordIdxMap = new HashMap<>();
        for (Term term : terms) {
            String w = term.word;
            String nature = term.nature.toString();
            Matcher match = STOP_MASK_PATTERN.matcher(w);
            if (!match.matches() && isLegalWord(w, nature) && !wordIdxMap.containsKey(w)) {
                wordIdxMap.put(w, wordIdxMap.size());
            }
        }
        return wordIdxMap;
    }

    private void updateParams(List<Integer> wordBasket, Set<Integer> freqBasket, CoMatrix coMatrix) {
        if (!freqBasket.isEmpty()) {
            for (int wid : wordBasket) {
                for (int freqId : freqBasket) {
                    coMatrix.coMatrix[wid][freqId] += 1;
                }
            }
            Set<Integer> idxSet = new HashSet<>(wordBasket);
            int sLen = wordBasket.size();
            for (int idx : idxSet) {
                coMatrix.nw[idx] += freqBasket.size();
            }
            for (int idx : freqBasket) {
                coMatrix.pg[idx] += sLen;
            }
        }

    }

    private List<Pair<String, Double>> computeChiSquares(CoMatrix coMatrix, String[] wordArr) {
        double[] chiSquares = new double[coMatrix.nw.length];
        for (int i = 0; i < coMatrix.nw.length; i++) {
            double maxSubChi = 0.0f;
            // 没有出现过的词忽略
            if (coMatrix.nw[i] == 0) {
                continue;
            }
            for (int j = 0; j < coMatrix.pg.length; j++) {
                double subChi = Math.pow(coMatrix.coMatrix[i][j] - coMatrix.nw[i] * coMatrix.pg[j], 2) / (coMatrix.nw[i] * coMatrix.pg[j]);
                chiSquares[i] += subChi;
                if (subChi > maxSubChi) {
                    maxSubChi = subChi;
                }
            }
            chiSquares[i] -= maxSubChi;
        }
        List<Pair<String, Double>> rstPairs = new ArrayList<>();
        for (int i = 0; i < chiSquares.length; i++) {
            // tfidf * sqrt(chi-square)
            rstPairs.add(new Pair<>(wordArr[i], (Math.sqrt(chiSquares[i]) + 1e-5) * coMatrix.tf[i] * mWordIDFMap.getOrDefault(wordArr[i], defaultIdf)));
        }
        rstPairs.sort((it1, it2) -> it2.getValue1().compareTo(it1.getValue1()));
        return rstPairs;
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private int maxFreqNum = 300;
        private float freqRate = FREQ_RATIO;
        private int freqLowerBound = 2;
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

        public ChiSquareKeyword build() {
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
            return new ChiSquareKeyword(stopWords, maxFreqNum, freqRate, freqLowerBound, idfWeight, defaultIdf);
        }
    }
}
