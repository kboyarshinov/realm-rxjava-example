package com.kboyarshinov.realmrxjavaexample;

import com.kboyarshinov.realmrxjavaexample.model.Issue;
import com.kboyarshinov.realmrxjavaexample.model.Label;
import com.kboyarshinov.realmrxjavaexample.model.User;

import java.util.List;

import rx.Observable;

public interface DataService {
    Observable<List<Issue>> issues();
    Observable<Issue> newIssue(String title, String body, User user, List<Label> labels);
}
