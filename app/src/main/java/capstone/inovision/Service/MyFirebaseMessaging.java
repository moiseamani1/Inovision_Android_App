package capstone.inovision.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Random;

import capstone.inovision.R;
import capstone.inovision.ui.DoorActivitiesScreen;


public class MyFirebaseMessaging extends FirebaseMessagingService {

    final static String GROUP_KEY_EMAILS = "0";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendNotifications(remoteMessage);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotifications(RemoteMessage remoteMessage) {

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        Intent intent=new Intent(getBaseContext(), DoorActivitiesScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(getBaseContext(),0,intent, PendingIntent.FLAG_ONE_SHOT);

        String NOTIFICATION_CHANNEL_ID = "my_channel_id_02";
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int randomInt=new Random().nextInt(9999-1)+1;


        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Someone is at your door");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification.Builder builder= null;
            builder = new Notification.Builder(getBaseContext(),NOTIFICATION_CHANNEL_ID);
        builder.setAutoCancel(true).
                setWhen(System.currentTimeMillis()).
                setTicker("Inovision").setContentText(notification.getBody())
                .setContentIntent(pendingIntent)
                .setContentTitle(notification.getTitle())
                .setSmallIcon(R.drawable.ic_baseline_security_24)
                .setGroup(GROUP_KEY_EMAILS);

        notificationManager.notify(1,builder.build());


    }
}
