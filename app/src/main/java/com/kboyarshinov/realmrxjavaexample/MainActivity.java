package com.kboyarshinov.realmrxjavaexample;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.kboyarshinov.realmrxjavaexample.model.Issue;
import com.kboyarshinov.realmrxjavaexample.model.Label;
import com.kboyarshinov.realmrxjavaexample.model.User;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private DataService dataService;
    private CompositeSubscription compositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataService = new RealmDataService(this);
        findViewById(R.id.add_new_issue_button).setOnClickListener(this);
        findViewById(R.id.request_all_issues_button).setOnClickListener(this);
        findViewById(R.id.request_with_zip).setOnClickListener(this);
        findViewById(R.id.request_with_flatmap).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeSubscription.unsubscribe();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.add_new_issue_button) {
            addNewIssue();
        } else if (id == R.id.request_all_issues_button) {
            requestAllIssues();
        } else if (id == R.id.request_with_zip) {
            requestWithZip();
        } else if (id == R.id.request_with_flatmap) {
            requestWithFlatMap();
        }
    }

    private void requestWithFlatMap() {
        if (compositeSubscription == null) {
            return;
        }

        Subscription subscription = dataService.findUser("kboyarshinov").flatMap(new Func1<User, Observable<List<Issue>>>() {
            @Override
            public Observable<List<Issue>> call(User user) {
                return dataService.issuesListByUser(user);
            }
        }).subscribe(
                new Action1<List<Issue>>() {
                    @Override
                    public void call(List<Issue> issues) {
                        Log.d(TAG, "Issues by user received with size " + issues.size());
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Request issues by user error", throwable);
                    }
                }
        );

        compositeSubscription.add(subscription);
    }

    private void requestAllIssues() {
        if (compositeSubscription == null) {
            return;
        }
        Subscription subscription = dataService.issuesList().
            observeOn(AndroidSchedulers.mainThread()).
            subscribeOn(Schedulers.io()).
            subscribe(
                    new Action1<List<Issue>>() {
                        @Override
                        public void call(List<Issue> issues) {
                            Log.d(TAG, "Issues received with size " + issues.size());
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "Request all issues error", throwable);
                        }
                    }
            );
        compositeSubscription.add(subscription);
    }

    private void addNewIssue() {
        if (compositeSubscription == null) {
            return;
        }
        String title = "Feature request: removing issues";
        String body = "Add function to remove issues";
        User user = new User("kboyarshinov");
        List<Label> labels = new ArrayList<>();
        labels.add(new Label("feature", "FF5722"));
        Subscription subscription = dataService.newIssue(title, body, user, labels).
            observeOn(AndroidSchedulers.mainThread()).
            subscribeOn(Schedulers.io()).
            subscribe(
                new Action1<Issue>() {
                    @Override
                    public void call(Issue issue) {
                        Log.d(TAG, "Issue with title " + issue.getTitle() + " successfully saved");
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Add new issue error", throwable);
                    }
                }
            );
        compositeSubscription.add(subscription);
    }

    private void requestWithZip() {
        if (compositeSubscription == null) {
            return;
        }

        Subscription subscription = Observable.zip(dataService.issues().take(10), dataService.issues().takeLast(10), new Func2<Issue, Issue, Pair<Issue, Issue>>() {
            @Override
            public Pair<Issue, Issue> call(Issue issue, Issue issue2) {
                return new Pair<>(issue, issue2);
            }
        }).toList().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Pair<Issue, Issue>>>() {
            @Override
            public void call(List<Pair<Issue, Issue>> pairs) {
                Log.d(TAG, "List of issue pairs " + pairs.size());
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG, "Error requesting issue pairs", throwable);
            }
        });
        compositeSubscription.add(subscription);
    }
}
