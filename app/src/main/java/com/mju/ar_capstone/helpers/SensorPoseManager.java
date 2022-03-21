package com.mju.ar_capstone.helpers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class SensorPoseManager implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] accXYZ = new float[3];

    private boolean sensorCheckd = true;

    public SensorPoseManager(Object systemService) {
        sensorManager = (SensorManager) systemService;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    //리스너 등록
    public void startSensorcheck(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }
    //리스너 해제
    public void stopSensorCheck(){
        sensorManager.unregisterListener(this);
    }

    public float[] getAccXYZ() {
        // acc 한번 가져가면 잠깐 센서 수신 막아놓음
        // 센서 수신이 너무 빨라서 다른 값이 자꾸 들어오는듯
        sensorCheckd = false;
        return accXYZ;
    }
    //다시 수신 상태로 바꿔놓음
    public void setSensorCheckdTrue(){
        sensorCheckd = true;
    }

    //여기서 pose랑 acc 모두 처리하고 최종적인 pose만 리턴 하는걸로
    public void getRealPose(float[] poseT, float[] loadedAccXYZ, float[] currentAccXYZ){
        Log.d("앵커포즈 불러온 T: ", "x: " + String.valueOf(poseT[0]) +
                ", y:" + String.valueOf(poseT[1]) +
                ", z: " + String.valueOf(poseT[2]));

        Log.d("앵커포즈 불러온 acc: ", "x: " + String.valueOf(loadedAccXYZ[0]) +
                ", y:" + String.valueOf(loadedAccXYZ[1]) +
                ", z: " + String.valueOf(loadedAccXYZ[2]));

        Log.d("앵커포즈 새로운 나의 acc: ", "x: " + String.valueOf(currentAccXYZ[0]) +
                ", y:" + String.valueOf(currentAccXYZ[1]) +
                ", z: " + String.valueOf(currentAccXYZ[2]));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && sensorCheckd == true){
            accXYZ[0] = event.values[0];
            accXYZ[1] = event.values[1];
            accXYZ[2] = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
