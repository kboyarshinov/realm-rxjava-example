package com.kboyarshinov.realmrxjavaexample.model;

import java.util.List;

public class Issue {
    private final String title;
    private final String body;
    private final User user;
    private final List<Label> labels;

    public Issue(String title, String body, User user, List<Label> labels) {
        this.title = title;
        this.body = body;
        this.user = user;
        this.labels = labels;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public User getUser() {
        return user;
    }

    public List<Label> getLabels() {
        return labels;
    }
}
