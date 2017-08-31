package com.papawolf.wifiReceiver;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingActivity extends Activity {

    MainActivity mainActivity = new MainActivity();
    WifiReceiver myApp = new WifiReceiver();

    private SeekBar mSeekBarEpaCh1, mSeekBarEpaCh2, mSeekBarEpaCh3, mSeekBarEpaCh4;
    private SeekBar mSeekBarTrimCh1, mSeekBarTrimCh2, mSeekBarTrimCh3, mSeekBarTrimCh4;
    private TextView mTextViewEpaCh1, mTextViewEpaCh2, mTextViewEpaCh3, mTextViewEpaCh4;
    private TextView mTextViewTrimCh1, mTextViewTrimCh2, mTextViewTrimCh3, mTextViewTrimCh4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_setting);

        // 세팅값 불러오기
        mainActivity.loadSetting(myApp);

        ListView listviewReverse, listviewAutoCenter;
        CustomChoiceListViewAdapter adapterReverse, adapterAutoCenter;

        // Adapter 생성
        adapterReverse    = new CustomChoiceListViewAdapter();
        adapterAutoCenter = new CustomChoiceListViewAdapter();

        // Reverse Channel Setting
        listviewReverse = (ListView) findViewById(R.id.listViewReverse);
        listviewReverse.setAdapter(adapterReverse);

        // 첫 번째 아이템 추가.
        adapterReverse.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_1), myApp.isSettingReverseCh1());
        // 두 번째 아이템 추가.
        adapterReverse.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_2), myApp.isSettingReverseCh2());
        // 세 번째 아이템 추가.
        adapterReverse.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_3), myApp.isSettingReverseCh3());
        // 세 번째 아이템 추가.
        adapterReverse.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_4), myApp.isSettingReverseCh4());

        Dlog.d("WifiReceiver " + myApp.isSettingReverseCh1());
        Dlog.d("WifiReceiver " + myApp.isSettingReverseCh2());
        Dlog.d("WifiReceiver " + myApp.isSettingReverseCh3());
        Dlog.d("WifiReceiver " + myApp.isSettingReverseCh4());

        listviewReverse.setItemChecked(0, myApp.isSettingReverseCh1());
        listviewReverse.setItemChecked(1, myApp.isSettingReverseCh2());
        listviewReverse.setItemChecked(2, myApp.isSettingReverseCh3());
        listviewReverse.setItemChecked(3, myApp.isSettingReverseCh4());

        listviewReverse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Dlog.d("Item Click position -> " + position + " id -> " + id + " ");

                CheckableLinearLayout cb = (CheckableLinearLayout) v;

                // 체크된 리버스 채널 저장
                switch (position) {
                    case 0:
                        myApp.setSettingReverseCh1(cb.isChecked());
                        break;
                    case 1:
                        myApp.setSettingReverseCh2(cb.isChecked());
                        break;
                    case 2:
                        myApp.setSettingReverseCh3(cb.isChecked());
                        break;
                    case 3:
                        myApp.setSettingReverseCh4(cb.isChecked());
                        break;
                }

                Dlog.d("WifiReceiver " + myApp.isSettingReverseCh1());
                Dlog.d("WifiReceiver " + myApp.isSettingReverseCh2());
                Dlog.d("WifiReceiver " + myApp.isSettingReverseCh3());
                Dlog.d("WifiReceiver " + myApp.isSettingReverseCh4());

                mainActivity.saveSetting(myApp);
            }
        });


        // AutoCenter Channel Setting
        listviewAutoCenter = (ListView) findViewById(R.id.listViewAutoCenter);
        listviewAutoCenter.setAdapter(adapterAutoCenter);

        // 첫 번째 아이템 추가.
        adapterAutoCenter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_1), myApp.isSettingAutoCenterCh1());
        // 두 번째 아이템 추가.
        adapterAutoCenter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_2), myApp.isSettingAutoCenterCh2());
        // 세 번째 아이템 추가.
        adapterAutoCenter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_3), myApp.isSettingAutoCenterCh3());
        // 세 번째 아이템 추가.
        adapterAutoCenter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_swap_horiz_black_18dp), getResources().getString(R.string.channel_4), myApp.isSettingAutoCenterCh4());

        Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh1());
        Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh2());
        Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh3());
        Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh4());

        listviewAutoCenter.setItemChecked(0, myApp.isSettingAutoCenterCh1());
        listviewAutoCenter.setItemChecked(1, myApp.isSettingAutoCenterCh2());
        listviewAutoCenter.setItemChecked(2, myApp.isSettingAutoCenterCh3());
        listviewAutoCenter.setItemChecked(3, myApp.isSettingAutoCenterCh4());

        listviewAutoCenter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Dlog.d("Item Click position -> " + position + " id -> " + id + " ");

                CheckableLinearLayout cb = (CheckableLinearLayout) v;

                // 체크된 리버스 채널 저장
                switch (position) {
                    case 0:
                        myApp.setSettingAutoCenterCh1(cb.isChecked());
                        break;
                    case 1:
                        myApp.setSettingAutoCenterCh2(cb.isChecked());
                        break;
                    case 2:
                        myApp.setSettingAutoCenterCh3(cb.isChecked());
                        break;
                    case 3:
                        myApp.setSettingAutoCenterCh4(cb.isChecked());
                        break;
                }

                Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh1());
                Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh2());
                Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh3());
                Dlog.d("WifiReceiver " + myApp.isSettingAutoCenterCh4());

                mainActivity.saveSetting(myApp);
            }
        });


        // EPA Channel Setting
        mSeekBarEpaCh1 = (SeekBar) findViewById(R.id.seekBarEpaCh1);
        mSeekBarEpaCh2 = (SeekBar) findViewById(R.id.seekBarEpaCh2);
        mSeekBarEpaCh3 = (SeekBar) findViewById(R.id.seekBarEpaCh3);
        mSeekBarEpaCh4 = (SeekBar) findViewById(R.id.seekBarEpaCh4);

        mTextViewEpaCh1 = (TextView) findViewById(R.id.txtEpaCh1);
        mTextViewEpaCh2 = (TextView) findViewById(R.id.txtEpaCh2);
        mTextViewEpaCh3 = (TextView) findViewById(R.id.txtEpaCh3);
        mTextViewEpaCh4 = (TextView) findViewById(R.id.txtEpaCh4);

        // EPA설정값 가져와서 화면에 적용
        mSeekBarEpaCh1.setProgress(myApp.getSettingEpaCh1());
        mSeekBarEpaCh2.setProgress(myApp.getSettingEpaCh2());
        mSeekBarEpaCh3.setProgress(myApp.getSettingEpaCh3());
        mSeekBarEpaCh4.setProgress(myApp.getSettingEpaCh4());

        mTextViewEpaCh1.setText("" + myApp.getSettingEpaCh1());
        mTextViewEpaCh2.setText("" + myApp.getSettingEpaCh2());
        mTextViewEpaCh3.setText("" + myApp.getSettingEpaCh3());
        mTextViewEpaCh4.setText("" + myApp.getSettingEpaCh4());

        // Set a SeekBar change listener
        mSeekBarEpaCh1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewEpaCh1.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingEpaCh1(seekBar.getProgress());
                mainActivity.saveSetting(myApp);
            }
        });

        // Set a SeekBar change listener
        mSeekBarEpaCh2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewEpaCh2.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingEpaCh2(seekBar.getProgress());
                mainActivity.saveSetting(myApp);
            }
        });

        // Set a SeekBar change listener
        mSeekBarEpaCh3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewEpaCh3.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingEpaCh3(seekBar.getProgress());
                mainActivity.saveSetting(myApp);
            }
        });

        // Set a SeekBar change listener
        mSeekBarEpaCh4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewEpaCh4.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingEpaCh4(seekBar.getProgress());
                mainActivity.saveSetting(myApp);
            }
        });


        // Trim Channel Setting
        mSeekBarTrimCh1 = (SeekBar) findViewById(R.id.seekBarTrimCh1);
        mSeekBarTrimCh2 = (SeekBar) findViewById(R.id.seekBarTrimCh2);
        mSeekBarTrimCh3 = (SeekBar) findViewById(R.id.seekBarTrimCh3);
        mSeekBarTrimCh4 = (SeekBar) findViewById(R.id.seekBarTrimCh4);

        mTextViewTrimCh1 = (TextView) findViewById(R.id.txtTrimCh1);
        mTextViewTrimCh2 = (TextView) findViewById(R.id.txtTrimCh2);
        mTextViewTrimCh3 = (TextView) findViewById(R.id.txtTrimCh3);
        mTextViewTrimCh4 = (TextView) findViewById(R.id.txtTrimCh4);

        // EPA설정값 가져와서 화면에 적용
        mSeekBarTrimCh1.setProgress(myApp.getSettingTrimCh1() + 100);
        mSeekBarTrimCh2.setProgress(myApp.getSettingTrimCh2() + 100);
        mSeekBarTrimCh3.setProgress(myApp.getSettingTrimCh3() + 100);
        mSeekBarTrimCh4.setProgress(myApp.getSettingTrimCh4() + 100);

        mTextViewTrimCh1.setText("" + myApp.getSettingTrimCh1());
        mTextViewTrimCh2.setText("" + myApp.getSettingTrimCh2());
        mTextViewTrimCh3.setText("" + myApp.getSettingTrimCh3());
        mTextViewTrimCh4.setText("" + myApp.getSettingTrimCh4());

        // Set a SeekBar change listener
        mSeekBarTrimCh1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewTrimCh1.setText("" + (i - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingTrimCh1(Integer.parseInt(mTextViewTrimCh1.getText().toString()));
                mainActivity.saveSetting(myApp);
            }
        });

        // Set a SeekBar change listener
        mSeekBarTrimCh2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewTrimCh2.setText("" + (i - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingTrimCh2(Integer.parseInt(mTextViewTrimCh2.getText().toString()));
                mainActivity.saveSetting(myApp);
            }
        });

        // Set a SeekBar change listener
        mSeekBarTrimCh3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewTrimCh3.setText("" + (i - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingTrimCh3(Integer.parseInt(mTextViewTrimCh3.getText().toString()));
                mainActivity.saveSetting(myApp);
            }
        });

        // Set a SeekBar change listener
        mSeekBarTrimCh4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Display the current progress of SeekBar
                mTextViewTrimCh4.setText("" + (i - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myApp.setSettingTrimCh4(Integer.parseInt(mTextViewTrimCh4.getText().toString()));
                mainActivity.saveSetting(myApp);
            }
        });
    }
}
