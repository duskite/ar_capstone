package com.mju.ar_capstone.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.math.Vector3;
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
import java.util.Iterator;
import java.util.List;

public class FirebaseManager {

    private DatabaseReference mDatabase;
    private DatabaseReference contentsDatabase;
    private DatabaseReference anchorNumDatabase;
    private DatabaseReference imageNumDatabase;
    private DatabaseReference mp3NumDatabase;
    private DatabaseReference gpsDatabase;

    private static final String DB_REGION = "https://ar-capstone-dbf8e-default-rtdb.asia-southeast1.firebasedatabase.app";

    //값 불러오는 리스너
    private ValueEventListener contentsListener = null;
    private ValueEventListener anchorNumListener = null;
    private ValueEventListener imageNumListener = null;
    private ValueEventListener mp3NumListener = null;
    private ValueEventListener gpsListener = null;

    private static int nextAnchorNum;
    private static int nextImageNum;
    private static int nextMp3Num;

    public static ArrayList<WrappedAnchor> wrappedAnchorList = new ArrayList<>();
    
    public FirebaseManager(String channel){
        mDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child(channel);

        //앵커 넘버, 이미지 넘버 가져오기
        registerAnchorNumValueLisner();
        registerImageNumValueLisner();
        registerMp3NumValueLisner();

        DatabaseReference.goOnline();
    }


    // ar에서 가져가서 처리하는게 나을듯
    public ArrayList<WrappedAnchor> getWrappedAnchorList(){
        return wrappedAnchorList;
    }
    // 데이터를 다른 곳에서 한번 가져간후 호출하면 리스트 비워줌
    public void clearWrappedAnchorList(){
        wrappedAnchorList.clear();
    }


    //컨텐츠 생성 시간
    public String createdTimeOfContent(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        return (String) dateFormat.format(date);
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
        HashMap<String, Double> gps = new HashMap<String, Double>();
        gps.put("lat", wrappedAnchor.getLat());
        gps.put("lng", wrappedAnchor.getLng());
        anchorListDB.setValue(gps);

        //컨테츠 내용
        DatabaseReference contentDB = mDatabase.child("contents").child(wrappedAnchor.getCloudAnchorId());
        HashMap<String, String> contents = new HashMap<String, String>();
        contents.put("created", createdTimeOfContent());
        contents.put("userID", wrappedAnchor.getUserID());

        if(wrappedAnchor.getAnchorType() == 1){ //이미지
            contents.put("text_or_path", wrappedAnchor.getTextOrPath() + ".jpg");
            imageNumDatabase.setValue(nextImageNum + 1);
        }else if(wrappedAnchor.getAnchorType() == 2){ //음성

        }else { //텍스트
            contents.put("text_or_path", wrappedAnchor.getTextOrPath());
        }
        contentDB.setValue(contents);
        contentDB.child("azimuth").setValue(wrappedAnchor.getAzimuth());
        contentDB.child("type").setValue(wrappedAnchor.getAnchorType());


        //앵커 포즈
        DatabaseReference poseDB = contentDB.child("pose");
        HashMap<String, Float> poses = new HashMap<String, Float>();
        Pose pose = wrappedAnchor.getPose();
        float[] poseT = pose.getTranslation();
        float[] poseR = pose.getRotationQuaternion();
        poses.put("Tx", poseT[0]);
        poses.put("Ty", poseT[1]);
        poses.put("Tz", poseT[2]);

        poses.put("Rx", poseR[0]);
        poses.put("Ry", poseR[1]);
        poses.put("Rz", poseR[2]);
        poses.put("Rw", poseR[3]);

        poseDB.setValue(poses);

        //위경도, 위에 해시맵 재사용
        DatabaseReference gpsDB = contentDB.child("gps");
        gpsDB.setValue(gps);

        anchorNumDatabase.setValue(nextAnchorNum + 1);

    }

