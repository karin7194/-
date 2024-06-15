package com.example.myproject;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.myproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Makepost extends AppCompatActivity implements View.OnClickListener {
    Button b, b2;
    ImageView postpic, userpic;
    TextView username;
    EditText postinfo;
    ActivityResultLauncher activityResultLauncher;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int FROM_GALLERY = 1;
    private static final int FROM_CAMERA = 2;
    byte[] bytes;
    Uri uri;
    AlertDialog.Builder adb;
    AlertDialog ad;
    FirebaseDatabase firebaseUser;
    DatabaseReference db;
    String picName,userpicture;
    int flag;
    SharedPreferences sp;
    boolean f = false;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makepost);
        b = findViewById(R.id.button7);
        b.setOnClickListener(this);
        b2 = findViewById(R.id.button9);
        b2.setOnClickListener(this);
        sp = getSharedPreferences("user", 0);
        postpic = findViewById(R.id.imageView6);
        postpic.setOnClickListener(this);
        userpic = findViewById(R.id.imageView4);
        username = findViewById(R.id.textView12);
        postinfo = findViewById(R.id.editTextText3);
        firebaseUser = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Images/Users/" + sp.getString("Email", "").replace('.', ' '));
        fetchProfilePicture(sp.getString("Email", ""));
        getUsernameFromFirebase(sp.getString("Email", ""));
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() != null && result.getData().getAction() == null && result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            postpic.setImageURI(uri);
                            picName = System.currentTimeMillis() + "." + getFileExtension(uri);
                            Toast.makeText(Makepost.this, picName, Toast.LENGTH_LONG).show();
                            flag = FROM_GALLERY;
                            f = true;

                        } else if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");


                            picName = System.currentTimeMillis() + "." + "jpg";
                            Toast.makeText(Makepost.this, picName, Toast.LENGTH_LONG).show();

                            postpic.setImageBitmap(bitmap);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            bytes = baos.toByteArray();
                            flag = FROM_CAMERA;
                            f = true;


                        }
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
                    userpicture=dataSnapshot.getValue(String.class);
                    if (profilePicUrl != null) {
                        StorageReference profilePicRef = FirebaseStorage.getInstance().getReference().child("Images/Users/" + userEmail.replace(".", " ") + "/" + profilePicUrl);
                        profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(userpic);
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



    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Makepost.this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                activityResultLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(Makepost.this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == b2) {
            Intent go = new Intent(this, MainPage.class);
            startActivity(go);
            finish();
        } else {
            if (v == postpic) {
                adb = new AlertDialog.Builder(this);
                adb.setTitle("Choose A Picture");
                adb.setMessage("choose from gallery or take a photo");
                adb.setCancelable(true);
                adb.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int i) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        activityResultLauncher.launch(intent);
                    }
                });
                adb.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                            if (ActivityCompat.checkSelfPermission(Makepost.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                            }
                        } else {
                            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            activityResultLauncher.launch(intent);
                        }
                    }

                });
                adb.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int i) {
                    }
                });
                ad = adb.create();
                ad.show();
            } else if (!f) {
                Toast.makeText(Makepost.this, "must add a picture", Toast.LENGTH_LONG).show();
            } else {
                if (v == b) {
                    addPostToFirebase();
                }
            }
        }
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
    private void addPostToFirebase() {
        post p = new post(picName, username.getText().toString(), userpicture, postinfo.getText().toString(),sp.getString("Email", " ").replace(".", " "),System.currentTimeMillis());
        ArrayList<post> list = new ArrayList<>();
        db = firebaseUser.getReference("Posts/" + sp.getString("Email", " ").replace(".", " "));
        db.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    post temp = ds.getValue(post.class);
                    list.add(temp);
                }
                list.add(p);
                if (flag == FROM_GALLERY) {
                    storageReference = FirebaseStorage.getInstance().getReference("Posts/Users/" + sp.getString("Email", "").replace('.', ' '));
                    storageReference = storageReference.child(picName);
                    storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(Makepost.this, "תמונה הועלתה", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Makepost.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {

                    storageReference = FirebaseStorage.getInstance().getReference("Posts/Users/" + sp.getString("Email", "").replace('.', ' '));
                    storageReference = storageReference.child(picName);
                    UploadTask uploadTask = storageReference.putBytes(bytes);
                    Toast.makeText(Makepost.this, "תמונה הועלתה", Toast.LENGTH_LONG).show();
                }
                db = firebaseUser.getReference("Posts");
                Map<String, Object> map = new HashMap<>();
                map.put(sp.getString("Email", " ").replace(".", " "), list);
                db.updateChildren(map);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("post",picName);
                editor.commit();
                Intent go=new Intent(Makepost.this, MainPage.class);
                startActivity(go);
            }
        });
    }
}
