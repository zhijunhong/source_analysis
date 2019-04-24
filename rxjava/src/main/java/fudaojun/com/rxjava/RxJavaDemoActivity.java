package fudaojun.com.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;


public class RxJavaDemoActivity extends AppCompatActivity {
    private static final String LOG = "RxJavaDemoActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create 方法
        createMethodWidthObserver();
        createMethodWithSubScribe();

        //from 方法
        fromMethod();

        //just 方法
        justMethod();
    }

    /**
     * just方法
     */
    private void justMethod() {
        Observable.just("hello", "rxjava").subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "justMethod onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(LOG, "justMethod onNext()" + s);
            }
        });
    }

    /**
     * from方法使用
     */
    private void fromMethod() {
        String[] froms = {"hello", "rxjava"};
        Observable.from(froms).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "fromMethod onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(LOG, "fromMethod onNext()" + s);
            }
        });
    }

    /**
     * create方法使用Subscribe
     */
    private void createMethodWithSubScribe() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("你好");
                subscriber.onNext("你也好");
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "createMethodWithSubScribe onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(LOG, "createMethodWithSubScribe onNext()" + s);
            }
        });
    }

    /**
     * create方法使用Observer
     */
    private void createMethodWidthObserver() {
        Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("hello");
                subscriber.onNext("world");
                subscriber.onCompleted();
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "createMethodWidthObserver onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(LOG, "createMethodWidthObserver onNext()" + s);
            }
        });
    }
}
