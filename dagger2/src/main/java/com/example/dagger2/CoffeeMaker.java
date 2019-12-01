package com.example.dagger2;

import com.example.dagger2.component.CoffeeComponent;
import com.example.dagger2.component.DaggerCoffeeComponent;
import com.example.dagger2.dao.Heater;
import com.example.dagger2.dao.Ice;
import com.example.dagger2.dao.IceBox;
import com.example.dagger2.dao.Pump;

import javax.inject.Inject;

public class CoffeeMaker {
    @Inject
    Heater heater;
    @Inject
    Pump pump;
    @Inject
    Ice ice;
    @Inject
    IceBox iceBox;

    CoffeeMaker() {
        // DaggerCoffeeComponent这个类会在编译时产生，所以可以build一下
        CoffeeComponent component = DaggerCoffeeComponent.create();
        // 注入
        component.inject(this);
    }

    public void brew() {
        heater.on();
        pump.pump();
        System.out.println(" [_]P coffee! [_]P ");
        iceBox.addIce();
        heater.off();
    }
}