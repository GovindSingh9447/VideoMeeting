package com.RanaWat.VideoMeeting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.RanaWat.VideoMeeting.Utilites.Constants;
import com.RanaWat.VideoMeeting.network.ApiClient;
import com.RanaWat.VideoMeeting.network.ApiService;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    private  String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        ImageView imgMeetingType =findViewById(R.id.imgMeetingType);
        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        if(meetingType !=null){
            if (meetingType.equals("video")){
                imgMeetingType.setImageResource(R.drawable.ic_video);

            }else{
                imgMeetingType.setImageResource(R.drawable.ic_call);
            }
        }
        TextView txtFirstChar =findViewById(R.id.txtFirstChar);
        TextView txtUsername =findViewById(R.id.txtUsername);
        TextView txtEmail=findViewById(R.id.txtEmail);

        String  firstName =getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstName!= null){
            txtFirstChar.setText(firstName.substring(0,1));
        }
        txtUsername.setText(String.format("%s %s", firstName,getIntent().getStringExtra(Constants.KEY_LAST_NAME)));
        txtEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        ImageView imgAcceptedInv =findViewById(R.id.imgAcceptInv);
        imgAcceptedInv.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

        ImageView imgRejectedInv =findViewById(R.id.imgRejectInv);
        imgRejectedInv.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED, getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)));
    }

    private void sendInvitationResponse(String type, String receiverToken){
        try {

            JSONArray tokens =new JSONArray();
            tokens.put(receiverToken);

            JSONObject body =new JSONObject();
            JSONObject data =new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
            sendRemoteMessage(body.toString(), type);


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
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){

                        if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){


                            try{

                                URL serverURL =new URL("https:/meet.jit.si");

                                JitsiMeetConferenceOptions.Builder builder= new JitsiMeetConferenceOptions.Builder();
                                builder.setServerURL(serverURL);
                                builder.setWelcomePageEnabled(false);
                                builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));
                                if (meetingType.equals("audio")){
                                    builder.setVideoMuted(true);
                                }



                                JitsiMeetActivity.launch(IncomingInvitationActivity.this, builder.build());
                                finish();

                            }catch (Exception e){
                                Toast.makeText(IncomingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            }


                        }else{
                            Toast.makeText(IncomingInvitationActivity.this, "Invitation Rejected!!", Toast.LENGTH_SHORT).show();
                            finish();
                        }


                }else
                {
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();

                }

            }

            @Override
            public void onFailure(@NonNull  Call<String> call, @NonNull  Throwable t) {

                Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }


    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type!= null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
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