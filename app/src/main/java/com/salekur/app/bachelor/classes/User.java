package com.salekur.app.bachelor.classes;

public class User {
    String id, first_name, last_name, email, image, group;

    public User() {

    }

    public User(String id, String first_name, String last_name, String email, String image, String group) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.image = image;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}