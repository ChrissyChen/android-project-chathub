package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageButton;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import edu.sfsu.csc780.chathub.ui.MainActivity;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Xinlu Chen on 5/3/17.
 */

public class AudioUtilInstrumentedTest {

    private static final String TAG = AudioUtilInstrumentedTest.class.getSimpleName();

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    private Activity activity;

    @Before
    public void setup() {
        activity = mActivityRule.getActivity();
    }

    @Test
    public void audioRecordTest() {
        File dir = activity.getExternalFilesDir("AudioRecords");
        int len = dir.listFiles().length;
        AudioUtil.recordVoice(activity, new ImageButton(activity));
        assertEquals(dir.listFiles().length, len + 1);
    }
}
