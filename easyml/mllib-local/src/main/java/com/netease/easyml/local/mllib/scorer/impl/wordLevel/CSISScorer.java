package com.netease.easyml.local.mllib.scorer.impl.wordLevel;

import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xinfengli on 2018/06/27.
 * <p>
 * CSIS (Cross Sentence Informational Subsumption): If the information content of sentence a (denoted as I(a))
 * is contained within sentence b, then a becomes informationally redundant and the content of b is said to subsume
 * that of a.
 * <p>
 * For the details, please refer the following paper:
 * A common theory of information fusion from multiple text sources, step one: Cross-document structure. In Proceedings,
 * 1st ACL SIGDIAL Workshop on Discourse and Dialogue, Hong Kong, October 2000.
 * <p>
 * In short:
 * R = 2 * (#overlapping words) / (#words in sentence1 + #words in sentence2)
 */
public class CSISScorer implements IScorer<List<String>> {

    @Override
    public double getScores(List<String> wordList1, List<String> wordList2) {
        if (wordList1 == null || wordList2 == null) {
            throw new NullPointerException("CSISScorer Null Pointer Exception");
        }
        if (wordList1.isEmpty() || wordList2.isEmpty()) {
            return 0.0;
        }

        // copy the old word list for backing up
        List<String> oldWordList1 = new ArrayList<>();
        oldWordList1.addAll(wordList1);

        List<String> oldWordList2 = new ArrayList<>();
        oldWordList2.addAll(wordList2);

        int n1 = wordList1.size();
        int n2 = wordList2.size();
        List<String> commonWordList = new ArrayList<>();

        while (!oldWordList1.isEmpty() && !oldWordList2.isEmpty()) {
            String curWord = oldWordList1.get(0);
            if (oldWordList2.contains(curWord)) {
                oldWordList1.remove(curWord);
                oldWordList2.remove(curWord);
                commonWordList.add(curWord);
            } else {
                oldWordList1.remove(curWord);
            }
        }

        //if (commonWordList.equals(oldWordList1) || commonWordList.equals(oldWordList2)) {
        //    return 1.0;
        //}

        int n = commonWordList.size();
        return 2 * (double) n / (double) (n1 + n2);
    }

    public static CSISScorer newInstance() {
        return new CSISScorer();
    }
}
