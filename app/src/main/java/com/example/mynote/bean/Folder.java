package com.example.mynote.bean;


public class Folder {
    private long id;
    private String name;
    private long noteCounts;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNoteCounts() {
        return noteCounts;
    }

    public void setNoteCounts(long noteCounts) {
        this.noteCounts = noteCounts;
    }
}
