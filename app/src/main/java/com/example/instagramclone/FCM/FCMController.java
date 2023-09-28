package com.example.instagramclone.FCM;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FCMController {
    private static final String BASE_URL = "https://fcm.googleapis.com/";

    private FCMService fcmService;

    public FCMController() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        fcmService = retrofit.create(FCMService.class);
    }

    public void sendNotification(String to, String title, String body) {
        Map<String,String> data = new HashMap<>();
        data.put("title",title);
        data.put("body",body);
        FCMPayLoad fcmPayLoad = new FCMPayLoad(to,data);
        fcmService.sendNotification(fcmPayLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    Log.d("subcribe","done0");

                },throwable -> {
                    Log.d("subcribe","error");

                });
    }
}