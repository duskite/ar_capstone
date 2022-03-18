package com.mju.ar_capstone.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.ar.core.Anchor;
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

    //컨텐츠 생성 시간
    public String createdTimeOfContent(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        return (String) dateFormat.format(date);
    }

    // 컨텐츠 삭제, 앵커아이디 기준으로 삭제
    public void deleteContent(Anchor anchor){
        String tmpId = anchor.getCloudAnchorId();

        mDatabase.child("contents").child(tmpId).removeValue();
        mDatabase.child("anchor_list").child(tmpId).removeValue();
    }

    // wrappedAnchor 자체를 받아서 여기서 처리
    // 컨텐츠 업로드
    public void setContent(WrappedAnchor wrappedAnchor){
        String created = createdTimeOfContent();
        String cloudAnchorID = wrappedAnchor.getCloudAnchorId();
        String text_or_path = wrappedAnchor.getText();
        String userID = wrappedAnchor.getUserID();
        String anchorType = wrappedAnchor.getAnchorType();
        double lat = wrappedAnchor.getlat();
        double lng = wrappedAnchor.getlng();

        mDatabase.child("anchor_list").child(cloudAnchorID).child("lat").setValue(lat);
        mDatabase.child("anchor_list").child(cloudAnchorID).child("lng").setValue(lng);

        DatabaseReference contentDB = mDatabase.child("contents").child(cloudAnchorID);
        contentDB.child("lat").setValue(lat);
        contentDB.child("lng").setValue(lng);
        contentDB.child("userID").setValue(userID);
        contentDB.child("text_or_path").setValue(text_or_path);
        contentDB.child("created").setValue(created);
        contentDB.child("type").setValue(anchorType);
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
                        for(DataSnapshot postSnapshot: dataSnapshot.child("contents").getChildren()){
                            Log.d("순서 키들", postSnapshot.getKey());

                            String cloudAnchorID = (String) postSnapshot.getKey();
                            String text_or_path = (String) postSnapshot.child("text_or_path").getValue();
                            String userID = (String) postSnapshot.child("userID").getValue();
                            String anchorType = (String) postSnapshot.child("type").getValue();
//                            double lat = (double) postSnapshot.child("lat").getValue();
//                            double lng = (double) postSnapshot.child("lng").getValue();

                            wrappedAnchorList.add(new WrappedAnchor(cloudAnchorID, text_or_path, userID, anchorType));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };

        mDatabase.addValueEventListener(mDatabaseListener);
    }

}
