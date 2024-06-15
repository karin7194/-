package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Login extends AppCompatActivity implements View.OnClickListener {

    Button b, b2, forgotPasswordButton;
    EditText email, pass;
    SharedPreferences sp;
    CheckBox box;
    FirebaseAuth Auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myRef;
    User u;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        b = findViewById(R.id.button3);
        b2 = findViewById(R.id.button2);
        forgotPasswordButton = findViewById(R.id.button5);
        b.setOnClickListener(this);
        sp = getSharedPreferences("user", 0);
        b2.setOnClickListener(this);
        forgotPasswordButton.setOnClickListener(this);
        broadcastReceiver=new NetworkChangeReceiver();
        email = findViewById(R.id.editTextEmail);
        pass = findViewById(R.id.editTextTextPassword2);
        box = findViewById(R.id.checkBox);
        box.setOnClickListener(this);
        registerNetworkBroadcastReceiver();
        Auth = FirebaseAuth.getInstance();
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
    @Override
    public void onClick(View v) {
        if (b == v) {
            Intent go = new Intent(this, Registration.class);
            startActivity(go);
            finish();
        }
       else if(v==forgotPasswordButton){
            resetPassword();
        }
        else {
            if ((email.getText().toString() == null || email.getText().toString().equals("")) || (pass.getText().toString() == null || pass.getText().toString().equals("")))
                Toast.makeText(this, "must enter ", Toast.LENGTH_LONG).show();
            else if(b2==v) {
                    Auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseDatabase = FirebaseDatabase.getInstance();
                                myRef = firebaseDatabase.getReference("Users").child(email.getText().toString().replace(".", " "));
                                myRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        u = snapshot.getValue(User.class);
                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(Login.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("Email", email.getText().toString());
                                editor.putString("Password", pass.getText().toString());
                                editor.putBoolean("isChecked", box.isChecked());
                                editor.commit();
                                Intent go = new Intent(Login.this, MainPage.class);
                                startActivity(go);
                                finish();
                            }

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Login.this, "user not found/incorrect password or email", Toast.LENGTH_LONG).show();

                        }
                    });

                }
            }
        }



private void resetPassword() {
        String userEmail = email.getText().toString().trim();
        if (!TextUtils.isEmpty(userEmail)) {
            Auth.fetchSignInMethodsForEmail(userEmail).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.isSuccessful()) {
                                SignInMethodQueryResult result = task.getResult();
                                List<String> signInMethods = result.getSignInMethods();
                                if (signInMethods != null && signInMethods.size() > 0) {
                                    Toast.makeText(Login.this, "User with this email exists", Toast.LENGTH_LONG).show();
                                    Auth.sendPasswordResetEmail(userEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Login.this, "Password reset email sent", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(Login.this, "Failed to send password reset email", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(Login.this, "User with this email does not exist", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(Login.this, "Failed to check user existence", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(Login.this, "Enter your email address", Toast.LENGTH_SHORT).show();
        }
    }
}

