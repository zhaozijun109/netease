package com.netease.lofter.tango;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.lofter.tango.impl.service.AdDeepLinkService;
import com.netease.lofter.tango.impl.web.vo.ad.AdDeepLinkVO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DeepLinkTester extends BaseTester {

    @Autowired
    private AdDeepLinkService adDeepLinkService;

    @Test
    public void testAdd() {
        String str = "[\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"133811921\",\n" +
                "    \"target\": \"https://wuyishichaodan.lofter.com/post/4bf68c7f_2baf7d8e1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5288846\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩铁\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222708\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩铁\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222910\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/代号鸢\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5330412\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/耽美\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5250329\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/第五人格\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339728\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/短篇漫画\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5264686\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/短篇漫画\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339689\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/换兽文\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5250288\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/魔道祖师\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5330428\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/瓶邪\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5250259\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/图片\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5264601\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/网文\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339678\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/麦克阿瑟\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339712\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/新兰\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5290695\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222792\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222947\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339698\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/重生\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222541\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/咒术回战\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5168792\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/interact-novel/?njb_navigator=false&source=bilibili\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5275681\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/interact-novel/?njb_navigator=false&source=bilibili\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"advertiserId\",\n" +
                "    \"value\": \"26187501\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/pve-boyfriends/home/?njb_navigator=false&source=weichuang\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"advertiserId\",\n" +
                "    \"value\": \"1795671073946761\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/pve-boyfriends/home/?njb_navigator=false&source=1795671073946761\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2166733159\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2165833701\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/第五人格\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2165928298\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩坏星穹铁道\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2166142622\",\n" +
                "    \"target\": \"https://xiazhouwuyueba.lofter.com/post/742e1bd5_2b8abb325\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2166296285\",\n" +
                "    \"target\": \"https://woyaojianfei57632.lofter.com/post/740aec1b_2bb0887da\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2166377081\",\n" +
                "    \"target\": \"https://michellechou.lofter.com/post/1d4eee4e_1c725dedb\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2166385579\",\n" +
                "    \"target\": \"https://kuukausi.lofter.com/post/1f591b6e_1cbc85f38\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2166402758\",\n" +
                "    \"target\": \"https://ranqiuqiu147.lofter.com/post/1fa5fb77_2b93b4524\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2166428638\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/名侦探柯南\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7359894481535483958\",\n" +
                "    \"target\": \"https://yuzui44714.lofter.com/post/7c905271_2bb8afce7\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7359894496907640843\",\n" +
                "    \"target\": \"https://ailanxianruchensishunbianchenshui.lofter.com/post/4b99bdd3_2ba719268\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7359894480096673831\",\n" +
                "    \"target\": \"https://xinjinjumin322804128611.lofter.com/post/795b45e6_2bad5f78e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7356848992963297289\",\n" +
                "    \"target\": \"https://3260302303.lofter.com/post/749c538f_2badf0d02\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360886375078068278\",\n" +
                "    \"target\": \"https://3260302303.lofter.com/post/749c538f_2baa65721\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360886336852115492\",\n" +
                "    \"target\": \"https://peizongxihuanchilingshi.lofter.com/post/4b9dbde0_2b764a3b2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360886350110195723\",\n" +
                "    \"target\": \"https://venti-0616.lofter.com/post/4b53c250_2b533a03d\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360618003891552283\",\n" +
                "    \"target\": \"https://venti-0616.lofter.com/post/4b53c250_2b533a03d\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360886360368611337\",\n" +
                "    \"target\": \"https://sadu025019.lofter.com/post/4b775de8_2bb4632b4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360609391249408036\",\n" +
                "    \"target\": \"https://zhizhi36949.lofter.com/post/743fefa4_2bac73ec6\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360886339782590475\",\n" +
                "    \"target\": \"https://laoyan52999.lofter.com/post/4b7b654a_2bac80078\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360609432206131254\",\n" +
                "    \"target\": \"https://victory77.lofter.com/post/74237e74_2bab93a1b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360670310101221412\",\n" +
                "    \"target\": \"https://xinjinjumin213806470607.lofter.com/post/7d6d80bb_2bba36cce\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7358809719853563914\",\n" +
                "    \"target\": \"https://xinjinjumin213806470607.lofter.com/post/7d6d80bb_2bbad7fa8\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7358809694797758491\",\n" +
                "    \"target\": \"https://xinjinjumin213806470607.lofter.com/post/7d6d80bb_2bbad7fa8\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360609426090885159\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad43c0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360609426238128139\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad43c0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7359101295125381146\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad43c0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7356849170568200203\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad6156\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"7360609396970635276\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad4b36\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2182576941\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/代号鸢\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2182623843\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2182633014\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/王者荣耀\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2182598679\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/第五人格\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2182649911\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/世界之外\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2182678977\",\n" +
                "    \"target\": \"https://lueluehe7.lofter.com/post/31d33f9d_2bac0eb57\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2182686551\",\n" +
                "    \"target\": \"https://lueluehe7.lofter.com/post/31d33f9d_2bb10c158\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2187674513\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/代号鸢\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2188941775\",\n" +
                "    \"target\": \"https://hexyl.lofter.com/post/4c71c7d1_1ccc68a94?channel=h5-post-screenshot&pid=p_i42\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2188938813\",\n" +
                "    \"target\": \"https://ailisiwenmin.lofter.com/post/4cc7b082_2b59c627e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2188935013\",\n" +
                "    \"target\": \"https://taimeng726.lofter.com/post/31082837_2b925e765\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2188930686\",\n" +
                "    \"target\": \"https://cheqidoushi124.lofter.com/post/77eeeda3_2b9bc997f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2188944782\",\n" +
                "    \"target\": \"https://0106algernon.lofter.com/post/4b63429b_1ca6adaa4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"710760\",\n" +
                "    \"target\": \"https://xinjinjumin696042168491.lofter.com/post/7bc77ddf_2bb9d96c8\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"710946\",\n" +
                "    \"target\": \"https://xinjinjumin696042168491.lofter.com/post/7bc77ddf_2bb7be113\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"710223\",\n" +
                "    \"target\": \"https://jidongaiqingjianzhengrenpajiang.lofter.com/post/1f872b94_2bb84bc0a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"696482\",\n" +
                "    \"target\": \"https://duanqiaotoumaiyu.lofter.com/post/1d161fd1_2b5897d47\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719437\",\n" +
                "    \"target\": \"https://yui955.lofter.com/post/4cd51c9c_2bbab7760\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719464\",\n" +
                "    \"target\": \"https://vkook43939.lofter.com/post/4ce427f2_2bba94d05\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719637\",\n" +
                "    \"target\": \"https://snh48-zhangyuanying.lofter.com/post/4c1c78af_2bba9e29f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719638\",\n" +
                "    \"target\": \"https://snh48-zhangyuanying.lofter.com/post/4c1c78af_2bb9bb992\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719737\",\n" +
                "    \"target\": \"https://snh48-zhangyuanying.lofter.com/post/4c1c78af_2bb7ea102\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719639\",\n" +
                "    \"target\": \"https://pcalxun.lofter.com/post/1dd2506a_2bb98a7a5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719640\",\n" +
                "    \"target\": \"https://pcalxun.lofter.com/post/1dd2506a_2bb3ed186\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719642\",\n" +
                "    \"target\": \"https://xx00168857.lofter.com/post/3184dad9_2bbaf9a7e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719739\",\n" +
                "    \"target\": \"https://lingjiuzhendemeibeitaoyan.lofter.com/post/31fbfbe2_2bbb02e16\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"719644\",\n" +
                "    \"target\": \"https://fuxixi915.lofter.com/post/201c995d_2bbafda69\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2187662715\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/间谍过家家\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193689042\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/名侦探柯南\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193920052\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/光与夜之恋\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193931730\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/未定事件簿\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193938023\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/盗墓笔记\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193902307\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/恋与深空\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193946283\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/排球少年\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193953031\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/天官赐福\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193959281\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/时光代理人\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2190841447\",\n" +
                "    \"target\": \"https://chenbudaowo.lofter.com/post/73ce93d6_2bae4c1cd\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193765236\",\n" +
                "    \"target\": \"https://yeliangchenww.lofter.com/post/4c156a6d_2b63e9393\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193751477\",\n" +
                "    \"target\": \"https://lishan36161.lofter.com/post/4bb8efe1_1cbbd4539\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362565334652895241\",\n" +
                "    \"target\": \"https://grlm986815.lofter.com/post/73d0c610_2bb664d28\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362720965069766695\",\n" +
                "    \"target\": \"https://d1946.lofter.com/post/2034c891_2b8175524\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362730323668566067\",\n" +
                "    \"target\": \"https://xinjinjumin816340082779.lofter.com/post/7c8822e6_2bb853caf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362727378340757555\",\n" +
                "    \"target\": \"https://nitangge81747.lofter.com/post/760bfc5d_2b9879cf2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362734541084131365\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaea7f5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362736140291522570\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaeb2f2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362737019207893029\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad4b36\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362740101468651571\",\n" +
                "    \"target\": \"https://beixianglanmao.lofter.com/post/4bd85a27_2bb30b7fd\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362568833632698405\",\n" +
                "    \"target\": \"https://grlm986815.lofter.com/post/73d0c610_2bb664d28\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362720965069766695\",\n" +
                "    \"target\": \"https://d1946.lofter.com/post/2034c891_2b8175524\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362851633452285978\",\n" +
                "    \"target\": \"https://xinjinjumin816340082779.lofter.com/post/7c8822e6_2bb853caf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855472347594789\",\n" +
                "    \"target\": \"https://nitangge81747.lofter.com/post/760bfc5d_2b9879cf2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855628077809691\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaea7f5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855074505490482\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaeb2f2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855803006664755\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad4b36\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362868641556594714\",\n" +
                "    \"target\": \"https://beixianglanmao.lofter.com/post/4bd85a27_2bb30b7fd\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362860373215215653\",\n" +
                "    \"target\": \"https://grlm986815.lofter.com/post/73d0c610_2bb664d28\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362856899774169114\",\n" +
                "    \"target\": \"https://d1946.lofter.com/post/2034c891_2b8175524\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362877674388258854\",\n" +
                "    \"target\": \"https://xinjinjumin816340082779.lofter.com/post/7c8822e6_2bb853caf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362857294588100618\",\n" +
                "    \"target\": \"https://nitangge81747.lofter.com/post/760bfc5d_2b9879cf2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362856835089219621\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaea7f5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362858757846532105\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaeb2f2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362859380882767898\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad4b36\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855837583867913\",\n" +
                "    \"target\": \"https://beixianglanmao.lofter.com/post/4bd85a27_2bb30b7fd\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362853680888774665\",\n" +
                "    \"target\": \"https://grlm986815.lofter.com/post/73d0c610_2bb664d28\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362858177926119433\",\n" +
                "    \"target\": \"https://d1946.lofter.com/post/2034c891_2b8175524\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855519454789642\",\n" +
                "    \"target\": \"https://xinjinjumin816340082779.lofter.com/post/7c8822e6_2bb853caf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855796089028658\",\n" +
                "    \"target\": \"https://nitangge81747.lofter.com/post/760bfc5d_2b9879cf2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362853873038147594\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaea7f5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855622654967834\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaeb2f2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362853678435336203\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad4b36\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362859466943053887\",\n" +
                "    \"target\": \"https://beixianglanmao.lofter.com/post/4bd85a27_2bb30b7fd\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855198848565274\",\n" +
                "    \"target\": \"https://grlm986815.lofter.com/post/73d0c610_2bb664d28\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362852605536583691\",\n" +
                "    \"target\": \"https://d1946.lofter.com/post/2034c891_2b8175524\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362858855283851315\",\n" +
                "    \"target\": \"https://xinjinjumin816340082779.lofter.com/post/7c8822e6_2bb853caf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362854182284541990\",\n" +
                "    \"target\": \"https://nitangge81747.lofter.com/post/760bfc5d_2b9879cf2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362853177442893836\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaea7f5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362838778304675876\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbaeb2f2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362854782598266919\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad4b36\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7362855645374857228\",\n" +
                "    \"target\": \"https://beixianglanmao.lofter.com/post/4bd85a27_2bb30b7fd\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361689030335037479\",\n" +
                "    \"target\": \"https://nitangge81747.lofter.com/post/760bfc5d_2ba0936ff\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7360609426090885159\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad43c0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361688894746476580\",\n" +
                "    \"target\": \"https://xinjinjumin596131833923.lofter.com/post/763a6c98_2b79dca5d\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361688966418333746\",\n" +
                "    \"target\": \"https://xinjinjumin6492416.lofter.com/post/7aea254b_2bac7e63c\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7360897773343801398\",\n" +
                "    \"target\": \"https://beixianglanmao.lofter.com/post/4bd85a27_2b99b0afc\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7360897826823667731\",\n" +
                "    \"target\": \"https://beloved80914.lofter.com/post/7da01274_2bbad6ccf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361688973779894308\",\n" +
                "    \"target\": \"https://xinjinjumin3682879.lofter.com/post/7632f566_2b78ea50e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361688944490463251\",\n" +
                "    \"target\": \"https://2289026792.lofter.com/post/4b4f288e_2b419cb92\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729472510459958\",\n" +
                "    \"target\": \"https://shixiariyoulingdoujiubuhui.lofter.com/post/768066f9_2bb1434ae\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729470522949686\",\n" +
                "    \"target\": \"https://storm78743.lofter.com/post/7d4fda63_2bb9e5c8e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729454014431271\",\n" +
                "    \"target\": \"https://qingjiu46606.lofter.com/post/75c2e539_2baed0a52\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729499928690738\",\n" +
                "    \"target\": \"https://yongbadeyunshewuzuihunilianshangma.lofter.com/post/76000d94_2bb91d887\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729372442886163\",\n" +
                "    \"target\": \"https://ioc52.lofter.com/post/76b96ee4_2bab1fae6\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361728738247196711\",\n" +
                "    \"target\": \"https://muyou34998.lofter.com/post/4cd6e522_2bb1d19d4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361728698381434934\",\n" +
                "    \"target\": \"https://yangtuo42114.lofter.com/post/74700211_2b6306da1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361728749156794404\",\n" +
                "    \"target\": \"https://5760739796.lofter.com/post/77f447ac_2bb193956\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729223253327910\",\n" +
                "    \"target\": \"https://weishanbue.lofter.com/post/1F706B48_2BBA0A038\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729100497649703\",\n" +
                "    \"target\": \"https://qingjiaowolouzhuha.lofter.com/post/4cf044f8_2bb929ceb\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729046893756426\",\n" +
                "    \"target\": \"https://lizz749741.lofter.com/post/75872f55_2ba370078\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361728962312159243\",\n" +
                "    \"target\": \"https://yonghu7893366943.lofter.com/post/7af2385b_2bb0e7231\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729020599205942\",\n" +
                "    \"target\": \"https://lizz749741.lofter.com/post/75872f55_2ba0aef00\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729086597333011\",\n" +
                "    \"target\": \"https://yonghu7893366943.lofter.com/post/7af2385b_2bb58ac0f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729841487986707\",\n" +
                "    \"target\": \"https://kuokuonainai71529.lofter.com/post/4bf14956_1cb46434b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729887578931209\",\n" +
                "    \"target\": \"https://fox11911.lofter.com/post/30a86126_1cb555980\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361732211241271332\",\n" +
                "    \"target\": \"https://5760739796.lofter.com/post/77f447ac_2bb4fa95a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361731369330720787\",\n" +
                "    \"target\": \"https://xinglurensy.lofter.com/post/1d0ee292_2bb3fb95f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729786605830154\",\n" +
                "    \"target\": \"https://haiwei0567.lofter.com/post/319a980d_2b6ff6eb4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361731280726540342\",\n" +
                "    \"target\": \"https://1556019002.lofter.com/post/75fb0542_2b74f50a5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361731695539486746\",\n" +
                "    \"target\": \"https://cc99959110.lofter.com/post/743eb811_2b9d223e3\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361013700926619667\",\n" +
                "    \"target\": \"https://yuanshenoffice.lofter.com/post/31fc3e13_2b931a8f0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361013751736385572\",\n" +
                "    \"target\": \"https://yaoluo54799.lofter.com/post/76f8784f_2b90ffe89\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361013747320078377\",\n" +
                "    \"target\": \"https://weifengfeng690.lofter.com/post/30909701_2b6fca6ec\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361013679808692243\",\n" +
                "    \"target\": \"https://xinjinjumin816340082779.lofter.com/post/7c8822e6_2bb853caf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361013758313103423\",\n" +
                "    \"target\": \"https://d1946.lofter.com/post/2034c891_2b817552c\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361023514898628647\",\n" +
                "    \"target\": \"https://yiquzhongci.lofter.com/post/203a5b40_2bb272c89\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361023519558451211\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad6cb2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361023575668965417\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad43c0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7356849170568200203\",\n" +
                "    \"target\": \"https://chang30932.lofter.com/post/7d72746c_2bbad6156\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729187322986535\",\n" +
                "    \"target\": \"https://lacus-shouko.lofter.com/post/1ccab424_1cca3cf8b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729201001463862\",\n" +
                "    \"target\": \"https://yaoluoxiaomo.lofter.com/post/1ee3cb3c_2b5d82a5e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729201000955958\",\n" +
                "    \"target\": \"https://wumingshi26070.lofter.com/post/741c9124_2b571e431\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729153638891559\",\n" +
                "    \"target\": \"https://bipper0.lofter.com/post/1dcd26d8_2b4ecc065\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729175847288844\",\n" +
                "    \"target\": \"https://hulinjiang.lofter.com/post/44cfc1_2bb817c97\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361729167845310500\",\n" +
                "    \"target\": \"https://uehroqwiehoijepio.lofter.com/post/7ac6eda0_2bb68a964\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361727659090870284\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/%E7%AE%80%E9%9A%8B%E8%8B%B1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7361346535433256996\",\n" +
                "    \"target\": \"https://qianshiguang69285.lofter.com/post/4be6a706_2b59332b5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7360656204387516470\",\n" +
                "    \"target\": \"https://wuciwc.lofter.com/post/1fea67b0_2b3f2eaa0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7360656170426499083\",\n" +
                "    \"target\": \"https://xiaohailuogangmanyue66164.lofter.com/post/4d146662_2bb28415a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7362080988393406515\",\n" +
                "    \"target\": \"https://zhijinyixiangduoshaoqian.lofter.com/post/4c7a770b_2bb9a1d81\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7362080959649775654\",\n" +
                "    \"target\": \"https://wuciwc.lofter.com/post/1fea67b0_2ba12798a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"mid\",\n" +
                "    \"value\": \"7360897745723703315\",\n" +
                "    \"target\": \"https://bana112976.lofter.com/post/74ad652c_2b66d8aa4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"advertiserId\",\n" +
                "    \"value\": \"1795671070316544\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/pve-boyfriends/home/?njb_navigator=false&source=1795671070316544\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7363212126795890715\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/pve-boyfriends/home/?njb_navigator=false&source=xingtu240422\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7376200893726834739\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/pve-boyfriends/home/?njb_navigator=false&source=xingtu240422\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"7371375965789978661\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/pve-boyfriends/?blogId=2103793831&from=delivery&njb_navigator=false&source=7371375965789978661\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"133811921\",\n" +
                "    \"target\": \"https://wuyishichaodan.lofter.com/post/4bf68c7f_2baf7d8e1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"134611249\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/interact-novel/?njb_navigator=false&source=bilibili\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"cid\",\n" +
                "    \"value\": \"135960741\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/interact-novel/?njb_navigator=false&source=bilibili\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5288846\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩铁\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222708\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩铁\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222910\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/代号鸢\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5330412\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/耽美\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5250329\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/第五人格\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339728\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/短篇漫画\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5264686\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/短篇漫画\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339689\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/换兽文\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5250288\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/魔道祖师\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5330428\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/瓶邪\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5250259\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/图片\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5264601\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/网文\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339678\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/麦克阿瑟\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339712\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/新兰\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5290695\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222792\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222947\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5339698\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/重生\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5222541\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/咒术回战\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"5359734\",\n" +
                "    \"target\": \"https://www.lofter.com/spread/html/activities/interact-novel/?njb_navigator=false&source=bilibili\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2193762739\",\n" +
                "    \"target\": \"https://sixheartone.lofter.com/post/47dbb6_2b6e27fef\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2196509216\",\n" +
                "    \"target\": \"https://intlkleinblue.lofter.com/post/1ed1e89e_1cac89987\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2196515083\",\n" +
                "    \"target\": \"https://anonymousxiaoli.lofter.com/post/742416ef_2b4a192f8\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2196519188\",\n" +
                "    \"target\": \"https://ldududududu765.lofter.com/post/1fda3da0_12b520c23\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2210108361\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/哈利波特\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2210080716\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/时代少年团\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2210132687\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/盗墓笔记\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2210129219\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/文豪野犬\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2210120213\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/大理寺少卿游\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"D2218133910\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=1698803\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2218130683\",\n" +
                "    \"target\": \"https://baihuayuhongxia.lofter.com/post/1f073cb4_1c605fd2e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2218126890\",\n" +
                "    \"target\": \"https://lishan36161.lofter.com/post/4bb8efe1_1cbbd4539\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2218103972\",\n" +
                "    \"target\": \"https://ming-zhouye.lofter.com/post/4bf9e149_2b3e7b7e7\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2218099692\",\n" +
                "    \"target\": \"https://mixix19.lofter.com/post/31f5a4_2b8584ca8\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2220530013\",\n" +
                "    \"target\": \"https://chenbudaowo.lofter.com/post/73ce93d6_2bae4c1cd\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2220526370\",\n" +
                "    \"target\": \"https://zhouji-chenxizhuiluo.lofter.com/post/1eec1806_2b9de673a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2220523919\",\n" +
                "    \"target\": \"https://buzhidaoqushenmemingdekelepaopao.lofter.com/post/200a4562_2ba54799c\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2220521099\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=5649984\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2220516627\",\n" +
                "    \"target\": \"https://xinghewanli960717.lofter.com/post/1f9cda7a_2b4c6ec5f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2228388745\",\n" +
                "    \"target\": \"https://lofterfanc.lofter.com/post/4b62e964_1cac482e4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2228404614\",\n" +
                "    \"target\": \"https://karryrene.lofter.com/post/1d0fc85f_1cd334bc5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2228409099\",\n" +
                "    \"target\": \"https://karryrene.lofter.com/post/1d0fc85f_1cd334bc5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2228413199\",\n" +
                "    \"target\": \"https://bushiluoshu.lofter.com/post/1ebc8a88_1c6c6762d\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2228416225\",\n" +
                "    \"target\": \"https://caomeinongniunai278.lofter.com/post/1fd2c94b_2b95155c1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2236751733\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩坏星穹铁道\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2236751733\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩坏星穹铁道\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245227312\",\n" +
                "    \"target\": \"https://shiliu970.lofter.com/post/20222a1a_2b7c62de4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245224250\",\n" +
                "    \"target\": \"https://honey323.lofter.com/post/30a85eeb_2b72dc42b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245221661\",\n" +
                "    \"target\": \"https://jiuweiwu.lofter.com/post/1d4b0803_2b99d3383\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245219022\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=16712001\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245216809\",\n" +
                "    \"target\": \"https://trystal.lofter.com/post/4c21da3d_2b8b02d31\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245214881\",\n" +
                "    \"target\": \"https://nanwang914.lofter.com/post/311ce1ac_1cbd3bb42\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245212584\",\n" +
                "    \"target\": \"https://chisuhuiyuan.lofter.com/post/4c456c28_2b40ecc80\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2240313886\",\n" +
                "    \"target\": \"https://xiaodaomimang1822.lofter.com/post/317a7c1d_2bab4aa3b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2240310175\",\n" +
                "    \"target\": \"https://aidangairenmin645.lofter.com/post/1ff5f566_1c662f87a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2240306445\",\n" +
                "    \"target\": \"https://zhouji-chenxizhuiluo.lofter.com/post/1eec1806_2b9de673a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2240299996\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=15966498\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2240285478\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=14848659\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2240279287\",\n" +
                "    \"target\": \"https://zik-moon.lofter.com/post/76607440_2b844fef9\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258573941\",\n" +
                "    \"target\": \"https://moonlight242.lofter.com/post/30f601e2_2b9edfcf7\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258570550\",\n" +
                "    \"target\": \"https://xieningmengdexiaohao.lofter.com/post/73ce7c1a_2b8f5dd89\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258567787\",\n" +
                "    \"target\": \"https://keananke.lofter.com/post/201bbb27_2b7937f50\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258565613\",\n" +
                "    \"target\": \"https://heartrr.lofter.com/post/1f806dc4_2b8545b51\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258563138\",\n" +
                "    \"target\": \"https://xinjinjumin8323313.lofter.com/post/753eca0f_2b6da1974\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258558972\",\n" +
                "    \"target\": \"https://juneconnie.lofter.com/post/30e2a24f_1ca729dd4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258555398\",\n" +
                "    \"target\": \"https://yizhimo915.lofter.com/post/30a951b7_1c70e01f1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258551572\",\n" +
                "    \"target\": \"https://huaizhi43.lofter.com/post/4bfd136e_2baf5cace\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258546874\",\n" +
                "    \"target\": \"https://baixiongjuedekeyi.lofter.com/post/4c33ce3e_2b5bda0bc\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258538792\",\n" +
                "    \"target\": \"https://taimeng726.lofter.com/post/31082837_2b925e765\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258529973\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=7405120&incantation=hjEb1sUAoKiC\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258519070\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=14848659\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258497154\",\n" +
                "    \"target\": \"https://guisu55870.lofter.com/post/31f7046e_1cbb26dfe\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258492190\",\n" +
                "    \"target\": \"https://binrong885.lofter.com/post/30e216ab_2baea0d75\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2245231894\",\n" +
                "    \"target\": \"https://xihuan133.lofter.com/post/1fbac4ff_2b52fa101\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258467172\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩坏星穹铁道\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258449368\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258442178\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2258419783\",\n" +
                "    \"target\": \"https://karryttttt.lofter.com/post/73c00c62_2bbd67b5a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2257857858\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩坏星穹铁道\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2239745889\",\n" +
                "    \"target\": \"https://yiye775633.lofter.com/post/4c2fd19f_2bbc974bc\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2239716094\",\n" +
                "    \"target\": \"https://tongchen05442.lofter.com/post/79998009_2ba867da2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273096069\",\n" +
                "    \"target\": \"https://qiao4973.lofter.com/post/2037916c_2bbd70559\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273098822\",\n" +
                "    \"target\": \"https://rmcf329.lofter.com/post/77f9c4cd_2bbd6c7be\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273103445\",\n" +
                "    \"target\": \"https://da65746584.lofter.com/post/7ce46686_2bbd7358b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273107262\",\n" +
                "    \"target\": \"https://xh1780115.lofter.com/post/4c218e2c_2b6e319fa\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273137332\",\n" +
                "    \"target\": \"https://juejuezislv.lofter.com/post/772d9b80_2bbd874d3\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273204593\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273310405\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/哈利波特\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273277964\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/第五人格\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273201010\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/第五人格\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273174492\",\n" +
                "    \"target\": \"https://zezexixi123.lofter.com/post/777b57f3_2bb0f2e7a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273170032\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/咒术回战\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273292454\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩坏星穹铁道\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273078591\",\n" +
                "    \"target\": \"https://xiaobaozixianer.lofter.com/post/4c3133f7_2b83aefc4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273083304\",\n" +
                "    \"target\": \"https://www.lofter.com/front/blog/collection/share?collectionId=14848659\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273085749\",\n" +
                "    \"target\": \"https://lishan36161.lofter.com/post/4bb8efe1_1cbbd4539\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2273089221\",\n" +
                "    \"target\": \"https://muccio39872.lofter.com/post/4d0c7f8a_2b540b133\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283500626\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/重返未来1999\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283493248\",\n" +
                "    \"target\": \"https://xinjinjumin6150206.lofter.com/post/7854d120_2bbe17742\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283487532\",\n" +
                "    \"target\": \"https://stonyrose.lofter.com/post/1d09ac70_2bbdc4989\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283483608\",\n" +
                "    \"target\": \"https://xiaotangren965.lofter.com/post/30951e74_2bbd8c0f9\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283477147\",\n" +
                "    \"target\": \"https://legolanduil.lofter.com/post/42932c_1c9670b9f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283473352\",\n" +
                "    \"target\": \"https://mhnmhn.lofter.com/post/1e31f577_2bbda9f8b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283658645\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/原神\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283666776\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/盗墓笔记\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283536892\",\n" +
                "    \"target\": \"https://juejuezislv.lofter.com/post/772d9b80_2bbd874d3\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283526199\",\n" +
                "    \"target\": \"https://chaoxinian.lofter.com/post/205d56eb_2bbe0495b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283521881\",\n" +
                "    \"target\": \"https://xinjinjumin6150206.lofter.com/post/7854d120_2bbe17742\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283514927\",\n" +
                "    \"target\": \"https://lksuiz.lofter.com/post/1f4af14e_2bbde0a8a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283610997\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/第五人格\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283628697\",\n" +
                "    \"target\": \"https://www.lofter.com/tag/崩坏星穹铁道\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283461971\",\n" +
                "    \"target\": \"https://xiangzhounishuigou.lofter.com/post/4cc4700d_1cce19fa6\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283459595\",\n" +
                "    \"target\": \"https://xuni900785.lofter.com/post/4c41a878_2bb35b52f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283457467\",\n" +
                "    \"target\": \"https://ailuo871.lofter.com/post/309ac57f_2ba89c060\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283128686\",\n" +
                "    \"target\": \"https://xieningmengdexiaohao.lofter.com/post/73ce7c1a_2b8f5dd89\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283124776\",\n" +
                "    \"target\": \"https://xinjinjumin433941549299.lofter.com/post/771bc98b_2baba3c27\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283115679\",\n" +
                "    \"target\": \"https://toutouchenjingsheng.lofter.com/post/74ab4c74_2ba4833c3\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283110738\",\n" +
                "    \"target\": \"https://peixiaozhakanxingxing66619.lofter.com/post/4c87375f_2b434187b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283098864\",\n" +
                "    \"target\": \"https://miggg.lofter.com/post/31baa29d_2b9209378\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2283093626\",\n" +
                "    \"target\": \"https://wo515549.lofter.com/post/2011109d_2b6aa81a7\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"campaignId\",\n" +
                "    \"value\": \"2280484040\",\n" +
                "    \"target\": \"https://chocolateislife510.lofter.com/post/1ff0acf1_2ba8dd2db\"\n" +
                "  }\n" +
                "]";
        JSONArray jsonArray = JSONArray.parseArray(str);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            AdDeepLinkVO vo = new AdDeepLinkVO();
            vo.setChannel("kuaishou");
            vo.setUrl(jsonObject.getString("target"));
            vo.setOperator("system");
            String key = jsonObject.getString("key");
            String value = jsonObject.getString("value");
            if ("channel".equalsIgnoreCase(key)) {
                vo.setChannel(value);
            } else if ("advertiserId".equalsIgnoreCase(key)) {
                vo.setAdvertiseId(value);
            } else if ("campaignId".equalsIgnoreCase(key)) {
                vo.setCampaignId(value);
            } else if ("cid".equalsIgnoreCase(key)) {
                vo.setCid(value);
            } else if ("mid".equalsIgnoreCase(key)) {
                vo.setMid(value);
                vo.setChannel("toutiao2");
            }
            System.out.println(JSONObject.toJSONString(vo));
            adDeepLinkService.add(vo);
        }
    }
}
