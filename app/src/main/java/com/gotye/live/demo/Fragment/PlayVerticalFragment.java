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
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gotye.live.chat.Ack;
import com.gotye.live.chat.Code;
import com.gotye.live.chat.Message;
import com.gotye.live.chat.SendMsgAck;
import com.gotye.live.chat.TextMessage;
import com.gotye.live.demo.ChangeDirection;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.R;
import com.gotye.live.demo.ShareParams;
import com.gotye.live.demo.activity.BaseActivity;
import com.gotye.live.demo.activity.BasePlayActivity;
import com.gotye.live.demo.view.ChatRoom;
import com.gotye.live.demo.view.GLSurfaceViewContainer;
import com.gotye.live.demo.view.ThirdShare;
import com.gotye.live.demo.view.ViewHelper;
import com.gotye.live.player.GLSurfaceView;

import junit.framework.Test;

import java.util.ArrayList;

/**
 * Created by psp on 2015/12/11.
 */
public class PlayVerticalFragment extends Fragment {
    private ImageView chat, share, close, changeVideo, mute;
    private PopupWindow popupShare;
    private ThirdShare thirdShare;

    private LinearLayout llBottom;
    private boolean isAllShow = true;
    private RelativeLayout surfaceViewContainerV;
    private GLSurfaceView surfaceView;
    private EditText contentEdit;
    private Button btSubmit;
    private boolean loginOut = true;
    private TextView tab;
    private boolean isMute;
    public static ArrayList<Message> arrayList = new ArrayList<>();
    private Handler handler;
    private Runnable runnable;
    private View rootView;
    private ChangeDirection changeDirection;
    private RelativeLayout surfaceViewContainer;
    private SeekBar seekBar;
    private AudioManager mAudioManager;
    private GLSurfaceViewContainer glSurfaceViewContainer;
    private TextView tvPlayUserCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_watch_vertical, null);

        contentEdit = (EditText) rootView.findViewById(R.id.content);
        thirdShare = new ThirdShare(getActivity());
        llBottom = (LinearLayout) rootView.findViewById(R.id.llBottom);
        surfaceViewContainer = (RelativeLayout) rootView.findViewById(R.id.surfaceViewContainer);

        surfaceViewContainer.addView(glSurfaceViewContainer);
        surfaceViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all();
            }
        });
        changeVideo = (ImageView) rootView.findViewById(R.id.change);
        changeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceViewContainer.removeAllViews();
                changeDirection.changeDirection();
            }
        });
        share = (ImageView) rootView.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
        close = (ImageView) rootView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDirection.close();
            }
        });
        tvPlayUserCount = (TextView)rootView.findViewById(R.id.playuser_count);
        tvPlayUserCount.setText("当前在线人数" + ((BasePlayActivity)getActivity()).mPlayUserCount+"");
        ((BasePlayActivity)getActivity()).loadingView = rootView.findViewById(R.id.loading);
        ((BasePlayActivity)getActivity()).loadingText = (TextView) rootView.findViewById(R.id.loadingText);
        if (BasePlayActivity.isPlay||!((BasePlayActivity)getActivity()).playState.equals(""))
            ((BasePlayActivity)getActivity()).loadingView.setVisibility(View.GONE);
        else
            ((BasePlayActivity)getActivity()).loadingView.setVisibility(View.VISIBLE);
        ((BasePlayActivity)getActivity()).loadingText.setText(((BasePlayActivity)getActivity()).playState);
        tab = (TextView) rootView.findViewById(R.id.title);
        tab.setText("主播室名称:" + MyApplication.roomSession.getRoomId());
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        ((BasePlayActivity)getActivity()).seekBar = seekBar ;
        mAudioManager = (AudioManager)getActivity(). getSystemService(Context.AUDIO_SERVICE);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekBar.setMax(max);
        seekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
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
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mute();
            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isAllShow) {
                    fadeAnimation(llBottom, 1);
                    isAllShow = false;
                }
            }
        };
        handler.postDelayed(runnable, 3000);
        ((BaseActivity)getActivity()).chatRoom = (ChatRoom) rootView.findViewById(R.id.chatRoom);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ((BaseActivity)getActivity()).chatRoom.getllinput().getLayoutParams();
        lp.width = dip2px(300);
        ((BaseActivity)getActivity()).chatRoom.getllinput().setLayoutParams(lp);
        if(!PlayHorizontalFragment.isChatShow)
            ((BaseActivity)getActivity()).chatRoom.setVisibility(View.INVISIBLE);
        registerBoradcastReceiver();
        return rootView;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        changeDirection = (ChangeDirection) activity;
        mAudioManager = ((BasePlayActivity) getActivity()).mAudioManager;
        glSurfaceViewContainer =   ((BasePlayActivity)getActivity()).glSurfaceViewContainer;

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        surfaceViewContainer.removeAllViews();
    }
    private final class ViewHolder {
        public TextView name;
        public TextView content;

    }

    private void all() {
        handler.removeCallbacks(runnable);
        if (isAllShow) {
            fadeAnimation(llBottom, 1);
            isAllShow = false;
        } else {
            llBottom.setVisibility(View.VISIBLE);
            ariseAnimation(llBottom, 1);
            isAllShow = true;
            new Handler().postDelayed(runnable, 3000);
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

    public class chatAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {

                holder = new ViewHolder();
                convertView = View.inflate(getActivity(), R.layout.list_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.content = (TextView) convertView.findViewById(R.id.infor);
                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText((CharSequence) arrayList.get(position).getSenderNickname() + ": ");
            holder.content.setText((CharSequence) arrayList.get(position).getText());
            return convertView;
        }
    }

    private void share() {

        popupShare = new PopupWindow(getActivity());
        popupShare.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupShare.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        Drawable dr = this.getResources().getDrawable(R.drawable.sheer_bg);
        ImageView shareClose = (ImageView) thirdShare.findViewById(R.id.shut);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupShare.dismiss();
            }
        });
        popupShare.setBackgroundDrawable(dr);
        popupShare.setContentView(thirdShare);
        thirdShare.setListener(new ShareParams(getActivity()));
        popupShare.setFocusable(true);
        popupShare.showAtLocation(rootView, Gravity.CENTER, 0, 0);

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
    public  int dip2px( float dipValue){
        final float scale = getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MyApplication.ACTION_NAME)){
                tvPlayUserCount.setText("当前在线人数" + intent.getExtras().getInt("count"));
            }
        }

    };
    private void registerBoradcastReceiver(){
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
