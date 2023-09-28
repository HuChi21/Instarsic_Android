package com.example.instagramclone.FCM;

import com.example.instagramclone.utilities.Constants;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key="+Constants.KEY_FCMSERVERKEY
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMPayLoad payload);
}