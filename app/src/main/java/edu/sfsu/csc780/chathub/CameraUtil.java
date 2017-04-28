package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import edu.sfsu.csc780.chathub.ui.MainActivity;

/**
 * Created by Xinlu on 4/27/17.
 */

public class CameraUtil {

    private static final String LOG_TAG = CameraUtil.class.getSimpleName();
    private static String READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    private static String WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static int GRANTED = PackageManager.PERMISSION_GRANTED;
    public static final int REQUEST_CODE = 200;
    private static final String[] CAMERA_PERMISSIONS = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};

    public static void startCamera(Activity activity) {
        if (!checkCameraPermission(activity)) {
            return;
        } else {
            Log.d(LOG_TAG, "the app has read and write permission. in CameraUtil startCamera()");
            MainActivity mMainActivity = (MainActivity)activity;
            mMainActivity.takePhoto();
        }
    }

    //checks if the app has permission to read and write the camera, and requests the permission if necessary
    private static boolean checkCameraPermission(Activity activity) {
        boolean isPermitted = false;
        if (ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE) !=
                GRANTED && ActivityCompat.checkSelfPermission(activity,
                WRITE_EXTERNAL_STORAGE) != GRANTED) {
            Log.d(LOG_TAG, "requesting permissions for starting a camera");
            ActivityCompat.requestPermissions(activity, CAMERA_PERMISSIONS, REQUEST_CODE);
        } else {
            isPermitted = true;
        }
        return isPermitted;
    }

}
