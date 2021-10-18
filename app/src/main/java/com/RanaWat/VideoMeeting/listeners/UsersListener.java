package com.RanaWat.VideoMeeting.listeners;

import com.RanaWat.VideoMeeting.models.User;

public interface UsersListener {

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);


}
