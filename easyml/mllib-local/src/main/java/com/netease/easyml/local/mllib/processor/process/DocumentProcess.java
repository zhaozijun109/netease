package com.netease.easyml.local.mllib.processor.process;


import com.netease.easyml.local.mllib.bean.Document;

/**
 * Created by eddielin on 2019/3/1.
 */
@FunctionalInterface
public interface DocumentProcess {
    void process(Document document);
}
