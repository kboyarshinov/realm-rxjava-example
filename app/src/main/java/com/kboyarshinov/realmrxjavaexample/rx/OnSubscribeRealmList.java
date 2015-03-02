package com.kboyarshinov.realmrxjavaexample.rx;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class OnSubscribeRealmList<T extends RealmObject> implements Observable.OnSubscribe<RealmList<T>> {
    private Context context;
    private String fileName;

    public OnSubscribeRealmList(Context context) {
        this.context = context;
        fileName = null;
    }

    public OnSubscribeRealmList(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    @Override
    public void call(final Subscriber<? super RealmList<T>> subscriber) {
        final Realm realm = fileName != null ? Realm.getInstance(context, fileName) : Realm.getInstance(context);
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

        RealmList<T> object;
        realm.beginTransaction();
        try {
            object = get(realm);
            realm.commitTransaction();
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            subscriber.onError(new RealmException("Error during transaction.", e));
            return;
        } catch (Error e) {
            realm.cancelTransaction();
            subscriber.onError(e);
            return;
        }
        if (object != null) {
            subscriber.onNext(object);
        }
        subscriber.onCompleted();
    }

    public abstract RealmList<T> get(Realm realm);
}
