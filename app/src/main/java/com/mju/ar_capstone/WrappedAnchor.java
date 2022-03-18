package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Trackable;

public class WrappedAnchor {

    private String anchorId;
    private String anchorText;
    private String anchorGPS;
    private String createdUser;

    public WrappedAnchor(String anchorId, String anchorText){
        this.anchorId = anchorId;
        this.anchorText = anchorText;
    }

    public String getAnchorId(){
        return anchorId;
    }
    public String getAnchorText() {return anchorText;}
}
