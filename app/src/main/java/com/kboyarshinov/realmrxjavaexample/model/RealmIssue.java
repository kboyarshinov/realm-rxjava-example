package com.kboyarshinov.realmrxjavaexample.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class RealmIssue extends RealmObject {
    private String title;
    private String body;
    private RealmUser user;
    private RealmList<RealmLabel> labels;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public RealmUser getUser() {
        return user;
    }

    public void setUser(RealmUser user) {
        this.user = user;
    }

    public RealmList<RealmLabel> getLabels() {
        return labels;
    }

    public void setLabels(RealmList<RealmLabel> labels) {
        this.labels = labels;
    }
}
