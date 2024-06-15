package com.example.myproject;

import static com.google.common.io.Files.getFileExtension;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Registration extends AppCompatActivity implements View.OnClickListener {
    Button b;
    EditText et1, et2, et3,bio;
    CheckBox box;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    DatabaseReference myref=firebaseDatabase.getReference("Users");
    ActivityResultLauncher activityResultLauncher;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int FROM_GALLERY = 1;
    private static final int FROM_CAMERA = 2;
    byte[] bytes;

    AlertDialog.Builder adb,adb2;
    AlertDialog ad,ad2;
    ImageView profileImage;
    Uri uri;
    StorageReference mStorageRef;
    int flag;
    String picName="";
    int j=1;
    boolean f=false;

    SharedPreferences sp;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(this);
        et1 = findViewById(R.id.editTextText2);
        et2 = findViewById(R.id.editTextTextPassword);
        et3 = findViewById(R.id.editTextTextEmailAddress);
        bio=findViewById(R.id.editTextbio);
        b = findViewById(R.id.button);
        b.setOnClickListener(this);
        broadcastReceiver=new NetworkChangeReceiver();
        registerNetworkBroadcastReceiver();
        sp = getSharedPreferences("user", 0);
        box=findViewById(R.id.checkBox2);
        box.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData()!=null && result.getData().getAction()==null &&result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            profileImage.setImageURI(uri);
                            picName=System.currentTimeMillis() + "."+ getFileExtension(uri);
                            Toast.makeText(Registration.this, picName, Toast.LENGTH_LONG).show();
                            flag=FROM_GALLERY;
                            f= true;//boolean if must enter picture


                        }


                        else if (result.getData()!=null   && result.getResultCode() == Activity.RESULT_OK) {
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");


                            picName=System.currentTimeMillis() + "."+ "jpg";
                            Toast.makeText(Registration.this, picName, Toast.LENGTH_LONG).show();

                            profileImage.setImageBitmap(bitmap);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            bytes = baos.toByteArray();
                            flag=FROM_CAMERA;
                            f= true;


                        }
                    }
                });

    }
    public void registerNetworkBroadcastReceiver(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }
    public void unregisterNetwork(){
        try {
            unregisterReceiver(broadcastReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }
    public void insertimage() {
        if (!f) {
            adb2 = new AlertDialog.Builder(this);
            adb2.setTitle("Profile Picture");
            adb2.setMessage("Are you sure you don't want to upload a profile picture?");
            adb2.setCancelable(true);
            adb2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    flag=FROM_CAMERA;
                    Drawable d=profileImage.getDrawable();
                    Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    bytes = baos.toByteArray();
                    picName=System.currentTimeMillis() + "."+ "jpg";
                    mStorageRef = FirebaseStorage.getInstance().getReference("Images/Users/" + et3.getText().toString().replace('.', ' '));
                    mStorageRef = mStorageRef.child(picName);
                    UploadTask uploadTask = mStorageRef.putBytes(bytes);
                    Toast.makeText(Registration.this, "תמונה הועלתה", Toast.LENGTH_LONG).show();
                    f=true;
                }
            });
            adb2.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            ad2 = adb2.create();
            ad2.show();
        }
        else {
            j=0;
            if (flag == FROM_GALLERY) {
                mStorageRef = FirebaseStorage.getInstance().getReference("Images/Users/" + et3.getText().toString().replace('.', ' '));
                mStorageRef = mStorageRef.child(picName);
                mStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(Registration.this, "תמונה הועלתה", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(Registration.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {

                mStorageRef = FirebaseStorage.getInstance().getReference("Images/Users/" + et3.getText().toString().replace('.', ' '));
                mStorageRef = mStorageRef.child(picName);
                UploadTask uploadTask = mStorageRef.putBytes(bytes);
                Toast.makeText(Registration.this, "תמונה הועלתה", Toast.LENGTH_LONG).show();
            }
        }
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
                Toast.makeText(Registration.this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                activityResultLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(Registration.this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void insert() {
        if(isValidate()) {
            firebaseAuth.createUserWithEmailAndPassword(et3.getText().toString(), et2.getText().toString()).addOnCompleteListener(this,
                    new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull
                        Task<AuthResult>task) {
                    if (task.isSuccessful()){
                        User u = new User(et1.getText().toString(), et2.getText().toString(), et3.getText().toString(),bio.getText().toString(),picName);
                        myref=myref.child(et3.getText().toString().replace(".", " "));
                        myref.setValue(u);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("Email", et3.getText().toString());
                        editor.putString("Password", et2.getText().toString());
                        editor.putString("profile",picName);
                        editor.putString("username",et1.getText().toString());
                        editor.putBoolean("isChecked2", box.isChecked());
                        editor.commit();
                        Intent go = new Intent(Registration.this,MainPage.class);
                        go.putExtra("picName",picName);
                        go.putExtra("email",et3.getText().toString());
                        startActivity(go);
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registration.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    //בדיקה האם האימייל והסיסמא עומדים בהגדרות-סיסמא לפחות 6 תווים ואימייל תקין
    public boolean isValidate(){
        if (!Patterns.EMAIL_ADDRESS.matcher(et3.getText().toString()).matches()){
            et3.setError("Invalid email");
            et3.setFocusable(true);
            return false;
        }
        else if (et2.getText().toString().length()<6){
            et2.setError("password must have at least 6 characters");
            et2.setFocusable(true);
            return false;
        }
        return true;
    }
    @Override
    public void onClick(View v) {
        if (v == profileImage) {
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
                        if (ActivityCompat.checkSelfPermission(Registration.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
        } else if ((et1.getText().toString() == null || et1.getText().toString().equals("")) || (et2.getText().toString() == null || et2.getText().toString().equals("")) || (et3.getText().toString() == null || et3.getText().toString().equals("")))
            Toast.makeText(this, "must enter ", Toast.LENGTH_LONG).show();
        else {
            if (v == b) {
                insertimage();
                if(j==0)
                    insert();
            }
        }
    }
}
