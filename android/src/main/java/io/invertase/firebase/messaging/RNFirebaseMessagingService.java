package io.invertase.firebase.messaging;

import android.content.Intent;
import android.content.ComponentName;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.content.Context;

import com.facebook.react.HeadlessJsTaskService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.content.SharedPreferences;


import io.invertase.firebase.Utils;

public class RNFirebaseMessagingService extends FirebaseMessagingService {
  private static final String TAG = "RNFMessagingService";

  public static final String MESSAGE_EVENT = "messaging-message";
  public static final String NEW_TOKEN_EVENT = "messaging-token-refresh";
  public static final String REMOTE_NOTIFICATION_EVENT = "notifications-remote-notification";

  @Override
  public void onNewToken(String token) {
    Log.d(TAG, "onNewToken event received");

    Intent newTokenEvent = new Intent(NEW_TOKEN_EVENT);
    LocalBroadcastManager
      .getInstance(this)
      .sendBroadcast(newTokenEvent);
  }

  @Override
  public void onMessageReceived(RemoteMessage message) {
    Log.d(TAG, "onMessageReceived event received");

    if (message.getNotification() != null) {
      // It's a notification, pass to the Notifications module
      Intent notificationEvent = new Intent(REMOTE_NOTIFICATION_EVENT);
      notificationEvent.putExtra("notification", message);

      if (message.getData().get("message") != null && message.getData().get("message").split(":")[0].equals("call_cancel")) {
        SharedPreferences pref = getSharedPreferences("react-native", Context.MODE_PRIVATE);
        String destinationUid = pref.getString("destinationUid", "");
        String uid = message.getData().get("message").split(":")[1];

        if (uid.equals(destinationUid)) {
          android.os.Process.killProcess(android.os.Process.myPid());
        }
      }

      // Broadcast it to the (foreground) RN Application
      LocalBroadcastManager
        .getInstance(this)
        .sendBroadcast(notificationEvent);
    } else {
      // It's a data message
      // If the app is in the foreground we send it to the Messaging module

      if (message.getData().get("message") != null && message.getData().get("message").split(":")[0].equals("call_cancel")) {
        SharedPreferences pref = getSharedPreferences("react-native", Context.MODE_PRIVATE);
        String destinationUid = pref.getString("destinationUid", "");
        String uid = message.getData().get("message").split(":")[1];

        if (uid.equals(destinationUid)) {
          android.os.Process.killProcess(android.os.Process.myPid());
        }
      }

      if (Utils.isAppInForeground(this.getApplicationContext())) {
        Intent messagingEvent = new Intent(MESSAGE_EVENT);
        messagingEvent.putExtra("message", message);
        // Broadcast it so it is only available to the RN Application
        LocalBroadcastManager
          .getInstance(this)
          .sendBroadcast(messagingEvent);
      } else {
        try {
          // If the app is in the background we send it to the Headless JS Service
          Intent headlessIntent = new Intent(
            this.getApplicationContext(),
            RNFirebaseBackgroundMessagingService.class
          );
          headlessIntent.putExtra("message", message);
          ComponentName name = this.getApplicationContext().startService(headlessIntent);
          if (name != null) {
            HeadlessJsTaskService.acquireWakeLockNow(this.getApplicationContext());
          }
        } catch (IllegalStateException ex) {
          Log.e(
            TAG,
            "Background messages will only work if the message priority is set to 'high'",
            ex
          );
        }
      }
    }
  }
}
