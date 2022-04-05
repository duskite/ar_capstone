package com.mju.ar_capstone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.PlaneRenderer;
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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

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

    private final int GALLERY_CODE = 10;
    private ImageView tmpImageView;
    private Uri tmpImageUri;
    private FireStorageManager fireStorageManager;


    //xml 변수
    ImageButton audioRecordImageBtn;
    TextView audioRecordText;

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
    ImageView playIcon;


    // 내가 데이터를 쓰는 상황인지 불러오는 상황인지 체크해야할꺼 같음. 이미지를 내가 등록하는 상황인지
    // 불러오는 상황인지 체크
    private static boolean writeMode = false;
    // 화면 회전 체크
    private static boolean DEVICE_LANDSCAPE = false;

    //gps정보 앵커랑 같이 서버에 업로드하려고
    private double lat = 0.0;
    private double lng = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private PoseManager poseManager;
    private final int LOAD_DISTANCE = 30;

    private int azimuth = 0;

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
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        // 메인에서 넘겨준 방위각 값 한 번만 저장
        Intent intent = getIntent();
        azimuth = intent.getIntExtra("azimuth", 0);
        Log.d("방위각 불러오기 인텐트", String.valueOf(azimuth));

        //정밀 위경도 요청
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //firebase 관련
        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager("base_channel");
        firebaseManager.registerContentsValueListner();
        fireStorageManager = new FireStorageManager();

        poseManager = new PoseManager();
        //gps는 ar화면이 불러와지는 순간으로만 체크
        checkGPS(true);

        btnAnchorLoad = (Button) findViewById(R.id.btnAnchorLoad);
        btnAnchorLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        session.configure(config);

        cloudManager.setFirebaseManager(firebaseManager);
        fireStorageManager.setFirebaseManager(firebaseManager);
    }


    public void preLoadModels() {

        //모델 로드, 미리 만들어놓는거임
        // 불러오기 할때 일일히 만들면 느려서 처리가 안됨
        makePreModels(SELECT_MODEL);
        makePreModels(MP3_MODEL);
        for (int i = 0; i < 30; i++) {
            makePreModels(TEXT_MODEL);
            makePreModels(IMAGE_MODEL);

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
        audioRecordImageBtn = findViewById(R.id.audioRecordImageBtn);
        audioRecordText = findViewById(R.id.audioRecordText);

        audioRecordImageBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                     *       2. 처음으로 녹음 실행한건지 여부 확인
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
        String recordPath = getExternalFilesDir("/").getAbsolutePath();
        // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. 그 이유는 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자 함.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFileName = recordPath + "/" + "RecordExample_" + timeStamp + "_" + "audio.mp4";

        //Media Recorder 생성 및 설정
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

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

    }

    // 녹음 파일 재생
    private void playAudio(File file) {
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_pause, null));
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
        playIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_play, null));
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

        Log.d("앵커위치", "loadCloudAnchors");

        Log.d("차이 azimuth", "밖 체크");

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
                continue;
            }
            Log.d("거리", "앵커 생성");

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

            Vector3 cameraVector = wrappedAnchor.getCameraVector();
            //나와 앵커가 남겨졌던 방위각 차이 계산
            int degree = poseManager.azimuthDifference(azimuth, wrappedAnchor.getAzimuth());
            Log.d("차이 azimuth", "안 체크");
            Log.d("차이 azimuth", String.valueOf(degree));
            Pose realPose = poseManager.resolveRealPose(pose, degree, cameraVector);
            Log.d("앵커위치", "포즈생성");

            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(realPose);
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            Log.d("앵커위치", "앵커생성");

            TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
            model.setParent(anchorNode);
            model.select();
            model.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    CustomDialog customDialog = new CustomDialog(ArSfActivity.this, DEVICE_LANDSCAPE, new CustomDialog.CustomDialogClickListener() {
                        @Override
                        public void onPositiveClick(String tmpText, CustomDialog.AnchorType anchorType) {
                            writeMode = true;

                            Log.d("순서", "예스 클릭됨");
                            changeAnchor(model, tmpText, anchorType);
                            saveAnchor(anchor.getPose(),cameraVector, tmpText, anchorType);
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
    public void createSelectAnchor(HitResult hitResult, Vector3 cameraVector){

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
                CustomDialog customDialog = new CustomDialog(ArSfActivity.this, DEVICE_LANDSCAPE, new CustomDialog.CustomDialogClickListener() {
                    @Override
                    public void onPositiveClick(String tmpText, CustomDialog.AnchorType anchorType) {
                        Log.d("순서", "onTap/onPositiveClick");

                        writeMode = true;

                        changeAnchor(model, tmpText, anchorType);
                        //앵커 아이디 리턴함. 지울때 판단하는 용도
                        String tmpAnchorID = saveAnchor(pose, cameraVector, tmpText, anchorType);
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


        }else if(anchorType == CustomDialog.AnchorType.image) {
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
    public String saveAnchor(Pose pose,Vector3 cameraVector, String text, CustomDialog.AnchorType anchorType) {
        Log.d("순서", "saveAnchor");
        String userId = firebaseAuthManager.getUID().toString();

        if (anchorType == CustomDialog.AnchorType.text) {
            cloudManager.hostCloudAnchor(pose, cameraVector, text, userId, lat, lng, azimuth, "text");
        } else if (anchorType == CustomDialog.AnchorType.image) {
            String path = fireStorageManager.getImagePath();
            cloudManager.hostCloudAnchor(pose, cameraVector, path, userId, lat, lng, azimuth, "image");


            fireStorageManager.uploadImage(tmpImageUri);
        } else if (anchorType == CustomDialog.AnchorType.mp3) {
            cloudManager.hostCloudAnchor(pose, cameraVector, "mp3", userId, lat, lng, azimuth, "mp3");
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


    //앵커 방향과 위치 테스트용 삭제 금지
    public void test_ysy() {

//        Vector3 vector3 = new Vector3(
//                0,
//                0,
//                -1
//        );
//
//        Log.d("회전 v", "x: "+ vector3.x + ", y: " + vector3.y + ", z:" + vector3.z);

//
//
//        int degree = -30;
//
//        Vector3 rotatedVector = new Vector3(
//                (float) (vector3.x * Math.cos(Math.toRadians(degree)) - vector3.z * Math.sin(Math.toRadians(degree))),
//                vector3.y,
//                (float) (vector3.x * Math.sin(Math.toRadians(degree)) + vector3.z * Math.cos(Math.toRadians(degree)))
//
//        );
//        Log.d("회전 rotated", "x: "+ rotatedVector.x + ", y: " + rotatedVector.y + ", z:" + rotatedVector.z);
//
//        AnchorNode anchorNode90 = new AnchorNode();
//
//        anchorNode90.setParent(arFragment.getArSceneView().getScene());
//
//        anchorNode90.setRenderable(this.selectRenderable);
//        anchorNode90.setWorldPosition(rotatedVector);

    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        Camera camera = arFragment.getArSceneView().getScene().getCamera();
        Vector3 cameraVector = camera.getWorldPosition();

        Log.d("순서", "onTapPlane");
        createSelectAnchor(hitResult, cameraVector);

//        test_ysy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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




