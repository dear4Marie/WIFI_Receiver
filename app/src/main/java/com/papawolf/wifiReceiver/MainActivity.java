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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Locale;

import static android.os.StrictMode.setThreadPolicy;
import static com.papawolf.wifiReceiver.R.drawable.image_button;
import static com.papawolf.wifiReceiver.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = false;

    RelativeLayout layout_joystick1;
    RelativeLayout layout_joystick2;
    TextView textView1, textView2, textView3, textView4;

    JoyStickClass js1;
    JoyStickClass js2;

    ToggleButton tbWifi;

    Socket         apConnSocket = null;
    BufferedReader sockReader;
    BufferedWriter sockWriter;
    PrintWriter    sockPrintWriter;

    // Global channel position
    int ch1 = 0;
    int ch2 = 0;
    int ch3 = 0;
    int ch4 = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isAppBackGround) {
            isAppBackGround = false;
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(activity_main);

        registerScreenStateReceiver();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        setThreadPolicy(policy);

        // 디버그모드에 따라서 로그를 남기거나 남기지 않는다
        this.DEBUG = isDebuggable(this);

        // WIFI 버튼으로 WIFI 처리
        tbWifi = (ToggleButton) this.findViewById(R.id.toggleButtonWifi);

        tbWifi.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // WIFI 연결시
                if (tbWifi.isChecked()) {
                    try {
                        socketConnect();
                        //mHandler.sendEmptyMessage(0);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    Dlog.d("SOCKET!! : " + apConnSocket);

                    if (apConnSocket != null && apConnSocket.isConnected()) {
                        sendMsg = getResources().getString(R.string.msg_conn_succ);
                        sendServer(sendMsg);
                        Toast.makeText(getApplicationContext(), sendMsg + " 전송", Toast.LENGTH_SHORT).show();

                        tbWifi.setTextColor(Color.GREEN);
                    } else {
                        tbWifi.setChecked(false);

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_conn_fail), Toast.LENGTH_SHORT).show();
                    }
                }
                // WIFI 종료시
                else {
                    try {
                        socketDisconnect();
                        //mHandler.removeMessages(0);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Dlog.d("SOCKET!! : " + e);
                    }

                    tbWifi.setTextColor(Color.RED);
                }
            }

        });


        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccSensor  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorListener = new SensorListener();

        // GYTO 버튼으로 GyroScope 처리
        final ToggleButton tbGyro = (ToggleButton) this.findViewById(R.id.toggleButtonGyro);

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

                    Dlog.d("GyroscopeListener off");

                    try {
                        mSensorManager.unregisterListener(mSensorListener);
                        tbGyro.setTextColor(Color.RED);

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
        js1 = new JoyStickClass(getApplicationContext(), layout_joystick1, image_button);
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
                    js1.drawStick(arg1);
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
                    ch1 = 0;
                    ch2 = 0;
                }

                if (arg1.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(100);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(50);

                sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", ch1, ch2, ch3, ch4, arg1.getAction());
                sendServer(sendMsg);

                textView1.setText("CH1 : " + String.valueOf(ch1));
                textView2.setText("CH2 : " + String.valueOf(ch2));

                return true;
            }
        });

        // 2번 조이스틱 설정
        js2 = new JoyStickClass(getApplicationContext(), layout_joystick2, image_button);
        js2.setStickSize(200, 200);
        js2.setLayoutSize(800, 800);
        js2.setLayoutAlpha(150);
        js2.setStickAlpha(100);
        js2.setOffset(90);
        js2.setMinimumDistance(20);
        js2.setMaximumDistance(400);

        layout_joystick2.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg2, MotionEvent arg3) {
                js2.drawStick(arg3);

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
                    ch3 = 0;
                    ch4 = 0;
                }

                if (arg3.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(100);
                if (arg3.getAction() == MotionEvent.ACTION_DOWN)  vibrator.vibrate(50);

                sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", ch1, ch2, ch3, ch4, arg3.getAction());
                sendServer(sendMsg);

                textView3.setText("CH3 : " + String.valueOf(ch3));
                textView4.setText("CH4 : " + String.valueOf(ch4));

                return true;
            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();

        if (isWindowFocused == false) {
            isAppBackGround = true;
            // TODO 자이로 정지, failSafe 상태로
        }
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;
    }

    private void registerScreenStateReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        mReceiver = new ScreenActionReceiver();
        registerReceiver(mReceiver, filter);
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
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 소켓전송
    public void sendServer(String msg) {
        if (apConnSocket != null && apConnSocket.isConnected()) {

            try {
                sockPrintWriter.println(msg);
                //Dlog.i(sendMsg);
            } catch (Exception e) {
                Dlog.e("DATA SEND ERROR");
            }
        }
    }

    private void SendMsgAlert()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (apConnSocket.isConnected()) {
                    try {
                        apConnSocket.close();
                    } catch (IOException ioe) {

                    }
                }
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

        final String serverIp      = this.getApplication().getString(R.string.server_ip);
        final int    serverPort    = Integer.parseInt(this.getApplication().getString(R.string.server_port));
        final int    serverTimeout = Integer.parseInt(this.getApplication().getString(R.string.server_timeout));

        Dlog.i("SOCKET Connect start!!");

        SocketAddress socketAddress = new InetSocketAddress(serverIp, serverPort);

        apConnSocket = new Socket();

        try {
            //apConnSocket = new Socket(serverIp, serverPort);
            apConnSocket.setSoTimeout(serverTimeout);
            apConnSocket.connect(socketAddress, serverTimeout);

            sockWriter      = new BufferedWriter(new OutputStreamWriter(apConnSocket.getOutputStream(), "EUC-KR"));
            sockReader      = new BufferedReader(new InputStreamReader(apConnSocket.getInputStream()));
            sockPrintWriter = new PrintWriter(sockWriter, true);

            Dlog.i("SOCKET!! : " + apConnSocket + " " + apConnSocket.isConnected());

            sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", 0, 0, 0, 0, 5);
            sendServer(sendMsg);

        } catch (SocketException e) {
            Dlog.e("SOCKET!! : " + apConnSocket + " " + apConnSocket.isConnected());
            e.printStackTrace();
            apConnSocket = null;
        } catch (IOException e) {
            Dlog.e("SOCKET!! : " + apConnSocket + " " + apConnSocket.isConnected());
            e.printStackTrace();
            apConnSocket = null;
        }
    }

    private void socketDisconnect() {
        try {
            sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", 0, 0, 0, 0, 6);
            sendServer(sendMsg);

            if (apConnSocket != null) {
                sendMsg = ":EXIT:";
                sendServer(sendMsg);

                sockWriter.close();
                sockReader.close();
                sockPrintWriter.close();

                sockWriter = null;
                sockReader = null;
                sockPrintWriter = null;

                apConnSocket.close();
                apConnSocket = null;

                tbWifi.setChecked(false);
                tbWifi.setTextColor(Color.GREEN);
            }

            Dlog.i("SOCKET!! : " + apConnSocket);
        } catch (Exception e) {
            Dlog.e("SOCKET!! : " + apConnSocket);
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
     * get Debug mode
     *
     * @param context
     * @return
     */
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

        ch1 = (int) Commom.map((long) ( roll * 10), -400, 400, -400, 400);
        ch2 = (int) Commom.map((long) (pitch * 10), -400, 400, -400, 400) * (-1);

        Dlog.i(String.format(Locale.US, "PITCH : %d ROLL : %d", ch1, ch2));

        sendMsg = String.format(Locale.US, ":CH:%04d|%04d|%04d|%04d|%d", ch1, ch2, ch3, ch4, 9);
        sendServer(sendMsg);

        textView1.setText("CH1 : " + String.valueOf(ch1));
        textView2.setText("CH2 : " + String.valueOf(ch2));
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            try {
                sockPrintWriter.println(":CHK:");
            } catch (Exception e) {
                Dlog.e("DATA SEND ERROR");
                e.printStackTrace();
                socketDisconnect();
                mHandler.removeMessages(0);
            }

            if (apConnSocket != null && apConnSocket.isConnected()) {
                tbWifi.setChecked(true);
            } else {
                Dlog.i("SOCKET!! : DisConnected");
                tbWifi.setChecked(false);
                socketDisconnect();
            }

            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    public class ScreenActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                // TODO 화면꺼짐처리
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                // TODO 화면켜짐처리
                if (isAppBackGround == false) {
                    // TODO 화면켜짐처리
                }
            }
        }
    }
}

