package com.kboyarshinov.realmrxjavaexample;

import android.content.Context;

import com.kboyarshinov.realmrxjavaexample.model.Issue;
import com.kboyarshinov.realmrxjavaexample.model.Label;
import com.kboyarshinov.realmrxjavaexample.model.RealmIssue;
import com.kboyarshinov.realmrxjavaexample.model.RealmLabel;
import com.kboyarshinov.realmrxjavaexample.model.RealmUser;
import com.kboyarshinov.realmrxjavaexample.model.User;
import com.kboyarshinov.realmrxjavaexample.rx.RealmObservable;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

public class RealmDataService implements DataService {
    private final Context context;

    public RealmDataService(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Observable<List<Issue>> issuesList() {
        return RealmObservable.results(context, new Func1<Realm, RealmResults<RealmIssue>>() {
            @Override
            public RealmResults<RealmIssue> call(Realm realm) {
                // find all issues
                return realm.where(RealmIssue.class).findAll();
            }
        }).map(new Func1<RealmResults<RealmIssue>, List<Issue>>() {
            @Override
            public List<Issue> call(RealmResults<RealmIssue> realmIssues) {
                // map them to UI objects
                final List<Issue> issues = new ArrayList<Issue>(realmIssues.size());
                for (RealmIssue realmIssue : realmIssues) {
                    issues.add(issueFromRealm(realmIssue));
                }
                return issues;
            }
        });
    }

    @Override
    public Observable<Issue> newIssue(final String title, final String body, final User user, List<Label> labels) {
        // map internal UI objects to Realm objects
        final RealmUser realmUser = new RealmUser();
        realmUser.setLogin(user.getLogin());
        final RealmList<RealmLabel> realmLabels = new RealmList<>();
        for (Label label : labels) {
            RealmLabel realmLabel = new RealmLabel();
            realmLabel.setName(label.getName());
            realmLabel.setColor(label.getColor());
            realmLabels.add(realmLabel);
        }
        return RealmObservable.object(context, new Func1<Realm, RealmIssue>() {
            @Override
            public RealmIssue call(Realm realm) {
                // internal object instances are not created by realm
                // saving them using copyToRealm returning instance associated with realm
                RealmUser user = realm.copyToRealm(realmUser);
                RealmList<RealmLabel> labels = new RealmList<RealmLabel>();
                for (RealmLabel realmLabel : realmLabels) {
                    labels.add(realm.copyToRealm(realmLabel));
                }
                // create RealmIssue instance and save it
                RealmIssue issue = new RealmIssue();
                issue.setTitle(title);
                issue.setBody(body);
                issue.setUser(user);
                issue.setLabels(labels);
                return realm.copyToRealm(issue);
            }
        }).map(new Func1<RealmIssue, Issue>() {
            @Override
            public Issue call(RealmIssue realmIssue) {
                // map to UI object
                return issueFromRealm(realmIssue);
            }
        });
    }

    @Override
    public Observable<User> findUser(final String username) {
        return RealmObservable.object(context, new Func1<Realm, RealmUser>() {
            @Override
            public RealmUser call(Realm realm) {
                return realm.where(RealmUser.class).equalTo("login", username).findFirst();
            }
        }).map(new Func1<RealmUser, User>() {
            @Override
            public User call(RealmUser realmUser) {
                return userFromRealm(realmUser);
            }
        });
    }

    @Override
    public Observable<List<Issue>> issuesListByUser(final User user) {
        return issues().filter(new Func1<Issue, Boolean>() {
            @Override
            public Boolean call(Issue issue) {
                return issue.getUser().getLogin().equals(user.getLogin());
            }
        }).toList();
    }

    @Override
    public Observable<Issue> issues() {
        return issuesList().flatMap(new Func1<List<Issue>, Observable<Issue>>() {
            @Override
            public Observable<Issue> call(List<Issue> issues) {
                return Observable.from(issues);
            }
        });
    }

    private static Issue issueFromRealm(RealmIssue realmIssue) {
        final String title = realmIssue.getTitle();
        final String body = realmIssue.getBody();
        final User user = userFromRealm(realmIssue.getUser());
        final RealmList<RealmLabel> realmLabels = realmIssue.getLabels();
        final List<Label> labels = new ArrayList<>(realmLabels.size());
        for (RealmLabel realmLabel : realmLabels) {
            labels.add(labelFromRealm(realmLabel));
        }
        return new Issue(title, body, user, labels);
    }

    private static User userFromRealm(RealmUser realmUser) {
        return new User(realmUser.getLogin());
    }

    private static Label labelFromRealm(RealmLabel realmLabel) {
        return new Label(realmLabel.getName(), realmLabel.getColor());
    }
}
