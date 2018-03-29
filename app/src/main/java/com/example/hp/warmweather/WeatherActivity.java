package com.example.hp.warmweather;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hp.warmweather.gson.Forecast;
import com.example.hp.warmweather.gson.Weather;
import com.example.hp.warmweather.util.HttpUtil;
import com.example.hp.warmweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private TextView title_city;
    private TextView title_update_time;
    private TextView now_degree;
    private TextView weather_info;
    private LinearLayout forecastLayout;
    private TextView aqi_text;
    private TextView pm25_text;
    private TextView comfort_text;
    private TextView car_wash_text;
    private TextView sport_text;
    private ScrollView weatherLayout;
    private ImageView bacground;
    public SwipeRefreshLayout refreshLayout;
    public DrawerLayout drawerLayout;
    private Button home;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View dectorView=getWindow().getDecorView();
            dectorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        title_city=findViewById(R.id.title_city);
        title_update_time=findViewById(R.id.title_update_time);
        now_degree=findViewById(R.id.now_degree);
        weather_info=findViewById(R.id.weather_info);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqi_text=findViewById(R.id.aqi_text);
        pm25_text=findViewById(R.id.pm25_text);
        comfort_text=findViewById(R.id.comfort_text);
        car_wash_text=findViewById(R.id.car_wash_text);
        sport_text=findViewById(R.id.sport_text);
        weatherLayout=findViewById(R.id.weather_layout);
        bacground=findViewById(R.id.background);
        refreshLayout=findViewById(R.id.swipe_refresh);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        drawerLayout=findViewById(R.id.draw_layout);
        home=findViewById(R.id.nav_button);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        final String weatherId;
        if (weatherString!=null){
            Weather weather=Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
       String bingpic= prefs.getString("bing_pic",null);
        if (bingpic!=null){
            Glide.with(WeatherActivity.this).load(bingpic).into(bacground);
        }else {
            loadPic();
        }
    }

    /**
     * 加载必应的每日一图
     */
    private void loadPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingpic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(
                        WeatherActivity.this).edit();
                editor.putString("bing_pic",bingpic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingpic).into(bacground);
                    }
                });
            }
        });
    }
    public void requestWeather(String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="
                +weatherId+"&key=3868306f17c24a3090823f4498e2e71d";
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(WeatherActivity.this,"获取天气信息失败"
                               ,Toast.LENGTH_SHORT).show();
                       refreshLayout.setRefreshing(false);
                   }
               });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather= Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor= PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败"
                                    ,Toast.LENGTH_SHORT).show();
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }
    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        title_update_time.setText(updateTime);
        title_city.setText(cityName);
        now_degree.setText(degree);
        weather_info.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout
            ,false);
            TextView dateText=view.findViewById(R.id.date_text);
            TextView infoText=view.findViewById(R.id.info_text);
            TextView maxText=view.findViewById(R.id.max_text);
            TextView minText=view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqi_text.setText(weather.aqi.city.aqi);
            pm25_text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度:"+weather.suggestion.comfort.info;
        String carWash="洗车指数:"+weather.suggestion.carWash.info;
        String sport="运动建议:"+weather.suggestion.sport.info;
        comfort_text.setText(comfort);
        car_wash_text.setText(carWash);
        sport_text.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);


    }
}
