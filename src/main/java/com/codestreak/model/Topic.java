package com.codestreak.model;

import java.time.OffsetDateTime;

public class Topic {
    private int id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private int displayOrder;
    private OffsetDateTime createdAt;

    public Topic() {}

    public Topic(int id, String name, String slug, String description, String icon, int displayOrder) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.icon = icon;
        this.displayOrder = displayOrder;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
