package com.codewithshadow.quickchat.SendNotification;





import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationReq;
import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationResponce;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationRequest {

    @Headers({"Content-Type:application/json","Authorization:key=AAAAmSbbh6Q:APA91bGGXUh9eaFbz7wKJ-LXllFcXITLJUb-55WTxouvXYXohQa87zeDH2gC664GDL52P4dlPfp1exl7fyDB7MiiG8S8_6q11NAkeIKPlpoTKVGkpqFIjhUH87F__euH9eR7r-nbwZGN\t\n"})
    @POST("send")
    Call<NotificationResponce> sent(@Body NotificationReq req);

}
