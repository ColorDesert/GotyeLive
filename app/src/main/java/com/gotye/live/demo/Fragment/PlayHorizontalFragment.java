package com.gotye.live.demo.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gotye.live.demo.ChangeDirection;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.R;
import com.gotye.live.demo.ShareParams;
import com.gotye.live.demo.activity.BaseActivity;
import com.gotye.live.demo.activity.BasePlayActivity;
import com.gotye.live.demo.view.ChatRoom;
import com.gotye.live.demo.view.GLSurfaceViewContainer;
import com.gotye.live.demo.view.ListVideoPreset;
import com.gotye.live.demo.view.ThirdShare;
import com.gotye.live.demo.view.ViewHelper;
import com.gotye.live.player.GLSurfaceView;
import com.gotye.live.player.VideoQuality;

import java.util.ArrayList;

/**
 * Created by psp on 2015/12/11.
 */
public class PlayHorizontalFragment extends Fragment {

    private ChangeDirection changeDirection;
    private View rootView;
    private PopupWindow popup, popupShare;
    private ImageView chat, share, close, changeVideo, mute;
    private ThirdShare thirdShare;

    private LinearLayout llBottom;
    private boolean isShareShow, isAllShow = true;
    private GLSurfaceView surfaceView;
    private boolean isMute;
    private Handler handler;
    private Runnable runnable;

    private SeekBar seekBar;
    private AudioManager mAudioManager;
    private RelativeLayout surfaceViewContainer;
    private Context context;
    private GLSurfaceViewContainer glSurfaceViewContainer;
    public static boolean isChatShow = true;
    private LinearLayout rightTitle;
    private TextView tvPlayUserCount, tvVideoQuality;
    private ListVideoPreset listVideoPreset;
    public static ArrayList<VideoQuality> videoQualities;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_watch_horizontal, null);
        changeVideo = (ImageView) rootView.findViewById(R.id.change);
        changeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceViewContainer.removeAllViews();
                changeDirection.changeDirection();
            }
        });
        ((BaseActivity) getActivity()).chatRoom = (ChatRoom) rootView.findViewById(R.id.chatRoom);
        llBottom = (LinearLayout) rootView.findViewById(R.id.llBottom);
        rightTitle = (LinearLayout) rootView.findViewById(R.id.right);
        thirdShare = new ThirdShare(getActivity());
        chat = (ImageView) rootView.findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat();
            }
        });
        close = (ImageView) rootView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDirection.close();
            }
        });
        share = (ImageView) rootView.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
        tvVideoQuality = (TextView) rootView.findViewById(R.id.video_quality);
        tvVideoQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoQualities != null && videoQualities.size() > 0)
                    setVideoUrl();
            }
        });
        tvPlayUserCount = (TextView) rootView.findViewById(R.id.playuser_count);
        tvPlayUserCount.setText("当前在线人数" + ((BasePlayActivity) getActivity()).mPlayUserCount);
        surfaceViewContainer = (RelativeLayout) rootView.findViewById(R.id.surfaceViewContainer);
