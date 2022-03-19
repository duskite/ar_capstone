package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;

public class WrappedAnchor {

    private String cloudAnchorId;
    private String textOrPath;
    private double lat;
    private double lng;
    private String userID;
    private String anchorType;

    private Pose pose;

    public WrappedAnchor(){

    }

    //맵에서 불러올때
    public WrappedAnchor(String cloudAnchorId, String text, String userID, double lat, double lng, String anchorType){
        this.cloudAnchorId = cloudAnchorId;
        this.textOrPath = text;
        this.userID = userID;
        this.anchorType = anchorType;
        this.lat = lat;
        this.lng = lng;
    }


    //클라우드 앵커 없이 테스트 중
    public WrappedAnchor(Pose pose, String text, String userID, double lat, double lng, String anchorType){
        this.textOrPath = text;
        this.userID = userID;
        this.lat = lat;
        this.lng = lng;
        this.anchorType = anchorType;
        this.pose = pose;

    }
    //클라우드 앵커 없이 테스트 중
    public WrappedAnchor(String cloudAnchorId, Pose pose, String text, String userID, String anchorType){
        this.cloudAnchorId = cloudAnchorId;
        this.textOrPath = text;
        this.userID = userID;
        this.anchorType = anchorType;
        this.pose = pose;
    }

    public String getCloudAnchorId(){
        return cloudAnchorId;
    }
    public String getText() {return textOrPath;}
    public double getlat() {return (double) lat;}
    public double getlng() {return (double) lng;}
    public String getUserID() {return userID;}
    public String getAnchorType(){return anchorType;}

    public Pose getPose(){return pose;}


    public void setCloudAnchorID(String cloudAnchorID){
        this.cloudAnchorId = cloudAnchorID;
    }
}
