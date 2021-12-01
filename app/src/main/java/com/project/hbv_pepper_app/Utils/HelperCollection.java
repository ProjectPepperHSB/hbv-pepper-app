package com.project.hbv_pepper_app.Utils;

import android.annotation.SuppressLint;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.TransformTime;
import com.aldebaran.qi.sdk.object.geometry.Vector3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class HelperCollection {
    /* Class to store static functions
       └ Accessable from everywhere
    */

    public static String getUrlContents(String theUrl){
        StringBuilder content = new StringBuilder();
        // Use try and catch to avoid the exceptions
        try {
            URL url = new URL(theUrl); // creating a url object
            URLConnection urlConnection = url.openConnection(); // creating a urlconnection object

            // wrapping the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "latin1"));
            String line;
            // reading from the urlconnection using the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch(Exception e)  {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static void Say(QiContext qiContext, String message){
        SayBuilder.with(qiContext).withPhrase(new Phrase(message)).build().run();
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeStamp() {
        return new SimpleDateFormat("HHmmss_SSS").format(new Date());
    }

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

    public static HttpURLConnection getConnection(String url) throws  Exception {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(
                context.getSocketFactory());

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        return con;
    }


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
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
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

    public static String getOffer(String day) throws  Exception {
        try{
            HttpURLConnection con = getConnection("https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/mensadata");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject myResponse = new JSONObject(response.toString());

            String [] weekday = {"Montag","Dienstag","Mittwoch","Donnerstag","Freitag"};

            String tmp = myResponse.getString("offer1");
            String [] offer1 = tmp.split("\",\"", -1);
            tmp = myResponse.getString("offer2");
            String [] offer2 = tmp.split("\",\"", -1);

            String answer = "";
            for(int i = 0; i < weekday.length; ++i){
                if(day.equals(weekday[i])){
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
