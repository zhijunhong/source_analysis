![EventBus3.0](https://github.com/greenrobot/EventBus/blob/master/EventBus-Publish-Subscribe.png)
## EventBus 3.0源码分析
#### 创建EventBus3实例
```
  /** Convenience singleton for apps using a process-wide EventBus instance. */
    public static EventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) { //防止多线程出现两个不同的EventBus对象
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }
```
这里使用**单例模式**获取EventBus3对象，目的是保证getDefault()方法得到的是同一个EventBus对象。第一次创建实例，会调用默认的EventBus()构造方法

```
    //key:订阅事件 value:订阅这个事件的所有订阅者集合
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    
    //key:订阅对象 value:这个订阅者订阅的事件集合
    private final Map<Object, List<Class<?>>> typesBySubscriber;
    
    //key:粘性事件的class对象 value:实践对象
    private final Map<Class<?>, Object> stickyEvents;
```
#### EventBus3提供默认默认构造方法
```

 /**
     * Creates a new EventBus instance; each instance is a separate scope in which events are delivered. To use a
     * central bus, consider {@link #getDefault()}.
     */
    public EventBus() {
        this(DEFAULT_BUILDER);
    }

    EventBus(EventBusBuilder builder) {
        logger = builder.getLogger();
        subscriptionsByEventType = new HashMap<>();
        typesBySubscriber = new HashMap<>();
        stickyEvents = new ConcurrentHashMap<>();
        mainThreadSupport = builder.getMainThreadSupport();
        mainThreadPoster = mainThreadSupport != null ? mainThreadSupport.createPoster(this) : null;
        backgroundPoster = new BackgroundPoster(this);
        asyncPoster = new AsyncPoster(this);
        indexCount = builder.subscriberInfoIndexes != null ? builder.subscriberInfoIndexes.size() : 0;
        subscriberMethodFinder = new SubscriberMethodFinder(builder.subscriberInfoIndexes,
                builder.strictMethodVerification, builder.ignoreGeneratedIndex);
        logSubscriberExceptions = builder.logSubscriberExceptions;
        logNoSubscriberMessages = builder.logNoSubscriberMessages;
        sendSubscriberExceptionEvent = builder.sendSubscriberExceptionEvent;
        sendNoSubscriberEvent = builder.sendNoSubscriberEvent;
        throwSubscriberException = builder.throwSubscriberException;
        eventInheritance = builder.eventInheritance;
        executorService = builder.executorService;
    }
```

### 注册事件过程
#### register方法的实现
```
 public void register(Object subscriber) {
	//首先获取订阅者的class对象
	Class<?> subscriberClass = subscriber.getClass();
	//1 获取订阅者订阅的事件集合
	List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
	synchronized (this) {
		for (SubscriberMethod subscriberMethod : subscriberMethods) {
			//2 订阅
			subscribe(subscriber, subscriberMethod);
		}
	}
}

public SubscriberMethod(Method method, Class<?> eventType, ThreadMode threadMode, int priority, boolean sticky) {
	this.method = method; //事件处理方法的Method对象
	this.threadMode = threadMode; //线程的ThreadMode
	this.eventType = eventType; //订阅的事件类型
	this.priority = priority; //事件优先级
	this.sticky = sticky; //是否接收粘性事件
}
```
 通过subscriberMethodFinder.findSubscriberMethods方法返回SubscriberMethod对象的集合，下面分析一下findSubscriberMethods方法的实现

#### SubscriberMethodFinder的实现
 > SubscriberMethodFinder类就是用来查找和缓存订阅者响应方法的信息的类。那么怎么能获得订阅者响应函数的相关信息呢？在3.0版本中,EventBus提供了一个EventBusAnnotationProcessor注解处理器来在编译期通过读取@Subscribe注解并解析，然后生成java类来保存所有订阅者关于订阅的信息，这样就比在运行时使用反射来获得这些订阅者的信息速度要快。
```
 List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
	//先从Method_CACHE取看是否有缓存，key：保存订阅类的类名，value:保存类中订阅的方法名
	List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
	if (subscriberMethods != null) {
		return subscriberMethods;
	}
	
	//是否忽略注解器生成的MyEventIndex类，默认false
	if (ignoreGeneratedIndex) {
		//利用反射来读取订阅类中的订阅方法
		subscriberMethods = findUsingReflection(subscriberClass);
	} else {
		//从注解器生成的MyEventBusIndex类中获得订阅类的订阅方法
		subscriberMethods = findUsingInfo(subscriberClass);
	}
	if (subscriberMethods.isEmpty()) {
		throw new EventBusException("Subscriber " + subscriberClass
				+ " and its super classes have no public methods with the @Subscribe annotation");
	} else {
		//保存到缓存中
		METHOD_CACHE.put(subscriberClass, subscriberMethods);
		return subscriberMethods;
	}
}
```
> findUsingInfo()方法就是通过查找MyEventBusIndex类中的信息来转换成List从而获得订阅类的相关订阅方法的信息集合。

#### findUsingReflection方法实现
```
private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {
	//FindState用来做订阅方法的校验和保存
	FindState findState = prepareFindState();
	findState.initForSubscriber(subscriberClass);
	while (findState.clazz != null) {
		//通过反射获得订阅方法信息
		findUsingReflectionInSingleClass(findState);
		//查找父类的订阅方法
		findState.moveToSuperclass();
	}
	//获取findState中的SubscriberMethod(也就是订阅方法List)并返回
	return getMethodsAndRelease(findState);
}
```
> 这里通过FindState类来做订阅方法的校验和保存，并通过FIND_STATE_POOL静态数组来保存FindState对象，可以使FindState复用，避免重复创建过多的对象，最终是通过findUsingReflectionSingleClass()来具体获得相关订阅方法的信息的：

```
private void findUsingReflectionInSingleClass(FindState findState) {
	Method[] methods;
	//通过反射得到方法数组
	try {
		// This is faster than getMethods, especially when subscribers are fat classes like Activities
		methods = findState.clazz.getDeclaredMethods();
	} catch (Throwable th) {
		// Workaround for java.lang.NoClassDefFoundError, see https://github.com/greenrobot/EventBus/issues/149
		methods = findState.clazz.getMethods();
		findState.skipSuperClasses = true;
	}
	//遍历Method
	for (Method method : methods) {
		int modifiers = method.getModifiers();
		if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			//保证必须只有一个事件参数
			if (parameterTypes.length == 1) {
				//得到注解
				Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
				if (subscribeAnnotation != null) {
					Class<?> eventType = parameterTypes[0];
					//校验是否添加该方法
					if (findState.checkAdd(method, eventType)) {
						ThreadMode threadMode = subscribeAnnotation.threadMode();
						//实例化SubscriberMethod对象并添加
						findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
								subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
					}
				}
			} else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
				String methodName = method.getDeclaringClass().getName() + "." + method.getName();
				throw new EventBusException("@Subscribe method " + methodName +
						"must have exactly 1 parameter but has " + parameterTypes.length);
			}
		} else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
			String methodName = method.getDeclaringClass().getName() + "." + method.getName();
			throw new EventBusException(methodName +
					" is a illegal @Subscribe method: must be public, non-static, and non-abstract");
		}
	}
}
```
> 上面代码运行后，订阅类的所有SubscriberMethod都已经被保存了，最后在通过getMethodsAndRelease方法返回List集合。

#### subsribe()方法的实现
```
// 必须在同步代码块中调用
private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
	//获取订阅事件的事件类型
	Class<?> eventType = subscriberMethod.eventType;
	//创建SubScription对象
	Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
	//检查是否已经添加过该SubScription对象，添加过则抛出异常
	CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
	if (subscriptions == null) {
		subscriptions = new CopyOnWriteArrayList<>();
		subscriptionsByEventType.put(eventType, subscriptions);
	} else {
		if (subscriptions.contains(newSubscription)) {
			throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
					+ eventType);
		}
	}
	//根据优先级来添加SubScription对象
	int size = subscriptions.size();
	for (int i = 0; i <= size; i++) {
		if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
			subscriptions.add(i, newSubscription);
			break;
		}
	}
	//将订阅者对象以及订阅的事件保存到typesBySubscriber中
	List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
	if (subscribedEvents == null) {
		subscribedEvents = new ArrayList<>();
		typesBySubscriber.put(subscriber, subscribedEvents);
	}
	subscribedEvents.add(eventType);
	//如果接受sticky事件则立即分发sticky事件
	if (subscriberMethod.sticky) {
		//eventInheritance表示是否分发订阅了响应事件类及父类事件的方法
		if (eventInheritance) {
			// Existing sticky events of all subclasses of eventType have to be considered.
			// Note: Iterating over all events may be inefficient with lots of sticky events,
			// thus data structure should be changed to allow a more efficient lookup
			// (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
			Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
			for (Map.Entry<Class<?>, Object> entry : entries) {
				Class<?> candidateEventType = entry.getKey();
				if (eventType.isAssignableFrom(candidateEventType)) {
					Object stickyEvent = entry.getValue();
					checkPostStickyEventToSubscription(newSubscription, stickyEvent);
				}
			}
		} else {
			Object stickyEvent = stickyEvents.get(eventType);
			checkPostStickyEventToSubscription(newSubscription, stickyEvent);
		}
	}
```

### EventBus事件分发过程
EventBus通过post方法来发送一个事件，首先看看post方法的实现过程
```
 /** Posts the given event to the event bus. */
    public void post(Object event) {
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);

        if (!postingState.isPosting) {
            postingState.isMainThread = isMainThread();
            postingState.isPosting = true;
            if (postingState.canceled) {
                throw new EventBusException("Internal error. Abort state was not reset");
            }
            try {
                while (!eventQueue.isEmpty()) {
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }
```
#### postingSingleEvent()方法方法实现
```
private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
	Class<?> eventClass = event.getClass();
	boolean subscriptionFound = false;
	//是否触发订阅了该事件（eventClass）的父类，以及接口的类的响应方法
	if (eventInheritance) {
		////查找eventClass类所有的父类以及接口
		List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
		int countTypes = eventTypes.size();
		//循环postSingleEventForEventType
		for (int h = 0; h < countTypes; h++) {
			Class<?> clazz = eventTypes.get(h);
			subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
		}
	} else {
	//post单个事件
		subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
	}
	//如果没有发现
	if (!subscriptionFound) {
		if (logNoSubscriberMessages) {
			Log.d(TAG, "No subscribers registered for event " + eventClass);
		}
		
		if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
				eventClass != SubscriberExceptionEvent.class) {
				//发送一个NoSubscriberEvent事件，如果我们需要处理这种状态，接收这个事件就可以了
			post(new NoSubscriberEvent(this, event));
		}
	}
}
```
> 从上面可知，实际上事件分发是在postSingleEventForEventType()方法里进行的，代码如下
```
private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
	CopyOnWriteArrayList<Subscription> subscriptions;
	//获取订阅了这个事件的Subscription列表.
	synchronized (this) {
		subscriptions = subscriptionsByEventType.get(eventClass);
	}
	if (subscriptions != null && !subscriptions.isEmpty()) {
		for (Subscription subscription : subscriptions) {
			postingState.event = event;
			postingState.subscription = subscription;
			//是否被中断
			boolean aborted = false;
			try {
				//分发给订阅者
				postToSubscription(subscription, event, postingState.isMainThread);
				aborted = postingState.canceled;
			} finally {
				postingState.event = null;
				postingState.subscription = null;
				postingState.canceled = false;
			}
			if (aborted) {
				break;
			}
		}
		return true;
	}
	return false;
}

private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
	switch (subscription.subscriberMethod.threadMode) {
		case POSTING:
			invokeSubscriber(subscription, event);
			break;
		case MAIN:
			if (isMainThread) {
				invokeSubscriber(subscription, event);
			} else {
				mainThreadPoster.enqueue(subscription, event);
			}
			break;
		case BACKGROUND:
			if (isMainThread) {
				backgroundPoster.enqueue(subscription, event);
			} else {
				invokeSubscriber(subscription, event);
			}
			break;
		case ASYNC:
			asyncPoster.enqueue(subscription, event);
			break;
		default:
			throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
	}
}
```
> 首先从subscriptionsByEventType里获得所有订阅了这个事件的Subscription列表，然后在通过postToSubScription()方法来分发事件，在postToSubScription()通过不同的threadMode在不同的线程里invoke()订阅者的方法，ThreadMode共有四类：

1. PostThread：默认的 ThreadMode，表示在执行 Post 操作的线程直接调用订阅者的事件响应方法，不论该线程是否为主线程（UI 线程）。当该线程为主线程时，响应方法中不能有耗时操作，否则有卡主线程的风险。适用场景：对于是否在主线程执行无要求，但若 Post 线程为主线程，不能耗时的操作；

2. MainThread：在主线程中执行响应方法。如果发布线程就是主线程，则直接调用订阅者的事件响应方法，否则通过主线程的 Handler 发送消息在主线程中处理——调用订阅者的事件响应函数。显然，MainThread类的方法也不能有耗时操作，以避免卡主线程。适用场景：必须在主线程执行的操作；

3. BackgroundThread：在后台线程中执行响应方法。如果发布线程不是主线程，则直接调用订阅者的事件响应函数，否则启动唯一的后台线程去处理。由于后台线程是唯一的，当事件超过一个的时候，它们会被放在队列中依次执行，因此该类响应方法虽然没有PostThread类和MainThread类方法对性能敏感，但最好不要有重度耗时的操作或太频繁的轻度耗时操作，以造成其他操作等待。适用场景：操作轻微耗时且不会过于频繁，即一般的耗时操作都可以放在这里；

4. Async：不论发布线程是否为主线程，都使用一个空闲线程来处理。和BackgroundThread不同的是，Async类的所有线程是相互独立的，因此不会出现卡线程的问题。适用场景：长耗时操作，例如网络访问。

#### invokeSubscriber(subscription, event)代码如下
```
void invokeSubscriber(Subscription subscription, Object event) {
	try {
		subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
	} catch (InvocationTargetException e) {
		handleSubscriberException(subscription, event, e.getCause());
	} catch (IllegalAccessException e) {
		throw new IllegalStateException("Unexpected exception", e);
	}
}
```
> 实际上就是通过反射调用了订阅者的订阅函数并把event对象作为参数传入，至此post()流程如上述所示。

### 解除注册过程
解除注册只要调用unregister()方法即可，实现如下：
```
public synchronized void unregister(Object subscriber) {
	//通过typesBySubscriber来取出这个subscriber订阅者订阅的事件类型,
	List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
	if (subscribedTypes != null) {
		//分别解除每个订阅了的事件类型
		for (Class<?> eventType : subscribedTypes) {
			unsubscribeByEventType(subscriber, eventType);
		}
		//从typesBySubscriber移除subscriber
		typesBySubscriber.remove(subscriber);
	} else {
		Log.w(TAG, "Subscriber to unregister was not registered before: " + subscriber.getClass());
	}
}
private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
	//subscriptionsByEventType里拿出这个事件类型的订阅者列表.
	List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
	if (subscriptions != null) {
		int size = subscriptions.size();
		//取消订阅
		for (int i = 0; i < size; i++) {
			Subscription subscription = subscriptions.get(i);
			if (subscription.subscriber == subscriber) {
				subscription.active = false;
				subscriptions.remove(i);
				i--;
				size--;
			}
		}
	}
}
```
最终分别从typesBySubscriber和subscriptions里分别移除订阅者以及相关信息即可

### 设计模式
#### 观察者模式
> 观察者模式是对象的行为模式，又叫发布-订阅（Publish/Subscribe）模式、模型-视图（Model/View）模式、源-监听器（Source/Listener）模式或从属者模式。观察者模式定义了一种一对多的依赖关系，让多个观察者对象同时监听某一个主题对象。这个主题对象在状态上发生变化时，会通知所有观察者对象，使它们能够自动更新自己。EventBus并不是标准的观察者模式的实现，但是它的整体就是一个发布/订阅框架，也拥有观察者模式的有点，比如：发布者和订阅者的解耦。