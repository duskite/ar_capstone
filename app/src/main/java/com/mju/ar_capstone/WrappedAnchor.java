package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Trackable;

public class WrappedAnchor {
    private Anchor anchor;
    private Trackable trackable;

    public WrappedAnchor(Anchor anchor, Trackable trackable){
        this.anchor = anchor;
        this.trackable = trackable;
    }

    public Anchor getAnchor(){
        return anchor;
    }
    public Trackable getTrackable(){
        return trackable;
    }
}
