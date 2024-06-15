package com.example.myproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<User> users;
    private List<Chat> chats;
    private UserAdapter adapter;
    private DatabaseReference chatsRef, usersRef;
    private String currentuseremail;
    private TextView tv;
    private StorageReference profilePictureRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.rev);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        users = new ArrayList<>();
        chats = new ArrayList<>();
        SharedPreferences sp = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        currentuseremail = sp.getString("Email", "");
        tv = view.findViewById(R.id.textView15);
        adapter = new UserAdapter(users, chats);
        recyclerView.setAdapter(adapter);
        loadUsers();
        return view;
    }

    private void loadUsers() {
        chatsRef.child(currentuseremail.replace(".", " ")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String email = userSnapshot.getKey();
                    usersRef.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String username = dataSnapshot.child("name").getValue(String.class);
                                String profilePicUrl = dataSnapshot.child("profilepic").getValue(String.class);
                                User user = new User(username, " ", email, " ", profilePicUrl);
                                Chat chat = new Chat(currentuseremail, email, "", 1223, "", "");
                                fetchProfilePicture(user, email);
                                fetchLastMessage(currentuseremail.replace(".", " "), email, chat);
                                users.add(user);
                                chats.add(chat);
                                adapter.notifyDataSetChanged();
                            } else {
                                tv.setVisibility(View.GONE);
                                Log.d("LoadUser", "User data not found for email: " + email);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("LoadUser", "Error loading user: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fetchLastMessage(String currentuseremail, String otherUserEmail, Chat chat) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(currentuseremail.replace(".", " ")).child(otherUserEmail);
        Query lastMessageQuery = chatRef.orderByChild("timestamp").limitToLast(1);

        lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        String messageContent = messageSnapshot.child("message").getValue(String.class);
                        chat.setMessage(messageContent);
                        Log.d("LastMessage", "Last message: " + messageContent);
                    }
                } else {
                    Log.d("LastMessage", "No messages found for this chat.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LastMessage", "Error fetching last message: " + databaseError.getMessage());
            }
        });
    }


    private void fetchProfilePicture(User user, String email) {
        String userpicUrl = user.getProfilepic();
         profilePictureRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" + email + "/" + userpicUrl);

        profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
            user.setProfilepic(uri.toString());
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("ChatFragment", "Error fetching profile picture: " + e.getMessage());
        });
    }


    // Inner Adapter Class
    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;
        private List<Chat> chats;

        public UserAdapter(List<User> users, List<Chat> chats) {
            this.users = users;
            this.chats = chats;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragmentchatitem, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            Chat chat = chats.get(position);
            holder.message.setText(chat.getMessage());
            holder.usernameTextView.setText(user.getName());
            Picasso.get().load(user.getProfilepic()).into(holder.profilePicImageView);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), Chat_activity.class);
                intent.putExtra("postowneremail", user.getEmail());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        // Inner ViewHolder Class
        private static class UserViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTextView, message;
            ImageView profilePicImageView;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                message = itemView.findViewById(R.id.textView11);
                usernameTextView = itemView.findViewById(R.id.textView7);
                profilePicImageView = itemView.findViewById(R.id.imageView5);
            }
        }
    }
}
