package com.codewithshadow.quickchat.SendNotification;





import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationReq;
import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationResponce;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationRequest {

    @Headers({"Content-Type:application/json","Authorization:key=AAAA9Rz8h8I:APA91bGj6kYjCl9joh6jLooh8q1VjFHl_INMau812QMDujvr6Fbqnyz00BCtNvVf8D0hwUmbFlhlpFs5qV6wKoYK3ypojRppNSoa98YEN73TJP8v9WgKxRokygHkw8YjhtGEb5VsGH9c"})
    @POST("send")
    Call<NotificationResponce> sent(@Body NotificationReq req);

}
