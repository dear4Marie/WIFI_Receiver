package com.papawolf.wifiReceiver;


import android.app.Application;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Dlog {

    static final String TAG = "MARIE";


    /**
     * Log Level Error
     **/
    public static final void e(String message) {
        if (MainActivity.DEBUG) Log.e(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Warning
     **/
    public static final void w(String message) {
        if (MainActivity.DEBUG) Log.w(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Information
     **/
    public static final void i(String message) {
        if (MainActivity.DEBUG) Log.i(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Debug
     **/
    public static final void d(String message) {
        if (MainActivity.DEBUG) Log.d(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Verbose
     **/
    public static final void v(String message) {
        if (MainActivity.DEBUG) Log.v(TAG, buildLogMsg(message));
    }


    public static String buildLogMsg(String message) {

        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);

        return sb.toString();

    }

    /**
     * Created by papawolf on 2017-08-24.
     */

    public static class wifiReceiver extends Application {

        //Using the Accelometer & Gyroscoper
        private SensorManager mSensorManager = null;

        //Using the Gyroscope
        private SensorEventListener mGyroLis;
        private Sensor mGgyroSensor = null;

        //Roll and Pitch
        private double pitch;
        private double roll;
        private double yaw;

        //timestamp and dt
        private double timestamp;
        private double dt;

        // for radian -> dgree
        private double RAD2DGR = 180 / Math.PI;
        private static final float NS2S = 1.0f/1000000000.0f;

        public SensorManager getmSensorManager() {
            return mSensorManager;
        }

        public void setmSensorManager(SensorManager mSensorManager) {
            this.mSensorManager = mSensorManager;
        }

        public SensorEventListener getmGyroLis() {
            return mGyroLis;
        }

        public void setmGyroLis(SensorEventListener mGyroLis) {
            this.mGyroLis = mGyroLis;
        }

        public Sensor getmGgyroSensor() {
            return mGgyroSensor;
        }

        public void setmGgyroSensor(Sensor mGgyroSensor) {
            this.mGgyroSensor = mGgyroSensor;
        }

        public double getPitch() {
            return pitch;
        }

        public void setPitch(double pitch) {
            this.pitch = pitch;
        }

        public double getRoll() {
            return roll;
        }

        public void setRoll(double roll) {
            this.roll = roll;
        }

        public double getYaw() {
            return yaw;
        }

        public void setYaw(double yaw) {
            this.yaw = yaw;
        }

        public double getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(double timestamp) {
            this.timestamp = timestamp;
        }

        public double getDt() {
            return dt;
        }

        public void setDt(double dt) {
            this.dt = dt;
        }

        public double getRAD2DGR() {
            return RAD2DGR;
        }

        public void setRAD2DGR(double RAD2DGR) {
            this.RAD2DGR = RAD2DGR;
        }

        public static float getNS2S() {
            return NS2S;
        }
    }
}