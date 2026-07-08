package com.netease.easyml.launcher.command;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.launcher.EasyMLContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.HashMap;
import java.util.Map;

import static com.netease.easyml.launcher.Constant.ENV;

/**
 * Created by linjiuning on 2020/7/6.
 */
public interface SubCommand {
    void addOptions(Options args);

    void parse(CommandLine args);

    void run(EasyMLContext context) throws Exception;

    Map<String, String> env();

    default Map<String, String> env(Params params) {
        Map<String, Object> map = params.get(ENV, new HashMap<>());
        Map<String, String> res = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            res.put(entry.getKey(), entry.getValue().toString());
        }
        return res;
    }
}
