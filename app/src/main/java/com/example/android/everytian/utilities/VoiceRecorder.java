package com.example.android.everytian.utilities;

import android.animation.Animator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.everytian.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class VoiceRecorder extends LinearLayout {

    Context context;
    boolean expanded = false;
    boolean audioPlaying = false;
    boolean alreadyRecorded = false;

    FloatingActionButton recordButton;
    FloatingActionButton recordButtonOutter;
    FloatingActionButton playButton;

    int animationTime = 250;

    private MediaRecorder mRecorder;
    private static MediaPlayer mPlayer;
    private static final String LOG_TAG = "AudioRecording";
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    public VoiceRecorder(Context context) {
        super(context);
        initialize(context);
    }

    public VoiceRecorder(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VoiceRecorder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context) {
        this.context = context;
        LayoutInflater inflator = LayoutInflater.from(context);
        inflator.inflate(R.layout.voice_recorder, this, true);
        this.setVisibility(GONE);

        recordButton = findViewById(R.id.record_top);
        recordButtonOutter = findViewById(R.id.record_bottom);
        playButton = findViewById(R.id.play_top);

        final Context final_context = context;
        recordButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch ( motionEvent.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        stopAudio();
                        Vibrator v = (Vibrator) final_context.getSystemService(final_context.VIBRATOR_SERVICE);
                        MediaPlayer mediaPlayer = MediaPlayer.create(final_context, R.raw.beep);
                        recordStateActive(true);
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                startRecording();
                            }
                        });
                        mediaPlayer.start();
                        break;
                    case MotionEvent.ACTION_UP:
                        recordStateActive(false);
                        stopRecording();

                }
                return true;
            }
        });

        playButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioPlaying) {
                    playStateActive(false);
                    stopAudio();
                } else {
                    playStateActive(true);
                    playAudio();
                }
            }
        });

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording.3gp";
    }

    private void recordStateActive(boolean active) {
        if (active) {
            recordButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));
            recordButton.setImageResource(R.drawable.icon_record_big_active);
        } else {
            recordButton.setImageResource(R.drawable.icon_record_big);
            recordButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.primary_color));
        }
    }

    private void playStateActive(boolean active) {
        if (active) {
            playButton.setImageResource(R.drawable.stop_active);
            playButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));
        } else {
            playButton.setImageResource(R.drawable.icon_play);
            playButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.primary_color));
        }
    }

    public void show() {
        show(true);
    }

    public void show(boolean animated) {
        expanded = true;
        this.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FlipInX).duration(animationTime).playOn(this);
    }

    public void hide() {
        hide(true);
    }

    public void hide(boolean animated) {
        expanded = false;
        YoYo.with(Techniques.FlipOutX).duration(150).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                VoiceRecorder.this.setVisibility(View.GONE);
            }
        }).playOn(this);
    }

    public boolean toggle() {
        if (expanded) hide();
        else show();
        return expanded;
    }

    public void startRecording() {
        if(CheckPermissions()) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mFileName);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
            mRecorder.start();
//            Toast.makeText(context, "Recording Started", Toast.LENGTH_LONG).show();
        } else {
            RequestPermissions();
        }
    }

    public void stopRecording() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
                alreadyRecorded = true;
            } catch (Exception e) {

            }
            mRecorder = null;
//            Toast.makeText(context, "Recording Stopped", Toast.LENGTH_LONG).show();
        }
    }

    public void playAudio() {
        if (!alreadyRecorded) {
            YoYo.with(Techniques.Shake).playOn(recordButton);
            YoYo.with(Techniques.Shake).playOn(recordButtonOutter);
            Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
            v.vibrate(100);
            playStateActive(false);
            return;
        }

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playStateActive(false);
                    stopAudio();
                }
            });
            mPlayer.start();
            audioPlaying = true;
//            Toast.makeText(context, "Recording Started Playing", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopAudio() {
        audioPlaying = false;
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
//        Toast.makeText(context,"Playing Audio Stopped", Toast.LENGTH_SHORT).show();
    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
//                        Toast.makeText(context, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(context,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(context, RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
    private void RequestPermissions() {
        ActivityCompat.requestPermissions((android.app.Activity) context, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    public void reset() {
        stopAudio();

    }
}
