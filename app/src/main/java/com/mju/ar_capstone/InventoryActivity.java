package com.mju.ar_capstone;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.invenfragments.HostListFragment;
import com.mju.ar_capstone.invenfragments.UserInvenFragment;
import com.mju.ar_capstone.services.NotificationService;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback, Overlay.OnClickListener,NaverMap.OnMapClickListener {

    private int userType, channelType;
    private FloatingActionButton btnArSf;
    private String selectedChannel;

    // ar 화면 켜질때 방위각 넘김
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private int azimuth;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private InfoWindow infoWindow;

    //마커와 써클 오버레이 한번씩 비워줘야함
    private List<Marker> markerList = new ArrayList<>();
    private List<CircleOverlay> circleOverlayList = new ArrayList<>();

    //서버랑 연결
    private FirebaseManager firebaseManager;
    private FirebaseAuthManager firebaseAuthManager;

    //하단부 주최자 혹은 참가자에 따른 프래그먼트 변경
    private FragmentManager fragmentManager;
    private HostListFragment hostListFragment;
    private UserInvenFragment userInvenFragment;
    private FragmentTransaction fragmentTransaction;
    Context mContext;

    Bundle bundle;
    private boolean stateHaveKey = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        mContext=this;

        // 지도 객체 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        // onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this);


        Log.d("인벤", "onCreate");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        registerListener();


        // 메인에서 정보 가져옴
        Intent intent = getIntent();
        //디폴트는 참가자로
        userType = intent.getIntExtra("userType", 2);

        //디폴트는 공개로
        channelType = intent.getIntExtra("channelType", 1);
        selectedChannel = intent.getStringExtra("channel");
        //프래그먼트에 값 넘길 번들 객체
        bundle = new Bundle();
        bundle.putString("selectedChannel", selectedChannel);

        // 액티비티 생성시 서버와 연결후 데이터 가져옴
        firebaseManager = new FirebaseManager(selectedChannel);


        //여기 잠시 컨텐츠 불러오는거 지움

        firebaseAuthManager = new FirebaseAuthManager();
        //채널 생성시 기본 세팅하기, 이미 생성되어 있는 채널은 의미없음, 주최자일때만 해당됨
        //참가자일 경우는 아예 채널타입을 넘기지 않음
        //주최자일 경우에는 해당 채널의 소유를 먼저 확인하고, 이미 소유자가 있을 경우 채널타입은 사용되지 않음
        if(userType == 1){
            firebaseManager.setChannelInfo(selectedChannel, channelType, firebaseAuthManager.getUID());
        }else{
            //참가자일때
            firebaseManager.joinChannel(selectedChannel, firebaseAuthManager.getUID());
        }

        fragmentManager = getSupportFragmentManager();
        if (userType == 1){ //주최자 일 때
            hostListFragment = new HostListFragment();
            hostListFragment.setFirebaseManager(firebaseManager);
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.host_or_user_frame, hostListFragment).commitAllowingStateLoss();
            hostListFragment.setArguments(bundle);
        }else{ //참가자 일 때
            userInvenFragment = new UserInvenFragment(mContext);
            userInvenFragment.setFirebaseManager(firebaseManager, firebaseAuthManager);
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.host_or_user_frame, userInvenFragment).commitAllowingStateLoss();
            userInvenFragment.setArguments(bundle);
        }


        // ar화면으로 넘어가기
        btnArSf = (FloatingActionButton) findViewById(R.id.btnArSf);
        btnArSf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ArSfActivity.class);
                intent.putExtra("userType", userType);
                intent.putExtra("azimuth", getAzimuth());
                intent.putExtra("channel", selectedChannel);

                //키 소지 여부 넘기기
                stateHaveKey = firebaseManager.checkHaveKey();
                intent.putExtra("haveKey", stateHaveKey);

                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("인벤", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("인벤", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("인벤", "onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("인벤", "onStop");
    }

    //방위각 구해서 넘기는 부분
    public void registerListener(){
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI);
        }
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        //센서 바뀌는 순간마다 우선 방위각 구하도록 함
        azimuth = (int) (Math.toDegrees(orientationAngles[0]) + 360 ) % 360;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getAzimuth(){
//        if(ORIENTATION){ // 만약 화면이 눕혀져있는 상태로 앵커를 남겼다면 실제 방위각만큼 보정해줘야함
//            azimuth += 90;
//        }
        return sectionDegree(azimuth);
    }

    public int sectionDegree(int degree){

        int section = 0;

        if(degree > 357 || degree < 3){
            //이정도 범위는 그냥 0이라고 보기
        }else {
            section = degree / 3;
            section *= 3;
        }

        return section;
    }


    //지도 부분
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d("인벤", "onMapReady");
        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;

        // 기존 코드 onPause 발생으로 전체 동작 문제생겨서 수정함
        // 위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
        mNaverMap.setLocationSource(mLocationSource);
        mNaverMap.setOnMapClickListener(this);
        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        //카메라 기본 줌 변경함
        CameraPosition cameraPosition = new CameraPosition(mNaverMap.getCameraPosition().target, 18);
        mNaverMap.setCameraPosition(cameraPosition);

        //호출횟수가 너무 많기는 한데, 사용자가 카메라 이동시 마커 원활하게 보여주려면 이게 나은듯
        //대신 메모리 관리를 updateAnchors에서 해주고 있어서 동작에 무리 없음
        mNaverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                updateAnchors(mNaverMap);
            }
        });

