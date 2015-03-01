package com.kboyarshinov.realmrxjavaexample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.kboyarshinov.realmrxjavaexample.model.Issue;
import com.kboyarshinov.realmrxjavaexample.model.Label;
import com.kboyarshinov.realmrxjavaexample.model.User;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
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
        }
    }

    private void requestAllIssues() {
        Subscription subscription = dataService.issues().
            subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).
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
        if (compositeSubscription != null) {
            compositeSubscription.add(subscription);
        }
    }

    private void addNewIssue() {
        String title = "Feature request: removing issues";
        String body = "Add function to remove issues";
        User user = new User("kboyarshinov");
        List<Label> labels = new ArrayList<>();
        labels.add(new Label("feature", "FF5722"));
        Subscription subscription = dataService.newIssue(title, body, user, labels).
            subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).
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
        if (compositeSubscription != null) {
            compositeSubscription.add(subscription);
        }
    }
}
