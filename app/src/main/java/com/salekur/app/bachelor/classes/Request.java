package com.salekur.app.bachelor.classes;

//class for getting request information from firebase
public class Request {
    String type, from, to, group;

    public Request() {

    }

    public Request(String type, String from, String to, String group) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
