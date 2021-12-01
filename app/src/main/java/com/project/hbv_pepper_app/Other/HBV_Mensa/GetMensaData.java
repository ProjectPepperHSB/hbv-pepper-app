package com.project.hbv_pepper_app.Other.HBV_Mensa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.project.hbv_pepper_app.Utils.HelperCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

public class GetMensaData extends AsyncTask<String, Void, Mensa> {
    private String TAG = "GetMensaData";

    public AsyncResponse delegate = null;


    private Bitmap bitmap;
    private Mensa mensa;

    public GetMensaData(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected Mensa doInBackground(String... url) {
        bitmap = null;
        mensa = null;
        //Download Img
        try{
            String url_str = url[0] + "/mensaplan.png";
            InputStream srt = new URL(url_str).openStream();
            bitmap = BitmapFactory.decodeStream(srt);
        }catch (Exception e){
            e.printStackTrace();
        }

        //Download and read CSV
        try {
            String url_str = url[0] + "/menulist.csv";
            String response_str = HelperCollection.getUrlContents(url_str);
            BufferedReader reader = new BufferedReader(new StringReader(response_str));

            String [] days = new String[5];
            String [] offer1 = new String[5];
            String [] offer2 = new String[5];

            String line;

            int line_idx = 0;
            while ((line = reader.readLine()) != null) {
                ++line_idx;
                if (line_idx == 1)
                    continue;
                String[] content = line.split(";");
                days[line_idx-2]=content[0];
                offer1[line_idx-2]=content[1];
                offer2[line_idx-2]=content[2];
            }
            mensa = new Mensa(days, offer1, offer2, bitmap);
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return mensa;
    }

    protected void onPostExecute(Mensa mensa){
        super.onPostExecute(mensa);
        //imageView.setImageBitmap(mensa.getMensaImg());
        delegate.processFinish(mensa);
    }
}
