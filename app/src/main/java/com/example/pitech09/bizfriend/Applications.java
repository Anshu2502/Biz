package com.example.pitech09.bizfriend;

import android.app.Application;
import com.firebase.client.Firebase;

/**
 * Created by Pitech09 on 10/21/2016.
 */

public class Applications extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Initializing firebase
        Firebase.setAndroidContext(getApplicationContext());
    }

}
