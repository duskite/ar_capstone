package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.math.Vector3;
import com.google.firebase.database.Exclude;

public class WrappedAnchor {

    private String cloudAnchorId;
    private String textOrPath;
    private String userID;
    private String anchorType;

    private int azimuth;

    private double lat;
    private double lng;

    private Pose pose;

    private Vector3 cameraVector;

    public WrappedAnchor(){

    }

    // map gps
    public WrappedAnchor(String cloudAnchorId, double lat, double lng){
        this.cloudAnchorId = cloudAnchorId;
        this.lat = lat;
        this.lng = lng;
    }

    // ar에서 사용하는 객체
    public WrappedAnchor(String cloudAnchorId, Pose pose, Vector3 cameraVector, String text, String userID, double lat, double lng, int azimuth, String anchorType){
        this.cloudAnchorId = cloudAnchorId;
        this.textOrPath = text;
        this.userID = userID;
        this.anchorType = anchorType;
        this.pose = pose;
        this.lat = lat;
        this.lng = lng;
        this.azimuth = azimuth;
        this.cameraVector = cameraVector;
    }

    public String getCloudAnchorId() {
        return cloudAnchorId;
    }

    public void setCloudAnchorId(String cloudAnchorId) {
        this.cloudAnchorId = cloudAnchorId;
    }

    public String getTextOrPath() {
        return textOrPath;
    }

    public void setTextOrPath(String textOrPath) {
        this.textOrPath = textOrPath;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getAnchorType() {
        return anchorType;
    }

    public void setAnchorType(String anchorType) {
        this.anchorType = anchorType;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public int getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(int azimuth) {
        this.azimuth = azimuth;
    }

    public Vector3 getCameraVector(){
        return cameraVector;
    }
    public void setCameraVector(Vector3 vector3){
        this.cameraVector = vector3;
    }

}
