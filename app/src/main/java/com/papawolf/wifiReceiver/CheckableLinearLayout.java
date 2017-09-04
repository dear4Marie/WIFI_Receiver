package com.papawolf.wifiReceiver;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.Switch;

class CheckableLinearLayout extends LinearLayout implements Checkable {
// 만약 CheckBox가 아닌 View를 추가한다면 아래의 변수 사용 가능.
    // private boolean mIsChecked ;

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        // mIsChecked = false ;
    }

    @Override
    public boolean isChecked() {
        Switch sw = (Switch) findViewById(R.id.switch1);
        return sw.isChecked() ;
        // return mIsChecked ;
    }

    @Override
    public void toggle() {
        Switch sw = (Switch) findViewById(R.id.switch1);

        setChecked(sw.isChecked() ? false : true) ;
        // setChecked(mIsChecked ? false : true) ;
    }

    @Override
    public void setChecked(boolean checked) {
        Switch sw = (Switch) findViewById(R.id.switch1);

        if (sw.isChecked() != checked) {
            sw.setChecked(checked) ;
        }

        // CheckBox 가 아닌 View의 상태 변경.
    }
}
