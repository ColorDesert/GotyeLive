package com.gotye.live.demo.activity;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gotye.live.core.Code;
import com.gotye.live.core.GLCore;
import com.gotye.live.core.GLRoomSession;
import com.gotye.live.core.model.AuthToken;
import com.gotye.live.core.model.LiveContext;
import com.gotye.live.core.model.Role;
import com.gotye.live.core.model.RoomIdType;
import com.gotye.live.demo.Fragment.PlayVerticalFragment;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.R;
import com.gotye.live.publisher.GLRoomPublisher;

import org.json.JSONObject;

public class LiveMainActivity extends Activity {

    private Button login;

    private LoginThread mLoginThread = null;
    private View mProgressView;
    private EditText mRoomIdView;
    private EditText mPasswordView;
    private EditText mNicknameView;
    private boolean isCancel = false;
    private ProgressDialog loginDialog;
    private GestureDetector mDoubleTapListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.live_activity_main);
//        CrashReport.initCrashReport(appContext, "注册时申请的APPID", false);
        PlayVerticalFragment.arrayList.clear();
        MyApplication.metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(MyApplication.metric);
        mProgressView = findViewById(R.id.login_progress);
        mRoomIdView = (EditText) findViewById(R.id.roomId);
        mNicknameView = (EditText) findViewById(R.id.nickname);
        mNicknameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mPasswordView = (EditText) findViewById(R.id.password);
        mRoomIdView.setText(getLastRoomId());
        mNicknameView.setText(getLastNickname());
        mPasswordView.setText(getLastPassword());
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        mDoubleTapListener = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LiveMainActivity.this);
                builder.setTitle("修改AK");
                final EditText input = new EditText(LiveMainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setText(MyApplication.currentAK);
                builder.setView(input);
                builder.setPositiveButton("取消", null);
                builder.setNegativeButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MyApplication.updateAccessKey(LiveMainActivity.this, input.getText().toString());
                    }
                });
                builder.show();
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDoubleTapListener.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class LoginThread extends Thread {

        String roomId, password, nickaname;
        RoomIdType type;

        public LoginThread(String roomId, String password, String nickname, RoomIdType type) {
            this.roomId = roomId;
            this.password = password;
            this.nickaname = nickname;
            this.type = type;
            isCancel = false;
        }

        @Override
        public void run() {
            super.run();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //如果在登录时取消，则退出session
                    if (isCancel) {
                        MyApplication.roomSession.destroy();
                        mLoginThread = null;
                        handleProgress(false);
                        return;
                    }
                    MyApplication.roomSession.setRoomId(roomId);
                    MyApplication.roomSession.setPwd(password);
                    MyApplication.roomSession.setNickName(nickaname);
                    MyApplication.roomSession.setRoomIdType(type);
                    //首先到服务器验证session取得accessToken和role等信息
                    MyApplication.roomSession.auth(new GLRoomSession.Callback<AuthToken>() {
                        @Override
                        public void onFinish(int code, AuthToken object) {
                            if (isCancel || code != Code.SUCCESS) {
                                //session验证失败
                                MyApplication.roomSession.destroy();
                                mLoginThread = null;
                                handleProgress(false);
                                Toast.makeText(LiveMainActivity.this, "session验证失败", Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (Role.HOST == object.getRole())
                                MyApplication.publisher.login(false, new callback());
                            else {
                                handleProgress(false);
                                Intent intent = new Intent(LiveMainActivity.this, BasePlayActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            });
        }

        public void cancel() {
            isCancel = true;
            if (MyApplication.im != null) {
                MyApplication.im.logout();
            }
            MyApplication.roomSession.destroy();
        }
    }

    private class callback implements GLRoomPublisher.Callback {
        @Override
        public void onCallback(int i) {
            if (i == com.gotye.live.publisher.Code.OCCUPIED) {
                handleProgress(false);
                if (isCancel) {
                    MyApplication.roomSession.destroy();
                    MyApplication.im.logout();
                    mLoginThread = null;
                    return;
                }
                Dialog alertDialog = new AlertDialog.Builder(LiveMainActivity.this).
                        setTitle("提示").
                        setMessage("该ID已经有人登录，还要继续登录吗？").
                        setIcon(null).
                        setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                loginDialog = new ProgressDialog(LiveMainActivity.this);
                                loginDialog.setMessage("正在登录...");
                                loginDialog.setCancelable(true);
                                loginDialog.setCanceledOnTouchOutside(true);
                                loginDialog.show();
                                MyApplication.publisher.login(true, new callback());
                            }
                        }).
                        setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        }).
                        create();
                alertDialog.show();
            } else if (i == com.gotye.live.publisher.Code.SUCCESS) {
                handleProgress(false);
                Intent intent = new Intent(LiveMainActivity.this, AnchorHorizontalActivity.class);
                startActivity(intent);
            } else {
                handleProgress(false);
                MyApplication.roomSession.destroy();
                Toast.makeText(LiveMainActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                MyApplication.im.logout();
                mLoginThread = null;
            }
        }
    }

    private void handleProgress(final boolean show) {
        if (show) {
            loginDialog.show();
        } else {
            loginDialog.dismiss();
        }
    }

    private void attemptLogin() {
//        if (mLoginThread != null) {
//            return;
//        }
        if (MyApplication.im != null) {
            MyApplication.im.logout();
        }

        MyApplication.player.stop();
        MyApplication.roomSession.destroy();

        MyApplication.isFirst = true;
        mLoginThread = null;
        // Reset errors.
        mRoomIdView.setError(null);
        mPasswordView.setError(null);
        mNicknameView.setError(null);

        // Store values at the time of the login attempt.
        String roomId = mRoomIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        String nickname = mNicknameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(roomId)) {
            mRoomIdView.setError(getString(R.string.error_field_required));
            focusView = mRoomIdView;
            cancel = true;
        }

        if (TextUtils.isEmpty(nickname)) {
            mNicknameView.setError(getString(R.string.error_field_required));
            focusView = mNicknameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            save(roomId, password, nickname);
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginDialog = new ProgressDialog(this);
            loginDialog.setMessage("正在登录...");
            loginDialog.setCancelable(true);
            loginDialog.setCanceledOnTouchOutside(true);
            loginDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mLoginThread != null) {
                        mLoginThread.cancel();
                        mLoginThread = null;

                        handleProgress(false);
                        if (MyApplication.im != null) {
                            MyApplication.im.logout();
                        }
                        MyApplication.roomSession.destroy();
                        return;
                    }
                }
            });
            handleProgress(true);

            mLoginThread = new LoginThread(roomId, password, nickname, RoomIdType.GOTYE);
            mLoginThread.start();
        }
    }

    private void save(String roomId, String password, String nickname) {
        SharedPreferences sp = getSharedPreferences("config", 0);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("roomId", roomId);
        ed.putString("password", password);
        ed.putString("nickname", nickname);
        ed.commit();
    }

    private String getLastRoomId() {
        return getSharedPreferences("config", 0).getString("roomId", "");
    }

    private String getLastPassword() {
        return getSharedPreferences("config", 0).getString("password", "");
    }

    private String getLastNickname() {
        return getSharedPreferences("config", 0).getString("nickname", "");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //按下的如果是BACK，同时没有重复
            isCancel = true;
            if (mLoginThread != null) {

                mLoginThread = null;
                handleProgress(false);
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);

    }
//    @Override
//    public void onBackPressed() {
//
//        if (mLoginThread != null) {
//            mLoginThread.cancel();
//            mLoginThread = null;
//
//            handleProgress(false);
//            if (MyApplication.im != null) {
//                MyApplication.im.logout();
//            }
////            return;
//        }
//        super.onBackPressed();
//    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 1;
    }

}
