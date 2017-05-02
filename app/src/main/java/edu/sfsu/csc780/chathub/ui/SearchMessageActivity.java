package edu.sfsu.csc780.chathub.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import java.util.List;

import edu.sfsu.csc780.chathub.R;

/**
 * Created by Xinlu Chen on 5/1/17.
 */
public class SearchMessageActivity extends AppCompatActivity {

    private static final String TAG = "SearchMessageActivity";
    private ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_message);

        Log.d(TAG, "on create in SearchMessageActivity");
        mListView = (ListView) findViewById(R.id.searchResultListView);


    }
}
