package com.netease.easyml.local.mllib.keyword;

import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.Constant;
import com.netease.easyml.common.util.MathUtil;
import com.netease.easyml.common.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eddielin on 2019/3/27.
 */
public class Keywords {

    public static String[] stopPos = new String[]{
            "w", "wb", "wd", "wf", "wh", "wj", "wky", "wkz", "wm", "wn",
            "wp", "ws", "wt", "ww", "wyy", "wyz", "xx", "y", "yg", "vyou",
            "vshi", "vg", "uzhi", "uzhe", "uz", "uyy", "uv", "usuo", "uls", "ulian",
            "ule", "uguo", "ug", "udh", "udeng", "ude1", "ude2", "ude3", "q", "qg",
            "qt", "qv", "pbei", "pba", "p", "o", "m", "mg", "Mg", "mq",
            "end", "e", "dl", "dg", "begin",
            "c", "cc", "r" // add by showcai
    };
    public static String[] bannedCandidatePos = new String[]{
            "u", "p", "j", "d", "k", "c", "x", "a", "m", /*"nx",*/
            "zg", "z", "yg", "y", "xx", "xu", "wyz", "wyy", "ww", "wt", "ws",
            "wp", "wn", "wm", "wkz", "wky", "wj", "wh", "wf", "wd", "wb", "w", /*"ns",*/
            "vyou", "vx", "vshi", "vg", "uv", "uls", "ulian", "uj", "udh", "udeng", "uyy",
            "ud", "u", "tg", "rys", "rzv", "rzt", "rzs", "rz", "ryv", "ryt", "ry", "rr",
            "Rg", "rg", "r", "qv", "qt", "p", "nic", "ng", "Mg", "mg", "i", "h", "o",
            "f", "e", "dl", "dg", "d", "cc", "c", "bl", "bg", "al", "ag", "s", "mq", "ad", "v"
    }; // a,v


    public static Set<String> mStopPos = new HashSet<>(Arrays.asList(stopPos));
    public static Set<String> mBannedCandidatePos = new HashSet<>(Arrays.asList(bannedCandidatePos));

    public static List<Term> removeStopTerms(Set<String> stopwords, List<Term> rawTerms) {
        List<Term> cleanTerms = new ArrayList<>();
        for (Term t : rawTerms) {
            String flag = t.nature.toString();
            String word = t.word;
            if (!mStopPos.contains(flag) && (stopwords == null || !stopwords.contains(word)) && !StringUtil.isNumeric(word)) {
                cleanTerms.add(t);
            }
        }
        return cleanTerms;
    }

    public static Map<String, Double> normalize(Map<String, Double> score) {
        Map<String, Double> normScore = new LinkedHashMap<>();
        double sum = 0.0;
        for (Map.Entry<String, Double> entry : score.entrySet())
            sum += entry.getValue();
        if (MathUtil.isZero(sum))
            return score;
        for (Map.Entry<String, Double> entry : score.entrySet())
            normScore.put(entry.getKey(), entry.getValue() / sum);
        return normScore;
    }

    public static List<Term> clean(List<Term> terms) {
        List<Term> candidateTerms = new ArrayList<>();
        for (Term t : terms) {
            String word = t.word;
            String flag = t.nature.toString();
            if (!mStopPos.contains(flag) && !StringUtil.isNumeric(word) && word.length() > 1 && !mBannedCandidatePos.contains(flag)) {
                candidateTerms.add(t);
            }
        }
        return candidateTerms;
    }

    /**
     * Generate candidate terms from cleanTerms
     */
    public static List<Term> genCandidateTerms(List<Term> cleanTerms) {
        List<Term> candidateTerms = new ArrayList<>();
        for (Term t : cleanTerms) {
            String word = t.word;
            String flag = t.nature.toString();
            if (word.length() > 1 && !mBannedCandidatePos.contains(flag)) {
                candidateTerms.add(t);
            }
        }
        return candidateTerms;
    }

    public static Map<String, Double> getTopK(Map<String, Double> totKeywords, int topK) {
        if (totKeywords.size() <= topK || topK <= 0) return totKeywords;

        // truncate topK words
        int n = 0;
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : totKeywords.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
            n += 1;
            if (n >= topK) break;
        }
        return result;
    }

    public static List<Term> genCandidateTerms(Set<String> stopWords, List<Term> terms) {
        List<Term> cleanTerms = removeStopTerms(stopWords, terms);
        List<Term> candidateTerms = genCandidateTerms(cleanTerms);
        return candidateTerms;
    }

    public static List<String> genCandidate(Set<String> stopWords, List<Term> terms) {
        List<Term> candidateTerms = genCandidateTerms(stopWords, terms);
        return candidateTerms.stream().map(it -> it.word).collect(Collectors.toList());
    }

    public static String format(Map<String, Double> scores) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if (i > 0)
                sb.append(", ");
            sb.append(entry.getKey()).append(" = ").append(entry.getValue());
            i++;
        }
        return sb.toString();
    }

    public static String cleanStr(String text) {
        text = text.trim();
        text = text.replaceAll(Constant.URL_REGEX, " url ");
        text = StringUtil.fullToHalf(text); // full-width to half-width
        text = text.toLowerCase();                  // uppercase to lowercase

        // remove non-Chinese and non-English characters, including Japanese, Korean, and so on.
        text = text.replaceAll("[^a-zA-Z0-9\\u4E00-\\u9FFF\\u3400-\\u4DBF\\uF900-\\uFAFF,.?!。;:~()<>\\[\\]#]", " ");
        text = text.replaceAll("[.。]{3,}", " ");
        text = text.replaceAll("[?？]{2,}", "?");
        text = text.replaceAll("[!！]{2,}", "!");
        text = text.replaceAll("[;；]{2,}", ";");
        text = text.replaceAll("\\s{2,}", " ");
        return text.trim();
    }
}
