package com.mju.ar_capstone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.ar.sceneform.HitTestResult;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ArSfActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private ViewRenderable viewRenderable, tempRenderable;

    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();

    private Button btnAnchorLoad;

//    private CustomDialog customDialog;


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
        firebaseManager.registerValueListner();


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

        for(WrappedAnchor wrappedAnchor: firebaseManager.wrappedAnchorList){
            String tmpAnchorId = wrappedAnchor.getAnchorId();
            String tmpAnchorText = wrappedAnchor.getAnchorText();
            Log.d("순서", tmpAnchorId);
            Log.d("순서", tmpAnchorText);

            arFragment.getArSceneView().getSession().update();
            Anchor anchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(tmpAnchorId);

            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            Log.d("순서", "onClick test");

            // Create the transformable model and add it to the anchor.
            TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
            model.setParent(anchorNode);
            model.setRenderable(this.viewRenderable);

            TextView textView = (TextView) viewRenderable.getView();
            textView.setText(tmpAnchorText);

            //각각의 모델에 탭 리스너 부착
            model.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    Log.d("순서", "model onTapped");

                    Dialog dialog = new Dialog(ArSfActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.requestWindowFeature(WindowManager.LayoutParams.TYPE_PHONE);
                    dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    dialog.setContentView(R.layout.dialog_arsf);
                    dialog.show();

                    EditText edtDialog = dialog.findViewById(R.id.edtDialog);
                    TextView tvOk = dialog.findViewById(R.id.option_codetype_dialog_positive);
                    tvOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String text = edtDialog.getText().toString();
                            TextView textView = (TextView) viewRenderable.getView();
                            textView.setText(text);

                            Log.d("순서", "눌림");

                            dialog.dismiss();
                        }
                    });
                }
            });

            model.select();
        }

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
                .thenAccept(renderable -> {
                    ArSfActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = renderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

//    // 텍스트 tempRederable 생성 메소드
//    public void makeModels(String text){
//
//        Log.d("순서", "makeModels");
//
//
//        View view = getLayoutInflater().inflate(R.layout.view_model_text, null, false);
//        TextView textView = (TextView) view.findViewById(R.id.tvTestText);
//        textView.setText(text);
//
//        WeakReference<ArSfActivity> weakActivity = new WeakReference<>(this);
//        ViewRenderable.builder()
//                .setView(this, view)
//                .build()
//                .thenAccept(renderable -> {
//                    ArSfActivity activity = weakActivity.get();
//                    if (activity != null) {
//                        Log.d("순서", "tempRenderable에 넣음");
//                        activity.tempRenderable = renderable;
//                    }
//                })
//                .exceptionally(throwable -> {
//                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
//                    return null;
//                });
//
//        Log.d("순서", "makeModels end");
//    }


    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        if (viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d("순서", "onTapPlane");

        // Create the Anchor. and Listener
        Anchor anchor = hitResult.createAnchor();
        FirebaseCloudAnchorIdListener hostListener = new FirebaseCloudAnchorIdListener();
        cloudManager.hostCloudAnchor(anchor, hostListener);
        // 클라우드 앵커 상태가 success여야 앵커가 저장됨. 주변 정보를 충분히 얻었을때 success가 되는데...

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());


        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.viewRenderable);
        model.select();
        Toast.makeText(getApplicationContext(), "앵커에 글을 남기고 싶으면 앵커를 클릭하세요.", Toast.LENGTH_LONG).show();
        Log.d("순서", "model 생성됨");
        model.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                Log.d("순서", "model onTapped");

                Dialog dialog = new Dialog(ArSfActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.requestWindowFeature(WindowManager.LayoutParams.TYPE_PHONE);
                dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                dialog.setContentView(R.layout.dialog_arsf);
                dialog.show();

                EditText edtDialog = dialog.findViewById(R.id.edtDialog);
                TextView tvOk = dialog.findViewById(R.id.option_codetype_dialog_positive);
                tvOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = edtDialog.getText().toString();
                        TextView textView = (TextView) viewRenderable.getView();
                        textView.setText(text);

                        Log.d("순서", "눌림");

                        dialog.dismiss();
                    }
                });

//                customDialog = new CustomDialog(ArSfActivity.this, new CustomDialog.CustomDialogClickListener() {
//                    @Override
//                    public void onPositiveClick() {
//                        Log.d("순서", "버튼 눌림");
//
//                    }
//
//                    @Override
//                    public void onNegativeClick() {
//
//                    }
//                });
//                customDialog.setCanceledOnTouchOutside(true);
//                customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//                customDialog.setCancelable(true);
//                customDialog.show();

                Log.d("순서", "customDialog 생성됨");
            }
        });

        Log.d("순서", "onTapPlane end");
        cloudManager.onUpdate();

    }



    /**
     * Listens for both a new room code and an anchor ID, and shares the anchor ID in Firebase with
     * the room code when both are available.
     */
    private final class FirebaseCloudAnchorIdListener implements CloudAnchorManager.CloudAnchorHostListener {

        private String cloudAnchorId;

        @Override
        public void onCloudTaskComplete(Anchor anchor) {
            Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
            Log.d("순서", "cloudState: " + cloudState.toString());
            if (cloudState.isError()) {
                Log.e("test", "Error hosting a cloud anchor, state " + cloudState);
                return;
            }
            Preconditions.checkState(
                    cloudAnchorId == null, "The cloud anchor ID cannot have been set before.");
            cloudAnchorId = anchor.getCloudAnchorId();
            Log.d("순서", "cloudAnchorId: " + cloudAnchorId.toString());
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




