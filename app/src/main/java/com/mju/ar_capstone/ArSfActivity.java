package com.mju.ar_capstone;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
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

public class ArSfActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
//    private Renderable model;
    private ViewRenderable viewRenderable;

    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();
    private RoomCodeAndCloudAnchorIdListener hostListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_arsf);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if(savedInstanceState == null){
            if(Sceneform.isSupported(this)){
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        firebaseManager = new FirebaseManager();

        Log.d("순서", "before loadModels");

        loadModels();

        Log.d("순서", "after loadModels");
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {

        Log.d("순서", "onAttachFragment");
        if(fragment.getId() == R.id.arFragment){
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }

        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        session.configure(config);
        cloudManager.setSession(session);

        Log.d("순서", "onSessionConfig");

    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
        hostListener = new RoomCodeAndCloudAnchorIdListener();

        Log.d("순서", "onViewCreated");

    }

    public void loadModels() {
        Log.d("순서", "loadModels");
        WeakReference<ArSfActivity> weakActivity = new WeakReference<>(this);
//        ModelRenderable.builder()
//                .setSource(this, Uri.parse("https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb"))
//                .setIsFilamentGltf(true)
//                .setAsyncLoadEnabled(true)
//                .build()
//                .thenAccept(model -> {
//                    ArSfActivity activity = weakActivity.get();
//                    if (activity != null) {
//                        activity.model = model;
//                    }
//                })
//                .exceptionally(throwable -> {
//                    Toast.makeText(
//                            this, "Unable to load model", Toast.LENGTH_LONG).show();
//                    return null;
//                });
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

        if (viewRenderable == null){
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("순서", "onTapPlane");

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();

        // 클라우드 앵커 올리는거 손봐야함
        cloudManager.hostCloudAnchor(anchor, hostListener);
        cloudManager.onUpdate();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());


        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.viewRenderable); //기존 this.model에서 변경함
//                .animate(true).start();
        model.select();




        // 혹시 몰라서 아직 안지움
        // 원래는 모델에 위에 타이틀 노드를 붙혀서 만드는 방식이었음
//        Node titleNode = new Node();
//        titleNode.setParent(model);
//        titleNode.setEnabled(false);
//        titleNode.setLocalPosition(new Vector3(0.0f, 1.0f, 0.0f));
//        titleNode.setRenderable(viewRenderable);
//        titleNode.setEnabled(true);
    }



    private final class RoomCodeAndCloudAnchorIdListener implements CloudAnchorManager.CloudAnchorHostListener{

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




