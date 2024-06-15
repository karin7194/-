package com.example.myproject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Chat_activity extends AppCompatActivity{

    EditText message;
    RecyclerView recyclerView;
    MessageAdapter adapter;
    ImageButton send;
    ImageView profilepic;
    String postowneremail,currentuseremail, currentprofile,postprofile;
    Intent in;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myref, userRef;
    SharedPreferences sp;
    List<Chat> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        profilepic = findViewById(R.id.imageView7);
        message = findViewById(R.id.message_input);
        recyclerView = findViewById(R.id.recycler_view);
        send = findViewById(R.id.imageButton2);
        firebaseDatabase = FirebaseDatabase.getInstance();
        sp = getSharedPreferences("user", 0);
        in = getIntent();
        postowneremail = in.getStringExtra("postowneremail");
        currentuseremail = sp.getString("Email", " ");
        chatList = new ArrayList<>();
        adapter = new MessageAdapter(chatList, currentuseremail,postowneremail);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        fetchProfilePicture(currentuseremail,profilepic);
        myref= firebaseDatabase.getReference("Chats");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToFirebase();
            }
        });
        loadMessagesFromFirebase();
    }
    private void fetchProfilePicture(String userEmail,ImageView v) {
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userEmail.replace(".", " "));
        userRef.child("profilepic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profilePicUrl = dataSnapshot.getValue(String.class);
                    if (profilePicUrl != null) {
                        StorageReference profilePicRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" + userEmail.replace(".", " ") + "/" + profilePicUrl);
                        profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(v);
                                currentprofile=uri.toString();
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

    private void loadMessagesFromFirebase() {
        myref.child(currentuseremail.replace("."," ")).child(postowneremail.replace("."," ")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    chatList.add(chat);
                }
                adapter.notifyDataSetChanged();
                if(chatList.size()>0)
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load messages: " + databaseError.getMessage());
            }
        });

    }

    private void sendMessageToFirebase() {
        long timestamp = System.currentTimeMillis();
        String messageText = message.getText().toString();
        if (!messageText.isEmpty()) {
            Chat chat = new Chat(currentuseremail, postowneremail, messageText, timestamp, currentprofile, postprofile);
            ArrayList<Chat> list = new ArrayList<>();
            myref = firebaseDatabase.getReference("Chats/" + currentuseremail.replace(".", " ")+"/"+postowneremail.replace("."," "));
            myref.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Chat temp = ds.getValue(Chat.class);
                        list.add(temp);
                    }
                    list.add(chat);
                    myref = firebaseDatabase.getReference("Chats/" + currentuseremail.replace(".", " "));
                    Map<String, Object> map = new HashMap<>();
                    map.put(postowneremail.replace("."," "), list);
                    myref.updateChildren(map);
                    myref=firebaseDatabase.getReference("Chats/" + postowneremail.replace(".", " "));
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put(currentuseremail.replace("."," "), list);
                    myref.updateChildren(map2);
                    message.setText("");
                }
            });


        }
    }
    }

