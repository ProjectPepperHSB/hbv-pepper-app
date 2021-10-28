package com.example.hsb_pepper;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Phrase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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

}

/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