    //파이어 베이스에서 다음 게시물 숫자 가지고만 있음
    public int getNextAnchorNum(){
        return nextAnchorNum;
    }
    public int getNextImageNum() {return nextImageNum;}
    public int getNextMp3Num(){return nextMp3Num;}

    //앵커 게시글 수 업뎃용
    public void registerAnchorNumValueLisner(){
        anchorNumDatabase = mDatabase.child("nextAnchorNum");
        anchorNumListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    nextAnchorNum = snapshot.getValue(int.class);
                }catch (NullPointerException e){
                    anchorNumDatabase.setValue(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        anchorNumDatabase.addValueEventListener(anchorNumListener);

    }

    //이미지 파일 수 업뎃용
    public void registerImageNumValueLisner(){
        imageNumDatabase = mDatabase.child("nextImageNum");
        imageNumListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    nextImageNum = snapshot.getValue(int.class);
                }catch (NullPointerException e){
                    //이미지 파일은 한번 업로드 되고 삭제하지 않으면 그대로이니까 잠시 보류
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        imageNumDatabase.addValueEventListener(imageNumListener);

    }
    //이미지 파일 수 업뎃용
    public void registerMp3NumValueLisner(){
        mp3NumDatabase = mDatabase.child("nextMp3Num");
        mp3NumListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    nextMp3Num = snapshot.getValue(int.class);
                }catch (NullPointerException e){
                    //이미지 파일은 한번 업로드 되고 삭제하지 않으면 그대로이니까 잠시 보류
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        mp3NumDatabase.addValueEventListener(mp3NumListener);

    }


    public void registerContentsValueListner() {

        contentsDatabase = mDatabase.child("contents");
        contentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot tmpSnapshot: snapshot.getChildren()){
                    String anchorID = tmpSnapshot.getKey();

                    try{
                        //포즈 정보 불러오기
                        HashMap<String, Double> poses = (HashMap<String, Double>) tmpSnapshot.child("pose").getValue();
                        float[] poseT = {
                                ((Double) poses.get("Tx")).floatValue(),
                                ((Double) poses.get("Ty")).floatValue(),
                                ((Double) poses.get("Tz")).floatValue()
                        };
                        float[] poseR = {
                                ((Double) poses.get("Rx")).floatValue(),
                                ((Double) poses.get("Ry")).floatValue(),
                                ((Double) poses.get("Rz")).floatValue(),
                                ((Double) poses.get("Rw")).floatValue(),
                        };
                        Pose pose = new Pose(poseT, poseR);

                        //gps정보 불러오기
                        HashMap<String, Double> gps = (HashMap<String, Double>) tmpSnapshot.child("gps").getValue();

                        wrappedAnchorList.add(new WrappedAnchor(
                                anchorID,
                                pose,
                                tmpSnapshot.child("text_or_path").getValue(String.class),
                                tmpSnapshot.child("userID").getValue(String.class),
                                gps.get("lat"),
                                gps.get("lng"),
                                tmpSnapshot.child("azimuth").getValue(int.class),
                                tmpSnapshot.child("type").getValue(int.class)
                        ));
                    }catch (NullPointerException e){
                        Log.d("순서", "리스너 데이터 null 예외 발생");
                    }catch (ClassCastException e){

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


    public void registerGPSValueListner() {

        gpsDatabase = mDatabase.child("anchorList");
        gpsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot tmpSnapshot: snapshot.getChildren()){
                    String anchorID = tmpSnapshot.getKey();

                    try{
                        HashMap<String, Double> anchorList = (HashMap<String, Double>) tmpSnapshot.getValue();

                        wrappedAnchorList.add(new WrappedAnchor(
                                anchorID,
                                anchorList.get("lat").doubleValue(),
                                anchorList.get("lng").doubleValue()
                        ));
                    }catch (NullPointerException e){

                    }catch (ClassCastException e){

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        gpsDatabase.addValueEventListener(gpsListener);
    }

}
