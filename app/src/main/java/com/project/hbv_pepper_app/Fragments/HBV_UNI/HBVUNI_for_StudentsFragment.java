package com.project.hbv_pepper_app.Fragments.HBV_UNI;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.hbv_pepper_app.Fragments.MainFragment;
import com.project.hbv_pepper_app.MainActivity;
import com.project.hbv_pepper_app.R;

public class HBVUNI_for_StudentsFragment extends Fragment {
    private static final String TAG = "HBV_for_Students_Fragment";
    private MainActivity ma;

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_hbv_for_students;
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(fragmentId, container, false);
            } else {
                return inflater.inflate(fragmentId, container, false);
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        /*
        view.findViewById(R.id.button_stundenplan).setOnClickListener((v) -> do something);

        view.findViewById(R.id.button_about_whatever).setOnClickListener((v) -> do something);
        */

        // DAS HIER GEHT NOCH NICHT WEGEN MAIN THREAD
        view.findViewById(R.id.button_mensaplan).setOnClickListener((v) -> ma.setMensaImageView());


        // BACK 2 MAIN MENU
        view.findViewById(R.id.button_main_menu).setOnClickListener((v) ->
                ma.setFragment(new MainFragment()));
    }
}