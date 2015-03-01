package com.kboyarshinov.realmrxjavaexample.model;

import io.realm.RealmObject;

public class RealmLabel extends RealmObject {
    private String name;
    private String color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
