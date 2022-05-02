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

import com.google.ar.core.Pose;
import com.mju.ar_capstone.WrappedAnchor;

/**
 * A helper class to handle all the Cloud Anchors logic, and add a callback-like mechanism on top of
 * the existing ARCore API.
 */
public class CloudAnchorManager {

  private FirebaseManager firebaseManager;
  private static String strAnchorID = "anchorID_";
  private String currentAnchorID;
  private WrappedAnchor wrappedAnchor;

  public void setFirebaseManager(FirebaseManager firebaseManager) {this.firebaseManager = firebaseManager;}

  public String makeCloudAnchorID(){
    currentAnchorID = strAnchorID + String.valueOf(firebaseManager.getNextAnchorNum());
    return currentAnchorID;
  }

  public String getCurrentAnchorID(){
    return currentAnchorID;
  }

  public void hostCloudAnchor(Pose pose, String text_or_path, String userId, double lat, double lng, int azimuth, int anchorType) {
    wrappedAnchor = new WrappedAnchor(makeCloudAnchorID(), pose, text_or_path, userId, lat, lng, azimuth, anchorType);
  }

  public void onUpdate() {
    firebaseManager.setContent(wrappedAnchor);
  }

}

