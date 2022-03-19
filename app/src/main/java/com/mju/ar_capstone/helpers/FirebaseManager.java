package com.mju.ar_capstone.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.utilities.Preconditions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mju.ar_capstone.WrappedAnchor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FirebaseManager {

    private DatabaseReference mDatabase;
    private DatabaseReference contentsDatabase;
    private static final String DB_REGION = "https://ar-capstone-dbf8e-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String KEY_ROOT_DIR = "test_channel";

    //값 불러오는 리스너
    private ValueEventListener mDatabaseListener = null;
    private ValueEventListener contentsListener = null;

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

    public void getAnchorNum(){
    }

    // 컨텐츠 삭제, 앵커아이디 기준으로 삭제
    public void deleteContent(String anchorID){

        if(anchorID != null){
            mDatabase.child("contents").child(anchorID).removeValue();
            mDatabase.child("anchorList").child(anchorID).removeValue();
        }else {
            // null 이면 데이터가 전부 날아가버림
        }

    }

    // wrappedAnchor 자체를 받아서 여기서 처리
    // 컨텐츠 업로드
    public void setContent(WrappedAnchor wrappedAnchor){

        DatabaseReference anchorListDB = mDatabase.child("anchorList").child(wrappedAnchor.getCloudAnchorId());
        HashMap<String, Double> anchorList = new HashMap<String, Double>();
        anchorList.put("lat", wrappedAnchor.getLat());
        anchorList.put("lng", wrappedAnchor.getLng());
        anchorListDB.setValue(anchorList);

        DatabaseReference contentDB = mDatabase.child("contents").child(wrappedAnchor.getCloudAnchorId());
        HashMap<String, String> contents = new HashMap<String, String>();
        contents.put("created", createdTimeOfContent());
        contents.put("text", wrappedAnchor.getTextOrPath());
        contents.put("type", wrappedAnchor.getAnchorType());
        contents.put("userID", wrappedAnchor.getUserID());
        contentDB.setValue(contents);

        DatabaseReference poseDB = contentDB.child("pose");
        HashMap<String, Float> poses = new HashMap<String, Float>();
        Pose pose = wrappedAnchor.getPose();
        float[] poseT = pose.getTranslation();
        float[] poseR = pose.getRotationQuaternion();
        poses.put("Tx", poseT[0]);
        poses.put("Ty", poseT[1]);
        poses.put("Tz", poseT[2]);

        poses.put("Ra", poseR[0]);
        poses.put("Rb", poseR[1]);
        poses.put("Rc", poseR[2]);
        poses.put("Rd", poseR[3]);
        poseDB.setValue(poses);

    }


    public void registerContentsValueListner() {

        contentsDatabase = mDatabase.child("contents");
        contentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot tmpSnapshot: snapshot.getChildren()){
                    String anchorID = tmpSnapshot.getKey();

                    try{
                        HashMap<String, Double> poses = (HashMap<String, Double>) tmpSnapshot.child("pose").getValue();
                        float[] poseT = {
                                ((Double) poses.get("Tx")).floatValue(),
                                ((Double) poses.get("Ty")).floatValue(),
                                ((Double) poses.get("Tz")).floatValue()
                        };
                        float[] poseR = {
                                ((Double) poses.get("Ra")).floatValue(),
                                ((Double) poses.get("Rb")).floatValue(),
                                ((Double) poses.get("Rc")).floatValue(),
                                ((Double) poses.get("Rd")).floatValue(),
                        };
                        Pose pose = new Pose(poseT, poseR);

                        wrappedAnchorList.add(new WrappedAnchor(
                                anchorID,
                                pose,
                                tmpSnapshot.child("text").getValue(String.class),
                                tmpSnapshot.child("userID").getValue(String.class),
                                tmpSnapshot.child("type").getValue(String.class)
                        ));
                    }catch (NullPointerException e){
                        Log.d("순서", "리스너 데이터 null 예외 발생");
                    }
                }
                Log.d("순서", "리스너 데이터 로드 완료");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        contentsDatabase.addValueEventListener(contentsListener);

    }


    public void registerValueListnerForMap() {

        mDatabaseListener =
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot: dataSnapshot.child("contents").getChildren()){

                            Log.d("순서 더블 문제", "onDataChange");
                            Log.d("순서 더블 문제", postSnapshot.child("lat").getValue().toString());
                            String cloudAnchorID = (String) postSnapshot.getKey();
                            String text_or_path = (String) postSnapshot.child("text_or_path").getValue();
                            String userID = (String) postSnapshot.child("userID").getValue();
                            String anchorType = (String) postSnapshot.child("type").getValue();
                            double anchorLat = postSnapshot.child("lat").getValue(double.class);
                            double anchorLng = postSnapshot.child("lng").getValue(double.class);


                            wrappedAnchorList.add(new WrappedAnchor(cloudAnchorID, text_or_path, userID, anchorLat, anchorLng, anchorType));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };

        mDatabase.addValueEventListener(mDatabaseListener);
    }

}
