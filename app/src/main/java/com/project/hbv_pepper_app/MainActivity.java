package com.project.hbv_pepper_app;


import static com.project.hbv_pepper_app.Utils.HelperCollection.getTimeStamp;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.human.SmileState;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.EncodedImageHandle;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;
import com.project.hbv_pepper_app.Executors.FragmentExecutor;
import com.project.hbv_pepper_app.Executors.VariableExecutor;
import com.project.hbv_pepper_app.Fragments.LoadingFragment;
import com.project.hbv_pepper_app.Fragments.MainFragment;
import com.project.hbv_pepper_app.Fragments.SplashFragment;
import com.project.hbv_pepper_app.Utils.ChatData;
import com.project.hbv_pepper_app.Utils.CountDownNoInteraction;
import com.project.hbv_pepper_app.Utils.HelperCollection;
import com.project.hbv_pepper_app.Utils.Person;
import com.project.hbv_pepper_app.Utils.RealTimeDashboardAPI;
import com.project.hbv_pepper_app.Utils.TimeTableChatBot;

import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 *
 *  __  __       _          _        _   _       _ _
 * |  \/  | __ _(_)_ __    / \   ___| |_(_)_   _(_) |_ _   _
 * | |\/| |/ _` | | '_ \  / _ \ / __| __| \ \ / / | __| | | |
 * | |  | | (_| | | | | |/ ___ \ (__| |_| |\ V /| | |_| |_| |
 * |_|  |_|\__,_|_|_| |_/_/   \_\___|\__|_| \_/ |_|\__|\__, |
 *                                                     |___/
 *
 */

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
     /* Main Function of this Applications
        ├ Creates chatBots, handles interactions
        └ Sends data to Backend
     */

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements VARIABLES

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG_MODE = true;
    public static final String language = "de";


    ImageView imageView;
    private QiChatbot oldQiChatBot;

    private Chat chat;
    private QiContext qiContext = null;
    private HumanAwareness humanAwareness;

    private Chat chatAction;
    private boolean isChatActive = false;

    private Future<Void> currentChatFuture;
    public QiChatVariable qiVariablePrice;

    private Map<String, Bookmark> bookmarks;
    private final String DEFAULT_STRING = "UNKNOWN";

    RealTimeDashboardAPI dashboardAPI;
    private FragmentManager fragmentManager;
    private android.content.res.Configuration config;
    private Resources res;
    private CountDownNoInteraction countDownNoInteraction;
    private ChatData qiChatBot, currentChatBot;
    private final List<String> topicNames = Arrays.asList(
            "main",
            "hbvunimain", "hbvuni_for_students", "hbvuni_information", "hbvuni_study_counseling",
            "screentwo",
            "concepts",
            "background"
    );

    private String currentFragment, currentTopicName;
    private TopicStatus currentTopicStatus;
    private Future<Void> chatFuture;
    Future<TakePicture> takePictureFuture;
    private Person[] persons;
    private TimeTableChatBot TTChatBot = null;

    public class ActivePerson {

        final String DEFAULT_STRING = "UNKNOWN";
        private String distance = DEFAULT_STRING;
        private String age = DEFAULT_STRING;
        private String gender = DEFAULT_STRING;
        private String pleasure_state = DEFAULT_STRING;
        private String excitementState = DEFAULT_STRING;
        private String emotion = DEFAULT_STRING;
        private String smileState = DEFAULT_STRING;
        private Long dialog_time = System.currentTimeMillis();
        private String uuidStr = DEFAULT_STRING;
        private UUID uuid;
        private String semester = DEFAULT_STRING;
        private String course = DEFAULT_STRING;

        public ActivePerson(){
            this.uuid = UUID.randomUUID();
            this.uuidStr = this.uuid.toString();
        }

        public void saveToDatabase(){
            this.dialog_time = System.currentTimeMillis() - this.dialog_time;
            HelperCollection.saveConversationData(
                    this.uuidStr,
                    this.distance, this.age,
                    this.gender, this.emotion,
                    this.pleasure_state, this.excitementState,
                    this.smileState, String.valueOf(this.dialog_time / 1000 ));
        }

        public String getDistance() {return distance; }

        public Long getDialog_time(){ return dialog_time; }

        public void setDialog_time(long dialog_time){ this.dialog_time = dialog_time; }

        public void setDistance(String distance) { this.distance = distance; }

        public String getAge() { return age; }

        public void setAge(String age) { this.age = age; }

        public String getGender() { return gender; }

        public void setGender(String gender) { this.gender = gender; }

        public String getPleasure_state() { return pleasure_state; }

        public void setPleasure_state(String pleasure_state) { this.pleasure_state = pleasure_state; }

        public String getExcitementState() { return excitementState; }

        public void setExcitementState(String excitementState) { this.excitementState = excitementState; }

        public String getEmotion() { return emotion; }

        public void setEmotion(String emotion) { this.emotion = emotion; }

        public String getSmileState() { return smileState; }

        public void setSmileState(String smileState) { this.smileState = smileState; }

        public String getCourse() { return course; }

        public void setCourse(String course) { this.course = course; }

        public String getSemester() { return semester; }

        public void setSemester(String semester) { this.semester = semester; }

        public String getUUIDstr(){ return uuidStr; }

        public UUID getUUID(){ return uuid; }
    }

    public ActivePerson activePerson;

    //All qiVariables should be initialized here. These must be called at least once with $qiVariableXXX in the topics.
    public final String[] varNames = {"qiVariableMensa", "qiVariableStudium", "qiVariableNav", "qiVariableBack"};

    // endregion implements VARIABLES
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements EVENTS

    /**
     *               ____                _
     *   ___  _ __  / ___|_ __ ___  __ _| |_ ___
     *  / _ \| '_ \| |   | '__/ _ \/ _` | __/ _ \
     * | (_) | | | | |___| | |  __/ (_| | ||  __/
     *  \___/|_| |_|\____|_|  \___|\__,_|\__\___|
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Device: " + Build.DEVICE);

        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);

        countDownNoInteraction = new CountDownNoInteraction(this, new SplashFragment(),45000, 35000);
        countDownNoInteraction.start();

        String url;
        String local_ip="192.168.1.108";
        if (Build.DEVICE.equals("generic_x86")) url = "http://10.0.2.2:3000/api/data";
        else url = String.format("http://%s:3000/api/data", local_ip);

        dashboardAPI = new RealTimeDashboardAPI(url);
        dashboardAPI.send2RealtimeDashboard("Event", "message", "onCreate!");

        updateLocale(language);
        setContentView(R.layout.selfie);
    }

    /**
     * Sets language
     * @param strLocale
     */
    private void updateLocale(String strLocale) {
        Locale locale = new Locale(strLocale);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    /**
     * Called when focus lost, sends final data to backend.
     */
    @Override
    public void onRobotFocusLost() {
        Log.i(TAG,"Focus lost");
        humanAwareness.async().removeAllOnEngagedHumanChangedListeners();
        this.qiContext = null;

        activePerson.saveToDatabase();
        //activePerson = new ActivePerson();
        dashboardAPI.send2RealtimeDashboard("Event", "message", "Focus lost");
    }

    /**
     * Computer sagt "nein".
     * @param reason
     */
    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "onRobotFocusRefused");
        dashboardAPI.send2RealtimeDashboard("Event", "message", "Focus refused");
    }

    /**
     * Destructor of main
     */
    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        countDownNoInteraction.cancel();
        QiSDK.unregister(this, this);
        dashboardAPI.send2RealtimeDashboard("Event", "message", "onDestroy");
        super.onDestroy();
    }

    /**
     * New perosn detected; init variables and Chatbot
     * @param qiContext
     */
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        HelperCollection.Say(qiContext, "Hallihallo!?");
        dashboardAPI.send2RealtimeDashboard("Event", "message", "Focus gained");

        activePerson = new ActivePerson();


        //Future<TakePicture> takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync();


        qiChatBot = new ChatData(this, qiContext, new Locale(language), topicNames, true);
        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("FragmentExecutor", new FragmentExecutor(qiContext, this));
        executors.put("VariableExecutor", new VariableExecutor(qiContext, this));
        qiChatBot.setupExecutors(executors);

        List allQiVars = new ArrayList(Arrays.asList("qiVariablePrice", "qiVariable"));
        allQiVars.addAll(Arrays.asList(varNames));

        qiChatBot.setupQiVariables(allQiVars); // qiChatVariable
        currentChatBot = qiChatBot;
        currentChatBot.chat.async().addOnStartedListener(() -> { //qiChatVariable Pepper
            setQiVariable("qiVariablePrice", "undefined"); // this is done here because the chatBot needs to be running for this to work.
            setQiVariable("qiVariable", "Pepper");
            for(int i = 0; i < varNames.length; ++i) setQiVariable(varNames[i], "undefined");

            runOnUiThread(() -> {
                setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS); // Disable overlay mode for the rest of the app.
                setFragment(new MainFragment());
            });
        });

        currentChatBot.chat.async().addOnNormalReplyFoundForListener(input -> {
            countDownNoInteraction.reset();
        });

        // If pepper detects human voice but cannot determine the content of the phrase
        currentChatBot.chat.addOnNoReplyFoundForListener(cantReply -> {
            try {
                HttpURLConnection con = HelperCollection.getConnection(
                        "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/collector?subject=did_not_understand_data"
                                + "&identifier=" + activePerson.getUUIDstr()
                                + "&phrase=" + cantReply.getText());
                //int responseCode = con.getResponseCode();
                dashboardAPI.send2RealtimeDashboard("Not Understand", "Phrase", cantReply.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        chatFuture = currentChatBot.chat.async().run();
        humanAwareness = getQiContext().getHumanAwareness();

        humanAwareness.async().addOnEngagedHumanChangedListener(human -> {
            handle_new_human(human);
        });
        humanAwareness.addOnRecommendedHumanToApproachChangedListener(human -> {
            handle_new_human(human);
        });
    }

    /**
     * No interaction for long time
     */

    @Override
    public void onPause() {
        countDownNoInteraction.cancel();
        super.onPause();
    }

    /**
     * new interaction after pause
     */
    @Override
    protected void onResume() {
        super.onResume();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY); // We don't want to see the speech bar while loading
        //this.setFragment(new LoadingFragment());
    }

    /**
     * if user touches screen in pause mode
     */

    @Override
    public void onUserInteraction() {
        if (getFragment() instanceof SplashFragment) {
            setFragment(new MainFragment());
            countDownNoInteraction.start();
        } else countDownNoInteraction.reset();
    }

    // endregion implements EVENTS
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implement FUN

    /**
     * Checks gender, age and other attributes, saves known params and send to dashboard and backend
     * @param engagedHuman
     * @return
     */
    public int handle_new_human(Human engagedHuman){
        if (engagedHuman == null) return 1;
        if (getFragment() instanceof SplashFragment) setFragment(new MainFragment());
        double distance_format;
        if (qiContext.getActuation() != null) {
            Actuation actuation = qiContext.getActuation();
            Frame robotFrame = actuation.robotFrame();
            Frame humanFrame = engagedHuman.getHeadFrame();
            //DecimalFormat format = new DecimalFormat("0.##");
            distance_format = HelperCollection.computeDistance(humanFrame, robotFrame);
        } else distance_format = 0;

        int age = engagedHuman.getEstimatedAge().getYears();
        Gender gender = engagedHuman.getEstimatedGender();
        PleasureState pleasureState = engagedHuman.getEmotion().getPleasure();
        ExcitementState excitementState = engagedHuman.getEmotion().getExcitement();
        String emotion = computeBasicEmotion(String.valueOf(excitementState), String.valueOf(pleasureState));
        SmileState smileState = engagedHuman.getFacialExpressions().getSmile();

        Log.i(TAG, "ActiveSpeaker Engage : ");
        Log.i(TAG, "distance : " + distance_format);

        activePerson.setDistance(String.valueOf(distance_format));

        if(gender.toString().toLowerCase() != "unknown") {
            Log.i(TAG, "Gender: " + gender);
            activePerson.setGender(gender.toString());
            dashboardAPI.send2RealtimeDashboard("gender", "gender", gender.toString());
        }
        if(String.valueOf(age).toLowerCase() != "unknown" && age != -1){
            Log.i(TAG, "Age: " + age + " year(s)");
            dashboardAPI.send2RealtimeDashboard("age", "age", String.valueOf(age));
            activePerson.setAge(String.valueOf(age));
        }
        if(pleasureState.toString().toLowerCase() != "unknown") {
            Log.i(TAG, "Pleasure state: " + pleasureState);
            activePerson.setPleasure_state(pleasureState.toString());
            dashboardAPI.send2RealtimeDashboard("pleasure_state", "state", pleasureState.toString());
        }
        if(excitementState.toString().toLowerCase() != "unknown") {
            Log.i(TAG, "Excitement state: " + excitementState);
            activePerson.setExcitementState(excitementState.toString());
            dashboardAPI.send2RealtimeDashboard("excitement_state", "state", excitementState.toString());
        }
        if(emotion.toLowerCase() != "unknown") {
            Log.i(TAG, "Basic Emotion : " + emotion);
            activePerson.setEmotion(emotion);
            dashboardAPI.send2RealtimeDashboard("emotion_state", "state", emotion);
        }
        if(smileState.toString().toLowerCase() != "unknown") {
            Log.i(TAG, "Smile state: " + smileState);
            activePerson.setSmileState(smileState.toString());
            dashboardAPI.send2RealtimeDashboard("smile_state", "state", smileState.toString());
        }
        return 0;
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

    /**
     * returns the chatbot
     * @return
     */
    public ChatData getCurrentChatBot() {
        return currentChatBot;
    }

    /**
     * returns the qiContext
     * @return
     */
    public QiContext getQiContext() {
        return qiContext;
    }

    /**
     * returns the ID of this app
     * @return
     */
    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * returns the current fragment / view
     * @return
     */
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
            Log.d("Set Topic", topicName);
            this.currentChatBot.goToBookmarkNewTopic("init", topicName);
        }
        Log.d("Sett fragment", fragment.getClass().getSimpleName());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left, R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        //transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * should detect people around the robot
     */
    private void findHumansAround() {
        Log.i(TAG, "findHumansAround");
        // Get the humans around the robot.
        Future<List<Human>> humansAroundFuture = humanAwareness.async().getHumansAround();
        humansAroundFuture.andThenConsume(humansAround -> {
            Log.i(TAG, humansAround.size() + " human(s) around.");
            retrieveCharacteristics(humansAround);
        });
    }

    /**
     * Processes the information about people around the robot
     * @param humans
     */
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
        }
        /*
        if (persons != null && persons.length > 1) {
            Log.i(TAG, "Hier sind aber viele Leute - beep boop");
            Log.i(TAG, "Status : " + status);
            if (!status.equals(this.DEFAULT_STRING) && !status.equals("kind")) {
                //status_check = true;
                qiChatStatus.async().setValue(status);
            }
        }
        */
    }

    /**
     * returns the basic emotion
     * @param excitement
     * @param pleasure
     * @return
     */
    private String computeBasicEmotion(String excitement, String pleasure) {
        if (excitement.equals(this.DEFAULT_STRING) || pleasure.equals(this.DEFAULT_STRING)) return this.DEFAULT_STRING;
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
}


/* ----- ----- E O F ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */