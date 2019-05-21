package fudaojun.com.retrofit2;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MyService {
    @GET("api/weather/city/101030100")
    Call<WeatherBean> getData();
}