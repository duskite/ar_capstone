package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Trackable;

public class WrappedAnchor {

    private Anchor anchor;
    private String cloudAnchorId;
    private String anchorText;
    private double lat;
    private double lng;
    private String userID;


    public WrappedAnchor(Anchor anchor, String text, String userID, double lat, double lng){
        this.anchor = anchor;
        this.anchorText = text;
        this.userID = userID;
        this.lat = lat;
        this.lng = lng;
    }

    public Anchor getAnchor(){
        return anchor;
    }
    public String getCloudAnchorId(){
        return cloudAnchorId;
    }
    public String getText() {return anchorText;}
    public double getlat() {return lat;}
    public double getlng() {return lng;}
    public String getUserID() {return userID;}


    public void setCloudAnchorID(String cloudAnchorID){
        this.cloudAnchorId = cloudAnchorID;
    }
}
