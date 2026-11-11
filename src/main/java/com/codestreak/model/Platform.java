package com.codestreak.model;

import java.time.OffsetDateTime;

public class Platform {
    private int id;
    private String name;
    private String code;
    private String baseUrl;
    private String iconName;
    private boolean active;
    private OffsetDateTime createdAt;

    public Platform() {}

    public Platform(int id, String name, String code, String baseUrl, String iconName) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.baseUrl = baseUrl;
        this.iconName = iconName;
        this.active = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
