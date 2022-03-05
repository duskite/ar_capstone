package com.mju.ar_capstone;

import com.google.ar.core.Anchor;
import com.google.ar.core.Trackable;

public class WrappedAnchor {

    private Anchor anchor;

    public WrappedAnchor(Anchor anchor){
        this.anchor = anchor;
    }

    public Anchor getAnchor(){
        return anchor;
    }
}
