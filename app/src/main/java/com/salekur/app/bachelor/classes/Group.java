package com.salekur.app.bachelor.classes;

//class for getting group information from firebase
public class Group {
    String id, name, admin;

    public Group() {

    }

    public Group(String id, String name, String admin) {
        this.id = id;
        this.name = name;
        this.admin = admin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
