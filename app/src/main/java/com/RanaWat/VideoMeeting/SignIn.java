package com.RanaWat.VideoMeeting;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.RanaWat.VideoMeeting.Utilites.Constants;
import com.RanaWat.VideoMeeting.Utilites.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;




public class SignIn extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private MaterialButton btnSignin;
    private ProgressBar signInProgressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        preferenceManager=new PreferenceManager(getApplicationContext());

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();

        }

        findViewById(R.id.txtsignUp).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                SignUp.class)));

        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputpassword);
        btnSignin=findViewById(R.id.btnsignin);
        signInProgressBar=findViewById(R.id.signinProgressBar);

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputEmail.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignIn.this, "Enter Email", Toast.LENGTH_SHORT).show();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                    Toast.makeText(SignIn.this, "Enter valid Email", Toast.LENGTH_SHORT).show();
                }else if(inputPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignIn.this, "Enter Password", Toast.LENGTH_SHORT).show();
                }
                    else
                    {
                        signin();

                    }
            }
        });


    }
    private void signin(){
        btnSignin.setVisibility(View.INVISIBLE);
        signInProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database =FirebaseFirestore.getInstance();
       database.collection(Constants.KEY_COLLECTION_USERS)

        .whereEqualTo(Constants.KEY_EMAIL, inputEmail.getText().toString())
        .whereEqualTo(Constants.KEY_PASSWORD,inputPassword.getText().toString())
               .get()
               .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if(task.isSuccessful()&& task.getResult()!=null && task.getResult().getDocuments().size()>0){
                           DocumentSnapshot documentSnapshot =task.getResult().getDocuments().get(0);
                           preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                           preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                           preferenceManager.putString(Constants.KEY_FIRST_NAME,documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                           preferenceManager.putString(Constants.KEY_LAST_NAME,documentSnapshot.getString(Constants.KEY_LAST_NAME));
                           preferenceManager.putString(Constants.KEY_EMAIL,documentSnapshot.getString(Constants.KEY_EMAIL));
                           Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);
                       }else
                       {
                           signInProgressBar.setVisibility(View.INVISIBLE);
                           btnSignin.setVisibility(View.VISIBLE);
                           Toast.makeText(SignIn.this, "Unable To sign In", Toast.LENGTH_SHORT).show();
                       }
                   }
               });
    }
}