//        // 정보창의 값을 listInforWindow에 저장
//        //listInfoWindow.add(infoWindow);
//        infoWindow = new InfoWindow();
//        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
//            @NonNull
//            @Override
//            public CharSequence getText(@NonNull InfoWindow infoWindow) {
//                //Marker marker = infoWindow.getMarker();
//                // 마커에 setTag된 정보를 infoWindow에서 불러옴
//                return (CharSequence)infoWindow.getMarker().getTag();
//            }
//        });

    }
    // 마커 클릭하면 정보창 나오기
    @Override
    public boolean onClick(@NonNull Overlay overlay) {
//        if (overlay instanceof Marker) {
//            Marker marker = (Marker) overlay;
//            if (marker.getInfoWindow() != null) {
//                infoWindow.close();
//            } else {
//                infoWindow.open(marker);
//            }
//            return true;
//        }
        return false;
    }

    // 맵 누르면 정보창 끄게하기
    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
//        if (infoWindow.getMarker() != null) {
//            infoWindow.close();
//        }
    }
    public void updateAnchors(NaverMap naverMap){


        //새로운 내용 반영하기 전에 이미 전에 찍혀있던거 모두 지워줌
        for(Marker m: markerList){
            m.setMap(null);
        }
        for(CircleOverlay c: circleOverlayList){
            c.setMap(null);
        }

        Log.d("써클문제", "updateAnchors");

        LatLng tmpLatLng = new LatLng(0, 0);

        for(WrappedAnchor wrappedAnchor: firebaseManager.wrappedAnchorList) {
            LatLng latLng = new LatLng(wrappedAnchor.getLat(), wrappedAnchor.getLng());

            if(userType == 2){ // 참가자일때

                //너무 가까운거는 넘어감, 메모리 문제 때문에 먼것들만 써클 오버레이 찍어줘야함
                if(latLng.distanceTo(tmpLatLng) < 10){
                    continue;
                }
                Log.d("지도 오버레이", "몇개찍혔나");
                // 마커를 보여주지 않고 부근만 찍음
                CircleOverlay circleOverlay = new CircleOverlay(latLng, 10);
                circleOverlay.setOutlineColor(Color.RED);
                circleOverlay.setOutlineWidth(10);
                circleOverlay.setMap(naverMap);

                Log.d("써클문제", "써클찍음");

                //위치 정보를 넣은 circleOverlay값을 circleOverlayList에 저장
                //호출시 직접 낱개로 지워줘야함
                circleOverlayList.add(circleOverlay);

            }else { //주최자일때
                //(루프 한번 돌 때 마다 marker객체 생성)
                Marker marker = new Marker();
                marker.setPosition(latLng);
                marker.setMap(naverMap);
                Log.d("써클문제", "마커찍음");

                //마커 크기 및 색상 설정
                marker.setWidth(75);
                marker.setHeight(100);
                marker.setIcon(MarkerIcons.BLACK);

                marker.setIconTintColor(Color.RED);
                //마커 클릭 리스너
                marker.setOnClickListener(this);
                //마커에 앵커 타입을 태그로 설정
                marker.setTag(wrappedAnchor.getAnchorType() + "앵커");

                //위치 정보를 넣은 marker값을 markerList에 저장
                //호출시 직접 낱개로 지워줘야함
                markerList.add(marker);
            }

            //이전 위경도값 잠시 담아둠
            tmpLatLng = latLng;
        }
    }
}
