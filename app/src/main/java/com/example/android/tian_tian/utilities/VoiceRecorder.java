package com.example.android.tian_tian.utilities;

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
import com.example.android.tian_tian.R;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class VoiceRecorder extends LinearLayout {

    Context context;
    boolean hasRecordedAudio = false;
    boolean audioPlaying = false;

    MaterialIconView recordButton;
    MaterialIconView playButton;
    MaterialIconView removeButton;

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

        recordButton = findViewById(R.id.record_button);
        playButton = findViewById(R.id.reproduce_button);
        removeButton = findViewById(R.id.delete_button);

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
                if (hasRecordedAudio) {
                    if (audioPlaying) {
                        playStateActive(false);
                        stopAudio();
                    } else {
                        playStateActive(true);
                        playAudio();
                    }
                }
            }
        });

        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasAudio()) deleteAudio();
            }
        });
    }

    private void recordStateActive(boolean active) {
        if (active) {
//            recordButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));
//            recordButton.setImageResource(R.drawable.icon_record_big_active);
        } else {
//            recordButton.setImageResource(R.drawable.icon_record_big);
//            recordButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.primary_color));
        }
    }

    public void deleteAudio() {
        stopRecording();
        File toDeleteFile = new File(mFileName);
        if (toDeleteFile.exists()) toDeleteFile.delete();
        hasRecordedAudio = false;
        playButton.setVisibility(GONE);
        removeButton.setVisibility(GONE);

    }

    public void setAudioName(String audioURI, boolean hasAudioAlready) {

        mFileName = audioURI;

        if (hasAudioAlready) setHasAudio();
    }

    public void setHasAudio() {
        hasRecordedAudio = true;
        playButton.setVisibility(VISIBLE);
        removeButton.setVisibility(VISIBLE);
    }

    public boolean hasAudio() {
        return hasRecordedAudio;
    }

    public void reset() {
        stopAudio();
    }

    private void playStateActive(boolean active) {
        if (active) {
            playButton.setIcon(MaterialDrawableBuilder.IconValue.STOP);
//            playButton.setImageResource(R.drawable.stop_active);
//            playButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));
        } else {
            playButton.setIcon(MaterialDrawableBuilder.IconValue.PLAY);
//            playButton.setImageResource(R.drawable.icon_play);
//            playButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.primary_color));
        }
    }

    public void startRecording() {
        if(CheckPermissions()) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mFileName);
            Log.w("444444444444444444", mFileName);

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
                setHasAudio();
            } catch (Exception e) {

            }
            mRecorder = null;
//            Toast.makeText(context, "Recording Stopped", Toast.LENGTH_LONG).show();
        }
    }

    public void playAudio() {
        mPlayer = new MediaPlayer();
        try {
//            Log.w("PLAYING", mFileName);
            Log.w("44444444444444444", mFileName);

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
}
