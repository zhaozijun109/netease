package com.netease.easyml.local.mllib.scorer.impl.wordLevel;

import com.netease.easyml.local.mllib.Utils;
import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.List;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class EditScorer implements IScorer<List<String>> {
    private static EditScorer SCORER;

    @Override
    public double getScores(List<String> s1, List<String> s2) {
        if (s1.size() == 0 || s2.size() == 0)
            return 0.0;
        int edits[][] = new int[s1.size() + 1][s2.size() + 1];
        for (int i = 0; i <= s1.size(); i++)
            edits[i][0] = i;
        for (int j = 1; j <= s2.size(); j++)
            edits[0][j] = j;
        for (int i = 1; i <= s1.size(); i++) {
            for (int j = 1; j <= s2.size(); j++) {
                int u = (s1.get(i - 1).equals(s2.get(j - 1)) ? 0 : 1);
                edits[i][j] = Math.min(
                        edits[i - 1][j] + 1,
                        Math.min(
                                edits[i][j - 1] + 1,
                                edits[i - 1][j - 1] + u
                        )
                );
            }
        }
        int dist = edits[s1.size()][s2.size()];
        return 1 - Utils.f1(s1.size(), s2.size(), dist);
    }

    public static EditScorer getInstance() {
        if (SCORER == null) {
            synchronized (EditScorer.class) {
                SCORER = new EditScorer();
            }
        }
        return SCORER;
    }
}
