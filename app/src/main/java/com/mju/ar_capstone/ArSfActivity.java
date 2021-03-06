package com.mju.ar_capstone;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.mju.ar_capstone.helpers.CloudAnchorManager;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.helpers.PoseManager;
import com.mju.ar_capstone.invenfragments.UserInvenFragment;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ArSfActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private ViewRenderable selectRenderable;
    private ArrayList<ViewRenderable> textRenderableList = new ArrayList<>();
    private ArrayList<ViewRenderable> imageRenderableList = new ArrayList<>();
    private ArrayList<ViewRenderable> mp3RenderableList = new ArrayList<>();
    private ViewRenderable denyRenderable;
    private ModelRenderable keyRenderable, boxRenderable;


    private ArrayList<Uri> audioUriList = new ArrayList<>();

    private static int cntTextRenderable = 0;
    private static int cntImageRenderable = 0;
    private static int cntMp3Renderable = 0;

    //모델 종류 상수 선언
    private static final int SELECT_MODEL = -1;

    private static final int TEXT_MODEL = 0;
    private static final int IMAGE_MODEL = 1;
    private static final int MP3_MODEL = 2;
    private static final int DENY_MODEL = -99;
    private static final int KEY_MODEL = 3;
    private static final int BOX_MODEL = 4;

    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();

    private Button btnAnchorLoad;

    // 갤러리에서 이미지 불러오는거 관련
    ActivityResultLauncher<Intent> activityResultLauncher;
    private final int GALLERY_CODE = 10;
    private static ImageView tmpImageView;
    private static Uri tmpImageUri;
    private FireStorageManager fireStorageManager;


    // 오디오 파일관련 변수
    // 오디오 권한
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    // 오디오 파일 녹음 관련 변수
    private MediaRecorder mediaRecorder;
    private String audioFileName; // 오디오 녹음 생성 파일 이름
    private boolean isRecording = false;    // 현재 녹음 상태를 확인하기 위함.
    private Uri audioUri = null; // 오디오 파일 uri

    // 오디오 파일 재생 관련 변수
    private MediaPlayer mediaPlayer = null;
    private Boolean isPlaying = false;
    private ImageView playIcon;


    // 내가 데이터를 쓰는 상황인지 불러오는 상황인지 체크해야할꺼 같음. 이미지를 내가 등록하는 상황인지
    // 불러오는 상황인지 체크
    private static boolean writeMode = false;
    // 화면 회전 체크
    private static boolean DEVICE_LANDSCAPE = false;

    //키 소지 여부
    private boolean stateHaveKey = false;

    //gps정보 앵커랑 같이 서버에 업로드하려고
    private double lat = 0.0;
    private double lng = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private PoseManager poseManager;
    private final int LOAD_DISTANCE = 20;

    //주최자인지 참가자인지 구분하는 변수
    private int userType;
    private int azimuth = 0;
    private String channel;

    public static int TO_GRID = 0;
    public static int TO_GPS = 1;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //화면 세로
            Log.d("화면 전환", "세로");
            DEVICE_LANDSCAPE = false;
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("화면 전환", "가로");
            DEVICE_LANDSCAPE = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arsf);
        Log.d("갤러리", "onCreate");

        //사용자가 들어오자마자 이미지를 남길때 모델이 없으면 안보임, 그래서 미리 하나 로드
        makePreModels(IMAGE_MODEL);
        //모델 로드, 미리 만들어놓는거임
        // 불러오기 할때 일일히 만들면 느려서 처리가 안됨
        //이 후에는 각각 필요한 모델들만 로드됨
        // 불러올때 좀 미리할 좋은 방법을 고민해야함
        preLoadModels();
        // 갤러리 콜백되는 부분 - 이게 있어야 사용자가 사진을 선택안해도 안꺼짐
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData().getData() != null){

                tmpImageUri = result.getData().getData();
                Glide.with(this).load(tmpImageUri).into(tmpImageView);
                Log.d("갤러리", "사진 띄움");
            }else{
                //사진을 선택안하고 뒤로가기 할 때
                //혹은 null
                Log.d("갤러리", "null");
            }
        });

        getSupportFragmentManager().addFragmentOnAttachListener(this);
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        // 인벤토리 액티비티에서 가져온 값들
        Intent intent = getIntent();
        azimuth = intent.getIntExtra("azimuth", 0);
        channel = intent.getStringExtra("channel");
        userType = intent.getIntExtra("userType",0);
        stateHaveKey = intent.getBooleanExtra("haveKey", false);
        Log.d("키넘어온거", String.valueOf(stateHaveKey));

        //정밀 위경도 요청
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d("채널넘기는거 ar", channel);

        //firebase 관련
        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager(channel);
        fireStorageManager = new FireStorageManager(channel);
        cloudManager.setFirebaseManager(firebaseManager);
        fireStorageManager.setFirebaseManager(firebaseManager);

        poseManager = new PoseManager();
        //gps는 ar화면이 불러와지는 순간과 앵커 로드할때 체크
        checkGPS(true);

        btnAnchorLoad = (Button) findViewById(R.id.btnAnchorLoad);
        btnAnchorLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseManager.getContents(new FirebaseManager.GetContentsListener() {
                    @Override
                    public void onDataLoaded() {
                        //데이터 로드하는 시점에 사용자의 위치와 남겨졌던 앵커간에 거리 판단하기 위해서
                        checkGPS(true);

                        //화면 구성하기 전에 로드 방지
                        if(arFragment.getArSceneView() != null){
                            loadCloudAnchors();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        Log.d("갤러리", "onSessionConfiguration");
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        session.configure(config);
    }


    public void preLoadModels() {

        //모델 로드, 미리 만들어놓는거임
        // 불러오기 할때 일일히 만들면 느려서 처리가 안됨
        // 얘네들은 하나의 모델 중복 사용하는 것들
        makePreModels(SELECT_MODEL);
        makePreModels(DENY_MODEL);
        makePreModels(KEY_MODEL);
        makePreModels(BOX_MODEL);
        for (int i = 0; i < 30; i++) {
            makePreModels(TEXT_MODEL);
            makePreModels(IMAGE_MODEL);
            makePreModels(MP3_MODEL);
        }
    }

    //미리 렌더러블을 만들어 놓기..
    public void makePreModels(int type) {
        WeakReference<ArSfActivity> weakActivity = new WeakReference<>(this);

        Log.d("불러오기", "makePreModels 시작");
        if (type == TEXT_MODEL) {
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
        } else if (type == IMAGE_MODEL) {
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
        } else if (type == SELECT_MODEL) {
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
        } else if (type == MP3_MODEL) {
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
        }else if(type == DENY_MODEL){
            //선택 모델 생성
            ViewRenderable.builder()
                    .setView(this, R.layout.view_model_deny)
                    .build()
                    .thenAccept(renderable -> {
                        ArSfActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.denyRenderable = renderable;
                        }
                    })
                    .exceptionally(throwable -> {
                        Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                        return null;
                    });
        }else if(type == KEY_MODEL){
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("file://models/key/scene.gltf"))
                    .setIsFilamentGltf(true)
                    .build()
                    .thenAccept(modelRenderable -> {
                        ArSfActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.keyRenderable = modelRenderable;
                        }
                        Log.d("모델렌더러블", "로드 성공");
                    }).exceptionally(throwable -> {
                        throwable.getStackTrace();
                        Log.d("모델렌더러블", throwable.getMessage());
                        Log.d("모델렌더러블", "로드 실패");
                        return null;
                    });
        }else if(type == BOX_MODEL){
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("file://models/lockedbox/scene.gltf"))
                    .setIsFilamentGltf(true)
                    .build()
                    .thenAccept(modelRenderable -> {
                        ArSfActivity activity = weakActivity.get();
                        if (activity != null) {
                            activity.boxRenderable = modelRenderable;
                        }
                        Log.d("모델렌더러블", "로드 성공");
                    }).exceptionally(throwable -> {
                throwable.getStackTrace();
                Log.d("모델렌더러블", throwable.getMessage());
                Log.d("모델렌더러블", "로드 실패");
                return null;
            });
        }

        Log.d("불러오기", "makePreModels 끝");

    }

    // 한번 만들어 놓은 렌더러블은 수정가능함
    // 각각 다른 글자 띄우려면 매번 빌드해야한다고 함...
    public ViewRenderable makeTextModels(String text) {

        Log.d("불러오기", "makeTextModels");
        Log.d("불러오기", "makeTextModels 가져온 텍스트: " + text);
        TextView textView = (TextView) textRenderableList.get(cntTextRenderable).getView().findViewById(R.id.tvTestText);
        textView.setText(text);
        Log.d("불러오기", "렌더러블 체크" + textRenderableList.get(cntTextRenderable).toString());
        Log.d("불러오기", "그냥 텍스트뷰 값" + textView.getText().toString());

        return textRenderableList.get(cntTextRenderable);
    }

    public ViewRenderable makeImageModels() {

        Log.d("이미지", "makeImageModels");

        ImageView imageView = (ImageView) imageRenderableList.get(cntImageRenderable).getView().findViewById(R.id.imgView);

        // 사용자가 다이얼로그에서 선택한 이미지가 tmpImage에 Uri로 들어가는거임
        if (tmpImageUri != null) {
            Log.d("이미지", "이미지 변경 성공");
            Glide.with(this).load(tmpImageUri).into(imageView);
        } else { //이미지가 선택 오류일때
            Log.d("이미지", "tmpImageUri가 비어있음");
            Glide.with(this).load(R.drawable.ic_launcher).into(imageView);
        }

        return imageRenderableList.get(cntImageRenderable);
    }

    public ViewRenderable makeMp3Models() {

        ImageButton mp3playBtn = (ImageButton)  mp3RenderableList.get(cntMp3Renderable).getView().findViewById(R.id.mp3play);;
        TextView mp3index = (TextView) mp3RenderableList.get(cntMp3Renderable).getView().findViewById(R.id.mp3index);
        mp3index.setText(Integer.toString(cntMp3Renderable));

        mp3playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = Integer.parseInt(mp3index.getText().toString());

                if(userType == 2){ //참가자일때는 항상 불러오는 경우만 있음
                    audioUriList = fireStorageManager.getMp3ListUri();
                }else { //주최자일때는 내가 만든 음성앵커와 불러온 음성 앵커를 합쳐줘야함
                    audioUriList.addAll(fireStorageManager.getMp3ListUri());
                }
                audioUri = audioUriList.get(index);

                Log.d("음성다운", String.valueOf(audioUri));

                //여기 부분 아직 고민중
                if(audioUri != null){
                    playAudio(audioUri);
                }
            }
        });
        return mp3RenderableList.get(cntMp3Renderable);
    }



    // 오디오 파일 권한 체크
    private boolean checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED)
            return true;
        else {
            ActivityCompat.requestPermissions(this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    // 녹음 시작
    private void startRecording() {
        //파일의 외부 경로 확인
        String recordPath = getExternalCacheDir().getAbsolutePath();
        // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. 그 이유는 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자 함.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFileName = recordPath + "/" + "RecordExample_" + timeStamp + "_" + "audio.3gp";

        //Media Recorder 생성 및 설정
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFileName);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //녹음 시작
        mediaRecorder.start();
    }

    // 녹음 종료
    private void stopRecording() {
        // 녹음 종료 종료
        mediaRecorder.stop();
        mediaRecorder.release();

        mediaRecorder = null;

        // 파일 경로(String) 값을 Uri로 변환해서 저장
        //      - Why? : 리사이클러뷰에 들어가는 ArrayList가 Uri를 가지기 때문
        //      - File Path를 알면 File을  인스턴스를 만들어 사용할 수 있기 때문
        audioUri = Uri.parse(audioFileName);

        //음성 앵커 오류 확인중
        audioUriList.add(Uri.parse(audioFileName));

        Log.d("mp3", audioUri.toString());
        fireStorageManager.uploadMp3(audioFileName);

    }



    // 녹음 파일 재생
    private void playAudio(Uri uri) {
        // null 값이면 리턴
        if(uri == null){
            return;
        }

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 여기 변수 선언후 초기화를 한 적이 없어서 null 뜸
//        playIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_pause, null));
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
            }
        });

    }

    // 녹음 파일 중지
    private void stopAudio() {
        // 여기 변수 선언후 초기화를 한 적이 없어서 null 뜸
//        playIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_play, null));
        isPlaying = false;
        mediaPlayer.stop();
    }


    //현재 위치 가져오기
    public void checkGPS ( boolean gpsCheck) {
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
                if (task.isSuccessful()) {
                    if (gpsCheck) {
                        Location location = task.getResult();
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                    Log.d("정밀 위치", "lat: " + lat + ", lng: " + lng);
                }
            }
        });
        Log.d("순서", "checkGPS end");
    }


    // 타입에 맞게 각각 다른 리스너 붙혀줘야함
    public void loadCloudAnchors(){
        writeMode = false;
        Log.e("HAN","loadCloudAnchors");
        ArrayList<WrappedAnchor> wrappedAnchorList = firebaseManager.getWrappedAnchorList();

        Iterator<WrappedAnchor> iterator = wrappedAnchorList.iterator();
        while (iterator.hasNext()){
            WrappedAnchor wrappedAnchor = iterator.next();
            //앵커가 내 근처에 있는지를 판단하고 보여줄꺼임
            //서버에서 가져온 앵커 위경도
            Double anchorlat = wrappedAnchor.getLat();
            Double anchorlng = wrappedAnchor.getLng();
            double[] distanceArray = getDistance(anchorlat, anchorlng);
            if(distanceArray[0] > LOAD_DISTANCE){ //둘 사이의 거리
                Log.d("거리", "거리가 너무 멈!!!");
                iterator.remove();
                continue;
            }
            Log.d("거리", "앵커 생성");

            HostDialog.AnchorType anchorType = null;
            Pose pose = wrappedAnchor.getPose();
            String cloudAnchorID = wrappedAnchor.getCloudAnchorId();
            String text_or_path = wrappedAnchor.getTextOrPath();
            int intAnchorType = wrappedAnchor.getAnchorType();

            // 키 또는 상자는 distanceArray가 10m여야 함
            // 참가자일때만 체크
            if(userType == 2){
                if(intAnchorType == 3 || intAnchorType == 4){

                    //거리 10이상은 로드 안함
                    if(distanceArray[0] > 10){
                        continue;
                    }

//                    //상자는 열쇠를 얻은 유저만 로드 가능함
//                    if(intAnchorType == 4){
//                        // 키 소유여부 false면 패스함
//                        if(!stateHaveKey){
//                            continue;
//                        }
//                    }
                }
            }

            Log.e("HAN","cloudAnchorID: "+cloudAnchorID);
            Log.e("HAN","text_or_path: "+text_or_path);
            Log.e("HAN","intAnchorType: "+intAnchorType);

            // null 예외 발생할 수도 있음 웬만하면 int로 처리하는게 좋을듯
            if(intAnchorType == 0){
                anchorType = HostDialog.AnchorType.text;
            }else if(intAnchorType == 1){
                anchorType = HostDialog.AnchorType.image;
            }else if(intAnchorType == 2){
                anchorType = HostDialog.AnchorType.mp3;
            }else if(intAnchorType == 3){
                anchorType = HostDialog.AnchorType.key;
            }else if(intAnchorType == 4){
                anchorType = HostDialog.AnchorType.box;
            }

            //나와 앵커가 남겨졌던 방위각 차이 계산
            int degree = poseManager.azimuthDifference(azimuth, wrappedAnchor.getAzimuth());

            try{
                Log.d("포즈 null", pose.getTranslation().toString());
            }catch (NullPointerException e){
                Log.d("포즈 null", "널 뜸");
            }
            Pose realPose = poseManager.resolveRealPose(pose, degree);
            Log.d("앵커위치", "포즈생성");

            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(realPose);
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            Log.d("앵커위치", "앵커생성");

            TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
            model.setParent(anchorNode);
            model.setName(cloudAnchorID);
            model.select();
            if(userType == 1){
                //주최자면 앵커 삭제 기능 지원
                model.setOnTapListener(new Node.OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {

                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ArSfActivity.this, SweetAlertDialog.WARNING_TYPE);
                        sweetAlertDialog.setContentText("앵커를 삭제하시겠습니까?");
                        sweetAlertDialog.setConfirmButton("삭제하기", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();

                                firebaseManager.deleteContent(model.getName());
                                anchor.detach();
                                model.setRenderable(null);

                                SweetAlertDialog sweetAlertDialogInner = new SweetAlertDialog(ArSfActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                                sweetAlertDialogInner.setContentText("삭제 완료");
                                sweetAlertDialogInner.show();
                            }
                        });
                        sweetAlertDialog.setCancelButton("취소", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        });
                        sweetAlertDialog.show();
                    }
                });
            }else{
                //참가자는 앵커 담기 기능 지원
                model.setOnTapListener(new Node.OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ArSfActivity.this, SweetAlertDialog.NORMAL_TYPE);

                        // 열쇠 앵커일때는
                        if(wrappedAnchor.getAnchorType() == 3){
                            sweetAlertDialog.setContentText("열쇠를 획득하시겠습니까?");
                            sweetAlertDialog.setConfirmText("열쇠 획득");
                            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    firebaseManager.userScrapAnchor(channel, firebaseAuthManager.getUID(), model.getName(), wrappedAnchor.getAnchorType());

                                    //어짜피 여기까지 오면 키를 획득했다는것이므로 따로 db조회 필요없음
                                    //바로 키 획득처리, db에는 윗 부분에서 반영할꺼임
                                    stateHaveKey = true;

                                    //앵커 획득한것처럼 보이게 화면상에서 지움
                                    anchor.detach();

                                    sweetAlertDialog.dismiss();

                                    SweetAlertDialog sweetAlertDialogInner = new SweetAlertDialog(ArSfActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                                    sweetAlertDialogInner.setContentText("획득 완료");
                                    sweetAlertDialogInner.show();
                                }
                            });
                        }else if(wrappedAnchor.getAnchorType() == 4){ //박스 앵커일때는
                            sweetAlertDialog.setContentText("상자를 여시겠습니까?\nKey를 가지고 있어야 합니다.");
                            sweetAlertDialog.setConfirmText("상자 열기");
                            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    if(stateHaveKey){ //키를 가지고 있으면
                                        //승리 유저 정보 보냄
                                        firebaseManager.sendWinnerInfo(channel, firebaseAuthManager.getUID());
                                        Intent intent = new Intent(getApplicationContext(), SuccessActivity.class);
                                        sweetAlertDialog.dismiss();
                                        startActivity(intent);
                                    }else{//키가 없으면

                                    }
                                }
                            });
                        }else{
                            sweetAlertDialog.setContentText("앵커를 스크랩하시겠습니까?");
                            sweetAlertDialog.setConfirmText("가방에 담기");
                            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    firebaseManager.userScrapAnchor(channel, firebaseAuthManager.getUID(), model.getName(), wrappedAnchor.getAnchorType());
                                    sweetAlertDialog.dismiss();

                                    SweetAlertDialog sweetAlertDialogInner = new SweetAlertDialog(ArSfActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                                    sweetAlertDialogInner.setContentText("스크랩 완료");
                                    sweetAlertDialogInner.show();
                                }
                            });
                        }
                        sweetAlertDialog.setCancelText("취소");
                        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        });
                        sweetAlertDialog.show();

                    }
                });
            }

            //현재 작업중
            if(anchorType == HostDialog.AnchorType.text){ //텍스트 이거 실행
                changeAnchor(model, text_or_path, anchorType);
            }else if(anchorType == HostDialog.AnchorType.image){ //이미지는 여기 실행
                Log.d("다운로드", "서버에서 불러온게 이미지앵커임");

                //서버에서 불러오는거는 여기서 다운로드하고 change까지 모두 함
                //이미지 Uri가 다운완료 되고나서 change해야해서
                Log.d("다운로드", String.valueOf(System.identityHashCode(model)));
                fireStorageManager.downloadImage(getApplicationContext(), text_or_path, model, imageRenderableList.get(cntImageRenderable));

                Log.d("다운로드", "끝 모델 추가로 만들러감");
                makePreModels(IMAGE_MODEL); //추가로 미리 모델 만드는 용도

                cntImageRenderable++;
            }else if(anchorType == HostDialog.AnchorType.mp3){
                fireStorageManager.downloadMp3(getApplicationContext(), text_or_path);
                changeAnchor(model, text_or_path, anchorType);
            }else if(anchorType == HostDialog.AnchorType.key){ //키 앵커 이거 실행
                changeAnchor(model, text_or_path, anchorType);
            }else if(anchorType == HostDialog.AnchorType.box){ //박스 앵커 이거 실행
                changeAnchor(model, text_or_path, anchorType);
            }
            iterator.remove();
        }

    }


    //임시 앵커 생성 후 실제 앵커까지
    public void createSelectAnchor(HitResult hitResult){
        Log.e("HAN","createSelectAnchor");
        Log.d("순서", "createSelectAnchor");

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
                Log.e("HAN","onTap");
                HostDialog hostDialog = new HostDialog(ArSfActivity.this, DEVICE_LANDSCAPE, new HostDialog.CustomDialogClickListener() {

                    @Override
                    public void onPositiveClick(String tmpText, HostDialog.AnchorType anchorType) {
                        Log.d("순서", "onTap/onPositiveClick");
                        Log.e("HAN","onPositiveClick");
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
                        Log.e("HAN","onNegativeClick");
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
                        Log.e("HAN","onImageClick");
                        writeMode = true;

                        tmpImageView = dialogImg;
                        loadAlbum();
                    }

                    @Override
                    public void onRecordClick(TextView audioRecordText, ImageButton audioRecordImageBtn) {
                        Log.e("HAN","onRecordClick");
                        if (isRecording) {
                            // 현재 녹음 중 O
                            // 녹음 상태에 따른 변수 아이콘 & 텍스트 변경
                            isRecording = false; // 녹음 상태 값
                            audioRecordImageBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_record, null)); // 녹음 상태 아이콘 변경
                            audioRecordText.setText("녹음 시작"); // 녹음 상태 텍스트 변경
                            stopRecording();
                            // 녹화 이미지 버튼 변경 및 리코딩 상태 변수값 변경
                        } else {
                            // 현재 녹음 중 X
                            /*절차
                             *       1. Audio 권한 체크
                             *       2. 처음으로 녹음 실행한건지 여부 확인//
                             * */
                            if (checkAudioPermission()) {
                                // 녹음 상태에 따른 변수 아이콘 & 텍스트 변경
                                isRecording = true; // 녹음 상태 값
                                audioRecordImageBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_recording_red, null)); // 녹음 상태 아이콘 변경
                                audioRecordText.setText("녹음 중"); // 녹음 상태 텍스트 변경
                                startRecording();
                            }
                        }

                    }

                    @Override
                    public void onPlayClick() {
                        Log.e("HAN","onPlayClick");
                        playAudio(audioUri);
                    }

                    @Override
                    public void onImgPuzzleClick(Button btnImgMakePuzzle) {
                        Log.e("HAN","onImgPuzzleClick");
                        btnImgMakePuzzle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(tmpImageUri != null){

                                    ArrayList<Bitmap> tmpBitmap = cropBitmapList(tmpImageUri);
                                    ImgPuzzleDialog imgPuzzleDialog = new ImgPuzzleDialog(ArSfActivity.this, tmpBitmap, new ImgPuzzleDialog.PuzzleDialogClickListener() {
                                        @Override
                                        public void onImgBtn0Click(Bitmap bitmap) {
                                            Log.e("HAN","onImgBtn0Click");
                                            Uri uri = getImageUri(getApplicationContext(), bitmap);
                                            tmpImageUri = uri;
                                            Glide.with(getApplicationContext()).load(bitmap).into(tmpImageView);
                                        }

                                        @Override
                                        public void onImgBtn1Click(Bitmap bitmap) {
                                            Log.e("HAN","onImgBtn1Click");
                                            Uri uri = getImageUri(getApplicationContext(), bitmap);
                                            tmpImageUri = uri;
                                            Glide.with(getApplicationContext()).load(bitmap).into(tmpImageView);
                                        }

                                        @Override
                                        public void onImgBtn2Click(Bitmap bitmap) {
                                            Log.e("HAN","onImgBtn2Click");
                                            Uri uri = getImageUri(getApplicationContext(), bitmap);
                                            tmpImageUri = uri;
                                            Glide.with(getApplicationContext()).load(bitmap).into(tmpImageView);
                                        }

                                        @Override
                                        public void onImgBtn3Click(Bitmap bitmap) {
                                            Log.e("HAN","onImgBtn3Click");
                                            Uri uri = getImageUri(getApplicationContext(), bitmap);
                                            tmpImageUri = uri;
                                            Glide.with(getApplicationContext()).load(bitmap).into(tmpImageView);
                                        }
                                    });

                                    imgPuzzleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    imgPuzzleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    imgPuzzleDialog.show();
                                }else {
                                    //선택된 이미지가 없을 때
                                }
                            }
                        });
                    }
                });
                hostDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                hostDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                hostDialog.show();
            }
        });
    }
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    public Bitmap uriToBitmap(Uri uri){
        Bitmap bitmap = null;
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            }else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        }catch (IOException e){

        }

        return bitmap;
    }

    public ArrayList<Bitmap> cropBitmapList(Uri uri) {
        Bitmap src = null;
        try {
            src = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(src == null)
            return null;

        ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();

        // 이미지의 크기 구함
        int width = src.getWidth();
        int height = src.getHeight();

        //시작점
        int x = 0;
        int y = 0;

        //반절 크기
        int hw = width / 2;
        int hh = height / 2;

        //0번째 0 -> hw, 0-> hh
        //1번째 hw -> w, 0 -> hh
        //2번째 0 -> hw, hh -> h
        //3번째 hw -> w, hh -> h
        bitmapArrayList.add(Bitmap.createBitmap(src, x, y, hw, hh));
        bitmapArrayList.add(Bitmap.createBitmap(src, hw, y, hw, hh));
        bitmapArrayList.add(Bitmap.createBitmap(src, x, hh, hw, hh));
        bitmapArrayList.add(Bitmap.createBitmap(src, hw, hh, hw, hh));

        return bitmapArrayList;
    }


    //화면상에 보여지는 앵커를 우선 바꿈
    public void changeAnchor(TransformableNode model, String text_or_path, HostDialog.AnchorType anchorType){
        Log.d("순서", "changeAnchor");
        Log.d("불러오기", "changeAnchor 들어옴");

        if(anchorType == HostDialog.AnchorType.text){
            Log.d("불러오기", "changeAnchor 앵커 텍스트 타입");
            model.setRenderable(makeTextModels(text_or_path));

            Log.d("불러오기", "model 렌더러블까지 끝남");

            //미리 모델 만들기..
            makePreModels(TEXT_MODEL);

            cntTextRenderable += 1;


        }else if(anchorType == HostDialog.AnchorType.image) {
            //여기는 주최자가 이미지를 등록할때 처리되는 부분임
            //서버에서 가져오는거는 firestorageManager가 책임짐

            Log.d("불러오기", "changeAnchor 앵커 이미지 타입");
            Log.d("이미지", "changeAnchor image로 분기");

            model.setRenderable(makeImageModels());

            makePreModels(IMAGE_MODEL);

            cntImageRenderable += 1;

        }else if(anchorType == HostDialog.AnchorType.mp3){
            model.setRenderable(makeMp3Models());

            makePreModels(MP3_MODEL);

            cntMp3Renderable += 1;
        }else if(anchorType == HostDialog.AnchorType.key){
            model.setRenderable(keyRenderable);
            model.getScaleController().setMinScale(0.001f);
            model.getScaleController().setMaxScale(0.01f);
            model.setLocalScale(new Vector3(0.005f, 0.005f, 0.005f));
            model.setLocalRotation(new Quaternion(1,1,1,1));
            model.select();

        }else if(anchorType == HostDialog.AnchorType.box){
            model.setRenderable(boxRenderable);
            model.getScaleController().setMinScale(0.1f);
            model.getScaleController().setMaxScale(0.2f);
            model.setLocalScale(new Vector3(0.15f, 0.15f, 0.15f));
            model.select();
        }

    }


    // 종류에 맞게 앵커 저장, 앵커 아이디 리턴
    public String saveAnchor(Pose pose, String text, HostDialog.AnchorType anchorType) {
        Log.e("HAN","saveAnchor");
        Log.d("순서", "saveAnchor");
        String userId = firebaseAuthManager.getUID().toString();

        if (anchorType == HostDialog.AnchorType.text) {
            cloudManager.hostCloudAnchor(pose, text, userId, lat, lng, azimuth, 0);
        } else if (anchorType == HostDialog.AnchorType.image) {
            String path = fireStorageManager.getImagePath();
            cloudManager.hostCloudAnchor(pose, path, userId, lat, lng, azimuth, 1);
            fireStorageManager.uploadImage(tmpImageUri);
        } else if (anchorType == HostDialog.AnchorType.mp3) {
            String path = fireStorageManager.getMp3Path();
            cloudManager.hostCloudAnchor(pose, path, userId, lat, lng, azimuth, 2);
            fireStorageManager.uploadMp3(audioFileName);
        } else if(anchorType == HostDialog.AnchorType.key){
            cloudManager.hostCloudAnchor(pose, "키", userId, lat, lng, azimuth, 3);
        }else if(anchorType == HostDialog.AnchorType.box){
            cloudManager.hostCloudAnchor(pose, "박스", userId, lat, lng, azimuth, 4);
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
    public void onViewCreated(ArSceneView arSceneView) {
        Log.d("갤러리", "onViewCreated");
        arFragment.setOnViewCreatedListener(null);
        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    //거리 구하는 함수
    public double[] getDistance(double latA, double lngA){
        double[] distance;

        //사용자의 위치
        Location locationA = new Location("user");
        locationA.setLatitude(lat);
        locationA.setLongitude(lng);

        //앵커가 남겨진 위치
        Location locationB = new Location("anchor");
        locationB.setLatitude(latA);
        locationB.setLongitude(lngA);

        distance = poseManager.distanceBetweenLocation(locationA, locationB);

        return distance;
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        //ar 화면이 구성된 이후 터치 가능 하도록
        if(arFragment.getArSceneView() != null){

            Log.d("순서", "onTapPlane");
            if(userType == 1){ //주최자 일 경우만 앵커 생성 가능
                //if(true){ //주최자 일 경우만 앵커 생성 가능
                createSelectAnchor(hitResult);
                Log.e("HAN", "onTapPlane type1");
            }else{
                Log.e("HAN", "onTapPlane type2");
                Anchor anchor = hitResult.createAnchor();
                Pose pose = anchor.getPose();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
                model.setRenderable(this.denyRenderable);
                model.setParent(anchorNode);
                model.select();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("갤러리", "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("갤러리", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("갤러리", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("갤러리", "onStop");
    }


    // 이미지 업로드 부분
    //사용자 갤러리 불러오기
    private void loadAlbum(){
        Log.d("갤러리", "loadAlbum start");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

        activityResultLauncher.launch(intent);
        Log.d("갤러리", "loadAlbum end");
    }


}



