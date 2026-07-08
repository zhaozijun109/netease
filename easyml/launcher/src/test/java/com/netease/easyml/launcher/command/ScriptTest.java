package com.netease.easyml.launcher.command;

import com.netease.easyml.launcher.EasyMLContext;
import org.junit.Test;

/**
 * Created by linjiuning on 2020/9/4.
 */
public class ScriptTest {
    private final Script script = new Script();

    @Test
    public void runWithClass() throws Exception {
        EasyMLContext context = EasyMLContext.builder().master("local[4]").getOrCreate();
        String path = "script/ExampleUDS.scala";
        script.setScript(path);
        script.setArgs("10,1");
        script.run(context);
    }

    @Test
    public void runWithError() throws Exception {
        EasyMLContext context = EasyMLContext.builder().master("local[4]").getOrCreate();
        String path = "script/ExampleErrorUDS.scala";
        script.setScript(path);
        script.setArgs("10");
        script.run(context);
    }

    @Test
    public void run() throws Exception {
        EasyMLContext context = EasyMLContext.builder().master("local[4]").getOrCreate();
        String path = "script/Script.scala";
        script.setScript(path);
        script.setArgs("");
        script.run(context);
    }

    @Test
    public void runMultiScript() throws Exception {
        EasyMLContext context = EasyMLContext.builder().master("local[4]").getOrCreate();
        String path = "script/ExampleUDS.scala,script/Script.scala,";
        script.setScript(path);
        script.setArgs("10");
        script.run(context);
    }

    @Test
    public void parseStatus() {
        String output =
                "scala>      |      |      |      |      | __status__: String =\n" +
                        "\"Job aborted due to stage failure:\n" +
                        "Aborting TaskSet 1.0 because task 3 (partition 3)\n" +
                        "cannot run anywhere due to node and executor blacklist.\n" +
                        "Most recent failure:\n" +
                        "Lost task 3.0 in stage 1.0 (TID 4, hadoop4256.jd.163.org, executor 1): java.lang.IllegalArgumentException: Field \"rank_id\" does not exist.\n" +
                        "Available fields: user_id, eid, source, attr_set, model_id, validation_id, attr_value, his_gender, music_gender, music_birth_y, yanxuan_gender, yanxuan_birth_y, nim_gender, nim_birth_y, blog_gender, blog_birth_y, edu_gender, edu_birth_y, yx_gender, yx_birth_y, fin_gender, fin_birth_y, kaola_gender_score, kaola_birth_y, urs_gender_score, urs_birth_y, music_first_delta, music_last_delta, lofter_first_delta, lofter_last_delta, yanxuan_first_delta, nim_first_delta, edu_first...\n" +
                        "scala> \n" +
                        "scala> :quit";
        String status = Script.getStatus(output);
        System.out.println(status);
        String error = Script.getConsoleError(output);
        System.out.println(error);
    }
}