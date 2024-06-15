package com.example.myproject;


import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private Button b;
    private SharedPreferences sp;
    private RecyclerView recyclerView;
    private List<post> posts;
    private PostAdapter adapter;
    private DatabaseReference databaseRef;
    private StorageReference postImageRef,profilePictureRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerview2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sp = this.getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        b=view.findViewById(R.id.button12);
        b.setOnClickListener(this);
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        posts = new ArrayList<>();
        adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);
        loadPosts();

        return view;
    }

    private void loadPosts() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear();
                List<post> allPosts = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.getKey();
                    for (DataSnapshot postSnapshot : userSnapshot.getChildren()) {
                        post post = postSnapshot.getValue(post.class);
                        boolean alreadyExists = false;
                        for (post existingPost : posts) {
                            if (existingPost.getPostimage().equals(post.getPostimage())) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (!alreadyExists) {
                            fetchProfilePicture(post, email);
                            fetchPostImage(post, email);
                            allPosts.add(post);
                        }
                    }
                }
                Collections.sort(allPosts, new Comparator<post>() {
                    @Override
                    public int compare(post post1, post post2) {
                        // Compare timestamps in reverse order (latest first)
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
        postImageRef = FirebaseStorage.getInstance().getReference().child("Posts/Users/" + email + "/" + postImageUrl);
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
    private void fetchProfilePicture(post post,String email) {
        String userpicUrl = post.getUserimage();
        profilePictureRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" +email+"/"+ userpicUrl);
        profilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                post.setUserimage(uri.toString());
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error fetching profile picture for userimage: " + userpicUrl, e);
            }
        });
    }

    @Override
    public void onClick(View v){
        Intent go=new Intent(getActivity(), Makepost.class);
        startActivity(go);
    }

    // Inner Adapter Class
    private class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
        private List<post> posts;
        public PostAdapter(List<post> posts) {
            this.posts = posts;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.recyclerviewmultipleusersitem, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            post post = posts.get(position);
            holder.username.setText(post.getUsername());
            holder.postinfo.setText(post.getPostinfo());
            Picasso.get().load(post.getUserimage()).into(holder.userpic);
            Picasso.get().load(post.getPostimage()).into(holder.postimage);
            String time = formatTimestamp(post.getTimestamp());
            holder.timestamp.setText(time);
            if (post.getEmail().equals(sp.getString("Email","").replace("."," "))) {
                holder.b.setVisibility(View.GONE);
            } else {
                holder.b.setVisibility(View.VISIBLE);
                holder.b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent go = new Intent(view.getContext(), Chat_activity.class);
                        go.putExtra("postowneremail",post.getEmail());
                        startActivity(go);
                    }
                });
            }
        }
        private String formatTimestamp(long timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            return dateFormat.format(new Date(timestamp));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

    }

    // Inner ViewHolder Class
    private static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postimage, userpic;
        TextView username, postinfo, timestamp;
        View view, view2;
        Button b;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postinfo = itemView.findViewById(R.id.postinfo2);
            postimage = itemView.findViewById(R.id.postimage2);
            userpic = itemView.findViewById(R.id.profilepic2);
            username = itemView.findViewById(R.id.username2);
            postinfo = itemView.findViewById(R.id.postinfo2);
            view = itemView.findViewById(R.id.view3);
            timestamp=itemView.findViewById(R.id.textView18);
            b=itemView.findViewById(R.id.button09);
            view2 = itemView.findViewById(R.id.view4);
        }

    }
}

