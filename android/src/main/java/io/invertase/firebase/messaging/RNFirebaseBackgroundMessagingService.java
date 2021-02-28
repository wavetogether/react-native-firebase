package io.invertase.firebase.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.google.firebase.messaging.RemoteMessage;

import javax.annotation.Nullable;

public class RNFirebaseBackgroundMessagingService extends HeadlessJsTaskService {

  @Override
  protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {

    HeadlessJsTaskConfig config = null;
    Bundle extras = intent.getExtras();
    if (extras != null) {
      RemoteMessage message = intent.getParcelableExtra("message");
      WritableMap messageMap = MessagingSerializer.parseRemoteMessage(message);
      config = new HeadlessJsTaskConfig("RNFirebaseBackgroundMessage", messageMap, 60000, false);
    }

    RemoteMessage _message = intent.getParcelableExtra("message");

    if (_message.getData().get("message").split(":")[0].equals("call")
      || _message.getData().get("message").split(":")[0].equals("call_group")) {
      Intent callIntent = new Intent(getApplicationContext(), CallingActivity.class);
      callIntent.setAction(Intent.ACTION_SCREEN_ON);
      callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

      Bundle b = new Bundle();
      b.putString("message", _message.getData().get("message"));
      callIntent.putExtras(b);

			Handler delayHandler = new Handler();
			delayHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					startActivity(callIntent);
				}
			}, 1000);

    }

    return config;
  }
}
