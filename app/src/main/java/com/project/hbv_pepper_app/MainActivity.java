package com.project.hbv_pepper_app;


import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.human.SmileState;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.project.hbv_pepper_app.Executors.FragmentExecutor;
import com.project.hbv_pepper_app.Executors.VariableExecutor;
import com.project.hbv_pepper_app.Fragments.LoadingFragment;
import com.project.hbv_pepper_app.Fragments.MainFragment;
import com.project.hbv_pepper_app.Fragments.SplashFragment;
import com.project.hbv_pepper_app.Other.HBV_Mensa.AsyncResponse;
import com.project.hbv_pepper_app.Other.HBV_Mensa.GetMensaData;
import com.project.hbv_pepper_app.Other.HBV_Mensa.Mensa;
import com.project.hbv_pepper_app.Other.HBV_TimeTable.Person;
import com.project.hbv_pepper_app.Utils.ChatData;
import com.project.hbv_pepper_app.Utils.CountDownNoInteraction;
import com.project.hbv_pepper_app.Utils.HelperCollection;
import com.project.hbv_pepper_app.Utils.TimeTableChatBot;

// TODO:
// ├ Set variable chat to null if focus lost for x seconds
// └ outsource classes and not-main functions

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
     /* Main Function of this Applications
        ├ Creates chatBots, handles interactions
        └ Some more magic with "AI"
     */

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements VARIABLES

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG_MODE = true;
    public static final String language = "de";

    ImageView imageView;
    private QiChatbot oldQiChatBot;
    private static final String START_BOOKMARK_GREETING = "greeting";
    private static final String START_BOOKMARK_PRIVACY = "privacy";
    private static final String START_BOOKMARK_CATEGORIES = "categories";

    private Chat chat;
    private QiContext qiContext = null;
    private HumanAwareness humanAwareness;

    private Chat chatAction;
    private boolean isChatActive = false;

    private Future<Void> currentChatFuture;

    private QiChatVariable qiChatStatus;
    private QiChatVariable qiChatBemot;
    private QiChatVariable qiChatSmile;
    private QiChatVariable qiChatAge;
    public QiChatVariable qiVariablePrice;

    private StringBuilder personDetails;

    private int ageActiveSpeaker;
    private String emotionActiveSpeaker;

    private Bookmark proposalBookmark;
    private Map<String, Bookmark> bookmarks;

    private String status;
    private final String DEFAULT_STRING = "UNKNOWN";
    private boolean isSomeone;

    private String smileActiveSpeaker;

    // ---- N E W ---- ---- ----
    private FragmentManager fragmentManager;
    private android.content.res.Configuration config;
    private Resources res;
    private CountDownNoInteraction countDownNoInteraction;
    private ChatData qiChatBot, currentChatBot;
    private final List<String> topicNames = Arrays.asList(
            "main",
            "hbvunimain", "hbvuni_for_students", "hbvuni_information", "hbvuni_study_counseling",
            "screentwo",
            "concepts"
    );

    private String currentFragment, currentTopicName;
    private TopicStatus currentTopicStatus;
    private Future<Void> chatFuture;
    // ---- E N D - N E W ----- ------

    private Person[] persons;
    private TimeTableChatBot TTChatBot = null;

    // Mensa Stuff
    private String mensaURL = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-web/mensa";
    public Mensa mensa;

    // endregion implements VARIABLES
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -------- N E W --------
        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        countDownNoInteraction = new CountDownNoInteraction(this, new SplashFragment(),
                45000, 35000);
        countDownNoInteraction.start();
        updateLocale(language);
        setContentView(R.layout.activity_main);
        // -------- E N D - N E W --------

        // wird nicht mehr benötigt
        //initVariables();
        //QiSDK.register(this, this);
    }

    private void updateLocale(String strLocale) {
        Locale locale = new Locale(strLocale);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG,"Focus lost");
        humanAwareness.async().removeAllOnEngagedHumanChangedListeners();
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "onRobotFocusRefused");
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        countDownNoInteraction.cancel();
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        HelperCollection.Say(qiContext, "jo!");

        /*initQIChat();
        if (!this.DEBUG_MODE) {
            initHumanAwareness();
        } else {

            if(true){
                TTChatBot = new TimeTableChatBot(qiContext);
                TTChatBot.start();
            } else{
                TestMensa(qiContext);
            }
        }
        */

        // -------- N E W --------
        qiChatBot = new ChatData(this, qiContext, new Locale(language), topicNames, true);
        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("FragmentExecutor", new FragmentExecutor(qiContext, this));
        executors.put("VariableExecutor", new VariableExecutor(qiContext, this));
        qiChatBot.setupExecutors(executors);
        qiChatBot.setupQiVariables(Arrays.asList("qiVariablePrice","qiVariable","qiVariableMensa")); // qiChatVariable
        currentChatBot = qiChatBot;
        currentChatBot.chat.async().addOnStartedListener(() -> { //qiChatVariable Pepper
            setQiVariable("qiVariablePrice", "undefined"); // this is done here because the chatBot needs to be running for this to work.
            setQiVariable("qiVariable", "Pepper");
            setQiVariable("qiVariableMensa", "undefined");
            runOnUiThread(() -> {
                setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS); // Disable overlay mode for the rest of the app.
                setFragment(new MainFragment());
            });
        });

        currentChatBot.chat.async().addOnNormalReplyFoundForListener(input -> {
            countDownNoInteraction.reset();
        });
        chatFuture = currentChatBot.chat.async().run();
        humanAwareness = getQiContext().getHumanAwareness();
        humanAwareness.async().addOnEngagedHumanChangedListener(engagedHuman -> {
            if (getFragment() instanceof SplashFragment) {
                if (engagedHuman != null) {
                    setFragment(new MainFragment());
                }
            } else {
                countDownNoInteraction.reset();
            }
        });
        // -------- E N D - N E W --------
    }

    @Override
    public void onPause() {
        countDownNoInteraction.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY); // We don't want to see the speech bar while loading
        this.setFragment(new LoadingFragment());
    }

    @Override
    public void onUserInteraction() {
        if (getFragment() instanceof SplashFragment) {
            setFragment(new MainFragment());
            countDownNoInteraction.start();
        } else {
            countDownNoInteraction.reset();
        }
    }

    /**
     * updates the value of the qiVariable
     *
     * @param variableName the name of the variable
     * @param value        the value that needs to be set
     */

    public void setQiVariable(String variableName, String value) {
        Log.d(TAG, "size va : " + currentChatBot.variables.size());
        currentChatBot.variables.get(variableName).async().setValue(value);
    }

    public ChatData getCurrentChatBot() {
        return currentChatBot;
    }

    public QiContext getQiContext() {
        return qiContext;
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Fragment getFragment() {
        return fragmentManager.findFragmentByTag("currentFragment");
    }

    /**
     * Change the fragment displayed by the placeholder in the main activity, and goes to the
     * bookmark init in the topic assigned to this fragment
     *
     * @param fragment the fragment to display
     */

    public void setFragment(Fragment fragment) {
        currentFragment = fragment.getClass().getSimpleName();
        String topicName = currentFragment.toLowerCase().replace("fragment", "");
        if (!(fragment instanceof LoadingFragment) && !(fragment instanceof SplashFragment)) {
            Log.d("_>", topicName);
            this.currentChatBot.goToBookmarkNewTopic("init", topicName);
        }
        Log.d(TAG, "Transaction for fragment : " + fragment.getClass().getSimpleName());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left,
                R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // endregion implements EVENTS
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // begin implements INIT

    private void initVariables() {
        /* ----- GENERAL ----- ----- ----- ----- ----- */

        this.status = this.DEFAULT_STRING;
        this.isSomeone = false;
        this.ageActiveSpeaker = -1;
        this.emotionActiveSpeaker = this.DEFAULT_STRING;


    }
    /*
    private void initQIChat(){

        main_topic = TopicBuilder.with(this.qiContext)
                .withResource(R.raw.main)
                .build();
        oldQiChatBot = QiChatbotBuilder.with(qiContext)
                .withTopic(main_topic)
                .build();
        Map<String, QiChatExecutor> executors = new HashMap<>();
        qiChatPrice = oldQiChatBot.variable("price");
        // Map the executor name from the topic to our qiChatExecutor
        executors.put("myExecutor", new BaseQiChatExecutor(qiContext) {
            //https://android.aldebaran.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/baseQiChatExecutor.html#baseqichatexecutor
            @Override
            public void runWith(List<String> list) {
                System.out.println("Looking for price of " + list.get(0));
                String coin = list.get(0);

                try {
                    qiChatPrice.setValue(HelperCollection.getPrice(coin+"-USDT"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void stop() {

            }
        });

        // Set the executors to the qiChatbot
        oldQiChatBot.setExecutors(executors);

        chatAction = ChatBuilder.with(qiContext)
                .withChatbot(oldQiChatBot)
                .build();

        mainTopicStatus = oldQiChatBot.topicStatus(main_topic);

        bookmarks = main_topic.getBookmarks();

        // chatvariables are to link this code with topics
        //qiChatStatus = qiChatbot.variable("Status");
        //qiChatBemot = qiChatbot.variable("Bemot");
        //qiChatSmile = qiChatbot.variable("Smile");
        //qiChatAge = qiChatbot.variable("Age");


        bookmarks = main_topic.getBookmarks();

        if (this.DEBUG_MODE) {
            currentChatFuture = chatAction.async().run();

            qiChatStatus.async().setValue("single");
            qiChatBemot.async().setValue("JOYFUL");

        }

        oldQiChatBot.addOnBookmarkReachedListener(bookmark -> {
            Log.i(TAG, "QiChatBot Bookmark : " + bookmark.getName());

            switch (bookmark.getName()) {
                // hier nur, wenn wir auch views haben
                case START_BOOKMARK_PRIVACY:
                    runOnUiThread(() -> setContentView(R.layout.privacy));
                    break;
                case START_BOOKMARK_CATEGORIES:
                    runOnUiThread(() -> setContentView(R.layout.categories));
                    break;
            }
        });

        chatAction.addOnNoPhraseRecognizedListener(() -> {
            Log.i(TAG, HelperCollection.getTimeStamp() + " Robot:  Not Understand");
        });

        chatAction.addOnHeardListener(heardPhrase -> {
            Log.i(TAG, HelperCollection.getTimeStamp()  + " Person: " + heardPhrase.getText());
        });

        chatAction.addOnSayingChangedListener(sayingPhrase -> {
            Log.i(TAG, HelperCollection.getTimeStamp()  + " Robot: " + sayingPhrase.getText());
        });
    }
    */

    /* DAS HIER KÖNNEN WIR NOCH IRGNEDWIE VERARBEITEN
    private void initHumanAwareness() {
        Log.i(TAG, "... running InitHumanAwareness");
        humanAwareness = this.qiContext.getHumanAwareness();
        humanAwareness.addOnRecommendedHumanToApproachChangedListener(human -> {
            if (human != null) {
                double distance_format;
                if (this.qiContext.getActuation() != null) {
                    Actuation actuation = this.qiContext.getActuation();
                    Frame robotFrame = actuation.robotFrame();
                    Frame humanFrame = human.getHeadFrame();
                    distance_format = HelperCollection.computeDistance(humanFrame, robotFrame);
                } else {
                    distance_format = 0;
                }

                if (distance_format < 3 && distance_format >= 1.5) {
                    findHumansAround();
                    int age = human.getEstimatedAge().getYears();
                    Gender gender = human.getEstimatedGender();
                    PleasureState pleasureState = human.getEmotion().getPleasure();
                    ExcitementState excitementState = human.getEmotion().getExcitement();
                    String emotion = computeBasicEmotion(String.valueOf(excitementState), String.valueOf(pleasureState));
                    SmileState smileState = human.getFacialExpressions().getSmile();

                    Log.i(TAG, "ActiveSpeaker Approach:");
                    Log.i(TAG, "distance : " + distance_format);
                    Log.i(TAG, "Age: " + age + " year(s)");
                    Log.i(TAG, "Gender: " + gender);
                    Log.i(TAG, "Pleasure state: " + pleasureState);
                    Log.i(TAG, "Excitement state: " + excitementState);
                    Log.i(TAG, "Basic Emotion : " + emotion);
                    Log.i(TAG, "Smile state: " + smileState);

                    if (age != -1) {
                        ageActiveSpeaker = age;
                    }

                    if (!emotion.equals(DEFAULT_STRING)) {
                        emotionActiveSpeaker = emotion;
                        qiChatBemot.async().setValue(emotionActiveSpeaker);
                    }

                    if (!status.equals(DEFAULT_STRING)) {
                        isChatActive = true;
                        currentChatFuture = chatAction.async().run();
                        getEngage();
                    } else if (!isSomeone) {
                        isSomeone = true;
                        if (!isChatActive) {
                            isChatActive = true;
                            currentChatFuture = chatAction.async().run();
                            getEngage();
                        }
                    }
                }
            }
        });
    }

    public void resetConversation(){
        // delete information about previous conversation
        Log.i(TAG, "RESETTING MasterRobo");

        // remove all the Listeners
        if (currentChatFuture != null) {
            currentChatFuture.requestCancellation();
        }

        if (oldQiChatBot != null) {
            oldQiChatBot.removeAllOnBookmarkReachedListeners();
        }
        if (humanAwareness != null) {
            humanAwareness.removeAllOnRecommendedHumanToApproachChangedListeners();
            humanAwareness.removeAllOnRecommendedHumanToEngageChangedListeners();
        }

        initQIChat();

        if (!DEBUG_MODE) {
            initHumanAwareness();
        }
    }
    */
    // endregion implements INIT
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implement FUN

    private void getEngage() {
        // ist es nicht das selbe wie inithumanawareness?
        Log.i(TAG, "getEngage started.");
        humanAwareness.removeAllOnRecommendedHumanToApproachChangedListeners();
        humanAwareness.addOnRecommendedHumanToEngageChangedListener(human -> {
            if (human != null) {
                if (status.equals(this.DEFAULT_STRING))
                    findHumansAround();
                else {
                    double distance_format;
                    if (qiContext.getActuation() != null) {
                        Actuation actuation = qiContext.getActuation();
                        Frame robotFrame = actuation.robotFrame();
                        Frame humanFrame = human.getHeadFrame();
                        // DecimalFormat format = new DecimalFormat("0.##");
                        distance_format = HelperCollection.computeDistance(humanFrame, robotFrame);
                    } else
                        distance_format = 0;

                    int age = human.getEstimatedAge().getYears();
                    Gender gender = human.getEstimatedGender();
                    PleasureState pleasureState = human.getEmotion().getPleasure();
                    ExcitementState excitementState = human.getEmotion().getExcitement();
                    String emotion = computeBasicEmotion(String.valueOf(excitementState), String.valueOf(pleasureState));
                    SmileState smileState = human.getFacialExpressions().getSmile();

                    Log.i(TAG, "ActiveSpeaker Engage : ");
                    Log.i(TAG, "distance : " + distance_format);
                    Log.i(TAG, "Age: " + age + " year(s)");
                    Log.i(TAG, "Gender: " + gender);
                    Log.i(TAG, "Pleasure state: " + pleasureState);
                    Log.i(TAG, "Excitement state: " + excitementState);
                    Log.i(TAG, "Basic Emotion : " + emotion);
                    Log.i(TAG, "Smile state: " + smileState);

                    if (age != -1) {
                        ageActiveSpeaker = age;
                    }
                    if (ageActiveSpeaker != -1) {
                        qiChatAge.async().setValue(String.valueOf(ageActiveSpeaker));
                    }

                    if (!emotion.equals(this.DEFAULT_STRING)) {
                        emotionActiveSpeaker = emotion;
                    }
                    if (!emotionActiveSpeaker.equals(this.DEFAULT_STRING)) {
                        qiChatBemot.async().setValue(emotionActiveSpeaker);
                    }

                    String smile = String.valueOf(smileState);
                    if (!smile.equals(this.DEFAULT_STRING)) {
                        smileActiveSpeaker = smile;
                        qiChatSmile.async().setValue(smileActiveSpeaker);
                    }
                }
            }
        });
    }

    private void findHumansAround() {
        Log.i(TAG, "findHumansAround");
        // Get the humans around the robot.
        Future<List<Human>> humansAroundFuture = humanAwareness.async().getHumansAround();
        humansAroundFuture.andThenConsume(humansAround -> {
            Log.i(TAG, humansAround.size() + " human(s) around.");
            retrieveCharacteristics(humansAround);
        });
    }

    private void retrieveCharacteristics(final List<Human> humans) {
        Log.i(TAG, "retrieveCharacteristics");
        persons = new Person[humans.size()];
        for (int i = 0; i < humans.size(); i++) {
            Actuation actuation = qiContext.getActuation();
            Human human = humans.get(i);

            // Get the characteristics.
            int age = human.getEstimatedAge().getYears();
            Gender gender = human.getEstimatedGender();
            PleasureState pleasureState = human.getEmotion().getPleasure();
            ExcitementState excitementState = human.getEmotion().getExcitement();
            String emotion = computeBasicEmotion(String.valueOf(excitementState), String.valueOf(pleasureState));
            SmileState smileState = human.getFacialExpressions().getSmile();

            Frame robotFrame = actuation.robotFrame();
            Frame humanFrame = human.getHeadFrame();
            double distance_format = HelperCollection.computeDistance(humanFrame, robotFrame);

            // Display the characteristics.
            Log.i(TAG, "----- Human " + i + " -----");
            Log.i(TAG, "distance : " + distance_format);
            Log.i(TAG, "Age: " + age + " year(s)");
            Log.i(TAG, "Gender: " + gender);
            Log.i(TAG, "Pleasure state: " + pleasureState);
            Log.i(TAG, "Excitement state: " + excitementState);
            Log.i(TAG, "Basic Emotion : " + emotion);
            Log.i(TAG, "Smile state: " + smileState);

            persons[i] = new Person();
            persons[i].setAge(age);
            persons[i].setEmotion(emotion);
            //persons[i].setGender(gender);
        }

        if (persons != null && persons.length > 1) {
            Log.i(TAG, "Hier sind aber viele Leute - beep boop");
            /*
            Log.i(TAG, "Status : " + status);
            if (!status.equals(this.DEFAULT_STRING) && !status.equals("kind")) {
                status_check = true;
                qiChatStatus.async().setValue(status);
            }

             */
        }
    }

    private String computeBasicEmotion(String excitement, String pleasure) {
        if (excitement.equals(this.DEFAULT_STRING) || pleasure.equals(this.DEFAULT_STRING)) {
            return this.DEFAULT_STRING;
        }
        switch (pleasure) {
            case "POSITIVE":
                return excitement.equals("CALM") ? "CONTENT" : "JOYFUL";
            case "NEGATIVE":
                return excitement.equals("CALM") ? "SAD" : "ANGRY";
        }
        return "NEUTRAL";
    }

    // endregion implement FUN
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implement TESTING

    public void setMensaImageView(){

        //Download Img
        try{
            String url_str = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/mensadata/img";
            InputStream srt = new URL(url_str).openStream();
            final Bitmap bitmap = BitmapFactory.decodeStream(srt);

            runOnUiThread(() -> {
                setContentView(R.layout.mensa_layout);
                imageView = (ImageView) findViewById(R.id.iMensa);
                imageView.setImageBitmap(bitmap);
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // endregion TESTING
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
}


/* ----- ----- E O F ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
