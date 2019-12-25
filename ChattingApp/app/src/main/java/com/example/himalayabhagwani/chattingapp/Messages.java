package com.example.himalayabhagwani.chattingapp;

public class Messages {

    private String message, type,from;
    boolean seen;
    long time;

    public Messages() {
    }

    public Messages(String from, String message, boolean seen, long time, String type) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
