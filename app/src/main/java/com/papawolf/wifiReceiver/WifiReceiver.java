package com.papawolf.wifiReceiver;

import android.app.Application;

/**
 * Created by papawolf on 2017-08-30.
 */

public class WifiReceiver extends Application {
    // Shared Preferences
    private boolean settingReverseCh1;
    private boolean settingReverseCh2;
    private boolean settingReverseCh3;
    private boolean settingReverseCh4;

    private boolean settingAutoCenterCh1;
    private boolean settingAutoCenterCh2;
    private boolean settingAutoCenterCh3;
    private boolean settingAutoCenterCh4;

    private int     settingEpaCh1;
    private int     settingEpaCh2;
    private int     settingEpaCh3;
    private int     settingEpaCh4;

    private int     settingTrimCh1;
    private int     settingTrimCh2;
    private int     settingTrimCh3;
    private int     settingTrimCh4;

    public boolean isSettingReverseCh1() {
        return settingReverseCh1;
    }

    public void setSettingReverseCh1(boolean settingReverseCh1) {
        this.settingReverseCh1 = settingReverseCh1;
    }

    public boolean isSettingReverseCh2() {
        return settingReverseCh2;
    }

    public void setSettingReverseCh2(boolean settingReverseCh2) {
        this.settingReverseCh2 = settingReverseCh2;
    }

    public boolean isSettingReverseCh3() {
        return settingReverseCh3;
    }

    public void setSettingReverseCh3(boolean settingReverseCh3) {
        this.settingReverseCh3 = settingReverseCh3;
    }

    public boolean isSettingReverseCh4() {
        return settingReverseCh4;
    }

    public void setSettingReverseCh4(boolean settingReverseCh4) {
        this.settingReverseCh4 = settingReverseCh4;
    }

    public boolean isSettingAutoCenterCh1() {
        return settingAutoCenterCh1;
    }

    public void setSettingAutoCenterCh1(boolean settingAutoCenterCh1) {
        this.settingAutoCenterCh1 = settingAutoCenterCh1;
    }

    public boolean isSettingAutoCenterCh2() {
        return settingAutoCenterCh2;
    }

    public void setSettingAutoCenterCh2(boolean settingAutoCenterCh2) {
        this.settingAutoCenterCh2 = settingAutoCenterCh2;
    }

    public boolean isSettingAutoCenterCh3() {
        return settingAutoCenterCh3;
    }

    public void setSettingAutoCenterCh3(boolean settingAutoCenterCh3) {
        this.settingAutoCenterCh3 = settingAutoCenterCh3;
    }

    public boolean isSettingAutoCenterCh4() {
        return settingAutoCenterCh4;
    }

    public void setSettingAutoCenterCh4(boolean settingAutoCenterCh4) {
        this.settingAutoCenterCh4 = settingAutoCenterCh4;
    }

    public int getSettingEpaCh1() {
        return settingEpaCh1;
    }

    public void setSettingEpaCh1(int settingEpaCh1) {
        this.settingEpaCh1 = settingEpaCh1;
    }

    public int getSettingEpaCh2() {
        return settingEpaCh2;
    }

    public void setSettingEpaCh2(int settingEpaCh2) {
        this.settingEpaCh2 = settingEpaCh2;
    }

    public int getSettingEpaCh3() {
        return settingEpaCh3;
    }

    public void setSettingEpaCh3(int settingEpaCh3) {
        this.settingEpaCh3 = settingEpaCh3;
    }

    public int getSettingEpaCh4() {
        return settingEpaCh4;
    }

    public void setSettingEpaCh4(int settingEpaCh4) {
        this.settingEpaCh4 = settingEpaCh4;
    }

    public int getSettingTrimCh1() {
        return settingTrimCh1;
    }

    public void setSettingTrimCh1(int settingTrimCh1) {
        this.settingTrimCh1 = settingTrimCh1;
    }

    public int getSettingTrimCh2() {
        return settingTrimCh2;
    }

    public void setSettingTrimCh2(int settingTrimCh2) {
        this.settingTrimCh2 = settingTrimCh2;
    }

    public int getSettingTrimCh3() {
        return settingTrimCh3;
    }

    public void setSettingTrimCh3(int settingTrimCh3) {
        this.settingTrimCh3 = settingTrimCh3;
    }

    public int getSettingTrimCh4() {
        return settingTrimCh4;
    }

    public void setSettingTrimCh4(int settingTrimCh4) {
        this.settingTrimCh4 = settingTrimCh4;
    }
}
