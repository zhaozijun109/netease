package com.netease.easyml.local.mllib.bean;

/**
 * Created by eddielin on 2019/4/8.
 */
public enum HTMLInfo {
    LINK("isLink"),
    PATH("path"),
    FILTER("isFilter"),

    // html
    FONT_SIZE("font-size"),
    STRONG("strong"),
    EM("em"),
    COLOR("color"),
    BACKGROUND_COLOR("background-color"),
    BACKGROUND("background"),
    BACKGROUND_IMAGE("background-image"),
    TEXT_ALIGN("text-align"),
    BORDER_WIDTH("border-width"),
    BORDER_COLOR("border-color"),
    BORDER_STYLE("border-style"),
    BORDER_LEFT("border-left"),
    BORDER_RIGHT("border-right"),
    BORDER_BOTTOM("border-bottom"),
    BORDER_TOP("border-top"),
    BLOCK_QUOTE("blockquote");
//    BOX_SIZING("box-sizing");

    private String key;

    HTMLInfo(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static HTMLInfo of(String key) {
        for (HTMLInfo value : values()) {
            if (value.key.equals(key))
                return value;
        }
        return null;
    }
}
