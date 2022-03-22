package com.mju.ar_capstone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.mju.ar_capstone.helpers.CloudAnchorManager;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.helpers.PoseManager;
import com.mju.ar_capstone.helpers.SensorAllManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class ArSfActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    private ArFragment arFragment;
    private ViewRenderable selectRenderable;
    private ArrayList<ViewRenderable> textRenderableList = new ArrayList<>();
    private ArrayList<ViewRenderable> imageRenderableList = new ArrayList<>();
    private ArrayList<ViewRenderable> mp3RenderableList = new ArrayList<>();

    private static int cntTextRenderable = 0;
    private static int cntImageRenderable = 0;
    private static int cntMp3Renderable = 0;

    //모델 종류 상수 선언
    private static final int SELECT_MODEL = -1;
    private static final int TEXT_MODEL = 0;
    private static final int IMAGE_MODEL = 1;
    private static final int MP3_MODEL = 2;

    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();

    private Button btnAnchorLoad, btnMapApp;

    //잠시 테스트 중인 애들
    private final int GALLERY_CODE = 10;
    private ImageView tmpImageView;
    private Uri tmpImageUri;
    private FireStorageManager fireStorageManager;

    private MediaPlayer mediaPlayer;

    // 내가 데이터를 쓰는 상황인지 불러오는 상황인지 체크해야할꺼 같음. 이미지를 내가 등록하는 상황인지
    // 불러오는 상황인지 체크
    private static boolean writeMode = false;

    //gps정보 앵커랑 같이 서버에 업로드하려고
    private double lat = 0.0;
    private double lng = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private PoseManager poseManager;


    //센서 정보를 가져와야 할 꺼 같아서 테스트 중
    private SensorAllManager sensorAllManager;

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

        //정밀 위경도 요청
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //firebase 관련
        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager();
        firebaseManager.registerContentsValueListner();
        fireStorageManager = new FireStorageManager();

        //센서 모음
        sensorAllManager = new SensorAllManager(getSystemService(SENSOR_SERVICE));
        sensorAllManager.startSensorcheck();

        poseManager = new PoseManager();

        // 기타 필요한 화면 요소들
        btnAnchorLoad = (Button) findViewById(R.id.btnAnchorLoad);
        btnAnchorLoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //불러오기 버튼 눌린순간 현재 나의 가속도 값 다시 가져오기
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

        //모델 로드, 미리 만들어놓는거임
        // 불러오기 할때 일일히 만들면 느려서 처리가 안됨
        //이 후에는 각각 필요한 모델들만 로드됨
        // 불러올때 좀 미리할 좋은 방법을 고민해야함
        preLoadModels();

    }


    public void preLoadModels(){

        //모델 로드, 미리 만들어놓는거임
        // 불러오기 할때 일일히 만들면 느려서 처리가 안됨
        makePreModels(SELECT_MODEL);
        makePreModels(MP3_MODEL);
        for(int i=0; i<30; i++){
            makePreModels(TEXT_MODEL);
            makePreModels(IMAGE_MODEL);

        }

    }


    //미리 렌더러블을 만들어 놓기
    public void makePreModels(int type){
        WeakReference<ArSfActivity> weakActivity = new WeakReference<>(this);

        Log.d("불러오기", "makePreModels 시작");
        if (type == TEXT_MODEL){
            Log.d("불러오기", "텍스트 모델 미리 만들러옴");
            //텍스트 모델 생성
            ViewRenderable.builder()
                    .setView(this, R.layout.view_model_text)
                    .build()
                    .thenAccept(renderable -> {
                        ArSfActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.textRenderableList.add(renderable);
                        }
                    })
                    .exceptionally(throwable -> {
                        Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                        return null;
                    });
        }else if(type == IMAGE_MODEL){
            // 이미지 모델 생성
            ViewRenderable.builder()
                    .setView(this, R.layout.view_model_image)
                    .build()
                    .thenAccept(renderable -> {
                        ArSfActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.imageRenderableList.add(renderable);
                        }
                    })
                    .exceptionally(throwable -> {
                        Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                        return null;
                    });
        }else if(type == SELECT_MODEL){
            //선택 모델 생성
            ViewRenderable.builder()
                    .setView(this, R.layout.view_model_select)
                    .build()
                    .thenAccept(renderable -> {
                        ArSfActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.selectRenderable = renderable;
                        }
                    })
                    .exceptionally(throwable -> {
                        Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                        return null;
                    });
        }else if(type == MP3_MODEL){
            //mp3 모델 생성
            ViewRenderable.builder()
                    .setView(this, R.layout.view_model_mp3)
                    .build()
                    .thenAccept(renderable -> {
                        ArSfActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.mp3RenderableList.add(renderable);
                        }
                    })
                    .exceptionally(throwable -> {
                        Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                        return null;
                    });
        }

        Log.d("불러오기", "makePreModels 끝");

    }


    // 한번 만들어 놓은 렌더러블은 수정가능함
    // 각각 다른 글자 띄우려면 매번 빌드해야한다고 함...
    public ViewRenderable makeTextModels(String text){

        Log.d("불러오기", "makeTextModels");
        Log.d("불러오기", "makeTextModels 가져온 텍스트: " + text);
        TextView textView = (TextView) textRenderableList.get(cntTextRenderable).getView().findViewById(R.id.tvTestText);
        textView.setText(text);
        Log.d("불러오기", "렌더러블 체크" + textRenderableList.get(cntTextRenderable).toString());
        Log.d("불러오기", "그냥 텍스트뷰 값" + textView.getText().toString());

        return textRenderableList.get(cntTextRenderable);
    }
    public ViewRenderable makeImageModels(){

        Log.d("이미지", "makeImageModels");

        ImageView imageView = (ImageView) imageRenderableList.get(cntImageRenderable).getView().findViewById(R.id.imgView);

        // 사용자가 다이얼로그에서 선택한 이미지가 tmpImage에 Uri로 들어가는거임
        if (tmpImageUri != null){
            Log.d("이미지", "이미지 변경 성공");
            Glide.with(this).load(tmpImageUri).into(imageView);
        }else{ //이미지가 선택 오류일때
            Log.d("이미지", "tmpImageUri가 비어있음");
            Glide.with(this).load(R.drawable.ic_launcher).into(imageView);
        }

        return imageRenderableList.get(cntImageRenderable);
    }
    public ViewRenderable makeMp3Models(){
        Button buttonPlay = (Button) mp3RenderableList.get(cntMp3Renderable).getView().findViewById(R.id.mp3play);
        Button buttonStop = (Button) mp3RenderableList.get(cntMp3Renderable).getView().findViewById(R.id.mp3stop);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.test);
                mediaPlayer.start();
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
            }
        });

        return mp3RenderableList.get(cntMp3Renderable);
    }


    //현재 위치 가져오기
    public void checkGPS() {
        Log.d("순서", "checkGPS");
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
        Task<Location> currentLocationTask = fusedLocationProviderClient.getCurrentLocation(
                100,
                cancellationTokenSource.getToken()
        );
        currentLocationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    lat = location.getLatitude();
                    lng = location.getLongitude();

                    Log.d("정밀 위치", "lat: " + lat + ", lng: " + lng);
                }
            }
        });
        Log.d("순서", "checkGPS end");
    }

    //거리 구하는 함수
    public double getDistance(double latA, double lngA){
        checkGPS();
        double distance;

        //사용자의 위치
        Location locationA = new Location("user");
        locationA.setLatitude(lat);
        locationA.setLongitude(lng);

        //앵커가 남겨진 위치
        Location locationB = new Location("anchor");
        locationB.setLatitude(latA);
        locationB.setLongitude(lngA);

        distance = locationA.distanceTo(locationB);

        return distance;
    }

    // 타입에 맞게 각각 다른 리스너 붙혀줘야함
    public void loadCloudAnchors(){
        writeMode = false;

        Log.d("불러오기", "loadCloudAnchors");

        ArrayList<WrappedAnchor> wrappedAnchorList = firebaseManager.getWrappedAnchorList();
        Iterator<WrappedAnchor> iterator = wrappedAnchorList.iterator();
        while (iterator.hasNext()){
            WrappedAnchor wrappedAnchor = iterator.next();
            //앵커가 내 근처에 있는지를 판단하고 보여줄꺼임
            //서버에서 가져온 앵커 위경도
            Log.d("순서", "잠깐");
            Double lat = wrappedAnchor.getLat();
            Double lng = wrappedAnchor.getLng();
            Log.d("순서", "거리" + String.valueOf(getDistance(lat, lng)));
            //거리가 멀면 넘어감 30 m 넘으면 안불러옴
            if(getDistance(lat, lng) > 30){
                continue;
            }

            CustomDialog.AnchorType anchorType = null;
            Pose pose = wrappedAnchor.getPose();
            String cloudAnchorID = wrappedAnchor.getCloudAnchorId();
            String text_or_path = wrappedAnchor.getTextOrPath();
            String stringAnchorType = wrappedAnchor.getAnchorType();


            // null 예외 발생할 수도 있음 웬만하면 int로 처리하는게 좋을듯
            if(stringAnchorType.equals("text")){
                anchorType = CustomDialog.AnchorType.text;
            }else if(stringAnchorType.equals("image")){
                anchorType = CustomDialog.AnchorType.image;
            }else if(stringAnchorType.equals("mp3")){
                anchorType = CustomDialog.AnchorType.mp3;
            }

            Log.d("불러오기", "불러와진 텍스트:" + text_or_path);

            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
            model.setParent(anchorNode);
            model.select();
            model.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    CustomDialog customDialog = new CustomDialog(ArSfActivity.this, new CustomDialog.CustomDialogClickListener() {
                        @Override
                        public void onPositiveClick(String tmpText, CustomDialog.AnchorType anchorType) {
                            writeMode = true;

                            Log.d("순서", "예스 클릭됨");
                            changeAnchor(model, tmpText, anchorType);
                            saveAnchor(anchor.getPose(), tmpText, anchorType);
                        }
                        @Override
                        public void onNegativeClick() {
                            firebaseManager.deleteContent(cloudAnchorID);
                            anchor.detach();
                            model.setRenderable(null);
                        }

                        @Override
                        public void onImageClick(ImageView dialogImg) {
                            writeMode = true;

                            tmpImageView = dialogImg;
                            loadAlbum();
                        }
                    });
                    customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    customDialog.show();
                }
            });


            //현재 작업중
            if(anchorType != CustomDialog.AnchorType.image){ //텍스트랑 mp3 이거 실행
                changeAnchor(model, text_or_path, anchorType);
            }else{ //이미지는 여기 실행
                Log.d("다운로드", "서버에서 불러온게 이미지앵커임");

                //서버에서 불러오는거는 여기서 다운로드하고 change까지 모두 함
                //이미지 Uri가 다운완료 되고나서 change해야해서
                Log.d("다운로드", String.valueOf(System.identityHashCode(model)));
                fireStorageManager.downloadImage(getApplicationContext(), text_or_path, model, imageRenderableList.get(cntImageRenderable));

                Log.d("다운로드", "끝 모델 추가로 만들러감");
                makePreModels(IMAGE_MODEL); //추가로 미리 모델 만드는 용도

                cntImageRenderable++;
            }

            iterator.remove();
        }
        //여러번 불러오기 호출로 똑같은 앵커가 계속 쌓이는거 방지
        firebaseManager.clearWrappedAnchorList();
    }

    //임시 앵커 생성 후 실제 앵커까지
    public void createSelectAnchor(HitResult hitResult){

        Log.d("순서", "createSelectAnchor");

        //터치 한 곳의 pose를 가져옴
        Anchor anchor = hitResult.createAnchor();
        Pose pose = anchor.getPose();


        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());


        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setRenderable(this.selectRenderable);
        model.setParent(anchorNode);
        model.select();
        model.setName("temp"); //모델명을 변수로 임시 사용
        model.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                CustomDialog customDialog = new CustomDialog(ArSfActivity.this, new CustomDialog.CustomDialogClickListener() {
                    @Override
                    public void onPositiveClick(String tmpText, CustomDialog.AnchorType anchorType) {
                        Log.d("순서", "onTap/onPositiveClick");

                        writeMode = true;

                        changeAnchor(model, tmpText, anchorType);
                        //앵커 아이디 리턴함. 지울때 판단하는 용도
                        String tmpAnchorID = saveAnchor(pose, tmpText, anchorType);
                        if(tmpAnchorID != null){
                            model.setName(tmpAnchorID);
                        }else{
                            model.setName("temp");
                        }

                    }
                    @Override
                    public void onNegativeClick() {
                        Log.d("순서", "onTap/onNegativeClick");
                        if(!model.getName().equals("temp")){
                            String tmpAnchorID = model.getName();
                            firebaseManager.deleteContent(tmpAnchorID);
                            anchor.detach();
                            model.setRenderable(null);
                        }else{
                            anchor.detach(); //여기는 임시일때
                            model.setRenderable(null);
                        }
                    }

                    @Override
                    public void onImageClick(ImageView dialogImg) {
                        Log.d("순서", "onTap/onImageClick");
                        writeMode = true;

                        tmpImageView = dialogImg;
                        loadAlbum();
                    }
                });
                customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                customDialog.show();
            }
        });
    }


    //화면상에 보여지는 앵커를 우선 바꿈
    public void changeAnchor(TransformableNode model, String text_or_path, CustomDialog.AnchorType anchorType){
        Log.d("순서", "changeAnchor");
        Log.d("불러오기", "changeAnchor 들어옴");

        if(anchorType == CustomDialog.AnchorType.text){
            Log.d("불러오기", "changeAnchor 앵커 텍스트 타입");
            model.setRenderable(makeTextModels(text_or_path));

            Log.d("불러오기", "model 렌더러블까지 끝남");

            //미리 모델 만들기
            makePreModels(TEXT_MODEL);

            cntTextRenderable += 1;


        }else if(anchorType == CustomDialog.AnchorType.image){
            //여기는 사용자가 이미지를 등록할때 처리되는 부분임
            //서버에서 가져오는거는 firestorageManager가 책임짐

            Log.d("불러오기", "changeAnchor 앵커 이미지 타입");
            Log.d("이미지", "changeAnchor image로 분기");

            model.setRenderable(makeImageModels());

            makePreModels(IMAGE_MODEL);

            cntImageRenderable += 1;


        }else if(anchorType == CustomDialog.AnchorType.mp3){
            model.setRenderable(makeMp3Models());

            makePreModels(MP3_MODEL);

            cntMp3Renderable += 1;
        }
    }

    // 종류에 맞게 앵커 저장, 앵커 아이디 리턴
    public String saveAnchor(Pose pose, String text, CustomDialog.AnchorType anchorType){
        Log.d("순서", "saveAnchor");
        String userId = firebaseAuthManager.getUID().toString();

        if(anchorType == CustomDialog.AnchorType.text){
            cloudManager.hostCloudAnchor(pose, text, userId, lat, lng,"text");
        }else if(anchorType == CustomDialog.AnchorType.image){
            String path = fireStorageManager.getImagePath();
            cloudManager.hostCloudAnchor(pose, path, userId, lat, lng,"image");


            fireStorageManager.uploadImage(tmpImageUri);
        }else if(anchorType == CustomDialog.AnchorType.mp3){
            cloudManager.hostCloudAnchor(pose, "mp3", userId, lat, lng, "mp3");
        }
        cloudManager.onUpdate();

        writeMode = false; // 모든 동작이 이 부분에서 read 모드라고 판단되기 시작함
        return cloudManager.getCurrentAnchorID();
    }


    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {

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
        cloudManager.setFirebaseManager(firebaseManager);
        fireStorageManager.setFirebaseManager(firebaseManager);

    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    private float azimuth;

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        Log.d("순서", "onTapPlane");
        //화면 터치하는 순간 앵커 남겼던 gps한번 가져옴
        checkGPS();
        //앵커 남겼던 방위각 구하는 부분
        azimuth = sensorAllManager.getAzimuth();
        Toast.makeText(this, "방위각 이용/ " + String.valueOf(azimuth), Toast.LENGTH_LONG).show();

        createSelectAnchor(hitResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            tmpImageUri = data.getData();
            Glide.with(this).load(tmpImageUri).into(tmpImageView);
        }
    }
}




