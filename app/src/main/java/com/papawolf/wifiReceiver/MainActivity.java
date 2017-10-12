/**************************************************************************************************/
// MainActivity
//
// ESP12E RC WIFI Receiver Project
//
// Copyright Dong-Seok Shin
/**************************************************************************************************/


package com.papawolf.wifiReceiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Locale;

import static android.os.StrictMode.setThreadPolicy;
import static com.papawolf.wifiReceiver.R.drawable.image_button;
import static com.papawolf.wifiReceiver.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = false;

    public static Context mContext;

    RelativeLayout layout_joystick1;
    RelativeLayout layout_joystick2;
    TextView textView1, textView2, textView3, textView4;

    JoyStickClass js1;
    JoyStickClass js2;

    ToggleButton tbWifi;

    DatagramSocket apConnSocket = null;
    BufferedReader sockReader;
    BufferedWriter sockWriter;
    PrintWriter    sockPrintWriter;

    // Global channel position
    int ch1 = 0;
    int ch2 = 0;
    int ch3 = 0;
    int ch4 = 0;
    int ch1Value = 0;
    int ch2Value = 0;
    int ch3Value = 0;
    int ch4Value = 0;

    String sendMsg;

    // Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;
    private SensorEventListener mSensorListener;
    private Sensor mAccSensor  = null;
    private Sensor mGyroSensor = null;

    // Sensor variables
    private float[] mGyroValues = new float[3];
    private float[] mAccValues = new float[3];
    private double mAccPitch, mAccRoll;

    // for unsing complementary fliter
    private float a = 0.2f;
    private static final float NS2S = 1.0f/1000000000.0f;
    private double pitch = 0, roll = 0;
    private double timestamp;
    private double dt;
    private double temp;
    private boolean running;
    private boolean gyroRunning;
    private boolean accRunning;

    private boolean isAppBackGround;
    private static boolean isWindowFocused;

    private ScreenActionReceiver mReceiver;

    private ToggleButton tbGyro;

    // Shared Preferences
    boolean settingReverseCh1;
    boolean settingReverseCh2;
    boolean settingReverseCh3;
    boolean settingReverseCh4;

    boolean settingAutoCenterCh1;
    boolean settingAutoCenterCh2;
    boolean settingAutoCenterCh3;
    boolean settingAutoCenterCh4;

    boolean isConnected = false;

    int     settingEpaCh1;
    int     settingEpaCh2;
    int     settingEpaCh3;
    int     settingEpaCh4;

    int     settingTrimCh1;
    int     settingTrimCh2;
    int     settingTrimCh3;
    int     settingTrimCh4;

    WifiReceiver myApp = new WifiReceiver();
    Context context = this;

    String serverIp        = "";
    int    serverPort     = 0;
    int    serverTimeout  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isAppBackGround) {
            isAppBackGround = false;
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(activity_main);

        mContext = this;

        registerScreenStateReceiver();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        setThreadPolicy(policy);

        serverIp        = context.getApplicationContext().getString(R.string.server_ip);
        serverPort     = Integer.parseInt(context.getApplicationContext().getString(R.string.server_port));
        serverTimeout  = Integer.parseInt(context.getApplicationContext().getString(R.string.server_timeout));

        // 디버그모드에 따라서 로그를 남기거나 남기지 않는다
        this.DEBUG = isDebuggable(this);

        // 세팅값 불러오기
        loadSetting(myApp);

        // WIFI 버튼으로 WIFI 처리
        tbWifi = (ToggleButton) this.findViewById(R.id.toggleButtonWifi);

        tbWifi.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // WIFI 연결시
                if (tbWifi.isChecked()) {
                    try {
                        socketConnect();
                        //mHandler.removeMessages(0);
                        //mHandler.sendEmptyMessage(0);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    Dlog.d("SOCKET!! : " + apConnSocket);

                    if (isConnected) {
                        sendMsg = getResources().getString(R.string.msg_conn_succ);
                        sendServer(sendMsg);
                        Toast.makeText(getApplicationContext(), sendMsg + " 전송", Toast.LENGTH_SHORT).show();

                        tbWifi.setTextColor(Color.GREEN);
                        tbWifi.setText(getResources().getText(R.string.wifi_on));
                    } else {
                        tbWifi.setChecked(false);
                        tbWifi.setTextColor(Color.GRAY);
                        tbWifi.setText(getResources().getText(R.string.wifi_off));

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_conn_fail), Toast.LENGTH_SHORT).show();
                    }
                }
                // WIFI 종료시
                else {
                    try {
                        //mHandler.removeMessages(0);
                        socketDisconnect();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Dlog.d("SOCKET!! : " + e);
                    }

                    tbWifi.setChecked(false);
                    tbWifi.setTextColor(Color.GRAY);
                    tbWifi.setText(getResources().getText(R.string.wifi_off));
                }
            }

        });


        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccSensor  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorListener = new SensorListener();

        // GYTO 버튼으로 GyroScope 처리
        tbGyro = (ToggleButton) this.findViewById(R.id.toggleButtonGyro);

        tbGyro.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // GyroScope 연결시
                if (tbGyro.isChecked()) {

                    Dlog.d("GyroscopeListener on");

                    try {
                        mSensorManager.registerListener(mSensorListener, mAccSensor, SensorManager.SENSOR_DELAY_GAME);
                        mSensorManager.registerListener(mSensorListener, mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
                        tbGyro.setTextColor(Color.GREEN);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { // GyroScope 종료시
                    sensorStop();
                }
            }
        });

        // 진동서비스 설정
        final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);

        layout_joystick1 = (RelativeLayout)findViewById(R.id.layout_joystick1);
        layout_joystick2 = (RelativeLayout)findViewById(R.id.layout_joystick2);

        // 1번 조이스틱 설정
        js1 = new JoyStickClass(getApplicationContext(), layout_joystick1, image_button, myApp.isSettingAutoCenterCh1(), myApp.isSettingAutoCenterCh2());
        js1.setStickSize(200, 200);
        js1.setLayoutSize(800, 800);
        js1.setLayoutAlpha(50);
        js1.setStickAlpha(100);
        js1.setOffset(90);
        js1.setMinimumDistance(20);
        js1.setMaximumDistance(400);

        layout_joystick1.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // Gyro 사용 중 1번째 조이스틱 비활성화
                if (tbGyro.isChecked()) {
                    js1.setStickAlpha(0);
                } else {
                    js1.drawStick(arg1, myApp.isSettingAutoCenterCh1(), myApp.isSettingAutoCenterCh2());
                    js1.setStickAlpha(100);
                }

                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    if (js1.getX() >= js1.getMaximumDistance()) {
                        ch1 = js1.getMaximumDistance();
                    } else if (js1.getX() <= js1.getMaximumDistance() * (-1)) {
                        ch1 = js2.getMaximumDistance() * (-1);
                    } else {
                        ch1 = js1.getX();
                    }

                    if (js1.getY() >= js1.getMaximumDistance()) {
                        ch2 = js1.getMaximumDistance();
                    } else if (js1.getY() <= js1.getMaximumDistance() * (-1)) {
                        ch2 = js2.getMaximumDistance() * (-1);
                    } else {
                        ch2 = js1.getY();
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {

                    if (myApp.isSettingAutoCenterCh1()) {
                        ch1 = 0;
                    }

                    if (myApp.isSettingAutoCenterCh2()) {
                        ch2 = 0;
                    }
                }

                if (arg1.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(100);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(50);

                // 리버스 케이스
                if (myApp.isSettingReverseCh1()) {
                    ch1Value = ch1 * (-1);
                } else {
                    ch1Value = ch1;
                }

                if (myApp.isSettingReverseCh2()) {
                    ch2Value = ch2 * (-1);
                } else {
                    ch2Value = ch2;
                }

                if (myApp.isSettingReverseCh3()) {
                    ch3Value = ch3 * (-1);
                } else {
                    ch3Value = ch3;
                }

                if (myApp.isSettingReverseCh4()) {
                    ch4Value = ch4 * (-1);
                } else {
                    ch4Value = ch4;
                }

                // 트림 케이스
                ch1Value = ch1Value + (myApp.getSettingTrimCh1());
                ch2Value = ch2Value + (myApp.getSettingTrimCh2());
                ch3Value = ch3Value + (myApp.getSettingTrimCh3());
                ch4Value = ch4Value + (myApp.getSettingTrimCh4());

                // EPA 케이스
                ch1Value = (int)((double)ch1Value * (double)(myApp.getSettingEpaCh1() / 100.0));
                ch2Value = (int)((double)ch2Value * (double)(myApp.getSettingEpaCh2() / 100.0));
                ch3Value = (int)((double)ch3Value * (double)(myApp.getSettingEpaCh3() / 100.0));
                ch4Value = (int)((double)ch4Value * (double)(myApp.getSettingEpaCh4() / 100.0));

                sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", ch1Value, ch2Value, ch3Value, ch4Value, arg1.getAction());
                sendServer(sendMsg);

                textView1.setText("CH1 : " + String.valueOf(ch1Value));
                textView2.setText("CH2 : " + String.valueOf(ch2Value));

                return true;
            }
        });

        // 2번 조이스틱 설정
        js2 = new JoyStickClass(getApplicationContext(), layout_joystick2, image_button, myApp.isSettingAutoCenterCh3(), myApp.isSettingAutoCenterCh4());
        js2.setStickSize(200, 200);
        js2.setLayoutSize(800, 800);
        js2.setLayoutAlpha(150);
        js2.setStickAlpha(100);
        js2.setOffset(90);
        js2.setMinimumDistance(20);
        js2.setMaximumDistance(400);

        layout_joystick2.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg2, MotionEvent arg3) {
                js2.drawStick(arg3, myApp.isSettingAutoCenterCh3(), myApp.isSettingAutoCenterCh4());

                if(arg3.getAction() == MotionEvent.ACTION_DOWN
                        || arg3.getAction() == MotionEvent.ACTION_MOVE) {
                    if (js2.getX() >= js2.getMaximumDistance()) {
                        ch3 = js2.getMaximumDistance();
                    } else if (js2.getX() <= js2.getMaximumDistance() * (-1)) {
                        ch3 = js2.getMaximumDistance() * (-1);
                    }
                    else {
                        ch3 = js2.getX();
                    }

                    if (js2.getY() >= js2.getMaximumDistance()) {
                        ch4 = js2.getMaximumDistance();
                    } else if (js2.getY() <= js2.getMaximumDistance() * (-1)) {
                        ch4 = js2.getMaximumDistance() * (-1);
                    } else {
                        ch4 = js2.getY();
                    }
                }
                else if(arg3.getAction() == MotionEvent.ACTION_UP) {

                    if (myApp.isSettingAutoCenterCh3()) {
                        ch3 = 0;
                    }

                    if (myApp.isSettingAutoCenterCh4()) {
                        ch4 = 0;
                    }
                }

                if (arg3.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(100);
                if (arg3.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(50);

                // 리버스 케이스
                if (myApp.isSettingReverseCh1()) {
                    ch1Value = ch1 * (-1);
                } else {
                    ch1Value = ch1;
                }

                if (myApp.isSettingReverseCh2()) {
                    ch2Value = ch2 * (-1);
                } else {
                    ch2Value = ch2;
                }

                if (myApp.isSettingReverseCh3()) {
                    ch3Value = ch3 * (-1);
                } else {
                    ch3Value = ch3;
                }

                if (myApp.isSettingReverseCh4()) {
                    ch4Value = ch4 * (-1);
                } else {
                    ch4Value = ch4;
                }

                // 트림 케이스
                ch1Value = ch1Value + (myApp.getSettingTrimCh1());
                ch2Value = ch2Value + (myApp.getSettingTrimCh2());
                ch3Value = ch3Value + (myApp.getSettingTrimCh3());
                ch4Value = ch4Value + (myApp.getSettingTrimCh4());

                // EPA 케이스
                ch1Value = (int)((double)ch1Value * (double)(myApp.getSettingEpaCh1() / 100.0));
                ch2Value = (int)((double)ch2Value * (double)(myApp.getSettingEpaCh2() / 100.0));
                ch3Value = (int)((double)ch3Value * (double)(myApp.getSettingEpaCh3() / 100.0));
                ch4Value = (int)((double)ch4Value * (double)(myApp.getSettingEpaCh4() / 100.0));

                sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", ch1Value, ch2Value, ch3Value, ch4Value, arg3.getAction());
                sendServer(sendMsg);

                textView3.setText("CH3 : " + String.valueOf(ch3Value));
                textView4.setText("CH4 : " + String.valueOf(ch4Value));

                return true;
            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();

        if (isWindowFocused == false) {
            isAppBackGround = true;

            sensorStop();
            socketDisconnect();

            Dlog.i("onStop!");
        }
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        sensorStop();
        socketDisconnect();

        Dlog.i("onDestroy!");
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;

        Dlog.i("onWindowFocusChanged! " + isWindowFocused);
    }

    private void registerScreenStateReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        mReceiver = new ScreenActionReceiver();
        registerReceiver(mReceiver, filter);

        Dlog.i("registerScreenStateReceiver!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_setting:
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_finish:
                ActivityCompat.finishAffinity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 소켓전송
    public boolean sendServer(String msg) {
        if (isConnected) {

            try {
                byte[] buf = msg.getBytes();

                DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverIp), serverPort);

                apConnSocket.send(p);

                //sockPrintWriter.println(msg);
                //Dlog.i(sendMsg);
            } catch (Exception e) {
                Dlog.e("DATA SEND ERROR");
                e.printStackTrace();
                return false;
            }
        }

        return  true;
    }

    private void SendMsgAlert()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());

            }

        });

        alert.setMessage("과도한 공지를 제한하기 위해 1회 발송 후 앱을 종료합니다.");
        alert.show();
    }

    // 소켓연결
