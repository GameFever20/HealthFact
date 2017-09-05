package utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import app.healthfact.craftystudio.healthfact.MainActivity;
import app.healthfact.craftystudio.healthfact.R;


/**
 * Created by Aisha on 8/16/2017.
 */

public class FirebasePushNotification extends FirebaseMessagingService {


    String healthFactId;

    Intent intent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {


            healthFactId = remoteMessage.getData().get("healthfact");


            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("healthFactID", healthFactId);

            showNotification(remoteMessage.getData().get("notificationT"), remoteMessage.getData().get("notificationB"));

        }

        // Check if message contains a notification payload.
        /*if (remoteMessage.getNotification() != null) {
            showNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }*/
    }

    private void showLinkNotification(String notificationT, String notificationB, String linkToOpen) {

        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkToOpen));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), linkIntent, PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(notificationT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(notificationB)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int id = (int) System.currentTimeMillis();


        notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
    }

    private void showNotification(String title, String body) {


        // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int id = (int) System.currentTimeMillis();


        notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
    }
}
