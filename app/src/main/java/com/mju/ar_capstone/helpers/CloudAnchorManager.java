/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mju.ar_capstone.helpers;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.ar.core.Anchor;
import com.google.ar.core.Anchor.CloudAnchorState;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.utilities.Preconditions;
import com.mju.ar_capstone.CustomDialog;
import com.mju.ar_capstone.WrappedAnchor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A helper class to handle all the Cloud Anchors logic, and add a callback-like mechanism on top of
 * the existing ARCore API.
 */
public class CloudAnchorManager {

  private FirebaseManager firebaseManager;
  private static String strAnchorID = "anchorID_";
  private String currentAnchorID;
  private WrappedAnchor wrappedAnchor;

  public synchronized void setFirebaseManager(FirebaseManager firebaseManager) {this.firebaseManager = firebaseManager;}

  public String makeCloudAnchorID(){
    currentAnchorID = strAnchorID + String.valueOf(firebaseManager.getNextAnchorNum());
    return currentAnchorID;
  }

  public String getCurrentAnchorID(){
    return currentAnchorID;
  }

  public void hostCloudAnchor(Pose pose, String text_or_path, String userId, double lat, double lng, float[] accXYZ, String anchorType) {
    wrappedAnchor = new WrappedAnchor(makeCloudAnchorID(), pose, text_or_path, userId, lat, lng, accXYZ, anchorType);
  }

  public void onUpdate() {
    firebaseManager.setContent(wrappedAnchor);
  }

}

