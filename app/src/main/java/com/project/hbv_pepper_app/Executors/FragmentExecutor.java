package com.project.hbv_pepper_app.Executors;

import androidx.fragment.app.Fragment;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.project.hbv_pepper_app.Fragments.HBV_UNI.HBVUNIMainFragment;
import com.project.hbv_pepper_app.Fragments.HBV_UNI.HBVUNI_InformationFragment;
import com.project.hbv_pepper_app.Fragments.HBV_UNI.HBVUNI_Study_CounselingFragment;
import com.project.hbv_pepper_app.Fragments.HBV_UNI.HBVUNI_for_StudentsFragment;
import com.project.hbv_pepper_app.Fragments.MainFragment;
import com.project.hbv_pepper_app.Fragments.ScreenTwoFragment;
import com.project.hbv_pepper_app.Fragments.SplashFragment;
import com.project.hbv_pepper_app.MainActivity;

import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class FragmentExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";

    public FragmentExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        String fragmentName;
        String optionalData; //use this if you need to pass on data when setting the fragment.
        if (params == null || params.isEmpty()) return;
        else {
            fragmentName = params.get(0);
            if(params.size() == 2) optionalData = params.get(1);
        }
        Fragment fragment;
        Log.d(TAG,"fragmentName :" + fragmentName);
        switch (fragmentName){
                /* -----> MAIN MENU <----- ----- ----- */
            case ("frag_main"):
                fragment = new MainFragment();
                break;

                /* -----> USE-CASE: HBV UNI <----- ----- ----- */
            case ("frag_hbv_uni_main"):
                fragment = new HBVUNIMainFragment();
                break;
            case ("frag_hbv_uni_for_students"):
                fragment = new HBVUNI_for_StudentsFragment();
                break;
            case ("frag_hbv_uni_information"):
                fragment = new HBVUNI_InformationFragment();
                break;
            case ("frag_hbv_uni_study_counseling"):
                fragment = new HBVUNI_Study_CounselingFragment();
                break;
                /* -----> USE-CASE: ??? <----- ----- ----- */
            case ("frag_screen_two"):
                fragment = new ScreenTwoFragment();
                break;

                /* -----> OTHER <----- ----- ----- */
            case ("frag_splash_screen"):
                fragment = new SplashFragment();
                break;
            default:
                fragment = new MainFragment();
        }
        ma.setFragment(fragment);
    }

    @Override
    public void stop() {}
}