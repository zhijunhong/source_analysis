package com.example.dagger2.dao.impl;

import com.example.dagger2.dao.Ice;

public class NajiIce implements Ice {
    public NajiIce() {

    }
    @Override
    public void add() {
        System.out.println("加冰了，心飞扬");
    }
}