/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import edu.sfsu.csc780.chathub.AudioUtil;
import edu.sfsu.csc780.chathub.CameraUtil;
import edu.sfsu.csc780.chathub.ImageUtil;
import edu.sfsu.csc780.chathub.LocationUtils;
import edu.sfsu.csc780.chathub.MapLoader;
import edu.sfsu.csc780.chathub.MessageUtil;
import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;

import static edu.sfsu.csc780.chathub.ImageUtil.saveImageToAlbum;
import static edu.sfsu.csc780.chathub.ImageUtil.savePhotoImage;
import static edu.sfsu.csc780.chathub.ImageUtil.scaleImage;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, MessageUtil.MessageLoadListener {

    private static final String TAG = "MainActivity";
    public static final int MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int LOCATION_PERMISSION = LocationUtils.REQUEST_CODE;
    private static final int CAMERA_PERMISSION = CameraUtil.REQUEST_CODE;
    private static final int AUDIO_PERMISSION = AudioUtil.REQUEST_CODE;
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseRecyclerAdapter<ChatMessage, MessageUtil.MessageViewHolder>
            mFirebaseAdapter;
    private ImageButton mImageButton;
    private ImageButton mLocationButton;
    private ImageButton mCameraButton;
    private ImageButton mVoiceButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        //Initialize Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Log.d("mUser", String.valueOf(mUser));
        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mUser.getDisplayName();
            //Log.d(TAG, mUsername);
            if (mUser.getPhotoUrl() != null) {
                mPhotoUrl = mUser.getPhotoUrl().toString();
            }
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mFirebaseAdapter = MessageUtil.getFirebaseAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mMessageRecyclerView);

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MSG_LENGTH_LIMIT)});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send messages on click.
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl);
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
            }
        });

        mImageButton = (ImageButton) findViewById(R.id.shareImageButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick pickImage ==============");
                pickImage();
            }
        });

        mLocationButton = (ImageButton) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LocationUtils.checkLocationPermission(MainActivity.this);
                //Log.d(TAG, "onClick checkLocationPermission!!!!!!!!!!!!!!!!");
