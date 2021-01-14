package com.example.blind_wallet.Volunteer.Notifications.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAfTml9hw:APA91bGTTWg3k2X6jPTTPDA9UhxgGT4pUgYpdsWSAob8TT2Jii8Z4KBRsVOtsPYpZElQaooEQFkfi0wE3kli6TFsBShNAQ5nrFp5dLkBVRvMOfR6iRHlowqqNzEChhqWpD7bGoicSLm-" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

