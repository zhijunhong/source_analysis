package com.example.dagger2.dao.impl;

import com.example.dagger2.dao.Pump;

public class Thermosiphon implements Pump {
    
    public Thermosiphon() {
   
    }

    @Override
    public void pump() {
        System.out.println("=> => pumping => =>");
    }
}