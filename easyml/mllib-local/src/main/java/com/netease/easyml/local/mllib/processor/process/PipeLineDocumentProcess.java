package com.netease.easyml.local.mllib.processor.process;

import com.netease.easyml.local.mllib.bean.Document;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Created by eddielin on 2019/3/1.
 */
@AllArgsConstructor
public class PipeLineDocumentProcess implements DocumentProcess {
    private List<DocumentProcess> processes;

    @Override
    public void process(Document document) {
        processes.forEach(it -> it.process(document));
    }
}
