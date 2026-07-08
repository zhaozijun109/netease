package com.netease.easyml.common.util;


import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2020/7/8.
 */
public class SequenceLabeling {

//    public static List<Pair<String, Pair<Integer, Integer>>> getEntities(List<String> seq) {
//        return getEntities(seq, false);
//    }

    /**
     * Gets entities from sequence.
     * <p>
     * Args:
     * seq (list): sequence of labels.
     * <p>
     * Returns:
     * list: list of (chunk_type, chunk_start, chunk_end).
     * <p>
     * Example:
     * >>> seq = ['B-PER', 'I-PER', 'O', 'B-LOC']
     * >>> get_entities(seq)
     * [('PER', 0, 1), ('LOC', 3, 3)]
     */
    public static List<Pair<String, Pair<Integer, Integer>>> getEntities(List<String> seq, boolean suffix) {
        String prevTag = "O";
        String prevType = "";
        int beginOffset = 0;
        List<Pair<String, Pair<Integer, Integer>>> chunks = new ArrayList<>();
        for (int i = 0; i < seq.size() + 1; i++) {
            String chunk = i < seq.size() ? seq.get(i) : "O";
            String tag = "";
            String type = "";
            String[] split = chunk.split("-");
            if (suffix) {
                tag = chunk.substring(chunk.length() - 1);
                type = split[0];
            } else {
                tag = chunk.substring(0, 1);
                type = split[split.length - 1];
            }

            if (endOfChunk(prevTag, tag, prevType, type)) {
                chunks.add(new Pair<>(prevType, new Pair<>(beginOffset, i - 1)));
            }
            if (startOfChunk(prevTag, tag, prevType, type)) {
                beginOffset = i;
            }
            prevTag = tag;
            prevType = type;
        }

        return chunks;
    }

    /**
     * Checks if a chunk ended between the previous and current word.
     * <p>
     * Args:
     * prevTag: previous chunk tag.
     * tag: current chunk tag.
     * prevType: previous type.
     * type: current type.
     * <p>
     * Returns:
     * chunkEnd: boolean.
     **/
    public static boolean endOfChunk(String prevTag, String tag, String prevType, String type) {
        boolean chunkEnd = false;

        if (prevTag.equals("E")) chunkEnd = true;
        if (prevTag.equals("S")) chunkEnd = true;

        if (prevTag.equals("B") && tag.equals("B")) chunkEnd = true;
        if (prevTag.equals("B") && tag.equals("S")) chunkEnd = true;
        if (prevTag.equals("B") && tag.equals("O")) chunkEnd = true;
        if (prevTag.equals("I") && tag.equals("B")) chunkEnd = true;
        if (prevTag.equals("I") && tag.equals("S")) chunkEnd = true;
        if (prevTag.equals("I") && tag.equals("O")) chunkEnd = true;

        if (!prevTag.equals("O") && !prevTag.equals(".") && !prevType.equals(type))
            chunkEnd = true;

        return chunkEnd;
    }

    /**
     * Checks if a chunk started between the previous and current word.
     * <p>
     * Args:
     * prevTag: previous chunk tag.
     * tag: current chunk tag.
     * prevType: previous type.
     * type: current type.
     * <p>
     * Returns:
     * chunkStart: boolean.
     **/
    public static boolean startOfChunk(String prevTag, String tag, String prevType, String type) {
        boolean chunkStart = false;

        if (tag.equals("B")) chunkStart = true;
        if (tag.equals("S")) chunkStart = true;

        if (prevTag.equals("E") && tag.equals("E")) chunkStart = true;
        if (prevTag.equals("E") && tag.equals("I")) chunkStart = true;
        if (prevTag.equals("S") && tag.equals("E")) chunkStart = true;
        if (prevTag.equals("S") && tag.equals("I")) chunkStart = true;
        if (prevTag.equals("O") && tag.equals("E")) chunkStart = true;
        if (prevTag.equals("O") && tag.equals("I")) chunkStart = true;

        if (!tag.equals("O") && !tag.equals(".") && !prevType.equals(type))
            chunkStart = true;

        return chunkStart;
    }
}
