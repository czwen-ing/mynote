package com.example.mynote.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.example.mynote.R;
import com.example.mynote.data.Notes;
import com.example.mynote.utils.DataUtils;

import java.io.IOException;

public class AlarmActivity extends Activity {

    private int noteId;
    private String content;
    private MediaPlayer mediaPlayer;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        if (!isScreenOn()) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        }

//        setContentView(R.layout.activity_alarm);
        noteId = Integer.valueOf(getIntent().getData().getPathSegments().get(1));
        content = DataUtils.getContentById(getContentResolver(),noteId);
        content = content.length() > 40 ? content.substring(0,
                40) + "..." : content;
        mediaPlayer = new MediaPlayer();
        showAlarmDialog();
        playAlarmSound();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void showAlarmDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this,
                android.R.style.Theme_Holo_Light_Dialog);
        dialog.setTitle("来自我的便签的提醒");
        dialog.setMessage(content);
        dialog.setNegativeButton("不要吵",null);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopAlarmSound();
                finish();
            }
        });
        dialog.setPositiveButton("瞧一瞧", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AlarmActivity.this, NoteEditActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Notes.NoteColumns.ID, noteId);
                startActivity(intent);
            }
        }).show();
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void playAlarmSound() {
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_ALARM);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        try {
            mediaPlayer.setDataSource(this,uri);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }
}
