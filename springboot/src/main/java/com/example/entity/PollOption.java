package com.example.entity;

public class PollOption {
    private Integer id;
    private Integer pollId;
    private String content;
    private String color;
    private Integer voteCount;
    private Integer orderNum;
    private Double percentage;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPollId() { return pollId; }
    public void setPollId(Integer pollId) { this.pollId = pollId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}
