package com.mju.ar_capstone.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mju.ar_capstone.InventoryActivity;
import com.mju.ar_capstone.MainActivity;
import com.mju.ar_capstone.ManageChannelActivity;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.managefragments.WinnerListFragment;

public class NotificationService extends Service {

    private static String NOTI_GROUP = "neAR";

    private DatabaseReference mReference;

    private static String selectedChannel;

    private FirebaseManager firebaseManager;

    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel, notificationChannel2;


    public NotificationService(){

    }
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("알림", "onCreate");

        firebaseManager = new FirebaseManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationChannel = new NotificationChannel("foreground", "우승자 알림", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("우승자 발생시 알림을 받음");
            notificationManager.createNotificationChannel(notificationChannel);

            notificationChannel2 = new NotificationChannel("winner", "최근 우승자", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel2);


            //포어그라운드 알림 클릭시 앱 실행되도록 함
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


            //알림창에서 포어그라운드 알림 닫을 수 있도록 함
            Intent closeIntent = new Intent(this, NotificationService.class);
            closeIntent.putExtra("close", true);
            //이게 호출되면 true 넘겨서 종료 시킴
            PendingIntent closePendingIntent = PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_MUTABLE);
            NotificationCompat.Action closeAction = new NotificationCompat.Action(0,"닫기", closePendingIntent);


            //포어그라운드 알림
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "foreground");
            builder.setSmallIcon(R.drawable.ic_arbutton);
            builder.setContentTitle("neAR");
            builder.setContentText("우승자 알림을 받을 수 있습니다.");
            builder.setContentIntent(pendingIntent);
            builder.setContentIntent(closePendingIntent);
            builder.addAction(closeAction);
            startForeground(1, builder.build());
        }
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            stopForeground(true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("알림닫기", String.valueOf(intent.getBooleanExtra("close", false)));

        if(intent.getBooleanExtra("close", false)){
            this.stopService(intent);
            Log.d("알림닫기", "종료 호출됨");
        }
        Log.d("알림", "onStartCommand");

        try{
            selectedChannel = intent.getStringExtra("selectedChannel");
            Log.d("알림", selectedChannel);
            initDB(selectedChannel);
        }catch (NullPointerException e){
            Log.d("알림", "onStartCommand 인텐트 null");
        }

        return super.onStartCommand(intent, flags, startId);
    }
    public void sendNotification(String user){


        //알림띄우기
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "winner");
        builder.setSmallIcon(R.drawable.ic_stat_celebration);
        builder.setContentTitle("방금 게임을 클리어한 유저가 있습니다.");
        builder.setContentText("채널: " + selectedChannel + "         ID: " + user);

        builder.setColor(Color.GREEN);

        builder.setAutoCancel(true);
        builder.setGroup(NOTI_GROUP);

        notificationManager.notify((int)(System.currentTimeMillis()/1000), builder.build());

    }

    public void initDB(String channel){

        try{
            mReference = firebaseManager.channelDatabase.child(channel).child("winner");
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try {
                        Log.d("알림",dataSnapshot.getValue().toString());
                        String tmp = dataSnapshot.getValue().toString();

                        sendNotification(tmp);
                    }catch (NullPointerException e){

                    }

                    //알림 한 번 수신하고 동일 유저에 대해 추가 수신하는거 방지
                    //우승자 한번 보내고 null값으로 비워줌
                    mReference.setValue(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }catch (NullPointerException e){
            //아직 위너가 없으면 데이터 안가져옴
        }

    }
}