package com.RanaWat.VideoMeeting.Utilites;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS="users";
    public static final String KEY_FIRST_NAME="first_name";
    public static final String KEY_LAST_NAME="last_name";
    public static final String KEY_EMAIL="email";
    public static final String KEY_PASSWORD="password";
    public static final String KEY_USER_ID="user_id";
    public static final String KEY_FCM_TOKEN="fcm_token";

    public static  final String KEY_PREFERENCE_NAME ="videoMeetingPreference";
    public static final String KEY_IS_SIGNED_IN ="isSignedIn";

    public static final  String REMOTE_MSG_AUTHORIZATION ="Authorization";
    public static final  String REMOTE_MSG_CONTENT_TYPE ="Content-Type";

    public  static final String REMOTE_MSG_TYPE ="type";
    public static final String REMOTE_MSG_INVITATION ="invitation";
    public static final String REMOTE_MSG_MEETING_TYPE ="meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN ="inviterToken";
    public static final String REMOTE_MSG_DATA ="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS ="registration_ids";


    public static  final String REMOTE_MSG_INVITATION_RESPONSE ="invitationResponse";

    public static final String REMOTE_MSG_INVITATION_ACCEPTED="accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED="rejected";

    public static final String REMOTE_MSG_INVITATION_CANCELLED="cancelled";

    public static final  String REMOTE_MSG_MEETING_ROOM ="meetingRoom";



    public  static HashMap<String, String> getRemoteMessageHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAVR-nHHE:APA91bEeRUz4sLaCyufbM7xQB3gAPuvAiq2BAHY5BeRahc8NfFZQxd3uLSiM3fW3QwAgKYKYyY-DQSJWIhLoKgnSXI8poBfz-Xrj5M2gWS8Xt4yp1-gbQjRlmS6Vs0cqAi2neD2Elee1"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE,"application/json");
        return headers;
    }

}
