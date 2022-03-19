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

    //불러올때 이런 구조
    public WrappedAnchor(String cloudAnchorId, String text, String userID, String anchorType){
        this.cloudAnchorId = cloudAnchorId;
        this.textOrPath = text;
        this.userID = userID;
        this.anchorType = anchorType;
    }

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
    public double getlat() {return lat;}
    public double getlng() {return lng;}
    public String getUserID() {return userID;}
    public String getAnchorType(){return anchorType;}


    public void setCloudAnchorID(String cloudAnchorID){
        this.cloudAnchorId = cloudAnchorID;
    }
}
