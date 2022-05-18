package com.mju.ar_capstone.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.mju.ar_capstone.invenfragments.UserInvenFragment;

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
    private DatabaseReference oneAnchorInfo;

    public DatabaseReference channelDatabase;

    private DatabaseReference scrapDatabase;

    private static final String DB_REGION = "https://ar-capstone-dbf8e-default-rtdb.asia-southeast1.firebasedatabase.app";

    //값 불러오는 리스너
    private ValueEventListener anchorNumListener = null;
    private ValueEventListener imageNumListener = null;
    private ValueEventListener mp3NumListener = null;
    //채널 리스트 불러오기
    private ValueEventListener channelListListener = null;

    private static int nextAnchorNum;
    private static int nextImageNum;
    private static int nextMp3Num;

    public ArrayList<WrappedAnchor> wrappedAnchorList = new ArrayList<>();
    public ArrayList<WrappedAnchor> userScrapAnchorList = new ArrayList<>();
    public ArrayList<String> userScrapAnchorIdList = new ArrayList<>();


    private String myID;
    private static boolean stateHaveKey = false;

    //채널 이름 넣을 변수
    public ArrayList<String> publicChannelList = new ArrayList<>();
    public ArrayList<String> allChannelList = new ArrayList<>();


    public FirebaseManager(){
        channelDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child("channel_list");
    }
    public FirebaseManager(String channel){
        addChannelList(channel);

        mDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child(channel);

        //앵커 넘버, 이미지 넘버 가져오기
        registerAnchorNumValueLisner();
        registerImageNumValueLisner();
        registerMp3NumValueLisner();

        DatabaseReference.goOnline();
    }

    //채널 삭제 메소드
    public void deleteChannel(String selectedChannel){
        DatabaseReference tmpDB = FirebaseDatabase.getInstance(DB_REGION).getReference();
        tmpDB.child("channel_list").child(selectedChannel).removeValue();
        tmpDB.child(selectedChannel).removeValue();
        unRegisterAnchorNumValueLisner();
    }

    //키를 가지고 있지는 여부 반환
    public boolean checkHaveKey(){
        return stateHaveKey;
    }

    // searching어쩌구 메소드에서 쓰려고 만듦 / 콜백 데이터 로드되면
    public interface GetOneAnchorInfoListener{
        void onDataLoaded(WrappedAnchor wrappedAnchor);
    }

    // 참가자가 스크랩시 db에 반영
    public void userScrapAnchor(String channel, String userID, String anchorID, int anchorType){
        scrapDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child("users").child(userID).child(channel);
        scrapDatabase.child(anchorID).setValue(anchorType);
    }
    // db에서 참가자가 스크랩했던 앵커들 가져옴
    public void loadUserScrapAnchors(String channel, String userID){
        scrapDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child("users").child(userID).child(channel);
        scrapDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){

                }else {
                    DataSnapshot dataSnapshot = task.getResult();
                    for(DataSnapshot tmpSnapshot :dataSnapshot.getChildren()) {
                        String tmpStr = tmpSnapshot.getKey().toString();
                        int tmpAnchorType = tmpSnapshot.getValue(int.class);
                        if(tmpAnchorType == 3){ //키를 가지고 있음
                            Log.d("스크랩", "키 소유중");
                            stateHaveKey = true;
                        }
                        userScrapAnchorIdList.add(tmpStr);
                        Log.d("스크랩", tmpStr);
                    }
                    Log.d("스크랩", "한 번 로드 끝");
                }
            }
        });

    }
    public ArrayList<WrappedAnchor> getUserScrapAnchorList(){

        for(String s: userScrapAnchorIdList){
            for(int i=0; i< wrappedAnchorList.size(); i++ )
            if(s.equals(wrappedAnchorList.get(i).getCloudAnchorId())){
                userScrapAnchorList.add(wrappedAnchorList.get(i));
            }
        }
        return userScrapAnchorList;
    }
    public void clearUserScrapAnchorIdList(){
        userScrapAnchorIdList.clear();
    }

    public ArrayList<String> getPublicChannelList(){
        return publicChannelList;
    }
    public ArrayList<String> getAllChannelList(){
        return allChannelList;
    }

    public void setAuth(String myID){
        this.myID = myID;
    }

    //앵커 아이디 가지고 내용물 찾아서 콜백함
    public void searchingContentWithAnchorID(String anchorID, GetOneAnchorInfoListener getOneAnchorInfoListener){
        Log.d("앵커1개", "get " + anchorID);
        oneAnchorInfo = mDatabase.child("contents").child(anchorID);
        oneAnchorInfo.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d("앵커1개", "다운 실패");

                }else{
                    Log.d("앵커1개", "다운 성공");
                    WrappedAnchor wrappedAnchor = null;
                    DataSnapshot dataSnapshot = task.getResult();
                    try{
                        //포즈 정보 불러오기
                        HashMap<String, Double> poses = (HashMap<String, Double>) dataSnapshot.child("pose").getValue();
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
                        HashMap<String, Double> gps = (HashMap<String, Double>) dataSnapshot.child("gps").getValue();

                        wrappedAnchor = new WrappedAnchor(
                                anchorID,
                                pose,
                                dataSnapshot.child("text_or_path").getValue(String.class),
                                dataSnapshot.child("userID").getValue(String.class),
                                gps.get("lat"),
                                gps.get("lng"),
                                dataSnapshot.child("azimuth").getValue(int.class),
                                dataSnapshot.child("type").getValue(int.class)
                        );
                    }catch (NullPointerException e){
                        Log.d("순서", "리스너 데이터 null 예외 발생");
                    }catch (ClassCastException e){

                    }
                    getOneAnchorInfoListener.onDataLoaded(wrappedAnchor);
                }
            }
        });
    }

    public void getChannelList(){
        channelDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){

                }else{
                    DataSnapshot dataSnapshot = task.getResult();
                    for(DataSnapshot tmpSnapshot: dataSnapshot.getChildren()){
                        Log.d("파베채널리스트", String.valueOf(tmpSnapshot.getKey()));

                    try{
                        int checkChannelType = tmpSnapshot.child("channelType").getValue(int.class);
                        String hostID = tmpSnapshot.child("hostID").getValue(String.class);
                        String channelName = tmpSnapshot.getKey();
                        Log.d("채널이름 파이어베이스 리스너", channelName);

                        if(hostID.equals(myID)){ //자기가 만든 채널만 접근 가능
                            allChannelList.add(channelName); //주최자가 접근가능한 채널이름 리스트에 넣는 부분
                        }
                        if(checkChannelType == 1){ // 공개 채널일때만 리스트에 넣는 부분
                            publicChannelList.add(channelName);
                            if(!hostID.equals(myID)){ //중복 방지
                                allChannelList.add(channelName); //주최자가 접근가능한 채널이름 리스트에 넣는 부분
                            }
                        }

                    }catch (NullPointerException e){
                        //만약 비어있으면 비공개 채널이라고 생각
                        //근데 비어있을리가 없음
                        //디폴트가 1 공개 채널임
                    }

                    }
                }
            }
        });

    }

    //승리자 정보 db에 저장
    public void sendWinnerInfo(String channel, String userID){
        DatabaseReference winnerDB = FirebaseDatabase.getInstance(DB_REGION).getReference().child("channel_list").child(channel);
        winnerDB.child("winner").setValue(userID);
        winnerDB.child("winnerList").child(userID).setValue(createdTimeOfContent());
    }

    //채널리스트에 채널 추가함
    public void addChannelList(String channel){
        channelDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child("channel_list");
        //우선 의미없는 값 넣어서 디비 만들음
        channelDatabase.child(channel).child("created").setValue("생성됨");
    }
    //채널 기본 정보 입력
    public void setChannelInfo(String channel, int channelType, String hostID){
        channelDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child("channel_list");
        channelDatabase.child(channel).child("hostID").setValue(hostID);
        channelDatabase.child(channel).child("channelType").setValue(channelType);
//        channelDatabase.child(channel).child("winner").setValue("없음");
//        channelDatabase.child(channel).child("winnerList").setValue("없음");
    }

    //참가자가 채널에 참가할때
    public void joinChannel(String channel, String userID){
        channelDatabase = FirebaseDatabase.getInstance(DB_REGION).getReference().child("channel_list");
        channelDatabase.child(channel).child("users").child(userID).setValue("참가");
    }


    // ar에서 가져가서 처리하는게 나을듯
    public ArrayList<WrappedAnchor> getWrappedAnchorList(){
        return wrappedAnchorList;
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
            contents.put("text_or_path", wrappedAnchor.getTextOrPath() + ".3gp");
            mp3NumDatabase.setValue(nextMp3Num + 1);
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


    // 채널 앵커 넘버 리스너 해제
    public void unRegisterAnchorNumValueLisner(){
        anchorNumDatabase.removeEventListener(anchorNumListener);
    }

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

    public void getContents(){
        contentsDatabase = mDatabase.child("contents");
        contentsDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){

                }else {
                    DataSnapshot dataSnapshot = task.getResult();
                    for(DataSnapshot tmpSnapshot: dataSnapshot.getChildren()){
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
            }
        });
    }


}
