package com.bschwertfeger.pepperappv3;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.QiContext;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // The robot focus is gained.
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
    }
    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }
}