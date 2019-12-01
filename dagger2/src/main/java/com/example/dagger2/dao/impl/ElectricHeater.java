package com.example.dagger2.dao.impl;

import com.example.dagger2.dao.Heater;

public class ElectricHeater implements Heater {
 
    @Override
    public void on() {
        System.out.println("~ ~ ~ heating ~ ~ ~");
    }

    @Override
    public void off() {
        
    }
}