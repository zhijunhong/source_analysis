package com.example.dagger2.model;

import com.example.dagger2.dao.Heater;
import com.example.dagger2.dao.Ice;
import com.example.dagger2.dao.IceBox;
import com.example.dagger2.dao.Pump;
import com.example.dagger2.dao.impl.ElectricHeater;
import com.example.dagger2.dao.impl.HaierIceBox;
import com.example.dagger2.dao.impl.NajiIce;
import com.example.dagger2.dao.impl.Thermosiphon;

import dagger.Module;
import dagger.Provides;

@Module // Module注明该类是Module类
public class CoffeeModule {
    @Provides
        // Provides注明该方法是用来提供依赖对象的方法
    Heater provideHeater() {
        return new ElectricHeater();
    }
    @Provides
    Pump providePump() {
        return new Thermosiphon();
    }

    @Provides
    Ice provideIce() {
        return new NajiIce();
    }

    @Provides
    IceBox provideIceBox(Ice ice) {
        return new HaierIceBox(ice);
    }
}