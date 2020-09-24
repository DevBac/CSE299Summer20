package com.salekur.app.bachelor.classes;

public class Meal {
    public String breakfast, lunch, dinner;

    public Meal() {
    }

    public Meal(String breakfast, String lunch, String dinner) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    public String getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(String breakfast) {
        this.breakfast = breakfast;
    }

    public String getLunch() {
        return lunch;
    }

    public void setLunch(String lunch) {
        this.lunch = lunch;
    }

    public String getDinner() {
        return dinner;
    }

    public void setDinner(String dinner) {
        this.dinner = dinner;
    }
}
