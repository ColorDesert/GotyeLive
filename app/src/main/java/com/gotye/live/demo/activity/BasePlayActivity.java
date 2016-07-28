package com.gotye.live.demo.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gotye.live.core.GLCore;
import com.gotye.live.core.GLRoomSession;
import com.gotye.live.core.model.LiveContext;
import com.gotye.live.demo.ChangeDirection;
import com.gotye.live.demo.Fragment.PlayHorizontalFragment;
import com.gotye.live.demo.Fragment.PlayVerticalFragment;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.R;
import com.gotye.live.demo.view.GLSurfaceViewContainer;
import com.gotye.live.player.Code;
import com.gotye.live.player.GLPlayer;
import com.gotye.live.player.GLRoomPlayer;
import com.gotye.live.player.VideoQuality;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class BasePlayActivity extends BaseActivity
        implements ChangeDirection, GLRoomPlayer.Listener {

    public SeekBar seekBar;
    public AudioManager mAudioManager;
    public RelativeLayout surfaceViewContainer;
    public GLSurfaceViewContainer glSurfaceViewContainer;
    private boolean isChange;
    public View loadingView;
    public TextView loadingText;
    public ProgressBar progressBar;
    public static boolean isPlay;
    public String playState;
    public int mPlayUserCount;
    public LinkedHashMap<String, String> urlsHashMap = new LinkedHashMap<>();
    private Timer updateUserCountTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//保持全屏，不显示系统状态栏

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        playState = "";
        orientation = Orientation.Vertical;
//        orientation = Orientation.Horizontal;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setContentView(R.layout.activity_base);
        glSurfaceViewContainer = new GLSurfaceViewContainer(this);
        MyApplication.player.setSurfaceView(glSurfaceViewContainer.getSurfaceView());
        MyApplication.player.setVideoQualityCallback(new GLRoomPlayer.VideoQualityCallback() {
            @Override
            public void onGetVideoQualities(List<VideoQuality> videoQualities) {
                PlayHorizontalFragment.videoQualities = new ArrayList<>(videoQualities);
            }
        });
//        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.fragmnet);
//        frameLayout.addView(glSurfaceViewContainer);
        Fragment fragment = new PlayVerticalFragment();
//        Fragment fragment = new PlayHorizontalFragment();
        FragmentTransaction fragmentManager = getFragmentManager().beginTransaction().replace(R.id.fragmnet, fragment);
        fragmentManager.commit();
        updateUserCountTimer = new Timer();
        updateUserCountTimer.schedule(new MyTask(), 1000, 60000);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//            }
//        }, 3000);

        MyApplication.player.play();
    }

    @Override
    public void onPlayerDisconnected(GLPlayer player) {
        loadingView.setVisibility(View.VISIBLE);
        isPlay = false;
        playState = "网络异常";
        loadingText.setText(playState);
    }

    @Override
    public void onPlayerReconnecting(GLPlayer player) {
        loadingView.setVisibility(View.VISIBLE);
        loadingText.setText("正在连接...");
        isPlay = false;
    }

    @Override
    public void onPlayerConnected(GLPlayer player) {
        loadingView.setVisibility(View.GONE);
        loadingText.setText("");
        Toast.makeText(this, "开始播放", Toast.LENGTH_LONG).show();
        isPlay = true;
    }

    @Override
    public void onPlayerError(GLPlayer player, int errorCode) {
        isPlay = false;
        loadingView.setVisibility(View.GONE);
        loadingText.setVisibility(View.VISIBLE);
        switch (errorCode) {
            case Code.LIVE_NOT_STARTTEDYET:
                loadingText.setText("直播未开始");
                break;
            case Code.NETWORK_DISCONNECT:
                loadingText.setText("网络断开");
                break;
            case Code.GET_LIVE_STATE_FAILED:
                loadingText.setText("获取直播状态失败");
                break;
            case Code.VERIFY_FAILED:
                loadingText.setText("token无效");
                break;
            case Code.FAILED:
                loadingText.setText("失败");
                break;
            case Code.GET_LIVE_URL_FAILED:
                loadingText.setText("获取直播URL失败");
                break;
        }

    }

    @Override
    public void onPlayerStatusUpdate(GLPlayer player) {

    }

    @Override
    public void onLiveStateChanged(GLRoomPlayer player, GLRoomPlayer.LiveState state) {
        Toast.makeText(this, "主播状态改变 : " + state, Toast.LENGTH_SHORT).show();
        if (state == GLRoomPlayer.LiveState.STOPPED) {
            isPlay = false;
            playState = "直播结束";
            loadingText.setText(playState);
            loadingView.setVisibility(View.GONE);
        } else {
            isPlay = true;
            playState = "";
            loadingText.setText(playState);
            loadingView.setVisibility(View.VISIBLE);
        }
    }


    class MyTask extends TimerTask {

        @Override
        public void run() {
            MyApplication.roomSession.getLiveContext(new GLRoomSession.Callback<LiveContext>() {
                @Override
                public void onFinish(int code, LiveContext object) {
                    mPlayUserCount = object.getPlayUserCount();
                    Intent mIntent = new Intent(MyApplication.ACTION_NAME);
                    mIntent.putExtra("count", mPlayUserCount);
                    //发送广播
                    sendBroadcast(mIntent);
                }
            });

        }

    }

    @Override
    public void changeDirection() {
        isChange = true;
        if (orientation == Orientation.Horizontal) {
            orientation = Orientation.Vertical;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Fragment fragment = new PlayVerticalFragment();
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction().replace(R.id.fragmnet, fragment);
            fragmentManager.commit();
        } else {
            orientation = Orientation.Horizontal;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Fragment fragment = new PlayHorizontalFragment();
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction().replace(R.id.fragmnet, fragment);
            fragmentManager.commit();
        }
        playState = loadingText.getText().toString();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//            }
//        }, 4000);

    }

    @Override
    public void close() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.player.setListener(this);
        MyApplication.player.onResume();
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApplication.player.onStop();
        isPlay = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.player.onDestroy();
        updateUserCountTimer.cancel();
        updateUserCountTimer.purge();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekBar.setMax(max);
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:// 音量增大
                seekBar.setProgress(++currentVolume);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:// 音量减小
                seekBar.setProgress(--currentVolume);
                break;
            case KeyEvent.KEYCODE_BACK:// 返回键
                return super.onKeyDown(keyCode, event);
            default:
                break;
        }
        return true;

    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        if (isChange) {
//            isChange = false;
//            super.onConfigurationChanged(newConfig);
//            return;
//        }
//        if (surfaceViewContainer != null)
//            surfaceViewContainer.removeAllViews();
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            orientation = Orientation.Horizontal;
//            Fragment fragment = new PlayHorizontalFragment();
//            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction().replace(R.id.fragmnet, fragment);
//            fragmentManager.commit();
//        }
//        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            orientation = Orientation.Vertical;
//            Fragment fragment = new PlayVerticalFragment();
//            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction().replace(R.id.fragmnet, fragment);
//            fragmentManager.commit();
//        }
//        super.onConfigurationChanged(newConfig);
//
//    }


}
