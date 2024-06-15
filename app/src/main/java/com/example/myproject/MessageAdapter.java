package com.example.myproject;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_RIGHT = 2;
     String postemail;
    List<Chat> chatList;
    String currentUserId;

    public MessageAdapter(List<Chat> messageList, String currentUserId,String postemail) {
        this.chatList = messageList;
        this.currentUserId = currentUserId;
        this.postemail=postemail;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_LEFT) {
            view = inflater.inflate(R.layout.leftmessagerecyclerview, parent, false);
            return new LeftMessageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.rightmessagerecyclerview, parent, false);
            return new RightMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        if (getItemViewType(position) == VIEW_TYPE_LEFT) {
            ((LeftMessageViewHolder) holder).bind(chat);
        } else {
            ((RightMessageViewHolder) holder).bind(chat);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = chatList.get(position);
        if (chat != null && chat.getCurrentuseremail() != null && currentUserId != null && chat.getCurrentuseremail().equals(currentUserId)) {
            return VIEW_TYPE_RIGHT;
        } else {
            return VIEW_TYPE_LEFT;
        }
    }

    public  class LeftMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePictureImageView3;
        TextView messageTextView;
        TextView timestampTextView;

        public LeftMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePictureImageView3 = itemView.findViewById(R.id.imageView8);
            messageTextView = itemView.findViewById(R.id.message_text_left);
            timestampTextView = itemView.findViewById(R.id.timestamp_left);
        }

        public void bind(Chat chat) {
            messageTextView.setText(chat.getMessage());
            String timestamp = formatTimestamp(chat.getTimestamp());
            timestampTextView.setText(timestamp);
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(postemail.replace("."," "));
                userRef.child("profilepic").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String profilePicUrl = dataSnapshot.getValue(String.class);
                            if (profilePicUrl != null) {
                                StorageReference profilePicRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" + postemail.replace("."," ") + "/" + profilePicUrl);
                                profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.get().load(uri.toString()).into(profilePictureImageView3);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Failed to fetch profile picture: " + e.getMessage());
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to fetch profile picture: " + databaseError.getMessage());
                    }
                });
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(new Date(timestamp));
        }
    }

    public static class RightMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePictureImageView;
        TextView messageTextView;
        TextView timestampTextView;

        public RightMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePictureImageView = itemView.findViewById(R.id.profile_picture_right);
            messageTextView = itemView.findViewById(R.id.message_text_right);
            timestampTextView = itemView.findViewById(R.id.timestamp_right);
        }

        public void bind(Chat chat) {
            messageTextView.setText(chat.getMessage());
            String timestamp = formatTimestamp(chat.getTimestamp());
            timestampTextView.setText(timestamp);
            Picasso.get().load(chat.getCurrentprofile()).into(profilePictureImageView);
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(new Date(timestamp));
        }
    }
}
