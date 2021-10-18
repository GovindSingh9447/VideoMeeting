package com.RanaWat.VideoMeeting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.RanaWat.VideoMeeting.Utilites.Constants;
import com.RanaWat.VideoMeeting.Utilites.PreferenceManager;
import com.RanaWat.VideoMeeting.models.User;
import com.RanaWat.VideoMeeting.network.ApiClient;
import com.RanaWat.VideoMeeting.network.ApiService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToInstanceMap;
import com.google.common.reflect.TypeToken;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private String inviterToken=null;
    private String meetingRoom =null;
    private String meetingType= null;

    private TextView textFirstChar;
    private TextView textUsername;
    private TextView txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        preferenceManager= new PreferenceManager(getApplicationContext());


        ImageView imgMeetingType = findViewById(R.id.imgMeetingType);

         meetingType = getIntent().getStringExtra("type");

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imgMeetingType.setImageResource(R.drawable.ic_video);
            }else{
                imgMeetingType.setImageResource(R.drawable.ic_call);
            }
        }
        textFirstChar = findViewById(R.id.txtFirstChar);
        textUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {

            textFirstChar.setText(user.firstname.substring(0,1));
            textUsername.setText(String.format("%s %s", user.firstname, user.lastname));
            txtEmail.setText(user.email);

        }

        ImageView imageStopInv = findViewById(R.id.imgStopInv);
        imageStopInv.setOnClickListener(v ->{
            if (user !=null)
            {
                cancelInvitation(user.token);
            }
        });


        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() !=null){
                inviterToken=task.getResult().getToken();

                if (meetingType != null){
                    if (getIntent().getBooleanExtra("isMultiple",false)){
                        Type type=new TypeToken<ArrayList<User>>(){}.getType();
                        ArrayList<User>receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                        initiateMeeting(meetingType, null,receivers);
                    }else{
                        if (user !=null){
                            initiateMeeting(meetingType,user.token, null);
                        }
                    }
                }



            }
        });



    }



    private void initiateMeeting(String meetingType, String receiverToken, ArrayList<User> recerivers)
    {
        try {

            JSONArray tokens=new JSONArray();

            if (receiverToken != null){
                tokens.put(receiverToken);
            }
            if (recerivers !=null && recerivers.size()>0){
                StringBuilder userNames =new StringBuilder();
                for (int i=0; i<recerivers.size(); i++)
                {
                    tokens.put(recerivers.get(i).token);
                    userNames.append(recerivers.get(i).firstname).append(" ").append(recerivers.get(i).lastname).append("\n");
                }
                textFirstChar.setVisibility(View.GONE);
                txtEmail.setVisibility(View.GONE);
                textUsername.setText(userNames.toString());
            }



            JSONObject body =new JSONObject();
            JSONObject data =new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            meetingRoom= preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                    UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);


            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);



        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(),remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull  Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    }else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation Cancelled!!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else
                {
                    Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull  Call<String> call, @NonNull  Throwable t) {

                Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void cancelInvitation( String receiverToken){
        try {

            JSONArray tokens =new JSONArray();
            tokens.put(receiverToken);

            JSONObject body =new JSONObject();
            JSONObject data =new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);


        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
      String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
      if(type!= null){
          if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){


              try{

                  URL serverURL =new URL("https:/meet.jit.si");
                  JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                  builder.setServerURL(serverURL);
                  builder.setWelcomePageEnabled(false);
                  builder.setRoom(meetingRoom);
                  if(meetingType.equals("audio")){
                      builder.setVideoMuted(true);

                  }



                  JitsiMeetActivity.launch(OutgoingInvitationActivity.this, builder.build());
                  finish();

              }catch (Exception e){
                  Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                  finish();
              }



          }else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
              Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
              finish();
          }
      }


        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}