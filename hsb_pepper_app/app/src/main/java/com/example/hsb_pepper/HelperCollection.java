package com.example.hsb_pepper;

import android.annotation.SuppressLint;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.TransformTime;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.object.human.Human;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;import android.annotation.SuppressLint;

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


}

/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
