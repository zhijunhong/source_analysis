package com.example.dagger2.component;

import com.example.dagger2.CoffeeMaker;
import com.example.dagger2.model.CoffeeModule;

import dagger.Component;

// 这里是声明要从CoffeeModule中去找对应的依赖，从`CoffeeModule`中去通过`provideXXX`方法来获取对应的对象
@Component(modules = CoffeeModule.class)
// 该接口会在编译时自动生成对应的实现类，这里是DaggerCoffeeComponent
public interface CoffeeComponent {
    // 提供一个供目标类使用的注入方法,该方法表示要将Module中的管理类注入到哪个类中，这里当然是CoffeeMaker，因为我们要用他俩去生产咖啡
    void inject(CoffeeMaker maker);
}