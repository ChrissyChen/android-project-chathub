package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.sfsu.csc780.chathub.model.ChatMessage;

/**
 * Created by Xinlu Chen on 4/2/17.
 */

public class MessageUtil {
    private static final String LOG_TAG = MessageUtil.class.getSimpleName();
    public static final String MESSAGES_CHILD = "messages";
    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();
    private static MessageLoadListener sAdapterListener;
    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    private static MediaPlayer mediaPlayer = new MediaPlayer();

    public interface MessageLoadListener { public void onLoadComplete(); }

    public static void send(ChatMessage chatMessage) {
        //update Firebase db
        sFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chatMessage);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;
        public ImageView messageImageView;
        public ImageButton voiceMessageImageButton;
        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            voiceMessageImageButton = (ImageButton) itemView.findViewById(R.id.voiceMessageImageButton);
        }
    }

    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             MessageLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView) {
        sAdapterListener = listener;
        final FirebaseRecyclerAdapter adapter =
                new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(
                        ChatMessage.class,
                        R.layout.item_message,
                        MessageViewHolder.class,
                        sFirebaseDatabaseReference.child(MESSAGES_CHILD)) {
                    @Override
                    protected void populateViewHolder(final MessageViewHolder viewHolder,
                                                      final ChatMessage chatMessage, int position) {
                        sAdapterListener.onLoadComplete();
                        viewHolder.messageTextView.setText(chatMessage.getText());
                        viewHolder.messengerTextView.setText(chatMessage.getName());
                        if (chatMessage.getPhotoUrl() == null) {
                            viewHolder.messengerImageView
                                    .setImageDrawable(ContextCompat
                                            .getDrawable(activity,
                                                    R.drawable.ic_account_circle_black_36dp));
                        } else {
                            SimpleTarget target = new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                    viewHolder.messengerImageView.setImageBitmap(bitmap);
                                }
                            };
                            Glide.with(activity)
                                    .load(chatMessage.getPhotoUrl())
                                    .asBitmap()
                                    .into(target);
                        }

                        if (chatMessage.getImageUrl() != null) {
                            Log.d(LOG_TAG, "show image message");
                            //Set view visibilities for an image message
                            viewHolder.messageImageView.setVisibility(View.VISIBLE);
                            viewHolder.messageTextView.setVisibility(View.GONE);
                            viewHolder.voiceMessageImageButton.setVisibility(View.GONE);
                            // load image for message
                            try {
                                final StorageReference gsReference =
                                        sStorage.getReferenceFromUrl(chatMessage.getImageUrl());
                                gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(activity)
                                                .load(uri)
                                                .into(viewHolder.messageImageView);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.e(LOG_TAG, "Could not load image for message", exception);
                                    }
                                });
                            } catch (IllegalArgumentException e) {
                                viewHolder.messageTextView.setText("Error loading image");
                                Log.e(LOG_TAG, e.getMessage() + " : " + chatMessage.getImageUrl());
                            }

                        } else if (chatMessage.getAudioUrl() != null) {
                            Log.d(LOG_TAG, "show audio message");
                            //Set view visibilities for an audio message
                            viewHolder.voiceMessageImageButton.setVisibility(View.VISIBLE);
                            viewHolder.messageImageView.setVisibility(View.GONE);
                            viewHolder.messageTextView.setVisibility(View.GONE);

                            final StorageReference gsReference =
                                    sStorage.getReferenceFromUrl(chatMessage.getAudioUrl());
                            gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    Log.d(LOG_TAG, "AUDIO URL: " + chatMessage.getAudioUrl());
                                    viewHolder.voiceMessageImageButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (mediaPlayer.isPlaying()) return; //avoid multiple clicks at the same time
                                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            try {
                                                mediaPlayer.setDataSource(uri.toString());
                                                Log.d(LOG_TAG, "AUDIO download from uri: " + uri.toString());
                                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                    @Override
                                                    public void onPrepared(MediaPlayer mp) {
                                                        mediaPlayer.start();
                                                    }
                                                });
                                                mediaPlayer.prepareAsync();
                                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                    @Override
                                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                                        mediaPlayer.reset();
                                                    }
                                                });
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.e(LOG_TAG, "Could not play the voice message", exception);
                                }
                            });
                        }
                        else {
                            Log.d(LOG_TAG, "show text message");
                            //Set view visibilities for a text message
                            viewHolder.messageTextView.setVisibility(View.VISIBLE);
                            viewHolder.messageImageView.setVisibility(View.GONE);
                            viewHolder.voiceMessageImageButton.setVisibility(View.GONE);
                        }
                    }
                };
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = linearManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        return adapter;
    }

    public static StorageReference getStorageReference(FirebaseUser user, Uri uri) {
        //Create a blob storage reference with path : bucket/userId/timeMs/filename
        long nowMs = Calendar.getInstance().getTimeInMillis();
        return sStorage.getReference().child(user.getUid() + "/" + nowMs + "/" + uri
                .getLastPathSegment());
    }
}
