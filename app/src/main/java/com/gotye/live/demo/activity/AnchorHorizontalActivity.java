package com.gotye.live.demo.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.gotye.live.core.GLCore;
import com.gotye.live.core.GLRoomSession;
import com.gotye.live.core.model.LiveContext;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.R;
import com.gotye.live.demo.ShareParams;
import com.gotye.live.demo.view.ChatRoom;
import com.gotye.live.demo.view.ListVideoPreset;
import com.gotye.live.demo.view.ThirdShare;
import com.gotye.live.publisher.GLPublisher;
import com.gotye.live.publisher.GLRoomPublisher;
import com.gotye.live.publisher.GLVideoView;
import com.gotye.live.publisher.VideoPreset;

import java.util.Timer;
import java.util.TimerTask;

public class AnchorHorizontalActivity extends BaseActivity implements GLRoomPublisher.Listener {
    private GLVideoView gotyeVideoView;
    private ImageView chat, share, close, change, flash, camera, record, microphone, rec, recIcon, filter;
    private TextView time, tvPlayUserCount;
    private LinearLayout title, extension, llRec;
    private static final String TAG = "Record";
    private PopupWindow popupChat, popup;
    private boolean isChatShow = true, isTitleShow = true, isMicrophone = true, isFront = true, isRecord, isFlash, isRec = true, enableFilter = false;
    private ThirdShare thirdShare;
    private TimerTask task;
    private Timer timer;
    private int count;
    private Handler handler;
    private ProgressDialog publisherDialog;
    private TextView selectVideoPreset;
    private ListVideoPreset listVideoPreset;
    private Animation alphaAnimation = null;

    public static final int PERMISSION_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        orientation = BaseActivity.Orientation.Horizontal;
        if (!isPad()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anchor_horizontal);
        chatRoom = (ChatRoom) findViewById(R.id.chatRoom);
        tvPlayUserCount = (TextView) findViewById(R.id.playuser_count);
        llRec = (LinearLayout) findViewById(R.id.ll_rec);
        thirdShare = new ThirdShare(this);
        chat = (ImageView) findViewById(R.id.chat);
        share = (ImageView) findViewById(R.id.share);
        close = (ImageView) findViewById(R.id.close);
        rec = (ImageView) findViewById(R.id.rec);
        change = (ImageView) findViewById(R.id.change);
        flash = (ImageView) findViewById(R.id.flash);
        camera = (ImageView) findViewById(R.id.camera);
        record = (ImageView) findViewById(R.id.play);
        microphone = (ImageView) findViewById(R.id.microphone);
        time = (TextView) findViewById(R.id.time);
        recIcon = (ImageView) findViewById(R.id.icon_rec);
        filter = (ImageView) findViewById(R.id.filter);

        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);


        title = (LinearLayout) findViewById(R.id.title);
        extension = (LinearLayout) findViewById(R.id.extension);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change();
            }
        });
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera();
            }
        });
        microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                microphone();
            }
        });
        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flash();
            }
        });
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRec();
            }
        });
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter();
            }
        });
        gotyeVideoView = (GLVideoView) findViewById(R.id.surfaceView);
        gotyeVideoView.setDisplayMode(GLVideoView.SCREEN_FILL);
        selectVideoPreset = (TextView) findViewById(R.id.select_resolution);
        selectVideoPreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecord)
                    setVideoPreset();
            }
        });
        selectVideoPreset.setText(MyApplication.publisher.getVideoPreset().getVideoWidth() + "x" + MyApplication.publisher.getVideoPreset().getVideoHeight());
        listVideoPreset = new ListVideoPreset(this);
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                time.setText((count / 60 < 10 ? "0" + count / 60 : count / 60) + ":" + (count % 60 < 10 ? "0" + count % 60 : count % 60));
                if (count % 60 == 0)
                    MyApplication.roomSession.getLiveContext(new GLRoomSession.Callback<LiveContext>() {
                        @Override
                        public void onFinish(int code, LiveContext object) {
                            tvPlayUserCount.setText("当前在线人数" + object.getPlayUserCount());
                        }
                    });

            }
        };
        task = new TimerTask() {
            public void run() {

                if (isRecord) {
                    count++;
                    handler.sendEmptyMessage(1);
                }
            }
        };
        timer = new Timer(true);
        timer.schedule(task, 1000, 1000);
