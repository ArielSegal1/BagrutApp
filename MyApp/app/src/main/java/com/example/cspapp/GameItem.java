package com.example.cspapp;

public class GameItem {
    private String id;
    private String name;
    private String type;
    private int speed;
    private String imageUrl;
    private String musicUrl;

    public GameItem(String id, String name, String type, int speed, String imageUrl) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.speed = speed;
        this.imageUrl = imageUrl;
        this.musicUrl = null;
    }

    public GameItem(String id, String name, String type, int speed, String imageUrl, String musicUrl) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.speed = speed;
        this.imageUrl = imageUrl;
        this.musicUrl = musicUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getSpeed() {
        return speed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public boolean hasMusic() {
        return musicUrl != null && !musicUrl.isEmpty();
    }
}