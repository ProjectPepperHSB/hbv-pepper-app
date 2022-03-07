package com.project.hbv_pepper_app.Utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class RealTimeDashboardAPI {
    /*
    Class to interact with the realtime dashboard by sending data
     */
    String url;

    public RealTimeDashboardAPI(String url){
        this.url = url;
    }

    public void send2RealtimeDashboard(String topic, String key2, String message) {
        // Sends a request to the RTD in form: {"data": {"topic": topic, key2: message}}
        final String reference = String.valueOf(UUID.randomUUID());

        Log.i("Send 2 Dashboard; " + reference + ": ", "{ \"topic\": " + topic + ": { \"" + key2 + "\": \"" + message + "\" }");
        JSONObject payload = new JSONObject();
        try{
            payload.put("topic", topic);
            payload.put(key2, message);
        } catch (org.json.JSONException e){
            e.printStackTrace();
        }
        sendPost(this.url, payload, reference);
    }

    public static void sendPost(String destinationURL, JSONObject payload, String reference) {
        // Sends a POST request to a desired destination url
        // the receiver will get data in form: { "data": paylaod }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(destinationURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    JSONObject payload_wrapper = new JSONObject();
                    payload_wrapper.put("data", payload.toString());

                    try(OutputStream os = conn.getOutputStream()) {
                        byte[] input = payload_wrapper.toString().getBytes("UTF-8");
                        os.write(input, 0, input.length);
                        os.flush();
                    }
                    // dont comment the following out, bc sometimes requests get lost; idk why
                    Log.i("Request " + reference + " status", String.valueOf(conn.getResponseCode()) + " " + conn.getResponseMessage());
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
