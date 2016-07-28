package com.gotye.live.demo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import com.gotye.live.chat.GLChatSession;
import com.gotye.live.core.GLCore;
import com.gotye.live.core.GLRoomSession;
import com.gotye.live.player.GLRoomPlayer;
import com.gotye.live.publisher.GLRoomPublisher;

//import com.gotye.live.core.GLRoomSession;

public class MyApplication extends Application {

    private static final String ACCESS_KEY = "a11617e9fb20430b8f0f6785d462da18";
    private static final String APPKEY = "18796bb08dfd4e1f82e1e7e57a03894d";

    @Override
    public void onCreate() {
        super.onCreate();
        loadAccessKey();
//        ShareSDK.initSDK(this);
        roomSession = new GLRoomSession();
        im = new GLChatSession(roomSession);
        publisher = new GLRoomPublisher(roomSession);
        player = new GLRoomPlayer(roomSession);
    }

    public static GLChatSession im;
    public static GLRoomSession roomSession;
    public static GLRoomPublisher publisher;
    public static DisplayMetrics metric;
    public static final String ACTION_NAME = "UPDATA";
    public static GLRoomPlayer player;

    public static boolean isFirst = true;
    public static String currentAK;

    private void loadAccessKey() {
        SharedPreferences sharedPreferences = getSharedPreferences("ACCESS_KEY", 0);
        String ak = sharedPreferences.getString("ak", ACCESS_KEY);
        updateAccessKey(this, ak);
    }

    public static void updateAccessKey(Context context, String ak) {
        GLCore.registerApp(context, APPKEY, ACCESS_KEY, null);

        SharedPreferences sharedPreferences = context.getSharedPreferences("ACCESS_KEY", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ak", ak);
        editor.apply();

        currentAK = ak;
    }

}
