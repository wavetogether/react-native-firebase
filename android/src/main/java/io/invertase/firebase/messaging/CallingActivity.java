package io.invertase.firebase.messaging;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import io.invertase.firebase.R;

public class CallingActivity extends Activity {
  private Vibrator v;
  private String destinationUid;
  private String preferencesName = "react-native";
  private Handler handler;

  @SuppressLint("SourceLockedOrientationActivity")
	@Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    destinationUid = getIntent().getExtras().getString("message").split(":")[1];
    String message = getIntent().getExtras().getString("message").split(":")[2];

    SharedPreferences pref = getApplicationContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();
    editor.putString("destinationUid", destinationUid).commit();

    ActionBar actionBar = this.getActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(Color.TRANSPARENT);
		}

		this.hideSoftMenuBar();

		if(getResources().getBoolean(R.bool.portrait_only)){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

    setContentView(R.layout.activity_calling);

    TextView tv_title = findViewById(R.id.call_title);
		tv_title.setText(message.split(";")[0]);

    final ImageView callDecline = findViewById(R.id.call_decline);
    final ImageView callAnswer = findViewById(R.id.call_answer);
    View.OnClickListener clickListener = new View.OnClickListener() {
      public void onClick(View v) {
        if (v.equals(callAnswer)) {

          SharedPreferences pref = getApplicationContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = pref.edit();
          editor.putString("androidCallUid", destinationUid).commit();
          editor.putString("destinationUid", "").commit();

					Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
          handler.removeCallbacksAndMessages(null);
          startActivity(i);
          finish();
        } else if (v.equals(callDecline)) {
          SharedPreferences pref = getApplicationContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = pref.edit();
          editor.putString("destinationUid", "").commit();

          handler.removeCallbacksAndMessages(null);
          finishAffinity();
        }
      }
    };
    callDecline.setOnClickListener(clickListener);
    callAnswer.setOnClickListener(clickListener);

    callAnswer.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN: {
            v.setAlpha(0.5f);
            break;
          }
          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_CANCEL: {
            v.setAlpha(1f);
            break;
          }
        }
        return false;
      }
    });

    callDecline.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN: {
            v.setAlpha(0.5f);
            break;
          }
          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_CANCEL: {
            v.setAlpha(1f);
            break;
          }
        }
        return false;
      }
    });


    this.vibrateApp();

    handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        finish();
      }
    }, 30000);
  }

	public void hideSoftMenuBar() {
		getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
				View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				this.cancelVibrate();
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

  private void vibrateApp() {
    AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    switch (am.getRingerMode()) {
      case AudioManager.RINGER_MODE_SILENT:
        break;
      case AudioManager.RINGER_MODE_VIBRATE:
      case AudioManager.RINGER_MODE_NORMAL:
        if (android.os.Build.VERSION.SDK_INT >= 23) {

          if (v == null) {
            v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
          }

          long[] pattern = { 500, 300 };
          v.vibrate(pattern, 0);
        }
        break;
    }
  }

  private void cancelVibrate() {
    if (v != null) {
      v.cancel();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    this.vibrateApp();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    this.cancelVibrate();
  }

  @Override
  protected void onPause() {
    super.onPause();
    this.cancelVibrate();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    this.cancelVibrate();
  }
}
