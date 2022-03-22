package com.mju.ar_capstone.helpers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class SensorAllManager implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private int azimuth;

    public SensorAllManager(Object systemService) {
        sensorManager = (SensorManager) systemService;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        registerListener();
    }

    public void unRegisterListener(){
        sensorManager.unregisterListener(this);
    }
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


    public int getAzimuth(boolean ORIENTATION){
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        Log.d("앵커위치", "앵커 라디안: " + orientationAngles[0]);
        azimuth = (int) (Math.toDegrees(orientationAngles[0]) + 360 ) % 360;
        Log.d("앵커위치", "앵커 디그리: " + Math.toDegrees(orientationAngles[0]));
        Log.d("앵커위치", "앵커 방위각: " + azimuth);

        if(ORIENTATION){ // 만약 화면이 눕혀져있는 상태로 앵커를 남겼다면 실제 방위각만큼 보정해줘야함
            azimuth += 90;
        }

        return azimuth;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
