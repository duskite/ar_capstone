package com.mju.ar_capstone.helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FirebaseManager {

    private DatabaseReference mDatabase;
    private static final String DB_REGION = "https://ar-capstone-dbf8e-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String KEY_ROOT_DIR = "base_channel";

    public FirebaseManager(){
        mDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child(KEY_ROOT_DIR);
        DatabaseReference.goOnline();
    }

    public void setContent(String anchorId, String anchorType, String text){

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String created = dateFormat.format(date);

        mDatabase.child("anchor_list").child(anchorId).setValue(anchorType);
        DatabaseReference contentDB = mDatabase.child("contents").child(anchorId);

        contentDB.child("lat_lng").setValue("1231313");
        contentDB.child("userID").setValue("ysy5593");
        contentDB.child("text").setValue(text);
        contentDB.child("image").setValue("image url");
        contentDB.child("created").setValue(created);
        contentDB.child("type").setValue(anchorType);

    }

    public void setContent(String anchorId){

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String created = dateFormat.format(date);

        mDatabase.child("anchor_list").child(anchorId).setValue("text");
        DatabaseReference contentDB = mDatabase.child("contents").child(anchorId);

        contentDB.child("lat_lng").setValue("1231313");
        contentDB.child("userID").setValue("ysy5593");
        contentDB.child("text").setValue("anchorid test....");
        contentDB.child("image").setValue("image url");
        contentDB.child("created").setValue(created);
        contentDB.child("type").setValue("text");

    }

    public void getContent(){

    }
}
