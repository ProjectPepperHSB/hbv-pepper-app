package com.project.hbv_pepper_app.Executors;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.project.hbv_pepper_app.Fragments.ScreenTwoFragment;
import com.project.hbv_pepper_app.MainActivity;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.Lectures;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.TimeTable;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.TimeTableHandler;
import com.project.hbv_pepper_app.R;
import com.project.hbv_pepper_app.Utils.HelperCollection;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Time;
import java.util.List;
import java.util.Scanner;

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


                String htmlStr = "";



                List[] weekdays = {timeTable.Mo, timeTable.Di, timeTable.Mi, timeTable.Do, timeTable.Fr};
                String[] weekdaysStr = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag"};
                short weIdx = 0;


                //System.out.println(timeTable.Mo.get(0).getCourse());

                for ( List we : weekdays){

                    htmlStr += "<h3>" + weekdaysStr[weIdx++] + "</h3>";   //Extra Textview und dann untereinander
                    if(we.size() == 0){
                        System.out.println("Empty");
                    }else{
                        for(int i = 0; i < we.size(); ++i){
                            Lectures tmp = (Lectures) we.get(i);
                            htmlStr += "<p>" + tmp.getBegin() + " - " + tmp.getEnd()+"&emsp;" +
                                    tmp.getCourse()+"&emsp;" +
                                    tmp.getProf()+"</p>";
                        }
                    }
                }

                final String html = htmlStr;
                System.out.println("#################");
                System.out.println(html);
                ma.runOnUiThread(() -> {

                    TextView timetableId = (TextView) ma.findViewById(R.id.iTimetable);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        timetableId.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        timetableId.setText(Html.fromHtml(html));
                    }

                });





                break;
            case("timetable_detail"):
                break;
            case("qiVariableMensa"):
                String day = params.get(1);
                if(day.equals("Plan")){
                    //Download Img
                    try{
                        String url_str = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/mensadata/img";
                        InputStream srt = new URL(url_str).openStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(srt);

                        ma.runOnUiThread(() -> {
                            //setContentView(R.layout.mensa_layout);
                            ImageView imageView = (ImageView) ma.findViewById(R.id.iMensa2);
                            imageView.setImageBitmap(bitmap);
                            // change visibility if student said "hide" or so
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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
            case("qiVariableStudium"):
                String studiengang = params.get(1);
                System.out.println(studiengang);
                try {
                    ma.runOnUiThread(() -> {
                        //ma.setContentView(R.layout.Course_Info.BWL);

                    });

                    String answer = "Hallow asldnfg "+studiengang+" asfghfgdsgf";//HelperCollection.getOffer(day);
                    System.out.println(answer);
                    ma.getCurrentChatBot().setQiVariable(variableName, answer);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
        case("qiVariableNav"):
            String nav = params.get(1);
            if(nav.equals("Plan")) {
                try {

                    ma.runOnUiThread(() -> {
                        ma.setContentView(R.layout.campus_plan);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //....


            break;
            default:
                Log.d(TAG, "I don't know this variable");
        }
    }

    @Override
    public void stop() {

    }
}
