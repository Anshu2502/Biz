package com.example.pitech09.bizfriend;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Pitech09 on 10/21/2016.
 */
public class NotiListener extends Service {
    SharedPreferences.Editor editor;
    String username, password;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPreferences = getSharedPreferences(Constant.SHARED_PREF, MODE_PRIVATE);


        String id = sharedPreferences.getString(Constant.UNIQUE_ID, null);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        username=pref.getString("email", null);
        password = pref.getString("password",null);


        Firebase firebase = new Firebase(Constant.FIREBASE_APP + id);

        //Adding a valueevent listener to firebase
        //this will help us to  track the value changes on firebase
        firebase.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot snapshot) {

                
                if(snapshot.child("msg").getValue() != null){
                    String msg = snapshot.child("msg").getValue().toString();


                    if (msg.equals("none"))
                        return;

                    showNotification(msg);
                } else{
                    Log.e("Value-->","Null");
                }
       }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
    }

    private void showNotification(String msg) {

        Intent intent = new Intent(this, Noti_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this);
        noBuilder.setSmallIcon(R.mipmap.mbz)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.big_logo))
                .setContentTitle("MBZ")
                .setContentText(msg)
                .setAutoCancel(true)
                .setSound(sound)
                .setLights(0x3300FF,1000,3000)
                .setVibrate(new long[] { 1000, 1000})
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build());

        /* //Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://makebizfriends.com/index.php/welcome/mobilelogincheck/" + username + "/" + password));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("Firebase Push Notification");
        builder.setContentText(msg);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());*/





    }
}
