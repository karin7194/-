package com.example.myproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostProfileAdapter extends RecyclerView.Adapter<PostProfileAdapter.PostViewHolder2> {
    private List<post> posts;
    String email;

    public PostProfileAdapter(List<post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.recyclerviewitem, parent, false);
        return new PostViewHolder2(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder2 holder, int position) {
        post post = posts.get(position);
        holder.postinfo.setText(post.getPostinfo());
        Picasso.get().load(post.getPostimage()).into(holder.postimage);
        String time = formatTimestamp(post.getTimestamp());
        holder.timestamp.setText(time);
        email = post.getEmail();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }






public static class PostViewHolder2 extends RecyclerView.ViewHolder {
    ImageView postimage;
    TextView  postinfo,timestamp;
    View view, view2;

    public PostViewHolder2(@NonNull View itemView) {
        super(itemView);
        postinfo = itemView.findViewById(R.id.postinfo);
        postimage = itemView.findViewById(R.id.postpic);
        timestamp=itemView.findViewById(R.id.textView16);
        view = itemView.findViewById(R.id.view);
        view2 = itemView.findViewById(R.id.view2);
    }
}
}

