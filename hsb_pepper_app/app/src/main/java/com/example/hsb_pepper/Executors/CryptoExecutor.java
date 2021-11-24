package com.example.hsb_pepper.Executors;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;

import java.util.List;

//import androidx.fragment.app.Fragment;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.example.hsb_pepper.Utils.HelperCollection;
import com.example.hsb_pepper.MainActivity;


/**
 * Triggered in qiChat as follow : ^execute( CryptoExecutor, frag_XXXX )
 */

public class CryptoExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "CryptoExecutor";
    private QiChatVariable qiChatPrice;
    public CryptoExecutor(QiContext qiContext, MainActivity mainActivity, QiChatVariable qiChatPrice) {
        super(qiContext);
        this.ma = mainActivity;
        this.qiChatPrice = qiChatPrice;
    }

    @Override
    public void runWith(List<String> params) {
        String fragmentName;
        String optionalData; //use this if you need to pass on data when setting the fragment.
        if (params == null || params.isEmpty()) {
            return;
        }else{
            fragmentName = params.get(0);
            if(params.size() == 2){
                optionalData = params.get(1);
            }
        }
        String coin = params.get(0);
        System.out.println("Looking for price of " + coin);
        try {
            HelperCollection.Say(ma.getQiContext(), "Der Preis pro " + coin + " ist " + HelperCollection.getPrice(coin+"-USDT"));
        } catch (Exception e) {
            e.printStackTrace();
            HelperCollection.Say(ma.getQiContext(), "Das weiß ich gerade leider nicht.");
        }
    }

    @Override
    public void stop() {

    }
}