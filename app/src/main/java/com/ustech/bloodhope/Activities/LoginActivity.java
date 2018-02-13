package com.ustech.bloodhope.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ustech.bloodhope.R;
import com.ustech.bloodhope.Utils.Constants;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity {
    TextView signupText;
    private FirebaseAuth mAuth;
    FancyButton btnLogin;
    EditText email,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        signupText = findViewById(R.id.sign_up_text);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                //startActivity(intent);
            }
        });

        email = findViewById(R.id.username_edittext);
        password = findViewById(R.id.pass_edittext);
        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.e(Constants.TAG,"email "+email.getText().toString());

                    LoginUser(email.getText().toString().trim(),password.getText().toString());
                  //  LoginUser(email.getText().toString(),password.getText().toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.e(Constants.TAG,"exception: "+ex.getMessage());
                }
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            Toast.makeText(this, "" + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            //currentUser.getIdToken(
            //Constants.currentUser = currentUser;

            Log.d(Constants.TAG,"token: "+ FirebaseInstanceId.getInstance().getToken());
            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
            finish();

        }
        //updateUI(currentUser);
    }
    public  void LoginUser(String email,String pass)
    {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(Constants.TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(Constants.TAG,"token: "+ FirebaseInstanceId.getInstance().getToken());
                            finish();
                            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                            Log.e(Constants.TAG,"starting activity");

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Constants.TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });

    }
}
