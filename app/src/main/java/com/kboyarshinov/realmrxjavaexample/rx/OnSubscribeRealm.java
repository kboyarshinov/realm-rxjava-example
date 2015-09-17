package com.kboyarshinov.realmrxjavaexample.rx;

import android.content.Context;
import android.support.annotation.Nullable;

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

    @Nullable
    private Thread thread;

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        thread = Thread.currentThread();
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (thread != null && !thread.isInterrupted()) {
                    thread.interrupt();
                }
            }
        }));

        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(context);
        if (fileName != null) {
            builder.name(fileName);
        }
        Realm realm = Realm.getInstance(builder.build());
        boolean interrupted = false;

        T object = null;
        try {
            while (!interrupted) {
                realm.beginTransaction();
                object = get(realm);
                interrupted = thread.isInterrupted();
                if (object != null) {
                    realm.commitTransaction();
                    break;
                } else {
                    realm.cancelTransaction();
                }
            }
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            subscriber.onError(new RealmException("Error during transaction.", e));
            return;
        } catch (Error e) {
            realm.cancelTransaction();
            subscriber.onError(e);
            return;
        }
        if (object != null && !interrupted) {
            subscriber.onNext(object);
        }

        boolean withError = false;
        try {
            realm.close();
        } catch (RealmException ex) {
            subscriber.onError(ex);
            withError = true;
        }
        thread = null;
        if (!withError) {
            subscriber.onCompleted();
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public abstract T get(Realm realm) ;
}
