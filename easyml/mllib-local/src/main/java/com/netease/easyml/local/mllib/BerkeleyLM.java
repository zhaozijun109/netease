package com.netease.easyml.local.mllib;

import com.google.common.io.Files;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.MathUtil;
import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.collections.BoundedList;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.LmReaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by linjiuning on 2019/6/19.
 * Based on gloverli's code
 */
public class BerkeleyLM {
    private static final Logger log = LoggerFactory.getLogger(BerkeleyLM.class);
    private ArrayEncodedProbBackoffLm<String> lm;
    private boolean segment;

    private WordIndexer<String> wordIndexer;
    private int unkIndex;

    private BerkeleyLM(ArrayEncodedProbBackoffLm<String> lm, boolean segment) {
        this.lm = lm;
        this.segment = segment;
        wordIndexer = lm.getWordIndexer();
        unkIndex = wordIndexer.getIndexPossiblyUnk(wordIndexer.getUnkSymbol());
    }

    public static void train(int lmOrder, List<String> inputs, String output) {
        if (inputs.isEmpty()) inputs.add("-");
        StringWordIndexer wordIndexer = new StringWordIndexer();
        wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
        wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
        wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
        LmReaders.createKneserNeyLmFromTextFiles(inputs, wordIndexer, lmOrder, new File(output), new ConfigOptions());
    }

    public double predict(List<String> tokens) {
        return predict(tokens, false);
    }

    public double fluency(List<String> tokens) {
        if (tokens.isEmpty())
            return 1.0;
        double mean = predict(tokens, true);
        return 1.0 / (1.0 - mean);
    }

    public double predict(List<String> tokens, boolean mean) {
        if (segment)
            tokens = segment(tokens);
        List<String> sentenceWithBounds = new BoundedList<>(tokens, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());
        int lmOrder = lm.getLmOrder();
        float sentenceScore = 0.0F;

        int i;
        List<String> ngram;
        float scoreNgram;
        int cnt = 0;
        for (i = 1; i < lmOrder - 1 && i <= sentenceWithBounds.size() + 1; ++i) {
            ngram = sentenceWithBounds.subList(-1, i);
            scoreNgram = lm.getLogProb(ngram);
            sentenceScore += scoreNgram;
            cnt += 1;
        }

        for (i = lmOrder - 1; i < sentenceWithBounds.size() + 2; ++i) {
            ngram = sentenceWithBounds.subList(i - lmOrder, i);
            scoreNgram = lm.getLogProb(ngram);
            sentenceScore += scoreNgram;
            cnt += 1;
        }

        return mean ? MathUtil.safeDiv(sentenceScore, cnt) : sentenceScore;
    }

    private List<String> segment(List<String> tokens) {
        List<String> words = new ArrayList<>();
        for (String word : tokens) {
            if (wordIndexer.getIndexPossiblyUnk(word) == unkIndex) {
                List<String> dictSegments = dictSeg(word);
                words.addAll(dictSegments);
            } else {
                words.add(word);
            }
        }
        return words;
    }

    private List<String> dictSeg(String s) {
        if (s.length() < 2)
            return Collections.singletonList(s);
        List<String> forwordSegments = forwordDictSeg(s);
        List<String> backwordSegments = backwordDictSeg(s);
        if (backwordSegments.size() < forwordSegments.size())
            return backwordSegments;
        return forwordSegments;
    }

    private List<String> forwordDictSeg(String s) {
        List<String> result = new ArrayList<>();
        int begin = 0;
        int end = s.length();

        while (begin < end) {
            for (; end > begin; end--) {
                String is = s.substring(begin, end);
                if (is.length() > 1) {
                    if (wordIndexer.getIndexPossiblyUnk(is) != unkIndex) {
                        result.add(is);
                        begin = end;
                        end = s.length();
                        break;
                    }
                } else {
                    result.add(is);
                    begin = end;
                    end = s.length();
                    break;
                }
            }
        }
        return result;
    }

    private List<String> backwordDictSeg(String s) {
        List<String> result = new ArrayList<>();
        int begin = 0;
        int end = s.length();
        while (begin < end) {
            for (; begin < end; begin++) {
                String is = s.substring(begin, end);
                if (is.length() > 1) {
                    if (wordIndexer.getIndexPossiblyUnk(is) != unkIndex) {
                        result.add(is);
                        begin = 0;
                        end -= is.length();
                        break;
                    }
                } else {
                    result.add(is);
                    begin = 0;
                    end -= is.length();
                    break;
                }
            }
        }
        Collections.reverse(result);
        return result;
    }

    public static class Builder {
        private String modelFile;
        private float unknownWordLogProb = 0.0f;
        private boolean compress = false;
        private boolean segment = true;

        public String getModelFile() {
            return modelFile;
        }

        public Builder setModelFile(String modelFile) {
            this.modelFile = modelFile;
            return this;
        }

        public float getUnknownWordLogProb() {
            return unknownWordLogProb;
        }

        public Builder setUnknownWordLogProb(float unknownWordLogProb) {
            this.unknownWordLogProb = unknownWordLogProb;
            return this;
        }

        public boolean isCompress() {
            return compress;
        }

        public Builder setCompress(boolean compress) {
            this.compress = compress;
            return this;
        }

        public boolean isSegment() {
            return segment;
        }

        public Builder setSegment(boolean segment) {
            this.segment = segment;
            return this;
        }

        public BerkeleyLM build() {
            String tmpModelFile = modelFile;
            if (IOUtil.isHdfs(modelFile)) {
                tmpModelFile = IOUtil.join(Files.createTempDir().getAbsolutePath(), IOUtil.baseName(modelFile));
                byte[] bytes = IOUtil.readAllBytes(modelFile);
                log.info(String.format("Copy from %s to %s", modelFile, tmpModelFile));
                IOUtil.writeBytes(tmpModelFile, bytes);
            }

            File lmFile = new File(tmpModelFile);
            ConfigOptions configOptions = new ConfigOptions();
            configOptions.unknownWordLogProb = unknownWordLogProb;
            ArrayEncodedProbBackoffLm<String> lm = LmReaders.readArrayEncodedLmFromArpa(lmFile.getPath(), compress, new StringWordIndexer(), configOptions,
                    Integer.MAX_VALUE);

            BerkeleyLM berkeleyLM = new BerkeleyLM(lm, segment);
            if (!tmpModelFile.equals(modelFile)) {
                IOUtil.delete(tmpModelFile);
            }
            return berkeleyLM;
        }
    }
}
