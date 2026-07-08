package com.netease.easyml.local.mllib.processor.process;

import com.netease.easyml.common.util.SortUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.bert.BertMaskLM;
import com.netease.easyml.local.mllib.bert.Constant;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by eddielin on 2019/2/25.
 */
@Slf4j
public class AutoPunct {
    private BertMaskLM bertMaskLM;
    private String[] punct;

    @Setter
    private double threshold;

    public AutoPunct(BertMaskLM bertMaskLM, String[] punct, double threshold) {
        this.bertMaskLM = bertMaskLM;
        this.punct = punct;
        this.threshold = threshold;
    }

    public List<List<Map<String, Double>>> transformWithProb(List<List<String>> examples) {
        String[] seqs = new String[examples.size()];
        for (int i = 0; i < examples.size(); i++) {
            String concat = StringUtil.join(examples.get(i), Constant.MASK_PH);
            seqs[i] = concat;
        }

        double[][][] predict = bertMaskLM.predict(seqs, punct);

        List<List<Map<String, Double>>> result = new ArrayList<>();
        for (double[][] doubles : predict) {
            List<Map<String, Double>> hosts = new ArrayList<>();
            if (doubles != null) {
                for (double[] probs : doubles) {
                    Map<String, Double> token2probs = new HashMap<>();
                    for (int k = 0; k < probs.length; k++) {
                        double sc = probs[k];
                        token2probs.put(punct[k], sc);
                    }
                    token2probs = SortUtil.sortByValueDesc(token2probs);
                    hosts.add(token2probs);
                }
            }
            result.add(hosts);
        }
        return result;
    }

    public List<List<String>> transform(List<List<String>> examples) {
        List<List<Map<String, Double>>> allProbs = transformWithProb(examples);

        List<List<String>> result = new ArrayList<>();

        for (List<Map<String, Double>> allMaskProbs : allProbs) {
            List<String> hosts = new ArrayList<>();
            for (Map<String, Double> probs : allMaskProbs) {
                double maxProb = 0.0;
                String host = "";
                if (!probs.isEmpty()) {
                    Iterator<Map.Entry<String, Double>> iterator = probs.entrySet().iterator();
                    Map.Entry<String, Double> next = iterator.next();
                    host = next.getKey();
                    maxProb = next.getValue();
                }
                log.debug(String.format("Max punct: %s, prob: %.4f", host, maxProb));
                if (maxProb < threshold) {
                    host = "";
                }
                hosts.add(host);
            }
            result.add(hosts);
        }
        return result;
    }

    public static String join(List<String> sentences, List<String> puncts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentences.size(); i++) {
            sb.append(sentences.get(i));
            if (i < puncts.size())
                sb.append(puncts.get(i));
        }
        return sb.toString();
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private BertMaskLM bertMaskLM;
        private double threshold = 0.01;
        private String punct = ",，.。?？!！:：";

        public AutoPunct build() {
            String[] _punct = new String[punct.length()];
            for (int i = 0; i < punct.length(); i++)
                _punct[i] = String.valueOf(punct.charAt(i));
            return new AutoPunct(bertMaskLM, _punct, threshold);
        }
    }
}
