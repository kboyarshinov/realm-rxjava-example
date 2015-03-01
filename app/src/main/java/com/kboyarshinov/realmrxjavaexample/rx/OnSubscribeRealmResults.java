package com.kboyarshinov.realmrxjavaexample.rx;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class OnSubscribeRealmResults<T extends RealmObject> implements Observable.OnSubscribe<RealmResults<T>> {
    private Context context;
    private String dbName;

    public OnSubscribeRealmResults(Context context) {
        this.context = context;
        dbName = null;
    }

    public OnSubscribeRealmResults(Context context, String dbName) {
        this.context = context;
        this.dbName = dbName;
    }

    @Override
    public void call(final Subscriber<? super RealmResults<T>> subscriber) {
        final Realm realm = dbName != null ? Realm.getInstance(context, dbName) : Realm.getInstance(context);

        RealmResults<T> object = null;
        realm.beginTransaction();
        try {
            object = get(realm);
            realm.commitTransaction();
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            subscriber.onError(new RealmException("Error during transaction.", e));
            subscriber.onCompleted();
        } catch (Error e) {
            realm.cancelTransaction();
            subscriber.onError(e);
            subscriber.onCompleted();
        }
        if (object != null)
            subscriber.onNext(object);
        subscriber.onCompleted();

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                try {
                    realm.close();
                } catch (RealmException ex) {
                    subscriber.onError(ex);
                }
            }
        }));
    }

    public abstract RealmResults<T> get(Realm realm);
}
