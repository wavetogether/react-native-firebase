package io.invertase.firebase.messaging;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Build;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.graphics.Color;

import java.util.Locale;

import io.invertase.firebase.R;
import android.os.Handler;

public class CallingActivity extends Activity {
  private Vibrator v;
  private String destinationUid;
  private String preferencesName = "react-native";
  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    destinationUid = getIntent().getExtras().getString("message").split(":")[1];
    String message = getIntent().getExtras().getString("message").split(":")[2];

    SharedPreferences pref = getApplicationContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();
    editor.putString("destinationUid", destinationUid).commit();

    Boolean isGroup = false;
    if (message.split(";").length > 1) {
      isGroup = true;
    }

    Locale currentLocale = Locale.getDefault();
    Boolean isKo = false;

    if (currentLocale.getLanguage().equals("ko")) {
      isKo = true;
    }

    Window window = this.getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    ActionBar actionBar = this.getActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.setStatusBarColor(Color.parseColor("#4f1e91"));
    }

    setContentView(R.layout.activity_calling);

    TextView tv_title = findViewById(R.id.call_title);
    TextView tv_description = findViewById(R.id.call_description);
    TextView tv_decline = findViewById(R.id.tv_decline);
    TextView tv_accept = findViewById(R.id.tv_accept);

    if (isKo) {
      tv_decline.setText("거절하기");
      tv_accept.setText("들어가기");
      tv_title.setText(message.split(";")[0].concat("님의 방"));
      if (isGroup) {
        tv_description.setText(message.split(";")[1].concat("님이 부르고 있어요"));
      } else {
        tv_description.setText("님이 부르고 있어요");
      }
    } else {
      tv_decline.setText("Decline");
      tv_accept.setText("Accept");
      tv_title.setText(message.split(";")[0].concat("'s room"));
      tv_title.setText(message.split(";")[0].concat("'s room"));
      if (isGroup) {
        tv_description.setText(message.split(";")[1].concat(" are calling you"));
      } else {
        tv_description.setText("is calling you");
      }
    }

    final ImageView callDecline = findViewById(R.id.call_decline);
    final ImageView callAnswer = findViewById(R.id.call_answer);
    View.OnClickListener clickListener = new View.OnClickListener() {
      public void onClick(View v) {
        if (v.equals(callAnswer)) {
          Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
          // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

          SharedPreferences pref = getApplicationContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = pref.edit();
          editor.putString("androidCallUid", destinationUid).commit();
          editor.putString("destinationUid", "").commit();

          handler.removeCallbacksAndMessages(null);
          startActivity(i);
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
