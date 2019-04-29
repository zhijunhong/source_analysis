## RxJava源码分析
#### RxJava常用方法及Demo
1. 移步查看Git：[RxJavaDemoActivity.java](https://github.com/zhijunhong/source_analysis/blob/master/rxjava/src/main/java/fudaojun/com/rxjava/RxJavaDemoActivity.java) 类中方法
#### RxJava源码分析
1. 由浅入深，首先我们分析类中没有线程切换的情况，相对比较容易
```
 //create 方法
 createMethodWidthObserver();
```
 具体方法内容
```
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
```
#### 首先我们分析 Observable.create(OnSubscribe<T> f)部分
跟进Rxjava的源码
```
    public final static <T> Observable<T> create(OnSubscribe<T> f) {
        return new Observable<T>(hook.onCreate(f));
    }
```
 代码里面有hook钩子方法，考虑一般情况，我们可以忽略钩子方法，最终仅是创建了Observable对象，继续跟进return new Observable<T>(hook.onCreate(f));
```
    protected Observable(OnSubscribe<T> f) {
        this.onSubscribe = f;
    }
```
 可以看到Observable.create(OnSubscribe<T> f)仅是在初始化Observable<T>类的成员变量final OnSubscribe<T> onSubscribe,也就是将我们Demo类RxJavaDemoActivity.java中的匿名类部类
```
new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("hello");
                subscriber.onNext("world");
                subscriber.onCompleted();
            }
        }
```
 赋值给Observable<T>类的成员变量final OnSubscribe<T> onSubscribe;
#### 接下来我们分析比较关键的subscribe(final Observer<? super T> observer)方法
跟进代码
```
 public final Subscription subscribe(final Observer<? super T> observer) {
        if (observer instanceof Subscriber) {
            return subscribe((Subscriber<? super T>)observer);
        }
        return subscribe(new Subscriber<T>() {

            @Override
            public void onCompleted() {
                observer.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onNext(T t) {
                observer.onNext(t);
            }

        });
    }
```
 if分支不是Subscriber类型所以直接跳过，继续调用内部的subscribe方法
```
    public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }
```
 可以看出调用自身重载的方法，参数分别是**new出来的new Subscriber<T>()和自身this**
```
private static <T> Subscription subscribe(Subscriber<? super T> subscriber, Observable<T> observable) {
  //...去除验证代码
        
        // new Subscriber so onStart it
        subscriber.onStart();
        
        /*
         * See https://github.com/ReactiveX/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls
         * to user code from within an Observer"
         */
        // if not already wrapped
        if (!(subscriber instanceof SafeSubscriber)) {
            // assign to `observer` so we return the protected version
            subscriber = new SafeSubscriber<T>(subscriber);
        }

        // The code below is exactly the same an unsafeSubscribe but not used because it would 
        // add a significant depth to already huge call stacks.
        try {
            // allow the hook to intercept and/or decorate
            hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber);
            return hook.onSubscribeReturn(subscriber);
        } catch (Throwable e) {
           //...去除异常捕获代码
        }
    }
```
 代码块中的onStart()方法是一个备用方法，可以在执行前调用;这里我们主要关注
```
hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber);
```
这又是一个钩子方法，当前直接忽略,执行hook.onSubscribeStart(observable, observable.onSubscribe)后返回的就是传入的observable.onSubscribe变量
**也就是第一部分赋值的Observable<T>类的成员变量final OnSubscribe<T> onSubscribe;**，在调用call(T t)方法，也就自然会调用到第一部分匿名类部类的call(T t)方法
```
new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("hello");
                subscriber.onNext("world");
                subscriber.onCompleted();
            }
        }
```
call(T t)方法里面的参数就是我们传入的**new出来的new Subscriber<T>()** 当调用Subscriber的onNext方法后就相当于调用了
```
  @Override
            public void onNext(T t) {
                observer.onNext(t);
            }
```
observer的onNext()方法，其中的observer变量就是我们一开始在我们Demo类RxJavaDemoActivity.java中的匿名类部类
```
new Observer<String>() {
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
        }
```
这也就解释了程序执行的结果为helloworld的原因




