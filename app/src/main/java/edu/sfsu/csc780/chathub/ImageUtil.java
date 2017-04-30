package edu.sfsu.csc780.chathub;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cjkriese on 3/10/17.
 */

public class ImageUtil {

    public static final double MAX_LINEAR_DIMENSION = 500.0;
    public static final String IMAGE_FILE_NAME_PREFIX = "chathub-";
    private static final String TAG = ImageUtil.class.getSimpleName();
    private static File imageFile;

//    public static File getImageFile () {
//        return imageFile;
//    }

    public static Bitmap scaleImage(Bitmap bitmap) {
        int originalHeight = bitmap.getHeight();
        int originalWidth = bitmap.getWidth();
        double scaleFactor =  MAX_LINEAR_DIMENSION / (double)(originalHeight + originalWidth);

        // We only want to scale down images, not scale upwards
        if (scaleFactor < 1.0) {
            int targetWidth = (int) Math.round(originalWidth * scaleFactor);
            int targetHeight = (int) Math.round(originalHeight * scaleFactor);
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        } else {
            return bitmap;
        }
    }

    public static Uri savePhotoImage(Context context, Bitmap imageBitmap) {
        File photoFile = null;
        try {
            photoFile = createImageFile(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile == null) {
            Log.d(TAG, "Error creating media file");
            return null;
        }

        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return Uri.fromFile(photoFile);
    }

    protected static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String imageFileNamePrefix = IMAGE_FILE_NAME_PREFIX + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//        Log.d(TAG, "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());

        imageFile = File.createTempFile(
                imageFileNamePrefix,    /* prefix */
                ".jpg",                 /* suffix */
                storageDir              /* directory */
        );
        return imageFile;
    }

    public static Bitmap getBitmapForUri(Context context, Uri imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void saveImageToAlbum(Context context) {

        // insert image file to album
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    imageFile.getAbsolutePath(), imageFile.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "Image file cannot found in saveImageToAlbum().");
        }

//        Log.d(TAG, "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
//        Log.d(TAG, "file://" + context.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString());
//        Log.d(TAG, "file://" + context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());

        //broadcast the updates to the album
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString())));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + context.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString())));

    }
}
