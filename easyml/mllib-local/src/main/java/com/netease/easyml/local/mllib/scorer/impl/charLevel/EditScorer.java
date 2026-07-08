package com.netease.easyml.local.mllib.scorer.impl.charLevel;

import com.netease.easyml.local.mllib.Utils;
import com.netease.easyml.local.mllib.scorer.impl.IScorer;

/**
 * Created by linjiuning on 2018/7/30.
 */
public class EditScorer implements IScorer<String> {
    private static EditScorer SCORER;

    @Override
    public double getScores(String s1, String s2) {
        if (s1.length() == 0 || s2.length() == 0)
            return 0.0;
        int edits[][] = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++)
            edits[i][0] = i;
        for (int j = 1; j <= s2.length(); j++)
            edits[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int u = (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1);
                edits[i][j] = Math.min(
                        edits[i - 1][j] + 1,
                        Math.min(
                                edits[i][j - 1] + 1,
                                edits[i - 1][j - 1] + u
                        )
                );
            }
        }
        int dist = edits[s1.length()][s2.length()];
        return 1 - Utils.f1(s1.length(), s2.length(), dist);
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

