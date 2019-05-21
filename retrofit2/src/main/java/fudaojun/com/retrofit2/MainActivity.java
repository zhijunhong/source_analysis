package fudaojun.com.retrofit2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Retrofit2";
    private static final int DEFAULT_TIMEOUT = 30000;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRetrofit2();
        callHttpRequest();

    }

    /**
     * 请求Http
     */
    private void callHttpRequest() {
        MyService myService = retrofit.create(MyService.class);
        Call<WeatherBean> call = myService.getData();
        call.enqueue(new Callback<WeatherBean>() {
            @Override
            public void onResponse(Call<WeatherBean> call, Response<WeatherBean> response) {
                WeatherBean body = response.body();
                Log.i(TAG, body.getCityInfo().getCity());

            }

            @Override
            public void onFailure(Call<WeatherBean> call, Throwable t) {

            }
        });
    }

    /**
     * 初始化retrofit2
     */
    private void initRetrofit2() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).build();
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://t.weather.sojson.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
