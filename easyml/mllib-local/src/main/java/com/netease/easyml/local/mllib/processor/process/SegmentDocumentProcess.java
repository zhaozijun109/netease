package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by eddielin on 2019/3/6.
 * 基于Hanlp分词
 */
@NoArgsConstructor
@AllArgsConstructor
public class SegmentDocumentProcess implements DocumentProcess {
    private Segment segment;

    @Override
    public void process(Document document) {
        List<Sentence> sentences = new ArrayList<>();
        if (document.getTitle() != null)
            sentences.add(document.getTitle());
        sentences.addAll(document.getTextSentences());
        for (Sentence sentence : sentences) {
            String origin = sentence.getOrigin();
            List<Term> terms;
            if (segment != null)
                terms = segment.seg(origin);
            else
                terms = HanLP.segment(origin);
            sentence.setTerms(terms);

            String clean = sentence.getClean();
            if (!Objects.equals(origin, clean)) {
                if (segment != null)
                    terms = segment.seg(clean);
                else
                    terms = HanLP.segment(clean);
                sentence.setCleanTerms(terms);
            }
        }
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private Segment segment;

        public SegmentDocumentProcess build() {
            return new SegmentDocumentProcess(segment);
        }
    }
}
