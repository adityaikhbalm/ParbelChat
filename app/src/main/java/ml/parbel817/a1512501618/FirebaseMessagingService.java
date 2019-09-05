package ml.parbel817.a1512501618;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ipin on 11/27/2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private DatabaseReference mRootRef;

    private String online="false";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();

        String from_user_id = remoteMessage.getData().get("user_id");
        String from_user_name = remoteMessage.getData().get("user_name");
        String user_online = remoteMessage.getData().get("user_online");
        String user_login = remoteMessage.getData().get("user_login");

        if(!user_online.equals("true") && user_login.equals("true")){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(notification_title)
                            .setContentText(notification_message)
                            .setAutoCancel(true);

            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("user_id", from_user_id);
            resultIntent.putExtra("user_name", from_user_name);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);

            int mNotificationId = (int) System.currentTimeMillis();

            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }
}
