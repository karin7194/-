package com.example.myproject;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UsersProfile extends AppCompatActivity implements View.OnClickListener{
     TextView username,tv,bio;
    ImageView profilepic;
    RecyclerView recyclerView;
    String userpicture, email;
    List<post> posts;
    PostProfileAdapter adapter;
    DatabaseReference databaseRef,usersRef;
    ImageButton chat;
    Intent in;
    StorageReference postImageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);
        username=findViewById(R.id.textView);
        profilepic=findViewById(R.id.imageView2);
        recyclerView=findViewById(R.id.rv);
        bio=findViewById(R.id.textView17);
        chat=findViewById(R.id.imageButton);
        chat.setOnClickListener(this);
        in=getIntent();
        tv=findViewById(R.id.textView5);
        email=in.getStringExtra("useremail");
        username.setText(in.getStringExtra("username"));
        Picasso.get().load(in.getStringExtra("profilepicture")).into(profilepic);
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        posts = new ArrayList<>();
        getbioFromFirebase(email.replace("."," "));
        adapter = new PostProfileAdapter(posts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadPosts();
    }
    @Override
    public void onClick(View v){
        Intent go=new Intent(this, Chat_activity.class);
        go.putExtra("postowneremail",email);
        startActivity(go);
    }
    private void getbioFromFirebase(String userEmail) {
         usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query emailQuery = usersRef.orderByChild("email").equalTo(userEmail);

        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String usersbio = userSnapshot.child("bio").getValue(String.class);
                        bio.setText(usersbio);
                    }
                } else {
                    Log.d("TAG", "User with email " + userEmail + " not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", "Error: " + databaseError.getMessage());
            }
        });
    }
    private void loadPosts() {
        databaseRef.child(email.replace("."," ")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear();
                List<post> allPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    post post = postSnapshot.getValue(post.class);
                    boolean alreadyExists = false;
                    for (post existingPost : posts) {
                        if (existingPost.getPostimage().equals(post.getPostimage())) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        StorageReference profilePictureRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" +email.replace("."," ")+"/"+ userpicture);
                        profilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                post.setUserimage(uri.toString());
                                adapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error fetching profile picture for userimage: " + profilepic, e);
                            }
                        });
                        fetchPostImage(post, email);
                        allPosts.add(post);
                    }
                }
                Collections.sort(allPosts, new Comparator<post>() {
                    @Override
                    public int compare(post post1, post post2) {
                        return Long.compare(post2.getTimestamp(), post1.getTimestamp());
                    }
                });
                posts.addAll(allPosts);
                adapter.notifyDataSetChanged();
                if (posts.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tv.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("PostsFragment", "Error fetching posts: ", databaseError.toException());
            }
        });
    }
    private void fetchPostImage(post post,String email) {
        String postImageUrl = post.getPostimage();
        postImageRef = FirebaseStorage.getInstance().getReference().child("Posts/Users/" + email.replace("."," ") + "/" + postImageUrl);
        postImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                post.setPostimage(uri.toString());
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error fetching post image for postimage: " + postImageUrl, e);
            }
        });
    }

}