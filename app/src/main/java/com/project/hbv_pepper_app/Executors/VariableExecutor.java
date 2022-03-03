package com.project.hbv_pepper_app.Executors;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.VideoView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.google.gson.Gson;
import com.project.hbv_pepper_app.Fragments.ScreenTwoFragment;
import com.project.hbv_pepper_app.MainActivity;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.Lectures;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.TimeTable;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.TimeTableHandler;
import com.project.hbv_pepper_app.R;
import com.project.hbv_pepper_app.Utils.HelperCollection;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
*/

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


        for(int i = 0;i<ma.varNames.length; ++i){   //##SENDTOSERVER##
            if(variableName.equals(ma.varNames)){
                //Erhöhe den Counter beim Nodeserver
                try {
                    HttpURLConnection con = HelperCollection.getConnection(
                            "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/api/v1/saveUseCaseData"
                                    + "&identifier=" + ma.uuidHash
                                    + "&use_case=" + variableName
                    );
                    int responseCode = con.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        switch (variableName) {
            case ("qiVariable"):
                ScreenTwoFragment fragmentTwo = (ScreenTwoFragment) ma.getFragment();
                fragmentTwo.setTextQiVariableValue(variableValue);
                break;
            case ("qiVariablePrice"):
                String coin = params.get(1);
                Log.i(TAG, "Looking for price of " + params.get(1));

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
                /*
            case ("timetable_course"):
                String course = params.get(1);
                TimeTableHandler tth = new TimeTableHandler("WI", "1");
                TimeTable timeTable = tth.getTimeTable();
                break;
                */
            case ("timetable_course_semester"):
                String course_ = params.get(1);
                String semester_ = params.get(2);

                Log.i("------>","Looking for course " + course_ + " in semester " + semester_);

                //Send Post data
                ma.activePerson.setSemester(semester_);
                ma.activePerson.setCourse(course_);

                try {
                    URL url = new URL("https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/api/v1/saveAttributeData");

                    JSONObject jdata = new JSONObject();
                    jdata.put("course", course_);
                    jdata.put("semester", semester_);

                    Map<String, Object> params2 = new LinkedHashMap<>();
                    params2.put("identifier", ma.uuidHash.toString());
                    params2.put("data", jdata);

                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String, Object> param : params2.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postDataBytes);

                    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                    for (int c; (c = in.read()) >= 0; ){
                        System.out.print((char) c);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("Post Data sended");

                final String url = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/api/v1/timetable?course="
                        + course_ + "&semester="
                        + semester_ + "&htmlOnly=true";

                System.out.println(url);

                try{
                    ma.runOnUiThread(() -> {
                        ma.setContentView(R.layout.webtest);

                        WebView web = (WebView) ma.findViewById(R.id.webView);
                        WebSettings webSettings = web.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        web.setWebViewClient(new Callback());
                        web.loadUrl(url);
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case("timetable_detail"):
                break;
            case("qiVariableMensa"):
                String day = params.get(1);
                if(day.equals("Plan")){
                    //Download Img
                    try{
                        String url_str = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/api/v1/mensadata/img";
                        InputStream srt = new URL(url_str).openStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(srt);

                        ma.runOnUiThread(() -> {
                            ma.setContentView(R.layout.mensa_layout);
                            ImageView imageView = (ImageView) ma.findViewById(R.id.iMensa);
                            imageView.setImageBitmap(bitmap);
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        try{
                            ma.runOnUiThread(() -> {
                                ma.setContentView(R.layout.mensa_layout);
                            });
                        }catch (Exception e2) {
                            e2.printStackTrace();
                        }
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

                    String answer = "Studiengang: "+studiengang;
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
                }else if(nav.equals("KzuMensa")) {
                    //Hier die richtige URL rein packen

                    /*
                    try{
                        ma.runOnUiThread(() -> {
                            ma.setContentView(R.layout.webtest);

                            WebView web = (WebView) ma.findViewById(R.id.webView);
                            WebSettings webSettings = web.getSettings();
                            webSettings.setJavaScriptEnabled(true);
                            web.setWebViewClient(new Callback());
                            web.loadUrl("https://drive.google.com/file/d/15vyETsIkbsxw3xczk3Lnim0eziVTB094/view");

                            // change visibility if student said "hide" or so
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                     */


                }else{
                    //Routefinder
                    System.out.println("Jojo");
                    String room = nav;
                    String handicapped = "M0000";

                    if(params.size() == 3){
                        handicapped = "M0001";
                    }

                    Log.i("------>","Looking for room " + room + ". handicapped: " + handicapped);
                    try{
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ma.getAssets().open("route_metadata.json")));
                        String[] routeParams = {"video_path", "location", "distance", "time"};

                        String response = new String();
                        for (String line; (line = reader.readLine()) != null; response += line);

                        Map jsonJavaRootObject = new Gson().fromJson(response, Map.class);


                        for(int i = 0; i< routeParams.length; ++i){
                            String param_ = ((Map)((Map)(jsonJavaRootObject.get(room))).get(handicapped)).get(routeParams[i]).toString();
                            System.out.println(param_);
                        }
                        String path = ((Map)((Map)(jsonJavaRootObject.get(room))).get(handicapped)).get("video_path").toString();
                        String urlVideo = "https://informatik.hs-bremerhaven.de/hbv-kms/"+ path;
                        System.out.println(urlVideo);
                        /*
                        try{
                            ma.runOnUiThread(() -> {
                                ma.setContentView(R.layout.webtest);

                                WebView web = (WebView) ma.findViewById(R.id.webView);
                                WebSettings webSettings = web.getSettings();
                                webSettings.setJavaScriptEnabled(true);
                                web.setWebViewClient(new Callback());
                                web.loadUrl(urlVideo);
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }


                break;
            case("qiVariableBack"):
                String opt = params.get(1);
                if(opt.equals("back")){
                    try {
                        ma.runOnUiThread(() -> {
                            ma.setContentView(R.layout.selfie);
                        });
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                break;
            default:
                Log.d(TAG, "I don't know this variable");
        }
    }
    /*
        private void sendPostReq(String stringURL, String course_, String semester_) {
            try {
                URL url = new URL(stringURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("Accept","application/json");
                //connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
                //connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setReadTimeout(10*1000);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Map<String,Object> params = new LinkedHashMap<>();

                params.put("subject", "conversation_data");
                params.put("identifier", ma.uuidHash.toString());
                params.put("data", "{\"course\":\""+ course_ + "\",\"semester\":\""+ semester_ + "\"}");


                //Request
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                //Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                response = new StringBuffer();
                //Expecting answer of type JSON single line {"json_items":[{"status":"OK","message":"<Message>"}]}
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
                rd.close();
                System.out.println(response.toString()+"\n");
                connection.disconnect(); // close the connection after usage

            } catch (Exception e){
                System.out.println(this.getClass().getSimpleName() + " ERROR - Request failed");
            }
        }
    */
    @Override
    public void stop() {

    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event){
            return false;
        }
    }
}