//    class SocketThread extends Thread {
//        @Override
//        public void run() {
//
//            try {
//                apConnSocket = new Socket(ipaddr, ipport);
//
//                sockWriter = new BufferedWriter(new OutputStreamWriter(apConnSocket.getOutputStream(), "EUC-KR"));
//                sockReader = new BufferedReader(new InputStreamReader(apConnSocket.getInputStream()));
//                sockPrintWriter = new PrintWriter(sockWriter, true);
//
//                Dlog.d("SOCKET!! : " + apConnSocket);
//            } catch (UnknownHostException ue) {
//                Dlog.d("SOCKET!! : " + apConnSocket);
//                System.out.println(ue);
//                ue.printStackTrace();
//            } catch (IOException ie) {
//                Dlog.d("SOCKET!! : " + apConnSocket);
//                System.out.println(ie);
//                ie.printStackTrace();
//            }
//        }
//    }

    private void socketConnect() {

        Dlog.i("SOCKET Connect start!!");

        try {
            InetAddress ip = InetAddress.getByName(serverIp);

            apConnSocket = new DatagramSocket();
            apConnSocket.setSoTimeout(serverTimeout);

            //sockWriter      = new BufferedWriter(new OutputStreamWriter(apConnSocket.getOutputStream(), "EUC-KR"));
            //sockReader      = new BufferedReader(new InputStreamReader(apConnSocket.getInputStream()));
            //sockPrintWriter = new PrintWriter(sockWriter, true);

            Dlog.i("SOCKET!! : " + apConnSocket + " " + apConnSocket.isConnected());

            sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", 0, 0, 0, 0, 5);
            sendServer(sendMsg);
            isConnected = true;

        } catch (SocketException e) {
            Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
            Dlog.e("SOCKET!! : " + apConnSocket + " " + apConnSocket.isConnected());
            e.printStackTrace();
            isConnected = false;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
            Dlog.e("SOCKET!! : " + apConnSocket + " " + apConnSocket.isConnected());
            e.printStackTrace();
            isConnected = false;
        }
    }

    private void socketDisconnect() {

        sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", 0, 0, 0, 0, 6);
        sendServer(sendMsg);

        Dlog.i("SOCKET!! : " + apConnSocket);

        try {
            isConnected = false;

            sockWriter.close();
            sockReader.close();
            sockPrintWriter.close();

            sockWriter = null;
            sockReader = null;
            sockPrintWriter = null;

            apConnSocket.close();
            apConnSocket = null;
        } catch (Exception e) {
            Dlog.e("SOCKET!! : " + apConnSocket);
            System.out.println(e);
            e.printStackTrace();
        }

        tbWifi.setChecked(false);
        tbWifi.setTextColor(Color.GRAY);
        tbWifi.setText(getResources().getText(R.string.wifi_off));
    }

    private boolean isDebuggable(Context context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
			/* debuggable variable will remain false */
        }

        return debuggable;
    }

    public class SensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            switch (event.sensor.getType()) {

                case Sensor.TYPE_GYROSCOPE:
                    mGyroValues = event.values;

                    if (!gyroRunning) gyroRunning = true;

                    break;

                case Sensor.TYPE_ACCELEROMETER:
                    mAccValues = event.values;

                    if (!accRunning) accRunning = true;

                    break;
            }

            if (gyroRunning && accRunning) {
                complementaty(event.timestamp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    // 자이로, 가속도 센서 정지
    private void sensorStop() {

        Dlog.d("GyroscopeListener off");

        try {
            mSensorManager.unregisterListener(mSensorListener);
            tbGyro.setTextColor(Color.RED);
            tbGyro.setTextOff(getResources().getString(R.string.gyro_off));
            tbGyro.setChecked(false);

            ch1 = 0;
            ch2 = 0;

            sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", ch1, ch2, ch3, ch4, 9);
            sendServer(sendMsg);

            textView1.setText("CH1 : " + String.valueOf(ch1));
            textView2.setText("CH2 : " + String.valueOf(ch2));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * 1차 상보필터 적용 메서드 */
    private void complementaty(double new_ts){

        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI; // Y 축 기준
        mAccRoll= Math.atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI; // X 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);

        temp = (1/a) * (mAccRoll - roll) + mGyroValues[0];
        roll = roll + (temp*dt);

        if (roll >  40) roll =  40;
        if (roll < -40) roll = -40;

        if (pitch >  40) pitch =  40;
        if (pitch < -40) pitch = -40;

        ch1 = (int) Common.map((long) ( roll * 10), -400, 400, -400, 400);
        ch2 = (int) Common.map((long) (pitch * 10), -400, 400, -400, 400) * (-1);

        Dlog.i(String.format(Locale.US, "PITCH : %d ROLL : %d", ch1, ch2));

        // 리버스 케이스
        if (myApp.isSettingReverseCh1()) ch1 = ch1 * (-1);
        if (myApp.isSettingReverseCh2()) ch2 = ch2 * (-1);
        if (myApp.isSettingReverseCh3()) ch3 = ch3 * (-1);
        if (myApp.isSettingReverseCh4()) ch4 = ch4 * (-1);

        // 트림 케이스
        ch1 = ch1 + (myApp.getSettingTrimCh1());
        ch2 = ch2 + (myApp.getSettingTrimCh2());
        ch3 = ch3 + (myApp.getSettingTrimCh3());
        ch4 = ch4 + (myApp.getSettingTrimCh4());

        // EPA 케이스
        ch1 = (int)((double)ch1 * (double)(myApp.getSettingEpaCh1() / 100.0));
        ch2 = (int)((double)ch2 * (double)(myApp.getSettingEpaCh2() / 100.0));
        ch3 = (int)((double)ch3 * (double)(myApp.getSettingEpaCh3() / 100.0));
        ch4 = (int)((double)ch4 * (double)(myApp.getSettingEpaCh4() / 100.0));

        sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", ch1, ch2, ch3, ch4, 9);
        sendServer(sendMsg);

        textView1.setText("CH1 : " + String.valueOf(ch1));
        textView2.setText("CH2 : " + String.valueOf(ch2));
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            Dlog.i("HANDLE MESSAGE!");

            try {
                sockPrintWriter.println(":CHK:");
                Dlog.i(sockReader.readLine());
            } catch (Exception e) {
                Dlog.e("ERROR");
                e.printStackTrace();
                socketDisconnect();
                mHandler.removeMessages(0);
            }

            if (isConnected) {
                tbWifi.setChecked(true);
                tbWifi.setTextColor(Color.GREEN);
                tbWifi.setText(getResources().getText(R.string.wifi_on));
                mHandler.sendEmptyMessageDelayed(0, 2000);
            } else {
                Dlog.i("SOCKET!! : DisConnected");
                tbWifi.setChecked(false);
                tbWifi.setTextColor(Color.RED);
                tbWifi.setText(getResources().getText(R.string.wifi_off));
                socketDisconnect();
                mHandler.removeMessages(0);
            }
        }
    };

    public class ScreenActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                Dlog.i("ACTION_SCREEN_OFF!");
                sensorStop();
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if (isAppBackGround == false) {
                    sensorStop();
                }

                Dlog.i("ACTION_SCREEN_ON!");
            }
        }
    }

    public void loadSetting(WifiReceiver myApp) {

        settingReverseCh1 = Common.getPreferencesBoolean(mContext, "reverseCh1");
        settingReverseCh2 = Common.getPreferencesBoolean(mContext, "reverseCh2");
        settingReverseCh3 = Common.getPreferencesBoolean(mContext, "reverseCh3");
        settingReverseCh4 = Common.getPreferencesBoolean(mContext, "reverseCh4");

        settingAutoCenterCh1 = Common.getPreferencesBoolean(mContext, "autoCenterCh1");
        settingAutoCenterCh2 = Common.getPreferencesBoolean(mContext, "autoCenterCh2");
        settingAutoCenterCh3 = Common.getPreferencesBoolean(mContext, "autoCenterCh3");
        settingAutoCenterCh4 = Common.getPreferencesBoolean(mContext, "autoCenterCh4");

        settingEpaCh1 = Common.getPreferencesInt(mContext, "epaCh1");
        settingEpaCh2 = Common.getPreferencesInt(mContext, "epaCh2");
        settingEpaCh3 = Common.getPreferencesInt(mContext, "epaCh3");
        settingEpaCh4 = Common.getPreferencesInt(mContext, "epaCh4");

        settingTrimCh1 = Common.getPreferencesInt(mContext, "trimCh1");
        settingTrimCh2 = Common.getPreferencesInt(mContext, "trimCh2");
        settingTrimCh3 = Common.getPreferencesInt(mContext, "trimCh3");
        settingTrimCh4 = Common.getPreferencesInt(mContext, "trimCh4");

        Dlog.i("Loading Setting Values!");

        myApp.setSettingReverseCh1(settingReverseCh1);
        myApp.setSettingReverseCh2(settingReverseCh2);
        myApp.setSettingReverseCh3(settingReverseCh3);
        myApp.setSettingReverseCh4(settingReverseCh4);

        myApp.setSettingAutoCenterCh1(settingAutoCenterCh1);
        myApp.setSettingAutoCenterCh2(settingAutoCenterCh2);
        myApp.setSettingAutoCenterCh3(settingAutoCenterCh3);
        myApp.setSettingAutoCenterCh4(settingAutoCenterCh4);

        myApp.setSettingEpaCh1(settingEpaCh1);
        myApp.setSettingEpaCh2(settingEpaCh2);
        myApp.setSettingEpaCh3(settingEpaCh3);
        myApp.setSettingEpaCh4(settingEpaCh4);

        myApp.setSettingTrimCh1(settingTrimCh1);
        myApp.setSettingTrimCh2(settingTrimCh2);
        myApp.setSettingTrimCh3(settingTrimCh3);
        myApp.setSettingTrimCh4(settingTrimCh4);
    }

    public void saveSetting(WifiReceiver myApp) {

        settingReverseCh1 = myApp.isSettingReverseCh1();
        settingReverseCh2 = myApp.isSettingReverseCh2();
        settingReverseCh3 = myApp.isSettingReverseCh3();
        settingReverseCh4 = myApp.isSettingReverseCh4();

        settingAutoCenterCh1 = myApp.isSettingAutoCenterCh1();
        settingAutoCenterCh2 = myApp.isSettingAutoCenterCh2();
        settingAutoCenterCh3 = myApp.isSettingAutoCenterCh3();
        settingAutoCenterCh4 = myApp.isSettingAutoCenterCh4();

        settingEpaCh1 = myApp.getSettingEpaCh1();
        settingEpaCh2 = myApp.getSettingEpaCh2();
        settingEpaCh3 = myApp.getSettingEpaCh3();
        settingEpaCh4 = myApp.getSettingEpaCh4();

        settingTrimCh1 = myApp.getSettingTrimCh1();
        settingTrimCh2 = myApp.getSettingTrimCh2();
        settingTrimCh3 = myApp.getSettingTrimCh3();
        settingTrimCh4 = myApp.getSettingTrimCh4();

        Common.setPreferencesBoolean(mContext, "reverseCh1"   , settingReverseCh1   );
        Common.setPreferencesBoolean(mContext, "reverseCh2"   , settingReverseCh2   );
        Common.setPreferencesBoolean(mContext, "reverseCh3"   , settingReverseCh3   );
        Common.setPreferencesBoolean(mContext, "reverseCh4"   , settingReverseCh4   );
        Common.setPreferencesBoolean(mContext, "autoCenterCh1", settingAutoCenterCh1);
        Common.setPreferencesBoolean(mContext, "autoCenterCh2", settingAutoCenterCh2);
        Common.setPreferencesBoolean(mContext, "autoCenterCh3", settingAutoCenterCh3);
        Common.setPreferencesBoolean(mContext, "autoCenterCh4", settingAutoCenterCh4);

        Common.setPreferencesInt(mContext, "epaCh1" , settingEpaCh1 );
        Common.setPreferencesInt(mContext, "epaCh2" , settingEpaCh2 );
        Common.setPreferencesInt(mContext, "epaCh3" , settingEpaCh3 );
        Common.setPreferencesInt(mContext, "epaCh4" , settingEpaCh4 );
        Common.setPreferencesInt(mContext, "trimCh1", settingTrimCh1);
        Common.setPreferencesInt(mContext, "trimCh2", settingTrimCh2);
        Common.setPreferencesInt(mContext, "trimCh3", settingTrimCh3);
        Common.setPreferencesInt(mContext, "trimCh4", settingTrimCh4);

        Dlog.i("Saving Setting Values!");
    }
}

