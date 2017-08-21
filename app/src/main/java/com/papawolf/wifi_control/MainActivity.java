package com.papawolf.wifi_control;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.papawolf.wifi_control.R.drawable.image_button;
import static com.papawolf.wifi_control.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    RelativeLayout layout_joystick1;
    RelativeLayout layout_joystick2;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7;

    JoyStickClass js1;
    JoyStickClass js2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView5 = (TextView)findViewById(R.id.textView5);
        textView6 = (TextView)findViewById(R.id.textView6);
        textView7 = (TextView)findViewById(R.id.textView7);

        layout_joystick1 = (RelativeLayout)findViewById(R.id.layout_joystick1);

        layout_joystick2 = (RelativeLayout)findViewById(R.id.layout_joystick2);

        js1 = new JoyStickClass(getApplicationContext()
                , layout_joystick1, image_button);
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
                    textView1.setText("X : " + String.valueOf(js1.getX()));
                    textView2.setText("Y : " + String.valueOf(js1.getY()));
                    textView3.setText("Angle : " + String.valueOf(js1.getAngle()));
                    textView4.setText("Distance : " + String.valueOf(js1.getDistance()));

                    int direction = js1.get8Direction();
                    if(direction == JoyStickClass.STICK_UP) {
                        textView5.setText("Direction : Up");
                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        textView5.setText("Direction : Up Right");
                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        textView5.setText("Direction : Right");
                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                        textView5.setText("Direction : Down Right");
                    } else if(direction == JoyStickClass.STICK_DOWN) {
                        textView5.setText("Direction : Down");
                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                        textView5.setText("Direction : Down Left");
                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        textView5.setText("Direction : Left");
                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        textView5.setText("Direction : Up Left");
                    } else if(direction == JoyStickClass.STICK_NONE) {
                        textView5.setText("Direction : Center");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView1.setText("X :");
                    textView2.setText("Y :");
                    textView3.setText("Angle :");
                    textView4.setText("Distance :");
                    textView5.setText("Direction :");
                }
                return true;
            }
        });

        js2 = new JoyStickClass(getApplicationContext()
                , layout_joystick2, image_button);
        js2.setStickSize(150, 150);
        js2.setLayoutSize(500, 500);
        js2.setLayoutAlpha(150);
        js2.setStickAlpha(100);
        js2.setOffset(90);
        js2.setMinimumDistance(20);
        js2.setMaximumDistance(200);

        layout_joystick2.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js2.drawStick(arg1);

                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    textView6.setText("X : " + String.valueOf(js2.getX()));
                    textView7.setText("Y : " + String.valueOf(js2.getY()));
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView6.setText("X :");
                    textView7.setText("Y :");
                }

                return true;
            }
        });
    }
}

