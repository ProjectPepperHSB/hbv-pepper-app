package com.project.hbv_pepper_app.Utils;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.project.hbv_pepper_app.MainActivity;
import com.project.hbv_pepper_app.R;

/*
 * Template source of this file: https://github.com/softbankrobotics-labs/App-Template (March 2022)
 */
public class CountDownNoInteraction extends CountDownTimer {

    private String TAG = "MSI_NoInteraction";
    private Fragment fragment;
    private MainActivity mainActivity;

    public CountDownNoInteraction(MainActivity mainActivity, Fragment fragmentToSet, long millisUtilEnd, long countDownInterval) {
        super(millisUtilEnd, countDownInterval);
        this.fragment = fragmentToSet;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //Log.d(TAG,"Millis until end : " + millisUntilFinished);
    }

    @Override
    public void onFinish() {
        Log.d(TAG, "Timer Finished");
        //mainActivity.setFragment(fragment);
        try {
            mainActivity.runOnUiThread(() -> {
                mainActivity.setContentView(R.layout.selfie);
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void reset() {
        Log.d(TAG, "Timer Reset");
        super.cancel();
        super.start();
    }
}