//                LocationUtils.startLocationUpdates(MainActivity.this);
//                Log.d(TAG, "onClick startLocationUpdates!!!!!!!!!!!!!!!!");
                loadMap();

            }
        });

        mCameraButton = (ImageButton) findViewById(R.id.cameraButton);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick startcamera~~~~~~~~~~");
                CameraUtil.startCamera(MainActivity.this);
            }
        });

        mVoiceButton = (ImageButton) findViewById(R.id.voiceButton);
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick startAudio~~~~~~~~~~");
                AudioUtil.startAudio(MainActivity.this, mVoiceButton);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationUtils.startLocationUpdates(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;

            case R.id.search_message_menu:
                startActivity(new Intent(this, SearchMessageActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadComplete() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void pickImage() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    public void loadMap() {
        Loader<Bitmap> loader = getSupportLoaderManager().initLoader(0, null,
                new LoaderManager.LoaderCallbacks<Bitmap>() {
                    @Override
                    public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
                        return new MapLoader(MainActivity.this);
                    }

                    @Override
                    public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap result) {
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                        mLocationButton.setEnabled(true);
                        if (result == null) return;
                        // Resize if too big for messaging
                        Bitmap resizedBitmap = scaleImage(result);
                        Uri uri = null;
                        if (result != resizedBitmap) {
                            uri = savePhotoImage(MainActivity.this, resizedBitmap);
                        } else {
                            uri = savePhotoImage(MainActivity.this, result);
                        }
                        uploadImageMessage(uri);
                        // add for fixing the duplicate loaction map bug
                        MainActivity.this.getSupportLoaderManager().destroyLoader(0);
                    }

                    @Override
                    public void onLoaderReset(final Loader<Bitmap> loader) {
                    }
                });
        loader.forceLoad();
    }

    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        boolean isGranted = (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED);

        switch (requestCode) {
            case LOCATION_PERMISSION: {
                if (isGranted) {
                    LocationUtils.startLocationUpdates(this);
                    Log.d(TAG, "onRequestPermissionsResult startLocationUpdates!!!!!!!!!!!!!!!!");
//                    loadMap();
//                    Log.d(TAG, "onRequestPermissionsResult loadMap +++++++++++++++");
                } else {
//                    mLocationButton.setEnabled(false);
//                    Log.d(TAG, "onRequestPermissionsResult disenable the location button +++++++++++++++");
//                    mLocationButton.setAlpha((float) 0.5);
                }
                break;
            }

            case CAMERA_PERMISSION: {
                if (isGranted) {
                    takePhoto();
                    Log.d(TAG, "onRequestPermissionsResult takePhoto ___________________");
                } else {
                    mCameraButton.setEnabled(false);
                    Log.d(TAG, "onRequestPermissionsResult disenable the camera button +++++++++++++++");
                    mCameraButton.setAlpha((float) 0.2);
                }
                break;
            }

            case AUDIO_PERMISSION: {
                if (isGranted) {
                    AudioUtil.recordVoice(MainActivity.this, mVoiceButton);
                    Log.d(TAG, "onRequestPermissionsResult recordVoice ___________________");
                } else {
                    mVoiceButton.setEnabled(false);
                    Log.d(TAG, "onRequestPermissionsResult disenable the voice button +++++++++++++++");
                    mVoiceButton.setAlpha((float) 0.2);
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request=" + requestCode + ", result=" + resultCode);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            // Process selected image here
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // Resize if too big for messaging
                Bitmap bitmap = ImageUtil.getBitmapForUri(this, uri);
                Bitmap resizedBitmap = scaleImage(bitmap);
                if (bitmap != resizedBitmap) {
                    uri = savePhotoImage(this, resizedBitmap);
                }
                uploadImageMessage(uri);
            } else {
                Log.e(TAG, "Cannot get an image for uploading");
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                Uri uri = savePhotoImage(this, bitmap);
                uploadImageMessage(uri);
                saveImageToAlbum(this);
            } else {
                Log.e(TAG, "Cannot get a photo for uploading");
            }
        }
    }

    private void uploadImageMessage(Uri uri) {
        if (uri == null) Log.e(TAG, "Could not create image message with null uri");

        final StorageReference imageReference = MessageUtil.getStorageReference(mUser, uri);
        UploadTask uploadTask = imageReference.putFile(uri);
        // Register observers to listen for when task is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload image message");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl, imageReference.toString());
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
                Log.d(TAG, "successfully upload image message to firebase");
                Log.d(TAG, "audio url: " + chatMessage.getAudioUrl());
                Log.d(TAG, "image url: " + chatMessage.getImageUrl());
                Log.d(TAG, "photo url: " + chatMessage.getPhotoUrl());
                Log.d(TAG, "user: " + chatMessage.getName());
                Log.d(TAG, "text: " + chatMessage.getText());
            }
        });
    }

    public void uploadAudioMessage(Uri uri) {
        if (uri == null) Log.e(TAG, "Could not create audio message with null uri");

        final StorageReference audioReference = MessageUtil.getStorageReference(mUser, uri);
        UploadTask uploadTask = audioReference.putFile(uri);
        // Register observers to listen for when task is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload audio message");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl, null, audioReference.toString());
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
                Log.d(TAG, "successfully upload audio message to firebase");
                Log.d(TAG, "audio url: " + chatMessage.getAudioUrl());
                Log.d(TAG, "image url: " + chatMessage.getImageUrl());
                Log.d(TAG, "photo url: " + chatMessage.getPhotoUrl());
                Log.d(TAG, "user: " + chatMessage.getName());
                Log.d(TAG, "text: " + chatMessage.getText());
            }
        });
    }

}
