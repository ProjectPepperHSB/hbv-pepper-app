package com.project.hbv_pepper_app.Executors;


import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.project.hbv_pepper_app.Fragments.ScreenTwoFragment;
import com.project.hbv_pepper_app.MainActivity;
import com.project.hbv_pepper_app.Utils.HelperCollection;

import java.util.List;

/**
 * VariableExecutor is used when a qiVariable is modified in the qiChat and
 * you want to have feedback on the tablet
 * Triggered in qiChat as follow : ^execute( VariableExecutor, variableName, variableValue)
 */

public class VariableExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_VariableExecutor";

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
            if(params.size() == 2){
                variableValue = params.get(1);
            }else{
                Log.d(TAG, "no value specified for variable : " + variableName);
                return;
            }
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

                if (coin == "bitcoin"){
                    coin = "btc";
                } else if (coin == "ethereum"){
                    coin = "eth";
                } else {
                    coin = "sol";
                }

                try {
                    String price = HelperCollection.getPrice(coin + "-USDT");
                    Log.i(TAG, "price of " + coin + " is " + price);
                    ma.getCurrentChatBot().setQiVariable(variableName, price);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            case("qiVariableTimeTable"):
                String w_word = params.get(1);
                String course = params.get(2);

                Log.i(TAG,"Looking for w-word " + w_word +" and course " + course);

                if (w_word == "wann"){} else if (w_word == "wo"){}


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
