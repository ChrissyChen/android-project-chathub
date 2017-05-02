package edu.sfsu.csc780.chathub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import edu.sfsu.csc780.chathub.R;

/**
 * Created by Xinlu Chen on 5/1/17.
 */
public class SearchMessageActivity extends AppCompatActivity {

    private static final String TAG = "SearchMessageActivity";
    private EditText mSearchBarEditText;
    private Button mSearchButton;
    private Button mCancelButton;
    private ListView mSearchResultListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_message);

        Log.d(TAG, "on create in SearchMessageActivity");

        mSearchBarEditText = (EditText) findViewById(R.id.searchBarEditText);
        mSearchBarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSearchButton.setEnabled(true);
                    Log.d(TAG, "search button is enabled");
                } else {
                    mSearchButton.setEnabled(false);
                    Log.d(TAG, "search button is disabled");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMessage();
                Log.d(TAG, "search button is clicked: search message");
            }
        });

        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchMessageActivity.this, MainActivity.class));
                Log.d(TAG, "go back to main activity");
            }
        });


        mSearchResultListView = (ListView) findViewById(R.id.searchResultListView);
    }


    private void searchMessage() {

    }
}
