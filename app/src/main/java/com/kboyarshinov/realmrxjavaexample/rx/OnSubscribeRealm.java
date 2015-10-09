package com.kboyarshinov.realmrxjavaexample.rx;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

abstract class OnSubscribeRealm<T> implements Observable.OnSubscribe<T> {
    private Context context;
    private String fileName;

    public OnSubscribeRealm(Context context) {
        this(context, null);
    }

    public OnSubscribeRealm(Context context, String fileName) {
        this.context = context.getApplicationContext();
        this.fileName = fileName;
    }

    private AtomicBoolean canceled;

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        if (canceled != null) {
            // prevent multiple subscribers
            subscriber.onError(new IllegalStateException("Only single subscriber allowed"));
            return;
        }
        canceled = new AtomicBoolean();
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                canceled.set(true);
                canceled = null;
            }
        }));

        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(context);
        if (fileName != null) {
            builder.name(fileName);
        }
        Realm realm = Realm.getInstance(builder.build());
        boolean withError = false;

        T object = null;
        try {
            realm.beginTransaction();
            object = get(realm);
            if (object != null && !canceled.get()) {
                realm.commitTransaction();
            } else {
                realm.cancelTransaction();
            }
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            subscriber.onError(new RealmException("Error during transaction.", e));
            withError = true;
        } catch (Error e) {
            realm.cancelTransaction();
            subscriber.onError(e);
            withError = true;
        }
        if (object != null && !canceled.get() && !withError) {
            subscriber.onNext(object);
        }

        try {
            realm.close();
        } catch (RealmException ex) {
            subscriber.onError(ex);
            withError = true;
        }
        if (!withError) {
            subscriber.onCompleted();
        }
    }

    public abstract T get(Realm realm);
}
