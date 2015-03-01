package com.kboyarshinov.realmrxjavaexample.model;

import io.realm.RealmObject;

public class RealmUser extends RealmObject {
    private String login;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
