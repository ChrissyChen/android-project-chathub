package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import edu.sfsu.csc780.chathub.ui.MainActivity;

/**
 * Created by Xinlu Chen on 5/2/17.
 */

public class MainActivityInstrumentedTest {

    private static final String TAG = ImageUtilInstrumentedTest.class.getSimpleName();
    private static final int LARGE_DRAWABLE = R.drawable.android_bigbox;

    private Context mContext;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    private Activity activity;

    @Before
    public void setup() {
        activity = mActivityRule.getActivity();
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test() {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), LARGE_DRAWABLE);
        Uri uri = ImageUtil.savePhotoImage(mContext, bitmap);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setData(uri);
        ((MainActivity)activity).onActivityResult(1, Activity.RESULT_OK, intent);
    }
}
