package com.netease.pojo.ecology.post;

/** Filed definition of db post hot. */
public class PostHot {
    private String tableName;
    private Long id;
    private Long postId;
    private Long blogId;
    private Long publisherUserId;
    private Long fromPostId;
    private Long fromBlogId;
    private Long toPostId;
    private Long toBlogId;
    private String content;
    private Long opTime;
    private Integer type;
    private String ip;
    private Integer status;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public Long getPublisherUserId() {
        return publisherUserId;
    }

    public void setPublisherUserId(Long publisherUserId) {
        this.publisherUserId = publisherUserId;
    }

    public Long getFromPostId() {
        return fromPostId;
    }

    public void setFromPostId(Long fromPostId) {
        this.fromPostId = fromPostId;
    }

    public Long getFromBlogId() {
        return fromBlogId;
    }

    public void setFromBlogId(Long fromBlogId) {
        this.fromBlogId = fromBlogId;
    }

    public Long getToPostId() {
        return toPostId;
    }

    public void setToPostId(Long toPostId) {
        this.toPostId = toPostId;
    }

    public Long getToBlogId() {
        return toBlogId;
    }

    public void setToBlogId(Long toBlogId) {
        this.toBlogId = toBlogId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getOpTime() {
        return opTime;
    }

    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
