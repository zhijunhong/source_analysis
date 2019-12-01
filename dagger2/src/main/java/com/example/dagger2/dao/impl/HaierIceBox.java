package com.example.dagger2.dao.impl;

import com.example.dagger2.dao.Ice;
import com.example.dagger2.dao.IceBox;

public class HaierIceBox implements IceBox {
    Ice ice;
    public HaierIceBox(Ice ice) {
        this.ice = ice;
    }
	@Override
    public void addIce() {
        ice.add();
    }
}