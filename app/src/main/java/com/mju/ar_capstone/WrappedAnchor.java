package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Trackable;

public class WrappedAnchor {

    private Anchor anchor;
    private String anchorId;

    public WrappedAnchor(Anchor anchor){
        this.anchor = anchor;
    }

    public WrappedAnchor(String anchorId){
        this.anchorId = anchorId;
    }

    public Anchor getAnchor(){
        return anchor;
    }
    public String getAnchorId(){
        return anchorId;
    }
}
