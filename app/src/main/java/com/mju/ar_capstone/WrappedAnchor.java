package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Trackable;

public class WrappedAnchor {

    private Anchor anchor;
    private String cloudAnchorId;
    private String textOrPath;
    private double lat;
    private double lng;
    private String userID;
    private String anchorType;

    public WrappedAnchor(){

    }

    // 클래스를 나눠놓음 double 오류때문에. 아직 원인을 잘 모르겠음
    //맵에서 불러올때
    public WrappedAnchor(String cloudAnchorId, String text, String userID, double lat, double lng, String anchorType){
        this.cloudAnchorId = cloudAnchorId;
        this.textOrPath = text;
        this.userID = userID;
        this.anchorType = anchorType;
        this.lat = lat;
        this.lng = lng;
    }

    // ar에서 불러올때
    public WrappedAnchor(String cloudAnchorId, String text, String userID, String anchorType){
        this.cloudAnchorId = cloudAnchorId;
        this.textOrPath = text;
        this.userID = userID;
        this.anchorType = anchorType;
    }

    //이거는 앵커 업로드 할때
    public WrappedAnchor(Anchor anchor, String text, String userID, double lat, double lng, String anchorType){
        this.anchor = anchor;
        this.textOrPath = text;
        this.userID = userID;
        this.lat = lat;
        this.lng = lng;
        this.anchorType = anchorType;
    }

    public Anchor getAnchor(){
        return anchor;
    }
    public String getCloudAnchorId(){
        return cloudAnchorId;
    }
    public String getText() {return textOrPath;}
    public double getlat() {return (double) lat;}
    public double getlng() {return (double) lng;}
    public String getUserID() {return userID;}
    public String getAnchorType(){return anchorType;}


    public void setCloudAnchorID(String cloudAnchorID){
        this.cloudAnchorId = cloudAnchorID;
    }
}
