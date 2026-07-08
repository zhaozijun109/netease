package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.Constant;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by eddielin on 2019/3/28.
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class RuleAutoPunctDocumentProcess implements DocumentProcess {
    private static final String IGNORE_PUNCT = Constant.START_CLOSE + Constant.END_CLOSE + "、\"“”《》\\/ \t";
    private int crossImage = 0;

    private List<Term> clean(List<Term> terms) {
        List<Term> nTerms = new ArrayList<>();
        for (Term term : terms) {
            if (nTerms.isEmpty() || !isPunct(term)) {
                nTerms.add(term);
                continue;
            }
            Term last = nTerms.get(nTerms.size() - 1);
            if (!isPunct(last))
                nTerms.add(term);
        }
        return nTerms;
    }

    @Override
    public void process(Document document) {
        List<Sentence> sentences = document.getSentences();

        for (int i = 0; i < sentences.size() - 1; i++) {
            Sentence cur = sentences.get(i);
            if (!Objects.equals(cur.getType(), SentenceType.TEXT)) {
                continue;
            }

            List<Term> terms = cur.getTerms();
            terms = clean(terms);
            if (terms.isEmpty())
                continue;
            Term last = terms.get(terms.size() - 1);
            if (isPunct(last))
                continue;

            int j = i + 1;
            while (j < sentences.size() &&
                    (Objects.equals(cur.getType(), SentenceType.IMAGE) || Objects.equals(cur.getType(), SentenceType.IMGTITLE))) {
                if (Objects.equals(cur.getType(), SentenceType.IMAGE))
                    j++;
            }

            if (j - i - 1 > crossImage) {
                log.debug("Cross: " + cur.getOrigin() + ", punct: 。");
                cur.getTerms().add(new Term("。", Nature.w));
                cur.setOrigin(cur.getOrigin() + "。");
                continue;
            }

            Sentence next = sentences.get(j);
            if (Objects.equals(next.getType(), SentenceType.SUBTITLE)) {
                log.debug("Cross: " + cur.getOrigin() + ", punct: 。");
                cur.getTerms().add(new Term("。", Nature.w));
                cur.setOrigin(cur.getOrigin() + "。");
                continue;
            }

            if (next.getStartId().getOffset() > cur.getEndId().getOffset() + 1) {
                log.debug("Cross: " + cur.getOrigin() + ", punct: 。");
                cur.getTerms().add(new Term("。", Nature.w));
                cur.setOrigin(cur.getOrigin() + "。");
                continue;
            }

            j = terms.size() - 1;
            while (j >= 0 && !isPunct(terms.get(j)))
                j--;

            List<Term> nextTerms = next.getTerms();
            if (nextTerms.isEmpty())
                continue;
            nextTerms = clean(nextTerms);
            int cnt = 0;
            for (Term term : nextTerms) {
                if (isPunct(term))
                    cnt++;
            }
            if ((terms.size() - j > 3) /*&& ruleMergeProc.isLegalStart(next)*/ &&
                    ((nextTerms.size() > 10 && isPunct(nextTerms.get(nextTerms.size() - 1))) ||
                            (next.getOrigin().length() > 5 && next.getOrigin().length() > cur.getOrigin().length() && cnt >= 2))) {
                log.debug("Pair: " + cur.getOrigin() + " " + next.getOrigin() + ", punct: 。");
                cur.getTerms().add(new Term("。", Nature.w));
                cur.setOrigin(cur.getOrigin() + "。");
            }
        }
    }

    private boolean isPunct(Term term) {
        return term.nature.toString().startsWith("w") && !IGNORE_PUNCT.contains(term.word);
    }
}
