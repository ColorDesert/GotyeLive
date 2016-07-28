package com.gotye.live.demo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.gotye.live.chat.Ack;
import com.gotye.live.chat.ChatObserver;
import com.gotye.live.chat.GLChatSession;
import com.gotye.live.chat.LoginAck;
import com.gotye.live.chat.Message;
import com.gotye.live.core.Code;
import com.gotye.live.core.GLRoomSession;
import com.gotye.live.core.model.LiveContext;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.view.ChatRoom;

public class BaseActivity extends AppCompatActivity implements ChatObserver {
    public boolean loginOut = true;

    @Override
    public void onDisconnected(GLChatSession chatSession) {

    }

    @Override
    public void onReloginSuccess(GLChatSession chatSession) {

    }

    @Override
    public void onRelogining(GLChatSession chatSession) {

    }

    @Override
    public void onReloginFailed(GLChatSession chatSession) {

    }

    @Override
    public void onForceLogout(GLChatSession chatSession) {
        Dialog alertDialog = new AlertDialog.Builder(BaseActivity.this).
                setTitle("提示").
                setMessage("你的帐号在别处登录").
                setIcon(null).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        finish();

                    }
                }).
                create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onReceiveMessage(GLChatSession chatSession, Message message) {
        String a = message.getText();
        if (a.equals("enter")&&message.getMessageType()== Message.NOTIFY) {
            message.setText("进入了频道");

            MyApplication.roomSession.getLiveContext(new GLRoomSession.Callback<LiveContext>() {
                @Override
                public void onFinish(int code, LiveContext object) {
                    Intent mIntent = new Intent(MyApplication.ACTION_NAME);
                    mIntent.putExtra("count", object.getPlayUserCount());
                    sendBroadcast(mIntent);
                }
            });

        }
        chatRoom.getChatBubble().addChat(message.getSenderNickname(), message.getText());
    }


    public enum Orientation {Horizontal, Vertical}


    public Orientation orientation;
    public ChatRoom chatRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕不变黑
        MyApplication.im.addObserver(this);
        //进入频道
        MyApplication.im.login(new Ack<LoginAck>() {
            @Override
            public void ack(LoginAck loginAck) {
                if (loginAck.getCode() == Code.SUCCESS) {
                    MyApplication.im.sendNotify("enter", null);
                    chatRoom.getChatBubble().addChat("连接聊天服务器成功", "");
                } else {
                    chatRoom.getChatBubble().addChat("连接聊天服务器失败", "");

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginOut = true;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            MyApplication.im.removeObserver(BaseActivity.this);
            MyApplication.im.logout();
            MyApplication.roomSession.destroy();

    }
}
