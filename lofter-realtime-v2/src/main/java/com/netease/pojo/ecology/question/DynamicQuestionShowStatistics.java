package com.netease.pojo.ecology.question;

public class DynamicQuestionShowStatistics {
    private Long questionId;
    private Integer questionType;
    private Long blogId;
    private Long hourCount;
    private Integer tagLottieType;
    private Long windowStartTime;
    private Long windowEndTime;
    private String tag;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public Long getHourCount() {
        return hourCount;
    }

    public void setHourCount(Long hourCount) {
        this.hourCount = hourCount;
    }

    public Integer getTagLottieType() {
        return tagLottieType;
    }

    public void setTagLottieType(Integer tagLottieType) {
        this.tagLottieType = tagLottieType;
    }

    public Long getWindowStartTime() {
        return windowStartTime;
    }

    public void setWindowStartTime(Long windowStartTime) {
        this.windowStartTime = windowStartTime;
    }

    public Long getWindowEndTime() {
        return windowEndTime;
    }

    public void setWindowEndTime(Long windowEndTime) {
        this.windowEndTime = windowEndTime;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
