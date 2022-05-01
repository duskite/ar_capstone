package com.mju.ar_capstone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.mju.ar_capstone.helpers.FirebaseManager;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback {

    private int userType;
    private Button btnArSf;
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

    //서버랑 연결
    private FirebaseManager firebaseManager;

    //테스트용
    private TextView testView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        registerListener();

        // 메인에서 정보 가져옴
        Intent intent = getIntent();
        userType = intent.getIntExtra("userType", 0);
        selectedChannel = intent.getStringExtra("channel");
        Log.d("채널넘기는거 인벤", selectedChannel);

        testView = (TextView) findViewById(R.id.userTypeTest);
        testView.setText(String.valueOf(userType));


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
        // 위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
        // 권한확인, onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        // 액티비티 생성시 서버와 연결후 데이터 가져옴
        firebaseManager = new FirebaseManager(selectedChannel);
        firebaseManager.registerContentsValueListner();


        // ar화면으로 넘어가기
        btnArSf = (Button) findViewById(R.id.btnArSf);
        btnArSf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ArSfActivity.class);
                intent.putExtra("userType", userType);
                intent.putExtra("azimuth", getAzimuth());
                intent.putExtra("channel", selectedChannel);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);
        mNaverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                updateAnchors(mNaverMap);
            }
        });

    }
    public void updateAnchors(NaverMap naverMap){
        List<Marker> listMarker = new ArrayList<Marker>();

        for(WrappedAnchor wrappedAnchor: firebaseManager.wrappedAnchorList) {
            //(루프 한번 돌 때 마다 marker객체 생성)
            Marker marker = new Marker();
            marker.setPosition(new LatLng(wrappedAnchor.getLat(), wrappedAnchor.getLng()));

            marker.setMap(naverMap);

            //위치 정보를 넣은 marker값을 listMarker에 저장
            listMarker.add(marker);
        }

        firebaseManager.clearWrappedAnchorList();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }
}
