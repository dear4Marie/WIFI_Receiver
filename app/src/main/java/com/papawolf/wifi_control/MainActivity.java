package com.papawolf.wifi_control;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import static com.papawolf.wifi_control.R.drawable.image_button;
import static com.papawolf.wifi_control.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    RelativeLayout layout_joystick1;
    RelativeLayout layout_joystick2;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7;

    JoyStickClass js1;
    JoyStickClass js2;

    // global channel position
    int ch1 = 0;
    int ch2 = 0;
    int ch3 = 0;
    int ch4 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        final ToggleButton tbWifi = (ToggleButton) this.findViewById(R.id.toggleButtonWifi);

        tbWifi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (tbWifi.isChecked()) {
                    tbWifi.setTextColor(Color.GREEN);
                }
                else {
                    tbWifi.setTextColor(Color.RED);
                }
            }
        });

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

        layout_joystick1.setOnTouchListener(new OnTouchListener() {
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

                    textView1.setText("CH1 : " + String.valueOf(ch1));
                    textView2.setText("CH2 : " + String.valueOf(ch2));
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
                    textView1.setText("CH1 :");
                    textView2.setText("CH2 :");
                    //textView3.setText("Angle :");
                    //textView4.setText("Distance :");
                    //textView5.setText("Direction :");
                }
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

        layout_joystick2.setOnTouchListener(new OnTouchListener() {
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

                    textView3.setText("CH3 : " + String.valueOf(ch3));
                    textView4.setText("CH4 : " + String.valueOf(ch4));
                }
                else if(arg3.getAction() == MotionEvent.ACTION_UP) {
                    textView3.setText("CH3 :");
                    textView4.setText("CH4 :");
                }

                return true;
            }
        });
    }
}

