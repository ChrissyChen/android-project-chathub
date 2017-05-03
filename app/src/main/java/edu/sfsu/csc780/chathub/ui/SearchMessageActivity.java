package edu.sfsu.csc780.chathub.ui;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
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
    private ArrayList<ChatMessage> mResultList;
    private TextView mNoResultTextView;
    private SearchMessageAdapter mAdapter;

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
                    // clear the list view
                    mSearchResultListView.setAdapter(null);
                    mNoResultTextView.setVisibility(View.GONE);
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
        mNoResultTextView = (TextView) findViewById(R.id.noResult);
    }


    private void searchMessage() {
        mResultList = new ArrayList<>();
        String queryText = mSearchBarEditText.getText().toString();
        Log.d(TAG, "user types the keyword: " + queryText);
        Query query = mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                .orderByChild("text")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "messages" node with all children with text starts with queryText
                    for (DataSnapshot message : dataSnapshot.getChildren()) {
                        ChatMessage chatMessage = new ChatMessage((HashMap<String, String>) message.getValue());
                        mResultList.add(chatMessage);
                    }
                    mAdapter = new SearchMessageAdapter(mResultList, SearchMessageActivity.this);
                    mSearchResultListView.setAdapter(mAdapter);

                } else {
                    Log.d(TAG, "no result found");
                    // show a no result found message
                    mSearchResultListView.setAdapter(null);
                    mNoResultTextView.setVisibility(View.VISIBLE);
                    mSearchResultListView.setEmptyView(mNoResultTextView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public class SearchMessageAdapter extends BaseAdapter {

        private ArrayList<ChatMessage> messageList;
        private Context mContext;

        public SearchMessageAdapter(ArrayList<ChatMessage> messageList, Context mContext) {
            this.messageList = messageList;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return messageList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_message, parent, false);

            TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            TextView messengerTextView = (TextView) convertView.findViewById(R.id.messengerTextView);
            final ImageView messengerImageView = (CircleImageView) convertView.findViewById(R.id.messengerImageView);

            messageTextView.setText(messageList.get(position).getText());
            messengerTextView.setText(messageList.get(position).getName());

            SimpleTarget target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                    messengerImageView.setImageBitmap(bitmap);
                }
            };
            Glide.with(SearchMessageActivity.this)
                    .load(messageList.get(position).getPhotoUrl())
                    .asBitmap()
                    .into(target);

            return convertView;
        }
    }

}

