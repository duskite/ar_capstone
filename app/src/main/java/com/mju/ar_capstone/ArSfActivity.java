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
import android.os.Parcelable;
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
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ArSfActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private ViewRenderable textRenderable, selectRenderable, imageRenderable;
    private List<ViewRenderable> textRenderableList = new ArrayList<>();
    private List<ViewRenderable> imageRenderableList = new ArrayList<>();

    private static int cntTextRenderable = -1;
    private static int cntImageRenderable = -1;


    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();

    private Button btnAnchorLoad, btnMapApp;

    //잠시 테스트 중인 애들
    private final int GALLERY_CODE = 10;
    private ImageView tmpImageView;
    private Uri tmpImage;
    private FireStorageManager fireStorageManager;

    //내가 데이터를 쓰는 상황인지 불러오는 상황인지 체크해야할꺼 같음. 이미지를 내가 등록하는 상황인지
    // 불러오는 상황인지 체크
    private boolean writeMode = false;

    //대략적인 gps정보 앵커랑 같이 서버에 업로드하려고
    private LocationManager locationManager;
    private double lat = 0.0;
    private double lng = 0.0;

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
        lat = (double) currentLocation.getLatitude();
        lng = (double) currentLocation.getLongitude();

        Log.d("순서 더블 문제", "checkGPS 실행완료");
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

        //여기서 MapActivity로 넘어가도록함
        btnMapApp = findViewById(R.id.btnMapApp);
        btnMapApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });


        loadModels();
        makePreModels();
    }

    // 타입에 맞게 각각 다른 리스너 붙혀줘야함
    public void loadCloudAnchors(){
        writeMode = false;

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
                fireStorageManager.downloadImage(text_or_path); //이미지 다운전에 모델 체인지가 이루어짐
                anchorType = CustomDialog.AnchorType.image;
            }else if(stringAnchorType.equals("test")){
                anchorType = CustomDialog.AnchorType.test;
            }

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
                            writeMode = true;

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
                            writeMode = true;

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
        model.setName("temp"); //모델명을 변수로 임시 사용
        Log.d("순서 모델 이름 임시", model.getName());
        model.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {

                Log.d("순서", "모델 클릭됨");
                //여기서 앵커 종류를 설정해줘야 할 듯
                CustomDialog customDialog = new CustomDialog(ArSfActivity.this, new CustomDialog.CustomDialogClickListener() {
                    @Override
                    public void onPositiveClick(String tmpText, CustomDialog.AnchorType anchorType) {
                        writeMode = true;

                        Log.d("순서", "예스 클릭됨");
                        changeAnchor(model, tmpText, anchorType);
                        String tmpAnchorID = saveAnchor(anchor, tmpText, anchorType);
                        if(tmpAnchorID != null){
                            model.setName(tmpAnchorID);
                        }else{
                            model.setName("temp");
                        }
                        Log.d("순서 모델 이름 확정", model.getName());

                    }
                    @Override
                    public void onNegativeClick() {
                        Log.d("순서 모델 이름 확정 있는지", model.getName());
                        if(!model.getName().equals("temp")){
                            String tmpAnchorID = model.getName();
                            Log.d("순서 모델 이름 문자변수에", tmpAnchorID);
                            firebaseManager.deleteContent(tmpAnchorID);
                            anchor.detach();
                        }else{
                            anchor.detach(); //여기는 임시일때
                        }


                    }

                    @Override
                    public void onImageClick(ImageView dialogImg) {
                        writeMode = true;

                        tmpImageView = dialogImg;
                        loadAlbum();
                    }
                });
                customDialog.show();
            }
        });
    }


    //화면상에 보여지는 앵커를 우선 바꿈
    //여기가 너무 일찍 실행됨
    public void changeAnchor(TransformableNode model, String text_or_path, CustomDialog.AnchorType anchorType){
        if(anchorType == CustomDialog.AnchorType.text){
            model.setRenderable(makeTextModels(text_or_path));

            //미리 모델 만들기
            makePreModels();

        }else if(anchorType == CustomDialog.AnchorType.image){
            if(!writeMode){
                Log.d("순서 이미지 로드 상태", "readMode임");
                tmpImage = fireStorageManager.getUri();
            }
            Log.d("순서 모델", "체인지 이미지 모델");
            model.setRenderable(makeImageModels());
        }
    }

    // 종류에 맞게 앵커 저장, 앵커 아이디 리턴
    public String saveAnchor(Anchor anchor, String text, CustomDialog.AnchorType anchorType){
        checkGPS();
        String userId = firebaseAuthManager.getUID().toString();

        if(lat == 0.0 || lat <= 0){
            Log.d("순서 더블", "문제 있음");
        }
        Log.d("순서 더블", String.valueOf(lat));
        Log.d("순서 더블", String.valueOf(lng));


        if(anchorType == CustomDialog.AnchorType.text){
            cloudManager.hostCloudAnchor(anchor, text, userId, lat, lng, "text");
        }else if(anchorType == CustomDialog.AnchorType.image){
            Log.d("순서 패스",fireStorageManager.getImagePath());
            String path = fireStorageManager.getImagePath();
            cloudManager.hostCloudAnchor(anchor, path, userId, lat, lng, "image");

            //자꾸 업로드 하는거 우선 막아놓음 테스트 다하고 풀 예정
//            fireStorageManager.uploadImage(tmpImage);
        }
        cloudManager.onUpdate();

        writeMode = false; // 모든 동작이 이 부분에서 read 모드라고 판단되기 시작함
        return cloudManager.getTmpCloudAnchorID();
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
                        activity.imageRenderable = renderable;
                    }
                    Log.d("순서 모델", "모델 생성 3");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    //미리 렌더러블을 만들어 놓기
    public void makePreModels(){
        WeakReference<ArSfActivity> weakActivity = new WeakReference<>(this);

        //텍스트 모델 생성
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_text)
                .build()
                .thenAccept(renderable -> {
                    ArSfActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.textRenderableList.add(renderable);
                        cntTextRenderable += 1;
                    }
                    Log.d("순서 미리 모델 로드", "미리 모델 생성 ");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }


    // 한번 만들어 놓은 렌더러블은 수정가능함
    // 각각 다른 글자 띄우려면 매번 빌드해야한다고 함...
    public ViewRenderable makeTextModels(String text){
//        ViewRenderable tmpRenderable = textRenderable.makeCopy();
        ViewRenderable tmpRenderable = textRenderableList.get(cntTextRenderable).makeCopy();
        TextView textView = (TextView) tmpRenderable.getView();
        textView.setText(text);

        return tmpRenderable;
    }
    public ViewRenderable makeImageModels(){
        ViewRenderable tmpRenderable = imageRenderable.makeCopy();
        ImageView imageView = (ImageView) tmpRenderable.getView();

        //이미지 다운 때문에 바로 처리가 안됨
        if (tmpImage != null){
            imageView.setImageURI(tmpImage);
        }else{
            imageView.setImageResource(R.drawable.ic_launcher);
            Toast.makeText(this, "현재 이미지가 다운중임...", Toast.LENGTH_LONG).show();
        }

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
            Log.d("순서 갤러리", "이미지 불러와서 이미지 뷰에 넣는 부분");
            tmpImage = data.getData();
            tmpImageView.setImageURI(tmpImage);
        }
    }
}




