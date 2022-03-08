package com.mju.ar_capstone.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.utilities.Preconditions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mju.ar_capstone.WrappedAnchor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FirebaseManager {

    private DatabaseReference mDatabase;
    private static final String DB_REGION = "https://ar-capstone-dbf8e-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String KEY_ROOT_DIR = "base_channel";

    //값 불러오는 리스너
    private ValueEventListener mDatabaseListener = null;

    public static List<WrappedAnchor> wrappedAnchorList = new ArrayList<>();


    public FirebaseManager(){
        mDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child(KEY_ROOT_DIR);
        DatabaseReference.goOnline();
    }

    //앵커아이디만 주어졌을때
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

    /**
     * Registers a new listener for the given room code. The listener is invoked whenever the data for
     * the room code is changed.
     */
    public void registerValueListner() {

        mDatabaseListener =
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for(DataSnapshot postSnapshot: dataSnapshot.child("anchor_list").getChildren()){
                            Log.d("순서", postSnapshot.getKey().toString());

                            String anchorId = (String) postSnapshot.getKey();
                            if (!anchorId.isEmpty()) {
                                wrappedAnchorList.add(new WrappedAnchor(anchorId));
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
        mDatabase.addValueEventListener(mDatabaseListener);
    }

}
