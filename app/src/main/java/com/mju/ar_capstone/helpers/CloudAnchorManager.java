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


  @Nullable
  private Session session = null;
  private FirebaseManager firebaseManager;
  private final List<WrappedAnchor> wrappedAnchorList = new ArrayList<>();

  //임시용
  private String tmpCloudAnchorID = null;


  public synchronized void setSession(Session session) {
    this.session = session;
  }
  public synchronized void setFirebaseManager(FirebaseManager firebaseManager) {this.firebaseManager = firebaseManager;}


  // 클라우드 앵커 발급
  public synchronized void hostCloudAnchor(Anchor anchor, String text_or_path, String userId, double lat, double lng, String anchorType) {
    Anchor newAnchor = session.hostCloudAnchor(anchor);
    wrappedAnchorList.add(new WrappedAnchor(newAnchor, text_or_path, userId, lat, lng, anchorType));

  }

  public String getTmpCloudAnchorID(){
    return tmpCloudAnchorID;
  }

  public synchronized void onUpdate() {
    Iterator iterator = wrappedAnchorList.iterator();
    while (iterator.hasNext()) {
      WrappedAnchor wrappedAnchor = (WrappedAnchor) iterator.next();
      Anchor anchor = wrappedAnchor.getAnchor();
      if (isReturnableState(anchor.getCloudAnchorState())) {
        String cloudAnchorId = anchor.getCloudAnchorId();

        //여기서 임시 변수에 한번 넣고 다른곳에서 뽑아가야할듯
        tmpCloudAnchorID = cloudAnchorId;

        wrappedAnchor.setCloudAnchorID(cloudAnchorId);
        Log.d("순서", "cloudAnchorId: " + cloudAnchorId);
        Log.d("순서", "tmpcloudAnchorId: " + tmpCloudAnchorID);

        firebaseManager.setContent(wrappedAnchor);
        iterator.remove();
      }
    }
  }

  // 클라우드 상태 체크
  private static boolean isReturnableState(CloudAnchorState cloudState) {
    Log.d("순서", "isReturnableState: " + cloudState.toString());
    switch (cloudState) {
      case NONE:
      case TASK_IN_PROGRESS:
        return false;
      default:
        return true;
    }
  }


//  /**
//   * This method resolves an anchor. The {@code listener} will be invoked when the results are
//   * available.
//   */
//  public synchronized void resolveCloudAnchor(
//          String anchorId, CloudAnchorResolveListener listener, long startTimeMillis) throws CameraNotAvailableException {
//    Preconditions.checkNotNull(session, "The session cannot be null.");
//    Anchor newAnchor = session.resolveCloudAnchor(anchorId);
//    deadlineForMessageMillis = startTimeMillis + DURATION_FOR_NO_RESOLVE_RESULT_MS;
//
//    Log.d("순서", "resolveCloudAnchor pendinfResolveAnchors에 넣음");
//    pendingResolveAnchors.put(newAnchor, listener);
//  }


//    Log.d("순서", "Cloud Manager onUpdate resolve");
//    Iterator<Map.Entry<Anchor, CloudAnchorResolveListener>> resolveIter =
//            pendingResolveAnchors.entrySet().iterator();
//    while (resolveIter.hasNext()) {
//      Map.Entry<Anchor, CloudAnchorResolveListener> entry = resolveIter.next();
//      Anchor anchor = entry.getKey();
//      CloudAnchorResolveListener listener = entry.getValue();
//      if (isReturnableState(anchor.getCloudAnchorState())) {
//        listener.onCloudTaskComplete(anchor);
//        resolveIter.remove();
//      }
//      if (deadlineForMessageMillis > 0 && SystemClock.uptimeMillis() > deadlineForMessageMillis) {
//        listener.onShowResolveMessage();
//        deadlineForMessageMillis = 0;
//      }
//    }

}