//        recIcon.setVisibility(View.VISIBLE);
//        recIcon.startAnimation(alphaAnimation);
//        MyApplication.publisher.beginRecording();
        MyApplication.publisher.setListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startPreview();
        } else {
            //申请相机以及录音权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MyApplication.publisher.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBroadcastReceiver);
        handler.removeMessages(1);
        onPublisherStop();
        MyApplication.publisher.unpublish();
//        MyApplication.publisher.stop();


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBoradcastReceiver();
        MyApplication.roomSession.getLiveContext(new GLRoomSession.Callback<LiveContext>() {
            @Override
            public void onFinish(int code, LiveContext object) {
                tvPlayUserCount.setText("当前在线人数" + object.getPlayUserCount());
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startPreview();
            } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showToast("请打开摄像机权限");
            } else {
                showToast("请打开录音权限");
            }
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void startPreview() {
        MyApplication.publisher.startPreview(gotyeVideoView, isFront, new GLPublisher.PreviewCallback() {
            @Override
            public void onCameraOpen(boolean success, boolean isFront) {
                if (!success) {
                    showToast("打开" + (isFront ? "前置" : "后置") + "摄像头失败");
                }
            }
        });
        MyApplication.publisher.setWatermark(BitmapFactory.decodeResource(getResources(), R.drawable.watermark), 20, 20, 228, 64);
    }

    private boolean isPad() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        // 屏幕宽度
        float screenWidth = display.getWidth();
        // 屏幕高度
        float screenHeight = display.getHeight();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        // 屏幕尺寸
        double screenInches = Math.sqrt(x + y);
        // 大于6尺寸则为Pad
        if (screenInches >= 7.0) {
            return true;
        }
        return false;
    }


    private void onPublisherStop() {
        count = 0;
        time.setText("");
        isRecord = false;
        if (isFlash) {
//            MyApplication.publisher.enableTorch(false);
            isFlash = false;
            flash.setImageResource(R.drawable.tool_flashlight_close);
        }
        record.setImageResource(R.drawable.tool_play);
    }

    private synchronized void record() {

        if (isRecord) {

            MyApplication.publisher.unpublish();

            isRecord = false;
            record.setImageResource(R.drawable.tool_play);
            time.setText("");
            count = 0;

        } else {
            publisherDialog = new ProgressDialog(AnchorHorizontalActivity.this);
            publisherDialog.setCancelable(false);
            publisherDialog.setCanceledOnTouchOutside(false);
            publisherDialog.show();
            MyApplication.publisher.publish();

        }
    }

    private synchronized void camera() {
        isFront = !isFront;
        MyApplication.publisher.switchCamera();
        if (isFront) {
            isFlash = false;
            flash.setImageResource(R.drawable.tool_flashlight_close);
        }
    }

    private void changeRec() {
        if (isRec) {

            MyApplication.publisher.endRecording(new GLRoomPublisher.Callback() {
                @Override
                public void onCallback(int status) {
                }
            });
            rec.setImageResource(R.drawable.stop_rec);
            if(recIcon.getAnimation()!=null)
            recIcon.getAnimation().cancel();
            recIcon.setVisibility(View.GONE);

            isRec = false;

        } else {
            rec.setImageResource(R.drawable.start_rec);
            if (isRecord) {
                recIcon.setVisibility(View.VISIBLE);
                recIcon.startAnimation(alphaAnimation);
                MyApplication.publisher.beginRecording();
            }
            isRec = true;
        }

    }

    private void microphone() {

        if (isMicrophone) {
            microphone.setImageResource(R.drawable.tool_microphone_close);
            MyApplication.publisher.setMute(true);
        } else {
            microphone.setImageResource(R.drawable.tool_microphone_open);
            MyApplication.publisher.setMute(false);
        }
        isMicrophone = !isMicrophone;

    }

    private void chat() {
        if (isChatShow) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chatRoom.getLayoutParams();
            lp.leftMargin = -(dip2px(280));
            chatRoom.setLayoutParams(lp);
            chat.setImageResource(R.drawable.tool_chat_close);
            chatRoom.getChatBubble().clear();
            isChatShow = false;
        } else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chatRoom.getLayoutParams();
            lp.leftMargin = 0;
            chatRoom.setLayoutParams(lp);
            chat.setImageResource(R.drawable.tool_chat_open);
            isChatShow = true;
        }

    }

    private void flash() {
        if (!isFront) {
            if (isFlash) {
                MyApplication.publisher.enableTorch(false);
                flash.setImageResource(R.drawable.tool_flashlight_close);
            } else {
                MyApplication.publisher.enableTorch(true);
                flash.setImageResource(R.drawable.tool_flashlight_open);
            }
            isFlash = !isFlash;
        }
    }

    private void setVideoPreset() {

        popup = new PopupWindow(this);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        popup.setContentView(listVideoPreset);
        listVideoPreset.setChangePresetCallBack(new ListVideoPreset.changePreset() {
            @Override
            public void changePreset(String preset, VideoPreset videoPreset) {
                selectVideoPreset.setText(preset);
                MyApplication.publisher.setVideoPreset(videoPreset);
                popup.dismiss();
            }

            @Override
            public void cancel() {
                popup.dismiss();
            }
        });
        popup.setFocusable(true);
        popup.showAtLocation(gotyeVideoView, Gravity.CENTER, 0, 0);
    }

    private void share() {
        popup = new PopupWindow(this);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        Drawable dr = this.getResources().getDrawable(R.drawable.sheer_bg);
        popup.setBackgroundDrawable(dr);
        popup.setContentView(thirdShare);
        thirdShare.setListener(new ShareParams(this));
        ImageView shareClose = (ImageView) thirdShare.findViewById(R.id.shut);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.setFocusable(true);
        popup.showAtLocation(gotyeVideoView, Gravity.CENTER, 0, 0);
    }

    private void change() {
        if (isTitleShow) {
            change.setImageResource(R.drawable.tool_pack_up);
            extension.setVisibility(View.GONE);
            isTitleShow = false;
        } else {
            change.setImageResource(R.drawable.tool_expand);
            extension.setVisibility(View.VISIBLE);
            isTitleShow = true;
        }
    }

    private void filter() {
        if (enableFilter) {
            enableFilter = false;
            filter.setImageResource(R.drawable.tool_filter_close);
        } else {
            enableFilter = true;
            filter.setImageResource(R.drawable.tool_filter_open);
        }

        MyApplication.publisher.enableBeautify2(enableFilter);

    }

    public int dip2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
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
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    @Override
    public void onPublisherForcelogout(GLPublisher publisher) {
        MyApplication.publisher.stop();
        isRecord = false;
        record.setImageResource(R.drawable.tool_play);
        time.setText("");
        count = 0;
//        Dialog alertDialog = new AlertDialog.Builder(AnchorHorizontalActivity.this).
//                setTitle("提示").
//                setMessage("你的帐号在别处登录").
//                setIcon(null).
//                setPositiveButton("确定", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//                        finish();
//
//                    }
//                }).
//                create();
//        alertDialog.show();
    }

    @Override
    public void onPublisherDisconnected(GLPublisher publisher) {
        publisherDialog.dismiss();
        onPublisherStop();
    }

    @Override
    public void onPublisherReconnecting(GLPublisher publisher) {
        publisherDialog = new ProgressDialog(AnchorHorizontalActivity.this);
        publisherDialog.setCancelable(false);
        publisherDialog.setCanceledOnTouchOutside(false);
        publisherDialog.show();
    }

    @Override
    public void onPublisherConnected(GLPublisher publisher) {
        record.setImageResource(R.drawable.tool_stop);
        if (isRec) {
            recIcon.setVisibility(View.VISIBLE);
            recIcon.startAnimation(alphaAnimation);
            MyApplication.publisher.beginRecording();
        }
        isRecord = true;
        publisherDialog.dismiss();
    }

    @Override
    public void onPublisherError(GLPublisher publisher, int errorCode) {
        publisherDialog.dismiss();
        onPublisherStop();
    }

    @Override
    public void onPublihserStatusUpdate(GLPublisher publisher) {

    }
}
