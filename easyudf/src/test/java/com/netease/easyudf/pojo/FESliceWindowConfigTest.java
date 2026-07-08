package com.netease.easyudf.pojo;

import com.netease.easyml.common.util.IOUtil;
import org.junit.Test;

import java.util.Arrays;

public class FESliceWindowConfigTest {

    @Test
    public void sql() {
//        FESliceWindowConfig config = new FESliceWindowConfig();
//        config.setEntity(Arrays.asList(
//                new FESliceWindowConfig.Expr("user_id, item_id", "ui"),
//                new FESliceWindowConfig.Expr("blog_id", "b")
//        ));
//        config.setWindow(Arrays.asList(
//                new FESliceWindowConfig.Expr("day>='2023-03-09'", "before3"),
//                new FESliceWindowConfig.Expr("day>='2023-03-05'", "before7")
//        ));
//        config.setTarget(Arrays.asList(
//                new FESliceWindowConfig.Expr("click_count", "click"),
//                new FESliceWindowConfig.Expr("exposed_count", "exposed")
//        ));
//        config.setAgg(Arrays.asList(
//                new FESliceWindowConfig.Expr("sum(?)", "cnt"),
//                new FESliceWindowConfig.Expr("avg", "avg")
//        ));
//        config.setApply(Arrays.asList(
//                new FESliceWindowConfig.Apply("ctr", "?/?", "exposed,click", "cnt")
//        ));
//        config.setOutput("rec.fea_?");
//        config.setDay("last_day");
        FESliceWindowConfig config = IOUtil.smartReadConfig("/Users/linjiuning/workspace/git/netease/py_scripts/lofter/fe/single_entity_window.yaml",
                FESliceWindowConfig.class);
        String ret = config.sql("a");
        System.out.println(ret);
    }
}