package com.example.cspapp;

public class GameItem {
    private String id;
    private String name;
    private String type;
    private int speed;

    public GameItem(String id, String name, String type, int speed) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.speed = speed;
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
}