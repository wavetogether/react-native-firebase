package io.invertase.firebase.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ButtonReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Notification:","Notification Dialog Closed");
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(0);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,  0, new Intent(), 0);
		NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
		mb.setContentIntent(resultPendingIntent);
	}
}
