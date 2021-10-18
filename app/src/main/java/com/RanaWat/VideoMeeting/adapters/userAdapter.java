package com.RanaWat.VideoMeeting.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.RanaWat.VideoMeeting.R;
import com.RanaWat.VideoMeeting.listeners.UsersListener;
import com.RanaWat.VideoMeeting.models.User;

import java.util.ArrayList;
import java.util.List;

public class userAdapter extends RecyclerView.Adapter<userAdapter.UserViewHolder>{

    private List<User> users;
    private UsersListener usersListener;
    private List<User> selectedUsers;



    public userAdapter(List<User> users , UsersListener usersListener) {
        this.users = users;
        this.usersListener=usersListener;
         selectedUsers =new ArrayList<>();
    }



    public  List<User> getSelectedUsers(){
        return selectedUsers;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user,
                        parent,
                        false
                )

        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        TextView textFirstChar,textUserName,textEmail;
        ImageView imageAudioMeeting, imageVideoMeeting;
        ConstraintLayout userContainer;
        ImageView imgSelected;

      UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textFirstChar=itemView.findViewById(R.id.textFirstChar);
          textUserName=itemView.findViewById(R.id.txtUsername);
          textEmail=itemView.findViewById(R.id.txtEmail);
          imageAudioMeeting=itemView.findViewById(R.id.imgAudioMeeting);
          imageVideoMeeting=itemView.findViewById(R.id.imgVideomeeting);
          userContainer=itemView.findViewById(R.id.userContainers);
          imgSelected=imageAudioMeeting.findViewById(R.id.imgSelected);

        }

        void setUserData(User user){
          textFirstChar.setText(user.firstname.substring(0,1));
          textUserName.setText(String.format("%s %s", user.firstname , user.lastname));
          textEmail.setText(user.email);
          imageAudioMeeting.setOnClickListener(v -> usersListener.initiateAudioMeeting(user));
          imageVideoMeeting.setOnClickListener(v -> usersListener.initiateVideoMeeting(user));


          userContainer.setOnLongClickListener(v -> {
              selectedUsers.add(user);
              imgSelected.setVisibility(View.VISIBLE);
              imageVideoMeeting.setVisibility(View.GONE);
              imageAudioMeeting.setVisibility(View.GONE);
              usersListener.onMultipleUsersAction(true);

              return true;
          });
         /* userContainer.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  if(imgSelected.getVisibility()==View.VISIBLE){
                      selectedUsers.remove(user);
                      imgSelected.setVisibility(View.GONE);
                      imageVideoMeeting.setVisibility(View.VISIBLE);
                      imageAudioMeeting.setVisibility(View.VISIBLE);
                      if (selectedUsers.size() ==0){
                          usersListener.onMultipleUsersAction(false);
                      }
                  }else
                  {
                      if (selectedUsers.size()>0){
                          selectedUsers.add(user);
                          imgSelected.setVisibility(View.VISIBLE);
                          imageVideoMeeting.setVisibility(View.GONE);
                          imageAudioMeeting.setVisibility(View.GONE);
                      }
                  }
              }
          });
        */

        }
    }
}