//        surfaceViewContainer.addView(MyApplication.glSurfaceViewContainer);
        surfaceViewContainer.addView(glSurfaceViewContainer);
        surfaceViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all();
            }
        });
        listVideoPreset = new ListVideoPreset(getActivity());
        ((BasePlayActivity) getActivity()).loadingView = rootView.findViewById(R.id.loading);
        ((BasePlayActivity) getActivity()).loadingText = (TextView) rootView.findViewById(R.id.loadingText);
        ((BasePlayActivity) getActivity()).loadingText.setText("");
        if (BasePlayActivity.isPlay || !((BasePlayActivity) getActivity()).playState.equals(""))
            ((BasePlayActivity) getActivity()).loadingView.setVisibility(View.GONE);
        else
            ((BasePlayActivity) getActivity()).loadingView.setVisibility(View.VISIBLE);
        ((BasePlayActivity) getActivity()).loadingText.setText(((BasePlayActivity) getActivity()).playState);
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekBar.setMax(max);
        seekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        ((BasePlayActivity) getActivity()).seekBar = seekBar;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mute = (ImageView) rootView.findViewById(R.id.mute);
        registerBoradcastReceiver();
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mute();
            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                //execute the task
                if (isAllShow) {
                    fadeAnimation(llBottom, 1);
                    fadeAnimation(rightTitle, 2);
                    isAllShow = false;
                }
            }
        };
        handler.postDelayed(runnable, 3000);


        return rootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        surfaceViewContainer.removeAllViews();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        changeDirection = (ChangeDirection) activity;
        glSurfaceViewContainer = ((BasePlayActivity) getActivity()).glSurfaceViewContainer;
        mAudioManager = ((BasePlayActivity) getActivity()).mAudioManager;


    }

    @Override
    public void onResume() {
        super.onResume();
        if (isChatShow) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((BaseActivity) getActivity()).chatRoom.getLayoutParams();
            lp.leftMargin = 0;
            ((BaseActivity) getActivity()).chatRoom.setLayoutParams(lp);
            chat.setImageResource(R.drawable.tool_chat_open);
        } else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((BaseActivity) getActivity()).chatRoom.getLayoutParams();
            lp.leftMargin = -(dip2px(290));
            ((BaseActivity) getActivity()).chatRoom.setLayoutParams(lp);
            chat.setImageResource(R.drawable.tool_chat_close);
            ((BaseActivity) getActivity()).chatRoom.getChatBubble().clear();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void setVideoUrl() {

        popup = new PopupWindow(getActivity());
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setContentView(listVideoPreset);
        listVideoPreset.setChangeUrlCallBack(videoQualities, new ListVideoPreset.ChangeVideoQuality() {
            @Override
            public void changeQuality(VideoQuality videoQuality) {
                MyApplication.player.setVideoQuality(videoQuality);
                tvVideoQuality.setText(videoQuality.toString());
                popup.dismiss();
            }

            @Override
            public void cancel() {
                popup.dismiss();
            }
        });

        popup.setFocusable(true);
        popup.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }

    private void mute() {
        if (isMute == true) {
            mute.setImageResource(R.drawable.volume_open);
            MyApplication.player.setMute(false);
        } else {
            mute.setImageResource(R.drawable.volume_close);
            MyApplication.player.setMute(true);
        }
        isMute = !isMute;
    }

    private void all() {
        handler.removeCallbacks(runnable);
        if (isAllShow) {
            fadeAnimation(llBottom, 1);
            fadeAnimation(rightTitle, 2);
            isAllShow = false;
        } else {
            llBottom.setVisibility(View.VISIBLE);
            rightTitle.setVisibility(View.VISIBLE);
            ariseAnimation(llBottom, 1);
            ariseAnimation(rightTitle, 2);
            isAllShow = true;
            handler.postDelayed(runnable, 3000);
        }
    }

    private void fadeAnimation(View view, int type) {
        Animation translateAnimation;
        if (type == 1) {
            translateAnimation = new TranslateAnimation(0, 0, 0, ViewHelper.getHeight(view));
            translateAnimation.setDuration(500);
            view.startAnimation(translateAnimation);

        } else {
            translateAnimation = new TranslateAnimation(0, ViewHelper.getHeight(view), 0, 0);
            translateAnimation.setDuration(500);
            view.startAnimation(translateAnimation);
        }
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llBottom.setVisibility(View.GONE);
                rightTitle.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void ariseAnimation(View view, int type) {
        if (type == 1) {
            Animation translateAnimation = new TranslateAnimation(0, 0, ViewHelper.getHeight(view), 0);
            translateAnimation.setDuration(500);
            view.startAnimation(translateAnimation);

        } else {
            Animation translateAnimation = new TranslateAnimation(ViewHelper.getHeight(view), 0, 0, 0);
            translateAnimation.setDuration(500);
            view.startAnimation(translateAnimation);
        }
    }


    private void chat() {
        if (isChatShow) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((BaseActivity) getActivity()).chatRoom.getLayoutParams();
            lp.leftMargin = -(dip2px(290));
            ((BaseActivity) getActivity()).chatRoom.setLayoutParams(lp);
            chat.setImageResource(R.drawable.tool_chat_close);
            ((BaseActivity) getActivity()).chatRoom.getChatBubble().clear();
            isChatShow = false;
        } else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((BaseActivity) getActivity()).chatRoom.getLayoutParams();
            lp.leftMargin = 0;
            ((BaseActivity) getActivity()).chatRoom.setLayoutParams(lp);
            chat.setImageResource(R.drawable.tool_chat_open);
            isChatShow = true;
        }

    }

    private void share() {

        popupShare = new PopupWindow(getActivity());
        popupShare.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupShare.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        Drawable dr = this.getResources().getDrawable(R.drawable.sheer_bg);
        popupShare.setBackgroundDrawable(dr);
        ImageView shareClose = (ImageView) thirdShare.findViewById(R.id.shut);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupShare.dismiss();

            }
        });
        popupShare.setContentView(thirdShare);
        thirdShare.setListener(new ShareParams(getActivity()));
        popupShare.setFocusable(true);
        popupShare.showAtLocation(rootView, Gravity.CENTER, 0, 0);

    }

    public int dip2px(float dipValue) {
        final float scale = getActivity().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public void updataCount() {

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MyApplication.ACTION_NAME)) {
                tvPlayUserCount.setText("当前在线人数" + intent.getExtras().getInt("count"));
            }
        }

    };

    private void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(MyApplication.ACTION_NAME);
        //注册广播
        getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }
}
