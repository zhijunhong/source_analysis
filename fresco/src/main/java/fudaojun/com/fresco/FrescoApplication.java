package fudaojun.com.fresco;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class FrescoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
