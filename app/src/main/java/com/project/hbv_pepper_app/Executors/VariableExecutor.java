package com.project.hbv_pepper_app.Executors;


import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.project.hbv_pepper_app.Fragments.ScreenTwoFragment;
import com.project.hbv_pepper_app.MainActivity;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.TimeTable;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.TimeTableHandler;
import com.project.hbv_pepper_app.R;
import com.project.hbv_pepper_app.Utils.HelperCollection;

import java.util.List;

/**
 * VariableExecutor is used when a qiVariable is modified in the qiChat and
 * you want to have feedback on the tablet
 * Triggered in qiChat as follow : ^execute( VariableExecutor, variableName, variableValue)
 */

public class VariableExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private final String TAG = "VariableExecutor";

    public VariableExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        String variableName;
        String variableValue;
        if (params == null || params.isEmpty()) {
            return;
        }else{
            variableName = params.get(0);
            if(params.size() < 2)  {Log.d(TAG, "no value specified for variable : " + variableName); return;}
            else variableValue = params.get(1);
        }
        Log.d(TAG,"variableName: " + variableName);
        switch (variableName){
            case ("qiVariable"):
                ScreenTwoFragment fragmentTwo  = (ScreenTwoFragment) ma.getFragment();
                fragmentTwo.setTextQiVariableValue(variableValue);
                break;
            case("qiVariablePrice"):
                String coin = params.get(1);
                Log.i(TAG,"Looking for price of " + params.get(1));

                if (coin.equals("bitcoin")) coin = "btc";
                else if (coin.equals("ethereum")) coin = "eth";
                else coin = "sol";

                try {
                    String price = HelperCollection.getPrice(coin + "-USDT");
                    Log.i(TAG, "price of " + coin + " is " + price);
                    ma.getCurrentChatBot().setQiVariable(variableName, price);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            case("timetable"):
                String course = params.get(1);
                String semester = params.get(2);

                Log.i("------>","Looking for course " + course + " in semester " + semester);

                TimeTableHandler tth = new TimeTableHandler("WI"/*course*/, semester);
                TimeTable timeTable = tth.getTimeTable();
                /*
                Log.i("TIMETBALE: ", timeTable.toString());

                for(int i = 0; i < timeTable.Mo.size(); i++){
                    Log.i(String.valueOf(i), timeTable.Mo.get(i).getCourse());
                }
                */

                /*
                ma.runOnUiThread(() -> {
                    final WebView webView = (WebView) ma.findViewById(R.id.webview);
                    WebSettings settings = webView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);
                    settings.setAllowContentAccess(true);
                    settings.setAllowFileAccessFromFileURLs(true);
                    settings.setAllowUniversalAccessFromFileURLs(true);
                    webView.loadData(tth.getHtmlPage(), "text/html; charset=utf-8", "UTF-8");
                    webView.setVisibility(View.VISIBLE);
                });
                
                 */


                break;
            case("timetable_detail"):
                break;
            case("qiVariableMensa"):
                String day = params.get(1);
                if(day.equals("Plan")){
                    System.out.println("Show Plan");
                    ma.setMensaImageView();
                    System.out.println("Show Plan End");
                } else{
                    try {
                        String offer = HelperCollection.getOffer(day);
                        System.out.println(offer);
                        ma.getCurrentChatBot().setQiVariable(variableName, offer);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                break;
            default:
                Log.d(TAG, "I don't know this variable");
        }
    }

    @Override
    public void stop() {

    }
}
