package com.RanaWat.VideoMeeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RanaWat.VideoMeeting.Utilites.Constants;
import com.RanaWat.VideoMeeting.Utilites.PreferenceManager;
import com.RanaWat.VideoMeeting.adapters.userAdapter;
import com.RanaWat.VideoMeeting.listeners.UsersListener;
import com.RanaWat.VideoMeeting.models.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListener {


    private userAdapter userAdapter;
    private PreferenceManager preferenceManager;
    private List<User> users;
    private  TextView textErrorMsg;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgConference;

    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());
        imgConference=findViewById(R.id.imgConference);

        TextView textTitle=findViewById(R.id.txtTitle);
        textTitle.setText(String.format("%s %s", preferenceManager.getString(Constants.KEY_FIRST_NAME),preferenceManager.getString(Constants.KEY_LAST_NAME)));

        findViewById(R.id.txtSignout).setOnClickListener(v -> signOut());

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> sendFCMTokenDatabase(task.getResult().getToken()));

        RecyclerView usersRecyclerView =findViewById(R.id.userRecycleview);


        textErrorMsg=findViewById(R.id.txtErrorMsg);


        users=new ArrayList<>();
        userAdapter= new userAdapter(users, this);
        usersRecyclerView.setAdapter(userAdapter);


        swipeRefreshLayout=findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();
checkForbatteryOptimizations();

    }

    private void getUsers(){
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                swipeRefreshLayout.setRefreshing(false);
               String myUserId =preferenceManager.getString(Constants.KEY_USER_ID);
               if (task.isSuccessful() && task.getResult()!= null){
                   users.clear();
                   for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                       if(myUserId.equals(documentSnapshot.getId())){
                           continue;
                       }
                       User user=new User();
                       user.firstname =documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                       user.lastname=documentSnapshot.getString(Constants.KEY_LAST_NAME);
                       user.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                       user.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                       users.add(user);
                   }

                   if (users.size()>0)
                   {
                       userAdapter.notifyDataSetChanged();
                   }else{
                       textErrorMsg.setText(String.format("%s" , "No users available"));
                       textErrorMsg.setVisibility(View.VISIBLE);
                   }


               }else
               {
                   textErrorMsg.setText(String.format("%s" , "No users available"));
                   textErrorMsg.setVisibility(View.VISIBLE);
               }
                });
    }


    private void  sendFCMTokenDatabase(String token){
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)

                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "unable to send token:" +e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private  void signOut(){
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=
        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String ,Object> updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreferences();
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    finish();

                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to sign out...", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void initiateVideoMeeting(User user) {
        if (user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(this, user.firstname+ " "+ user.lastname + " is not available for meeting", Toast.LENGTH_SHORT).show();
        }else
        {
           Intent intent=new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
           intent.putExtra("user", user);
           intent.putExtra("type", "video");
           startActivity(intent);
        }

    }

    @Override
    public void initiateAudioMeeting(User user) {

        if (user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(this, user.firstname+ " "+ user.lastname + " is not available for meeting", Toast.LENGTH_SHORT).show();
        }else
        {
           Intent intent= new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
           intent.putExtra("user", user);
           intent.putExtra("type", "audio");
           startActivity(intent);
        }

    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected){
            imgConference.setVisibility(View.VISIBLE);
            imgConference.setOnClickListener(v -> {
                Intent intent=new Intent(getApplicationContext() , OutgoingInvitationActivity.class);
                intent.putExtra("selectedUsers", new Gson().toJson(userAdapter.getSelectedUsers()));
                intent.putExtra("type", "video");
                intent.putExtra("isMultiple",true);
                startActivity(intent);
            });
        }else{
            imgConference.setVisibility(View.GONE);

        }
    }






private void checkForbatteryOptimizations(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt running background");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent=new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }

        }

}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==REQUEST_CODE_BATTERY_OPTIMIZATIONS){
            checkForbatteryOptimizations();
        }
    }
}