package com.project.hbv_pepper_app.Utils;

import android.annotation.SuppressLint;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.TransformTime;
import com.aldebaran.qi.sdk.object.geometry.Vector3;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 *  _   _      _                  ____      _           _   _
 * | | | | ___| |_ __   ___ _ __ / ___|___ | | ___  ___| |_(_) ___  _ __
 * | |_| |/ _ \ | '_ \ / _ \ '__| |   / _ \| |/ _ \/ __| __| |/ _ \| '_ \
 * |  _  |  __/ | |_) |  __/ |  | |__| (_) | |  __/ (__| |_| | (_) | | | |
 * |_| |_|\___|_| .__/ \___|_|   \____\___/|_|\___|\___|\__|_|\___/|_| |_|
 *              |_|
 * functions and utils that could not be assigned to a specifig context
 */
public class HelperCollection {

    /**
     * Prepares content for a GET / POST Request
     * @param theUrl
     * @return
     */
    public static String getUrlContents(String theUrl){
        System.out.println(theUrl);
        StringBuilder content = new StringBuilder();
        // Use try and catch to avoid the exceptions
        try {
            URL url = new URL(theUrl); // creating a url object
            URLConnection urlConnection = url.openConnection(); // creating a urlconnection object

            // wrapping the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "latin1"));
            String line;
            // reading from the urlconnection using the bufferedreader
            while ((line = bufferedReader.readLine()) != null) content.append(line + "\n");
            bufferedReader.close();
        } catch(Exception e)  {
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Lets the Robot say some phrase
     * @param qiContext
     * @param message
     */
    public static void Say(QiContext qiContext, String message){
        SayBuilder.with(qiContext).withPhrase(new Phrase(message)).build().run();
    }

    /**
     * returns the timestamp in date form
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getTimeStamp() {
        return new SimpleDateFormat("HHmmss_SSS").format(new Date());
    }

    /**
     * Returns the distance between pepper and the active person
     * @param humanFrame
     * @param robotFrame
     * @return
     */
    public static double computeDistance(Frame humanFrame, Frame robotFrame) {
        // Get the TransformTime between the human frame and the robot frame.
        TransformTime transformTime = humanFrame.computeTransform(robotFrame);
        // Get the transform.
        Transform transform = transformTime.getTransform();
        // Get the translation.
        Vector3 translation = transform.getTranslation();
        // Get the x and y components of the translation.
        double x = translation.getX();
        double y = translation.getY();
        // Compute the distance and return it.
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Creates a HTTP Connection to send GET Requests
     * @param url
     * @return
     * @throws Exception
     */
    public static HttpURLConnection getConnection(String url) throws  Exception {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        return con;
    }

    /**
     * Returns the price of a given cryptocurrency pair; price by kucoin.com
     * @param symbol
     * @return
     * @throws Exception
     */
    public static String getPrice(String symbol) throws Exception {
        try {
            HttpURLConnection con = getConnection("https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/crypto?subject=price&symbol=" + symbol);
            int responseCode = con.getResponseCode();
            /*
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            */
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
            in.close();
            //System.out.println(response.toString());
            //Read JSON response and print
            JSONObject myResponse = new JSONObject(response.toString());
            String price = new JSONObject(myResponse.getString("data")).getString("price");
            //System.out.println("Price: " + price);
            return price;
        } catch (Exception e) { // should never happen
            e.printStackTrace();
            return "undefined";
        }
    }

    /**
     * Saves the conversation data; called in onFocusLost
     * @param uuidStr
     * @param distance
     * @param age
     * @param gender
     * @param basic_emotion
     * @param pleasure_state
     * @param excitement_state
     * @param smile_state
     * @param dialog_time
     */
    public static void saveConversationData(String uuidStr, String distance, String age, String gender, String basic_emotion,
                                            String pleasure_state, String excitement_state, String smile_state,
                                            String dialog_time){
        try {
            HttpURLConnection con = getConnection(
                    "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/collector?subject=save_pepper_data&"
                            + "identifier" + uuidStr
                            + "distance=" + distance
                            + "&age=" + age
                            + "&gender=" + gender
                            + "&basic_emotion=" + basic_emotion
                            + "&pleasure_state="+ pleasure_state
                            + "&excitement_state=" + excitement_state
                            +"&smile_state=" + smile_state
                            +"&dialog_time=" + dialog_time
            );
            int responseCode = con.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * lets the robot say, what du essen kannst
     * @param day
     * @return
     */
    public static String getOffer(String day) {
        try{
            HttpURLConnection con = getConnection("https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/mensadata");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
            in.close();

            JSONObject myResponse = new JSONObject(response.toString());
            String tmp = myResponse.getString("offer1");
            String [] offer1 = tmp.split("\",\"", -1);
            tmp = myResponse.getString("offer2");
            String [] offer2 = tmp.split("\",\"", -1);

            //System.out.println(offer1);
            String [] daylist = {"Montag","Dienstag","Mittwoch","Donnerstag","Freitag"};
            String [] spcdays = {"Heute", "Morgen", "Übermorgen"};

            Calendar calendar = Calendar.getInstance();
            int curday = calendar.get(Calendar.DAY_OF_WEEK) - 2;

            String answer = "";

            //Check if the user wants to know the offer of today, tomorrow or the day after tomorrow
            for(int i = 0; i < spcdays.length; ++i){
                if(day.equals(spcdays[i])){
                    if((curday + i) > 5){
                        answer = "Am Wochenende ist die Mensa leider geschlossen";
                        break;
                    }
                    answer = offer1[curday + i].replaceAll("\"", "").replace("[","").replace("]","");
                    answer += " oder " + offer2[curday + i].replaceAll("\"", "").replace("[","").replace("]","");
                    break;
                }
            }
            //Check if the user wants to know the offer of a specific weekday
            for(int i = 0; i < daylist.length; ++i){
                if(day.equals(daylist[i])){
                    answer = offer1[i].replaceAll("\"", "").replace("[","").replace("]","");
                    answer += " oder " + offer2[i].replaceAll("\"", "").replace("[","").replace("]","");
                    break;
                }
            }

            return answer;
        } catch (Exception e) { // should never happen
            e.printStackTrace();
            return "undefined";
        }
    }
}

/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
