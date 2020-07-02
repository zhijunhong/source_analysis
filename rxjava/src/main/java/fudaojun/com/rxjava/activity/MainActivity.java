package fudaojun.com.rxjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import fudaojun.com.rxjava.R;
import fudaojun.com.rxjava.newdemo.activity.NewRxJavaDemoActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //跳转Rxjava Demo
        findViewById(R.id.tv_rxjava_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RxJavaDemoActivity.class);
                startActivity(intent);
            }
        });

        //跳转Rxjava Demo
        findViewById(R.id.new_tv_rxjava_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewRxJavaDemoActivity.class);
                startActivity(intent);
            }
        });
    }
}
