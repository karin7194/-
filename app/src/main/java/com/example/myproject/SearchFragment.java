package com.example.myproject;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements  SearchView.OnQueryTextListener {
    private RecyclerView recyclerView;
    private List<User> users;
    private UserAdapter adapter;
    private DatabaseReference usersRef;
    private SearchView searchView;
    private SharedPreferences sp;
    private StorageReference profilePictureRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.recyclerview4);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        searchView = view.findViewById(R.id.search);
        searchView.setOnQueryTextListener(this);
        sp=this.getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        users = new ArrayList<>();
        adapter = new UserAdapter(users);
        recyclerView.setAdapter(adapter);
        loadUsers();

        return view;
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.getKey();
                    if (email.equals(sp.getString("Email", "").replace(".", " "))){
                    }
                        else{
                        User user = userSnapshot.getValue(User.class);
                        fetchProfilePicture(user, email);
                        users.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("HomeFragment", "Error fetching users: ", databaseError.toException());
            }
        });
    }

    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
            filter(query);
        return true;
    }

    private void filter(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : users) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        adapter.setData(filteredList);
    }

    private void fetchProfilePicture(User user,String email) {
        String userpicUrl = user.getProfilepic();
        profilePictureRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" +email+"/"+ userpicUrl);
        profilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                user.setProfilepic(uri.toString());
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error fetching profile picture for userimage: " + userpicUrl, e);
            }
        });
    }

    // Inner Adapter Class
    private class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {
        private List<User> users;

        public UserAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_layout, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.usernameTextView.setText(user.getName());
            Picasso.get().load(user.getProfilepic()).into(holder.profilePicImageView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent go = new Intent(getActivity(), UsersProfile.class);
                    go.putExtra("useremail", user.getEmail());
                    go.putExtra("profilepicture", user.getProfilepic());
                    go.putExtra("username", user.getName());
                    startActivity(go);
                        }
                    });
                }


        @Override
        public int getItemCount() {
            return users.size();
        }

        public void setData(List<User> users) {
            this.users = users;
            notifyDataSetChanged();
        }
    }

    // Inner ViewHolder Class
    private  class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        ImageView profilePicImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.userN);
            profilePicImageView = itemView.findViewById(R.id.imageView);
        }
    }

}


