package edu.sfsu.csc780.chathub;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;

import edu.sfsu.csc780.chathub.ui.MainActivity;

/**
 * Created by Xinlu Chen on 4/29/17.
 */

public class AudioUtil {

    private static final String LOG_TAG = AudioUtil.class.getSimpleName();
//    private static String READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE;
//    private static String WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static int GRANTED = PackageManager.PERMISSION_GRANTED;
    public static final int REQUEST_CODE = 300;
    private static final String[] VOICE_PERMISSIONS = {RECORD_AUDIO};
    private static boolean sIsStart = false;
    private static MediaRecorder sMediaRecorder = null;

    public static void startAudio(Activity activity, ImageButton voiceButton) {
        if (!checkAudioPermission(activity)) {
            return;
        } else {
            Log.d(LOG_TAG, "the app has record audio permission. in AudioUtil startAudio()");
            recordVoice(activity, voiceButton);
        }
    }

    //checks if the app has permission to record audio, and requests the permission if necessary
    private static boolean checkAudioPermission(Activity activity) {
        boolean isPermitted = false;
        if (ActivityCompat.checkSelfPermission(activity, RECORD_AUDIO) != GRANTED ) {
            Log.d(LOG_TAG, "requesting permissions for recording audio");
            ActivityCompat.requestPermissions(activity, VOICE_PERMISSIONS, REQUEST_CODE);
        } else {
            isPermitted = true;
        }
        return isPermitted;
    }

    public static void recordVoice(Activity activity, ImageButton voiceButton) {
        if(!sIsStart){
            startRecord(activity);
            voiceButton.setBackgroundColor(Color.RED);
            sIsStart = true;
        }else{
            stopRecord();
            voiceButton.setBackgroundColor(Color.WHITE);
            sIsStart = false;
        }
    }

    private static void startRecord(Context context) {
        if(sMediaRecorder == null){
            //File dir = new File(Environment.getExternalStorageDirectory(),"AudioRecords");
            File dir = context.getExternalFilesDir("AudioRecords");
            Log.d(LOG_TAG, "start record file path: " + dir.getAbsolutePath());
            Log.d(LOG_TAG, "start record file path: " + dir.toString());
            if(!dir.exists()){
                dir.mkdirs();
            }
            File soundFile = new File(dir,System.currentTimeMillis()+".amr");
            if(!soundFile.exists()){
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "Can't create voice recording file");
                }
            }
            sMediaRecorder = new MediaRecorder();
            try {
                sMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  //set Microphone audio source
            } catch (Exception e) {
                e.printStackTrace();
            }
            sMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);   //set the output as a AMR WB file format
            sMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);   //set the AMR (Wideband) audio codec
            sMediaRecorder.setOutputFile(soundFile.getAbsolutePath());
            try {
                sMediaRecorder.prepare();
                sMediaRecorder.start();  //start recording
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void stopRecord(){
        if(sMediaRecorder != null){
            sMediaRecorder.stop();
            sMediaRecorder.release();
            sMediaRecorder = null;
        }
    }
}