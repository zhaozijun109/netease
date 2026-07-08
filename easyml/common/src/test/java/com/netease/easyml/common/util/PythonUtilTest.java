package com.netease.easyml.common.util;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by linjiuning on 2020/8/4.
 */
public class PythonUtilTest {

    @Test
    public void unpickle() throws IOException {
        String path = "/Users/linjiuning/workspace/git/netease/py_scripts/gender/data/model.pkl";
        Object obj = PythonUtil.unpickle(path);
        System.out.println(obj);
    }
}