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
    private float azimuth;

    private boolean sensorCheckd = true;

    //이거는 예전에 쓰던거라 나중에 삭제 예정
    private float[] accXYZ = new float[3];


    public SensorAllManager(Object systemService) {
        sensorManager = (SensorManager) systemService;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    //리스너 등록
    public void startSensorcheck(){
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }
    public float getAzimuth(){
        updateOrientationAngles();
        azimuth = (int) (Math.toDegrees(orientationAngles[0]) + 360 ) % 360;
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

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && sensorCheckd == true){
            accXYZ[0] = event.values[0];
            accXYZ[1] = event.values[1];
            accXYZ[2] = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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



}
