package edu.sfsu.csc780.chathub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by cjkriese on 3/12/17.
 */

public class ExampleInstrumentedTest {

    private static final String TAG = ExampleInstrumentedTest.class.getSimpleName();
    private static final int LARGE_DRAWABLE = R.drawable.android_bigbox;
    private static final int SMALL_DRAWABLE = R.drawable.android_smallbox;

    private Context mContext;
    private File mImageFile;

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
    }


    @Test
    public void verify_savePhotoImage() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), LARGE_DRAWABLE);
        Uri uri = ImageUtil.savePhotoImage(mContext, bitmap);
        mImageFile = new File(uri.getPath());
        Log.d(TAG, uri.toString());
        assertNotNull(uri);
        assertTrue(mImageFile.length() > 0);
        assertTrue(BitmapFactory.decodeFile(uri.getPath()).sameAs(bitmap));
        assertTrue(ImageUtil.getBitmapForUri(mContext, uri).sameAs(bitmap));
    }

    @Test
    public void scaleBitmap_verifyLarge() throws Exception {

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), LARGE_DRAWABLE);
        Bitmap resizedBitmap = ImageUtil.scaleImage(bitmap);
        assertNotEquals(bitmap, resizedBitmap);
        assertTrue((resizedBitmap.getHeight() + resizedBitmap.getWidth()) <= ImageUtil.MAX_LINEAR_DIMENSION);
    }


    @Test
    public void scaleBitmap_verifySmall() throws Exception {

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), SMALL_DRAWABLE);
        Bitmap resizedBitmap = ImageUtil.scaleImage(bitmap);
        assertEquals(bitmap, resizedBitmap);
        assertTrue((resizedBitmap.getHeight() + resizedBitmap.getWidth()) <= ImageUtil.MAX_LINEAR_DIMENSION);
    }

    @After
    public void cleanup() {
        if (mImageFile != null && mImageFile.exists()) {
            mImageFile.delete();
        }
    }
}
