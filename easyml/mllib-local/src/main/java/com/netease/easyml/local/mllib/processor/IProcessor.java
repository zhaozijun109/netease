package com.netease.easyml.local.mllib.processor;

import java.util.List;

/**
 * Created by eddielin on 2018/6/8.
 */
public interface IProcessor {
    String process(String text);

    List<String> process(List<String> tokens);
}
