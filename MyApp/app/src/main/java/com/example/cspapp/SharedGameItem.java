package com.example.cspapp;

public class SharedGameItem {
    private String id;
    private String name;
    private String type;
    private int speed;
    private String creatorId;
    private String creatorName;
    private long sharedAt;

    public SharedGameItem(String id, String name, String type, int speed,
                          String creatorId, String creatorName, long sharedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.speed = speed;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.sharedAt = sharedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getSpeed() { return speed; }
    public String getCreatorId() { return creatorId; }
    public String getCreatorName() { return creatorName; }
    public long getSharedAt() { return sharedAt; }
}