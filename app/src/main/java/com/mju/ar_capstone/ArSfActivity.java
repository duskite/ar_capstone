package com.mju.ar_capstone;

import android.net.Uri;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.utilities.Preconditions;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DatabaseError;
import com.mju.ar_capstone.helpers.CloudAnchorManager;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ArSfActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private ViewRenderable viewRenderable;

    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();

    private Button btnAnchorLoad;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_arsf);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        firebaseManager = new FirebaseManager();

        // 기타 필요한 화면 요소들
        btnAnchorLoad = (Button) findViewById(R.id.btnAnchorLoad);
        btnAnchorLoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "앵커 불러오는중...", Toast.LENGTH_SHORT).show();
                try {
                    loadCloudAnchors();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        Log.d("순서", "before loadModels");

        loadModels();

        Log.d("순서", "after loadModels");
    }

    // firebase에 저장된 앵커 불러오기
    public void loadCloudAnchors() throws CameraNotAvailableException {

        arFragment.getArSceneView().getSession().update();
        Anchor anchor = arFragment.getArSceneView().getSession().resolveCloudAnchor("ua-7eb0d710fcbd8988677728fe58d075a7");

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        Log.d("순서", "onClick test");

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.viewRenderable);
        model.select();

    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {

        Log.d("순서", "onAttachFragment");
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }

        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        session.configure(config);
        cloudManager.setSession(session);

        Log.d("순서", "onSessionConfiguratioin");


    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);

        Log.d("순서", "onViewCreated");

    }


    public void loadModels() {
        Log.d("순서", "loadModels");
        WeakReference<ArSfActivity> weakActivity = new WeakReference<>(this);
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {
                    ArSfActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }


    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        if (viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("순서", "onTapPlane");

        // Create the Anchor. and Listener
        Anchor anchor = hitResult.createAnchor();
        RoomCodeAndCloudAnchorIdListener hostListener = new RoomCodeAndCloudAnchorIdListener();

        // 클라우드 앵커 동기화 관련해서 문제가 있는거 같음
        // 현재 앵커는 찍혀도 서버로 데이터가 바로 안올라가는 경우가 있음
        cloudManager.hostCloudAnchor(anchor, hostListener);
        cloudManager.onUpdate();

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());


        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.viewRenderable);
        model.select();

    }

    /**
     * Listens for both a new room code and an anchor ID, and shares the anchor ID in Firebase with
     * the room code when both are available.
     */
    private final class RoomCodeAndCloudAnchorIdListener implements CloudAnchorManager.CloudAnchorHostListener {

        private String cloudAnchorId;

        @Override
        public void onCloudTaskComplete(Anchor anchor) {
            Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
            if (cloudState.isError()) {
                Log.e("test", "Error hosting a cloud anchor, state " + cloudState);
                return;
            }
            Preconditions.checkState(
                    cloudAnchorId == null, "The cloud anchor ID cannot have been set before.");
            cloudAnchorId = anchor.getCloudAnchorId();
            checkAndMaybeShare();
        }

        private void checkAndMaybeShare() {

            Log.d("순서", "checkAndMaybeShare");

            if (cloudAnchorId == null) {
                return;
            }
            firebaseManager.setContent(cloudAnchorId);
        }
    }
}




