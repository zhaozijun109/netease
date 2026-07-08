package com.netease.easyml.local.mllib.processor.process;

import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.processor.cleaner.ICleaner;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by eddielin on 2019/3/6.
 * 基于规则清洗句子
 */
@Slf4j
@AllArgsConstructor
public class RuleCleanDocumentProcess implements DocumentProcess {
    private ICleaner cleaner;
    private String repl;
    private boolean inplace;

    @Override
    public void process(Document document) {
        for (Sentence sentence : document.getTextSentences()) {
            String cleanBefore = sentence.getClean();
            String clean = cleaner.replace(repl, cleanBefore);
            sentence.setClean(clean);
            if (inplace) {
                if (!cleanBefore.equals(clean))
                    log.debug(String.format("Origin: %s -> %s", sentence.getOrigin(), clean));
                sentence.setOrigin(clean);
            }
        }
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private ICleaner cleaner;
        private String repl = "";
        private boolean inplace = false;

        public RuleCleanDocumentProcess build() {
            return new RuleCleanDocumentProcess(cleaner, repl, inplace);
        }
    }
}
