package com.example.mynote.bean;

public class Note {
    private int noteId;
    private int bgId;

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    private long parentId;
    private String content;
    private long createdTime;
    private long modifiedTime;
    private long alarmDate;


    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public int getBgId() {
        return bgId;
    }

    public void setBgId(int bgId) {
        this.bgId = bgId;
    }

    public Note(){
    }
    public Note(String content, long modifiedTime) {
        this.content = content;
        this.modifiedTime = modifiedTime;
    }

    public Note(int imageId, String content, long modifiedTime) {
        this.content = content;
        this.modifiedTime = modifiedTime;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(long alarmDate) {
        this.alarmDate = alarmDate;
    }
}
