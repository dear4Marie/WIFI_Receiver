package com.papawolf.wifiReceiver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
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
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import static android.os.StrictMode.setThreadPolicy;
import static com.papawolf.wifiReceiver.R.drawable.image_button;
import static com.papawolf.wifiReceiver.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = false;
    Handler mHandler = null;

    RelativeLayout layout_joystick1;
    RelativeLayout layout_joystick2;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7;

    JoyStickClass js1;
    JoyStickClass js2;

    private Socket apConnSocket = null;
    private BufferedReader sockReader;
    private BufferedWriter sockWriter;
    private PrintWriter sockPrintWriter;
    SocketThread thrSockConn;
    String userPhone;
   private  SocketAddress remoteAddr;

    // global channel position
    int ch1 = 0;
    int ch2 = 0;
    int ch3 = 0;
    int ch4 = 0;

    final String ipaddr = "192.168.0.1";
    final int ipport = 80;
    String sendMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        setThreadPolicy(policy);

        // 디버그모드에 따라서 로그를 남기거나 남기지 않는다
        this.DEBUG = isDebuggable(this);

        thrSockConn = new SocketThread();
        //thrSockConn.setDaemon(true);
        thrSockConn.start();

        if (apConnSocket != null && apConnSocket.isConnected())
        {
            Toast.makeText(getApplicationContext(), "서버 연결 성공", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show();
        }

        // WIFI 버튼으로 WIFI 처리
        final ToggleButton tbWifi = (ToggleButton) this.findViewById(R.id.toggleButtonWifi);

        tbWifi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (tbWifi.isChecked()) {
                    try {
                        apConnSocket = new Socket(ipaddr, ipport);
                        System.out.println("SOCKET!! : " + apConnSocket);
                    }
                    catch (UnknownHostException ue) {
                        System.out.println(ue);
                        ue.printStackTrace();
                        Dlog.d("SOCKET!! : " + apConnSocket);
                    } catch (IOException ie) {
                        System.out.println(ie);
                        ie.printStackTrace();
                        Dlog.d("SOCKET!! : " + apConnSocket);
                    }

                    sendMsg = "RECEIVER CONNECTED";
                    sendServer(sendMsg);
                    Toast.makeText(getApplicationContext(), sendMsg + " 전송", Toast.LENGTH_SHORT).show();

                    tbWifi.setTextColor(Color.GREEN);
                }
                else {
                    try {
                        apConnSocket.close();
                        Dlog.d("SOCKET!! : " + apConnSocket);
                    }
                    catch (UnknownHostException ue) {
                        System.out.println(ue);
                        ue.printStackTrace();
                        Dlog.d("SOCKET!! : " + apConnSocket);
                    } catch (IOException ie) {
                        System.out.println(ie);
                        ie.printStackTrace();
                        Dlog.d("SOCKET!! : " + apConnSocket);
                    }

                    tbWifi.setTextColor(Color.RED);
                }
            }

        });

        mHandler = new Handler();

        Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
                // UI 작업 수행 X
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        if (apConnSocket != null && apConnSocket.isConnected())
                        {
                            tbWifi.setChecked(true);
                        }
                        else
                        {
                            tbWifi.setChecked(false);
                        }
                    }
                });
            }
        });
        t.start();

        // 세팅버튼을 누르면 세팅화면으로
//        Button btSetting = (Button) findViewById(R.id.btSetting);
//        btSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick (View v) {
//                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
//                startActivity(intent);
//            }
//        });

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);

        layout_joystick1 = (RelativeLayout)findViewById(R.id.layout_joystick1);
        layout_joystick2 = (RelativeLayout)findViewById(R.id.layout_joystick2);

        js1 = new JoyStickClass(getApplicationContext(), layout_joystick1, image_button);
        js1.setStickSize(150, 150);
        js1.setLayoutSize(500, 500);
        js1.setLayoutAlpha(150);
        js1.setStickAlpha(100);
        js1.setOffset(90);
        js1.setMinimumDistance(20);
        js1.setMaximumDistance(200);

        layout_joystick1.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js1.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
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


                    //textView3.setText("Angle : " + String.valueOf(js1.getAngle()));
                    //textView4.setText("Distance : " + String.valueOf(js1.getDistance()));

//                    int direction = js1.get8Direction();
//                    if(direction == JoyStickClass.STICK_UP) {
//                        textView5.setText("Direction : Up");
//                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
//                        textView5.setText("Direction : Up Right");
//                    } else if(direction == JoyStickClass.STICK_RIGHT) {
//                        textView5.setText("Direction : Right");
//                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
//                        textView5.setText("Direction : Down Right");
//                    } else if(direction == JoyStickClass.STICK_DOWN) {
//                        textView5.setText("Direction : Down");
//                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
//                        textView5.setText("Direction : Down Left");
//                    } else if(direction == JoyStickClass.STICK_LEFT) {
//                        textView5.setText("Direction : Left");
//                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
//                        textView5.setText("Direction : Up Left");
//                    } else if(direction == JoyStickClass.STICK_NONE) {
//                        textView5.setText("Direction : Center");
//                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    ch1 = 0;
                    ch2 = 0;
                    //textView3.setText("Angle :");
                    //textView4.setText("Distance :");
                    //textView5.setText("Direction :");
                }

                textView1.setText("CH1 : " + String.valueOf(ch1));
                textView2.setText("CH2 : " + String.valueOf(ch2));

                sendMsg = ch1 + "|" + ch2 + "|" + ch3 + "|" + ch4;
                sendServer(sendMsg);

                Dlog.d(sendMsg);

                return true;
            }
        });

        js2 = new JoyStickClass(getApplicationContext(), layout_joystick2, image_button);
        js2.setStickSize(150, 150);
        js2.setLayoutSize(500, 500);
        js2.setLayoutAlpha(150);
        js2.setStickAlpha(100);
        js2.setOffset(90);
        js2.setMinimumDistance(20);
        js2.setMaximumDistance(200);

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

                sendMsg = ch1 + "|" + ch2 + "|" + ch3 + "|" + ch4;
                sendServer(sendMsg);

                Dlog.d(sendMsg);

                textView3.setText("CH3 : " + String.valueOf(ch3));
                textView4.setText("CH4 : " + String.valueOf(ch4));

                return true;
            }
        });
    }

    private void sendServer(String msg) {
        if (apConnSocket.isConnected())
            sockPrintWriter.println(msg);
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

    class SocketThread extends Thread {
        @Override
        public void run() {

            try {
                apConnSocket = new Socket(ipaddr, ipport);

                sockWriter = new BufferedWriter(new OutputStreamWriter(apConnSocket.getOutputStream(), "EUC-KR"));
                sockReader = new BufferedReader(new InputStreamReader(apConnSocket.getInputStream()));
                sockPrintWriter = new PrintWriter(sockWriter, true);

                Dlog.d("SOCKET!! : " + apConnSocket);
            } catch (UnknownHostException ue) {
                Dlog.d("SOCKET!! : " + apConnSocket);
                System.out.println(ue);
                ue.printStackTrace();
            } catch (IOException ie) {
                Dlog.d("SOCKET!! : " + apConnSocket);
                System.out.println(ie);
                ie.printStackTrace();
            }
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
}

