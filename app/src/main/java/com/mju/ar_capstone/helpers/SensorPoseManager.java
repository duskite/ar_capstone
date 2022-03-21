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

    public SensorPoseManager(Object systemService) {
        sensorManager = (SensorManager) systemService;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    //리스너 등록
    public void startSensorcheck(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    //리스너 해제
    public void stopSensorCheck(){
        sensorManager.unregisterListener(this);
    }


    public float[] getAccXYZ() {
        return accXYZ;
    }



    //여기서 pose랑 acc 모두 처리하고 최종적인 pose만 리턴 하는걸로
    public void getRealPose(){

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accXYZ[0] = event.values[0];
            accXYZ[1] = event.values[1];
            accXYZ[2] = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
