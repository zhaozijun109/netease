package com.netease.easyml.local.mllib.processor.process;

import com.google.common.primitives.Ints;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by eddielin on 2019/3/1.
 * 对句子或段落分组
 */
@AllArgsConstructor
public class ChunkDocumentProcess implements DocumentProcess {
    private int num;
    private int wordNum;
    private int minLen;
    private boolean cross;

    @Override
    public void process(Document document) {
        if (document.size() <= 1)
            return;
        List<Integer> chunk = new ArrayList<>();
        List<Integer> len = new ArrayList<>();
        int sn = 0;
        int wn = 0;
        Sentence last = null;
        for (int i = 0; i < document.size(); i++) {
            Sentence cur = document.getSentences().get(i);
            if (!Objects.equals(cur.getType(), SentenceType.TEXT))
                continue;
            if (last != null &&
                    ((num > 0 && sn >= num) || (wordNum > 0 && wn >= wordNum)
                            || (!cross && cur.getStartId().getParaId() > last.getEndId().getParaId()))) {
                chunk.add(i);
                len.add(wn);
                sn = 0;
                wn = 0;
            }
            last = cur;

            sn += 1;
            wn += cur.length();
        }
        chunk.add(document.size());
        len.add(wn);
        if (minLen > 0) {
            List<Integer> tmp = new ArrayList<>();
            int i = chunk.size() - 1;
            while (i >= 0) {
                int tlen = len.get(i);
                int tidx = chunk.get(i);
                i--;
                while (i >= 0 && tlen < minLen) {
                    tlen += chunk.get(i);
                }
                if (tidx < document.size())
                    tmp.add(0, tidx);
            }
            chunk = tmp;
        }
        int[] ints = Ints.toArray(chunk);
        document.setChunk(ints);
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private int num;
        private int wordNum;
        private int minLen = 0;
        private boolean cross = false;

        public ChunkDocumentProcess build() {
            return new ChunkDocumentProcess(num, wordNum, minLen, cross);
        }
    }
}
