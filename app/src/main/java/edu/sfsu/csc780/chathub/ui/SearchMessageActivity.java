package edu.sfsu.csc780.chathub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;

/**
 * Created by Xinlu Chen on 5/1/17.
 */
public class SearchMessageActivity extends AppCompatActivity {

    private static final String TAG = "SearchMessageActivity";
    private final String MESSAGES_CHILD = "messages";
    private DatabaseReference mFirebaseDatabaseReference =
                    FirebaseDatabase.getInstance().getReference();
    private EditText mSearchBarEditText;
    private Button mSearchButton;
    private Button mCancelButton;
    private ListView mSearchResultListView;
    private ArrayAdapter mArrayAdapter;
    private ArrayList<String> mResultList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_message);
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
        mResultList = new ArrayList<>();
        String queryText = mSearchBarEditText.getText().toString();
        Log.d(TAG, "user types the keyword: " + queryText);
        Query query = mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                .orderByChild("text")
                .startAt(queryText)
                .endAt(queryText+"\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "messages" node with all children with text starts with queryText
                    for (DataSnapshot message : dataSnapshot.getChildren()) {
//                        Log.d(TAG, "related messages: " + message.getKey());  //-KikpYt6Ns9jf9V11AHL
//                        Log.d(TAG, "related messages: " + message.getValue().toString());//{text=test, name=Chrissy Chen, photoUrl=https://lh5.googleusercontent.com/-j6CO4VXGJy4/AAAAAAAAAAI/AAAAAAAAAAA/ADPlhfItI0yF2xEd1MXMujMM9f1p1dUC8w/s96-c/photo.jpg}
//                        Log.d(TAG, "related messages: " + message.toString()); //DataSnapshot { key = -KikpYt6Ns9jf9V11AHL, value = {text=test, name=Chrissy Chen, photoUrl=https://lh5.googleusercontent.com/-j6CO4VXGJy4/AAAAAAAAAAI/AAAAAAAAAAA/ADPlhfItI0yF2xEd1MXMujMM9f1p1dUC8w/s96-c/photo.jpg} }
                        ChatMessage chatMessage = new ChatMessage((HashMap<String, String>) message.getValue());
                        mResultList.add(chatMessage.getText());
                    }
                    mArrayAdapter = new ArrayAdapter(SearchMessageActivity.this, R.layout.item_message, R.id.messageTextView, mResultList);
                    mSearchResultListView.setAdapter(mArrayAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}
