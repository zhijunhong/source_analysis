package fudaojun.com.rxjava.newdemo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import fudaojun.com.rxjava.R;
import fudaojun.com.rxjava.newdemo.module.Community;
import fudaojun.com.rxjava.newdemo.module.Hourse;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NewRxJavaDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG = "foo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_rxjava_demo);

        findViewById(R.id.btn_flat_map_and_filter).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_flat_map_and_filter:      //flatMap Filter使用
                launchFlatMapAndFilter();
                break;
            default:
                break;
        }
    }

    private void launchFlatMapAndFilter() {
        Observable.from(getCommunitiesFromServer()).flatMap(new Func1<Community, Observable<Hourse>>() {
            @Override
            public Observable<Hourse> call(Community community) {
                return Observable.from(community.getHourses());
            }
        }).filter(new Func1<Hourse, Boolean>() {
            @Override
            public Boolean call(Hourse hourse) {
                return hourse.getPrice() > 10000;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Hourse>() {
                    @Override
                    public void call(Hourse hourse) {
                        addHouseInformationToScreen(hourse);
                    }
                });
    }

    private void addHouseInformationToScreen(Hourse hourse) {
        Log.i(LOG, hourse.toString());
    }

    /**
     * 获取房源信息
     *
     * @return
     */
    private List<Community> getCommunitiesFromServer() {
        List<Community> communities = new ArrayList<>();

        Community community = new Community();
        List<Hourse> hourses = new ArrayList<>();
        Hourse hourse = new Hourse();
        hourse.setName("home1");
        hourse.setPrice(20000);
        hourses.add(hourse);
        community.setHourses(hourses);
        communities.add(community);

        Community community2 = new Community();
        List<Hourse> hourses2 = new ArrayList<>();
        Hourse hourse2 = new Hourse();
        hourse2.setName("home2");
        hourse2.setPrice(50000);
        hourses2.add(hourse2);
        community2.setHourses(hourses2);
        communities.add(community2);

        return communities;
    }

}
