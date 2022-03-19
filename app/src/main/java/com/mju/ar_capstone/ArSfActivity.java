package com.mju.ar_capstone;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mju.ar_capstone.helpers.CloudAnchorManager;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private ViewRenderable textRenderable, selectRenderable, imageRanderable;


    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();

    private Button btnAnchorLoad;

    //잠시 테스트 중인 애들
    private final int GALLERY_CODE = 10;
    private ImageView tmpImageView;
    private Uri tmpImage;
    private FireStorageManager fireStorageManager;

    //대략적인 gps정보 앵커랑 같이 서버에 업로드하려고
    private LocationManager locationManager;
    private double lat;
    private double lng;

    //현재 위치 가져오기
    public void checkGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lat = currentLocation.getLatitude();
        lng = currentLocation.getLongitude();
    }


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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager();
        firebaseManager.registerValueListner();
        fireStorageManager = new FireStorageManager();


        // 기타 필요한 화면 요소들
        btnAnchorLoad = (Button) findViewById(R.id.btnAnchorLoad);
        btnAnchorLoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "앵커 불러오는중...", Toast.LENGTH_SHORT).show();
                loadCloudAnchors();
            }
        });

        loadModels();
    }

    // 타입에 맞게 각각 다른 리스너 붙혀줘야함
    public void loadCloudAnchors(){
        for(WrappedAnchor wrappedAnchor: firebaseManager.wrappedAnchorList){
            String cloudAnchorID = wrappedAnchor.getCloudAnchorId();
            String text_or_path = wrappedAnchor.getText();
            String userID = wrappedAnchor.getUserID();
            String stringAnchorType = wrappedAnchor.getAnchorType();
            CustomDialog.AnchorType anchorType = null;

            if(stringAnchorType.equals("text")){
                anchorType = CustomDialog.AnchorType.text;
            }else if(stringAnchorType.equals("image")){
                //불러올때 여기서 이미지 한번 로드함
                Log.d("순서 이미지 로드", "시작");
                fireStorageManager.downloadImage(text_or_path);
                anchorType = CustomDialog.AnchorType.image;
            }else if(stringAnchorType.equals("test")){
                anchorType = CustomDialog.AnchorType.test;
            }

//            double lat = wrappedAnchor.getlat();
//            double lng = wrappedAnchor.getlng();

            Anchor anchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorID);

            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
            model.setParent(anchorNode);

            changeAnchor(model, text_or_path, anchorType);
            setTapListenerType(model, anchor, anchorType);
            model.select();
        }
    }

    // 서버에 생성되어있는 앵커 불러와서 리스너 달아주는 거임
    public void setTapListenerType(TransformableNode model, Anchor anchor, CustomDialog.AnchorType anchorType){
        if(anchorType == CustomDialog.AnchorType.text){
            model.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    CustomDialog customDialog = new CustomDialog(ArSfActivity.this, new CustomDialog.CustomDialogClickListener() {
                        @Override
                        public void onPositiveClick(String tmpText, CustomDialog.AnchorType anchorType) {
                            Log.d("순서", "예스 클릭됨");
                            changeAnchor(model, tmpText, anchorType);
                            saveAnchor(anchor, tmpText, anchorType);

                        }

                        @Override
                        public void onNegativeClick() {
                            Log.d("순서 불러온 앵커 아이디", anchor.getCloudAnchorId());
                            firebaseManager.deleteContent(anchor.getCloudAnchorId());
                            anchor.detach();

                        }

                        @Override
                        public void onImageClick(ImageView dialogImg) {

                            tmpImageView = dialogImg;
                            loadAlbum();
                        }
                    });
                    customDialog.show();
                }
            });

        }else if(anchorType == CustomDialog.AnchorType.image){
            model.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    Log.d("순서", "모델이 잘 로드 되기는 함");
                }
            });

        }else if(anchorType == CustomDialog.AnchorType.test){

        }

    }


    //임시 앵커 생성 후 실제 앵커까지
    public void createSelectAnchor(HitResult hitResult){

        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setRenderable(this.selectRenderable);
        model.setParent(anchorNode);
        model.select();
        model.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                Log.d("순서", "모델 클릭됨");
                //여기서 앵커 종류를 설정해줘야 할 듯
                CustomDialog customDialog = new CustomDialog(ArSfActivity.this, new CustomDialog.CustomDialogClickListener() {
                    @Override
                    public void onPositiveClick(String tmpText, CustomDialog.AnchorType anchorType) {
                        Log.d("순서", "예스 클릭됨");
                        changeAnchor(model, tmpText, anchorType);
                        saveAnchor(anchor, tmpText, anchorType);

                    }
                    @Override
                    public void onNegativeClick() {

                        if(cloudManager.getTmpCloudAnchorID() != null){
                            String tmpAnchorID = cloudManager.getTmpCloudAnchorID();
                            firebaseManager.deleteContent(tmpAnchorID); //여기는 확정된 앵커일때
                            anchor.detach();
                        }else{
                            anchor.detach(); //여기는 임시일때
                        }


                    }

                    @Override
                    public void onImageClick(ImageView dialogImg) {
                        tmpImageView = dialogImg;
                        loadAlbum();
                    }
                });
                customDialog.show();
            }
        });
    }


    //화면상에 보여지는 앵커를 우선 바꿈
    public void changeAnchor(TransformableNode model, String text_or_path, CustomDialog.AnchorType anchorType){
        if(anchorType == CustomDialog.AnchorType.text){
            model.setRenderable(makeTextModels(text_or_path));
        }else if(anchorType == CustomDialog.AnchorType.image){
            tmpImage = fireStorageManager.getUri();
            Log.d("순서 모델", "체인지 이미지 모델");
            model.setRenderable(makeImageModels());
        }
    }

    // 종류에 맞게 앵커 저장, 앵커 아이디 리턴
    public void saveAnchor(Anchor anchor, String text, CustomDialog.AnchorType anchorType){
        String userId = firebaseAuthManager.getUID().toString();
        checkGPS();

        if(anchorType == CustomDialog.AnchorType.text){
            cloudManager.hostCloudAnchor(anchor, text, userId, lat, lng, "text");
        }else if(anchorType == CustomDialog.AnchorType.image){
            Log.d("순서 패스",fireStorageManager.getImagePath());
            String path = fireStorageManager.getImagePath();
            cloudManager.hostCloudAnchor(anchor, path, userId, lat, lng, "image");
            fireStorageManager.uploadImage(tmpImage);
        }
        cloudManager.onUpdate();
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
        cloudManager.setFirebaseManager(firebaseManager);

        Log.d("순서", "onSessionConfiguratioin");
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);

        Log.d("순서", "onViewCreated");

    }


    //시작시 모델들 로드 미리 해놓음
    public void loadModels() {
        Log.d("순서", "loadModels");
        WeakReference<ArSfActivity> weakActivity = new WeakReference<>(this);

        //텍스트 모델 생성
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_text)
                .build()
                .thenAccept(renderable -> {
                    ArSfActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.textRenderable = renderable;
                    }
                    Log.d("순서 모델", "모델 생성 1");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

        //선택 모델 생성
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_select)
                .build()
                .thenAccept(renderable -> {
                    ArSfActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.selectRenderable = renderable;
                    }
                    Log.d("순서 모델", "모델 생성 2");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

        // 이미지 모델 생성
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_image)
                .build()
                .thenAccept(renderable -> {
                    ArSfActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.imageRanderable = renderable;
                    }
                    Log.d("순서 모델", "모델 생성 3");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    // 한번 만들어 놓은 렌더러블은 수정가능함
    // 각각 다른 글자 띄우려면 매번 빌드해야한다고 함...
    public ViewRenderable makeTextModels(String text){
        ViewRenderable tmpRenderable = textRenderable.makeCopy();
        TextView textView = (TextView) tmpRenderable.getView();
        textView.setText(text);

        return tmpRenderable;
    }
    public ViewRenderable makeImageModels(){
        ViewRenderable tmpRenderable = imageRanderable.makeCopy();
        ImageView imageView = (ImageView) tmpRenderable.getView();
        imageView.setImageURI(tmpImage);

        return tmpRenderable;
    }






    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        Log.d("순서", "onTapPlane");

        createSelectAnchor(hitResult);

    }




    // 이미지 업로드 부분
    //사용자 갤러리 불러오기
    private void loadAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, GALLERY_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE) {
            tmpImage = data.getData();
            tmpImageView.setImageURI(tmpImage);
        }
    }
}




