package com.example.entity;

import java.time.LocalDateTime;

public class EasterEggRecord {

    private Integer id;
    private Integer userId;
    private String userName;
    private String userRole;
    private String eggName;
    private LocalDateTime discoverTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getEggName() {
        return eggName;
    }

    public void setEggName(String eggName) {
        this.eggName = eggName;
    }

    public LocalDateTime getDiscoverTime() {
        return discoverTime;
    }

    public void setDiscoverTime(LocalDateTime discoverTime) {
        this.discoverTime = discoverTime;
    }
}
