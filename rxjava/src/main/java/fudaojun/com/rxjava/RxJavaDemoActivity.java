package fudaojun.com.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;


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

        //map方法
        mapMethod();

        //flatMap方法
        flatMapMethod();

        //scan方法
        scanMethod();

        //过滤操作
        filterMethod();

        //meger方法
        mergeMethod();

        //zip方法
        zipMethod();
    }

    /**
     * zip 方法
     */
    private void zipMethod() {
        Observable<Integer> observable1 = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> observable2 = Observable.just(7, 8, 9, 0);

        Observable.zip(observable1, observable2, new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) {
                return integer + integer2;
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "zipMethod onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                Log.i(LOG, "zipMethod onNext()" + integer);
            }
        });
    }

    /**
     * merge 方法
     */
    private void mergeMethod() {
        Observable<Integer> observable1 = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> observable2 = Observable.just(7, 8, 9, 0);

        Observable.merge(observable1, observable2).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "mergeMethod onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                Log.i(LOG, "mergeMethod onNext()" + integer);
            }
        });

    }

    /**
     * filter方法
     */
    private void filterMethod() {
        Observable.just(1, 2, 3, 4, 5, 6).filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer > 3;
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "filterMethod onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                Log.i(LOG, "filterMethod onNext()" + integer);
            }
        });
    }

    /**
     * scan方法
     */
    private void scanMethod() {
        Observable.just(1, 2, 3, 4, 5).scan(new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) {
                return integer * integer2;
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "scanMethod onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                Log.i(LOG, "scanMethod onNext()" + integer);
            }
        });
    }

    /**
     * flatMap方法
     */
    private void flatMapMethod() {
        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("rxjava");
        Observable.from(list).flatMap(new Func1<String, Observable<String>>() {
            @Override
            public Observable<String> call(String s) {
                return Observable.just(s.toUpperCase());
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "flatMapMethod onComplete()");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(LOG, "flatMapMethod onNext()" + s);
            }
        });
    }

    /**
     * map方法
     */
    private void mapMethod() {
        Observable.just("Hello", "world").map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return s.toUpperCase();
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "mapMethod onComplete()\n");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(LOG, "mapMethod onNext()" + s);
            }
        });
    }

    /**
     * just方法
     */
    private void justMethod() {
        Observable.just("hello", "rxjava").subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(LOG, "justMethod onComplete()\n");
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
                Log.i(LOG, "fromMethod onComplete()\n");
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
                Log.i(LOG, "createMethodWithSubScribe onComplete()\n");
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
                Log.i(LOG, "createMethodWidthObserver onComplete()\n");
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
