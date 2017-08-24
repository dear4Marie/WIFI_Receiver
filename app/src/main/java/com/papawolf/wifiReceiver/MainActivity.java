package com.papawolf.wifiReceiver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
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

import static android.os.StrictMode.setThreadPolicy;
import static com.papawolf.wifiReceiver.R.drawable.image_button;
import static com.papawolf.wifiReceiver.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = false;

    Handler mHandler = null;

    RelativeLayout layout_joystick1;
    RelativeLayout layout_joystick2;
    TextView textView1, textView2, textView3, textView4;

    JoyStickClass js1;
    JoyStickClass js2;

    private Socket         apConnSocket = null;
    private BufferedReader sockReader;
    private BufferedWriter sockWriter;
    private PrintWriter    sockPrintWriter;

    // Global channel position
    int ch1 = 0;
    int ch2 = 0;
    int ch3 = 0;
    int ch4 = 0;

    String sendMsg;

    //Using the Accelometer & Gyroscoper
    SensorManager mSensorManager = null;

    //Using the Gyroscope
    SensorEventListener mGyroLis;
    Sensor mGgyroSensor = null;

    //Roll and Pitch
    double pitch;
    double roll;
    double yaw;

    //timestamp and dt
    double timestamp;
    double dt;

    // for radian -> dgree
    double RAD2DGR = 180 / Math.PI;
    final float NS2S = 1.0f/1000000000.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        setThreadPolicy(policy);

        // 디버그모드에 따라서 로그를 남기거나 남기지 않는다
        this.DEBUG = isDebuggable(this);

        // WIFI 버튼으로 WIFI 처리
        final ToggleButton tbWifi = (ToggleButton) this.findViewById(R.id.toggleButtonWifi);

        tbWifi.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // WIFI 연결시
                if (tbWifi.isChecked()) {
                    try {
                        socketConnect();
                    }
                    catch (Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                        Dlog.d("SOCKET!! : " + e);
                    }

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
                    }
                    catch (Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                        Dlog.d("SOCKET!! : " + e);
                    }

                    tbWifi.setTextColor(Color.RED);
                }
            }

        });

        // WIFI 접속 상태 UI반영
        mHandler = new Handler();

        Thread threadWifiCheck = new Thread (new Runnable() {
            @Override
            public void run() {
                // UI 작업 수행 X
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (apConnSocket != null && apConnSocket.isConnected())
                        {
                            Dlog.i("SOCKET!! : " + apConnSocket);
                            tbWifi.setChecked(true);
                        }
                        else
                        {
                            Dlog.i("SOCKET!! : " + apConnSocket);
                            tbWifi.setChecked(false);
                        }
                    }
                });
            }
        });

        threadWifiCheck.start();

        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mGgyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGyroLis = new GyroscopeListener();

        // GYTO 버튼으로 GyroScope 처리
        final ToggleButton tbGyro = (ToggleButton) this.findViewById(R.id.toggleButtonGyro);

        tbGyro.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // GyroScope 연결시
                if (tbGyro.isChecked()) {

                    Dlog.d("GyroscopeListener on");

                    dt = 0.0;
                    timestamp = 0.0;

                    try {
                        mSensorManager.registerListener(mGyroLis, mGgyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
                        tbGyro.setTextColor(Color.GREEN);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { // GyroScope 종료시

                    Dlog.d("GyroscopeListener off");

                    try {
                        mSensorManager.unregisterListener(mGyroLis);
                        tbGyro.setTextColor(Color.RED);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        });

        // 세팅버튼을 누르면 세팅화면으로
//        Button btSetting = (Button) findViewById(R.id.btSetting);
//        btSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick (View v) {
//                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
//                startActivity(intent);
//            }
//        });

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
        js1.setMaximumDistance(300);



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

                textView1.setText("CH1 : " + String.valueOf(ch1));
                textView2.setText("CH2 : " + String.valueOf(ch2));

                sendMsg = ":CH:" + ch1 + "|" + ch2 + "|" + ch3 + "|" + ch4;
                sendServer(sendMsg);

                //Dlog.d(sendMsg);

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
        js2.setMaximumDistance(300);

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

                sendMsg = ":CH:" + ch1 + "|" + ch2 + "|" + ch3 + "|" + ch4;
                sendServer(sendMsg);

                //Dlog.d(sendMsg);

                textView3.setText("CH3 : " + String.valueOf(ch3));
                textView4.setText("CH4 : " + String.valueOf(ch4));

                return true;
            }
        });
    }

    // 소켓전송
    public void sendServer(String msg) {
        if (apConnSocket != null && apConnSocket.isConnected()) {

            try {
                sockPrintWriter.println(msg);
                Dlog.i(sendMsg);
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
                if(apConnSocket.isConnected()) {
                    try {
                        apConnSocket.close();
                    }catch(IOException ioe)
                    {

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

        Dlog.d("SOCKET Connect start!!");

        SocketAddress socketAddress = new InetSocketAddress(serverIp, serverPort);

        apConnSocket = new Socket();

        try {
            apConnSocket.setSoTimeout(serverTimeout);
            apConnSocket.connect(socketAddress, serverTimeout);

            sockWriter      = new BufferedWriter(new OutputStreamWriter(apConnSocket.getOutputStream(), "EUC-KR"));
            sockReader      = new BufferedReader(new InputStreamReader(apConnSocket.getInputStream()));
            sockPrintWriter = new PrintWriter(sockWriter, true);

            Dlog.d("SOCKET!! : " + apConnSocket);
        } catch (SocketException e) {
            e.printStackTrace();
            apConnSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
            apConnSocket = null;
        } finally {
            try {
                apConnSocket.close();
                apConnSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
                apConnSocket = null;
            }
        }
    }

    private void socketDisconnect() {
        try {
            if (apConnSocket != null) {
                sendMsg = ":EXIT:";
                sendServer(sendMsg);
                apConnSocket.close();
                apConnSocket = null;
            }
            Dlog.d("SOCKET!! : " + apConnSocket);
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

    public class GyroscopeListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

        /* 각 축의 각속도 성분을 받는다. */
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

        /* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(dt)을 구한다.
         * dt : 센서가 현재 상태를 감지하는 시간 간격
         * NS2S : nano second -> second */
            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

        /* 맨 센서 인식을 활성화 하여 처음 timestamp가 0일때는 dt값이 올바르지 않으므로 넘어간다. */
            if (dt - timestamp * NS2S != 0) {

            /* 각속도 성분을 적분 -> 회전각(pitch, roll)으로 변환.
             * 여기까지의 pitch, roll의 단위는 '라디안'이다.
             * SO 아래 로그 출력부분에서 멤버변수 'RAD2DGR'를 곱해주어 degree로 변환해줌.  */
                pitch = pitch + gyroY * dt;
                roll  = roll  + gyroX * dt;
                yaw   = yaw   + gyroZ * dt;

                ch1 = (int) (pitch * RAD2DGR);
                ch2 = (int) (roll  * RAD2DGR);

                textView1.setText("CH1 : " + String.valueOf(ch1));
                textView2.setText("CH2 : " + String.valueOf(ch2));

            Dlog.d("GYROSCOPE     [X]    : " + String.format("%.4f", event.values[0])
                    + "           [Y]    : " + String.format("%.4f", event.values[1])
                    + "           [dt]   : " + String.format("%.4f", dt));
//            Dlog.e("GYROSCOPE     [X]    : " + String.format("%.4f", event.values[0])
//                    + "           [Y]    : " + String.format("%.4f", event.values[1])
//                    + "           [Z]    : " + String.format("%.4f", event.values[2])
//                    + "           [Pitch]: " + String.format("%.1f", pitch * RAD2DGR)
//                    + "           [Roll] : " + String.format("%.1f", roll * RAD2DGR)
//                    + "           [Yaw]  : " + String.format("%.1f", yaw * RAD2DGR)
//                    + "           [dt]   : " + String.format("%.4f", dt));

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}

