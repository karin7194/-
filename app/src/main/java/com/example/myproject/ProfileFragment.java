package com.example.myproject;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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

public class ProfileFragment extends Fragment {
private TextView username,bio;
private ImageView profilepic;
private RecyclerView recyclerView;
private String email;
private SharedPreferences sp;
private List<post> posts;
private PostProfileAdapter adapter;
private DatabaseReference databaseRef;
private StorageReference postImageRef, profilePicRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        username=view.findViewById(R.id.user);
        profilepic=view.findViewById(R.id.profileImage);
        recyclerView=view.findViewById(R.id.recyclerview);
        bio=view.findViewById(R.id.textView9);
        sp = this.getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        fetchProfilePicture(sp.getString("Email", ""));
        getUsernameFromFirebase(sp.getString("Email", ""));
        email=sp.getString("Email", "");
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        posts = new ArrayList<>();
        getbioFromFirebase(sp.getString("Email","").replace("."," "));
        adapter = new PostProfileAdapter(posts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadPosts();
        return view;
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
    private void getbioFromFirebase(String userEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
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
    private void getUsernameFromFirebase(String userEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query emailQuery = usersRef.orderByChild("email").equalTo(userEmail);

        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userName = userSnapshot.child("name").getValue(String.class);
                        username.setText(userName);
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
    private void fetchProfilePicture(String userEmail) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userEmail.replace(".", " "));
        userRef.child("profilepic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profilePicUrl = dataSnapshot.getValue(String.class);
                    String userpicture=dataSnapshot.getValue(String.class);
                    if (profilePicUrl != null) {
                        profilePicRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" + userEmail.replace(".", " ") + "/" + profilePicUrl);
                        profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(profilepic);
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

}
