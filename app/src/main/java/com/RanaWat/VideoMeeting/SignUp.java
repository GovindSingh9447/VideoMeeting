package com.RanaWat.VideoMeeting;


import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.RanaWat.VideoMeeting.Utilites.Constants;
import com.RanaWat.VideoMeeting.Utilites.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class SignUp extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputEmail,inputPassword, inputConformPassword;
    private MaterialButton btnSignup;
    private ProgressBar signupprogressBar;
    private PreferenceManager preferenceManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        findViewById(R.id.imgback).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.textsignin).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignIn.class)));


        preferenceManager =new PreferenceManager(getApplicationContext());

        inputFirstName=findViewById(R.id.inputFirstName);
        inputLastName=findViewById(R.id.inputLasttName);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputpassword);
        inputConformPassword=findViewById(R.id.inputcfmpassword);

        signupprogressBar=findViewById(R.id.signupProgress);

        btnSignup=findViewById(R.id.btnsignup);

        btnSignup.setOnClickListener(v -> {
            if(inputFirstName.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUp.this, "Enter First name", Toast.LENGTH_SHORT).show();
            }else if(inputLastName.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUp.this, "Enter Last name", Toast.LENGTH_SHORT).show();
            }else if(inputEmail.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUp.this, "Enter Email", Toast.LENGTH_SHORT).show();
            }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                Toast.makeText(SignUp.this, "Enter valid Email", Toast.LENGTH_SHORT).show();
            }else if(inputPassword.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUp.this, "Enter Password", Toast.LENGTH_SHORT).show();
            }else if(inputConformPassword.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUp.this, "Confirm Your Password", Toast.LENGTH_SHORT).show();
            }else if(!inputPassword.getText().toString().equals(inputConformPassword.getText().toString())){
                Toast.makeText(SignUp.this, "Password & Confirm Password must be same", Toast.LENGTH_SHORT).show();
            }else{
                signup();

            }
        });
    }
    private  void signup(){
        btnSignup.setVisibility(View.INVISIBLE);
        signupprogressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database =FirebaseFirestore.getInstance();
        HashMap<String, Object> user=new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME,inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL,inputEmail.getText().toString());

                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    signupprogressBar.setVisibility(View.INVISIBLE);
                    btnSignup.setVisibility(View.VISIBLE);
                    Toast.makeText(SignUp.this, "Error:"+ e.getMessage(), Toast.LENGTH_SHORT).show();


                });
    }
}