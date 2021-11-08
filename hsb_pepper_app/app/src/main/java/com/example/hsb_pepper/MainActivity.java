package com.example.hsb_pepper;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.human.SmileState;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;


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

    ImageView imageView;

    private static final String START_BOOKMARK_GREETING = "greeting";
    private static final String START_BOOKMARK_PRIVACY = "privacy";
    private static final String START_BOOKMARK_CATEGORIES = "categories";

    private Chat chat;
    private QiContext qiContext = null;
    private HumanAwareness humanAwareness;

    private Chat chatAction;
    private boolean isChatActive = false;
    private QiChatbot qiChatbot;
    private Future<Void> currentChatFuture;

    private QiChatVariable qiChatStatus;
    private QiChatVariable qiChatBemot;
    private QiChatVariable qiChatSmile;
    private QiChatVariable qiChatAge;

    private StringBuilder personDetails;

    private Topic main_topic;
    private TopicStatus mainTopicStatus;

    private int ageActiveSpeaker;
    private String emotionActiveSpeaker;

    private Bookmark proposalBookmark;
    private Map<String, Bookmark> bookmarks;

    private String status;
    private final String DEFAULT_STRING = "UNKNOWN";
    private boolean isSomeone;

    private String smileActiveSpeaker;

    private Person activePerson;
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
        setContentView(R.layout.activity_main);
        initVariables();
        QiSDK.register(this, this);

        /* ----- ----- ----- ----- ----- ----- ----- ----- ----- */

        initVariables();

        /* ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG,"Focus lost");
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
        TTChatBot = null;
        resetConversation();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Remove on started listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
        TTChatBot = null;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG,"### Focus gained ###");
        this.qiContext = qiContext;
        HelperCollection.Say(qiContext, "Hallo!");
        Log.i(TAG, "Focus gained on DEBUG mode.");

        initQIChat();
        if (!this.DEBUG_MODE) {
            initHumanAwareness();
        } else {
            /*
            if(true){
                TTChatBot = new TimeTableChatBot(qiContext);
                TTChatBot.start();
            } else{
                TestMensa(qiContext);
            }
            */
        }
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

        /* ----- MENSA ----- ----- ----- ----- ----- */
        imageView = (ImageView) findViewById(R.id.iMensa); //Select img id
        GetMensaData getMensaData = new GetMensaData(imageView, new AsyncResponse() {
            //Get Mensa img and csv
            @Override
            public void processFinish(Mensa result) {
                mensa = result;
                System.out.println("got Mensa Info");
            }
        });
        getMensaData.execute(mensaURL);
    }

    private void initQIChat(){
        main_topic = TopicBuilder.with(this.qiContext)
                .withResource(R.raw.main)
                .build();
        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(main_topic)
                .build();
        chatAction = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        mainTopicStatus = qiChatbot.topicStatus(main_topic);

        bookmarks = main_topic.getBookmarks();
        qiChatStatus = qiChatbot.variable("Status");
        qiChatBemot = qiChatbot.variable("Bemot");
        qiChatSmile = qiChatbot.variable("Smile");
        qiChatAge = qiChatbot.variable("Age");

        bookmarks = main_topic.getBookmarks();

        /* ----- ----- ----- ----- ----- ----- ----- ----- ----- */

        if (this.DEBUG_MODE) {
            currentChatFuture = chatAction.async().run();
            qiChatStatus.async().setValue("single");
            qiChatBemot.async().setValue("JOYFUL");
        }

        qiChatbot.addOnBookmarkReachedListener(bookmark -> {
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

        if (qiChatbot != null) {
            qiChatbot.removeAllOnBookmarkReachedListeners();
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

    public void TestMensa(QiContext qiContext){

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext).withTexts("Mensa", "Mensaplan", "Essen", "Cafetaria").build();
        Listen listen = ListenBuilder.with(qiContext).withPhraseSets(phraseSetYes).build();
        ListenResult listenResult = listen.run();

        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();
        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            Log.i(TAG, "Heard phrase set: Mensa");

            runOnUiThread(() -> setContentView(R.layout.mensa_layout));
            System.out.println(mensa.getDay()[2]);
            System.out.println(mensa.getOffer1()[0]);
            System.out.println(mensa.getOffer2()[4]);

            TestMensa(qiContext);
        }
    }

    // endregion TESTING
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
}


/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
