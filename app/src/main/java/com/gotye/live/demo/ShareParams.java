package com.gotye.live.demo;

import android.content.Context;

import com.gotye.live.core.Code;
import com.gotye.live.core.GLCore;
import com.gotye.live.core.GLRoomSession;
import com.gotye.live.core.model.ClientUrl;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.view.ThirdShare;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by psp on 2015/12/3.
 */
public class ShareParams implements ThirdShare.OnShareCloseClick, PlatformActionListener {
    private Context mContext;
    public ShareParams(Context context)
    {
        mContext = context ;
    }
    @Override
    public void onShareCloseClickListener() {

    }

    @Override
    public void onShareWechatClickListener() {
        MyApplication.roomSession.getClientUrl(new GLRoomSession.Callback<ClientUrl>() {
            @Override
            public void onFinish(int code, ClientUrl object) {
                if (code != Code.SUCCESS) {
                    return;
                }

                //2、设置分享内容
//                ShareSDK.initSDK(mContext);
                //2、设置分享内容
                Platform.ShareParams sp = new Platform.ShareParams();
                sp.setShareType(Platform.SHARE_WEBPAGE);//非常重要：一定要设置分享属性
                sp.setTitle("我是分享标题");  //分享标题
                sp.setText(object.getEducVisitorUrl());   //分享文本
//                        sp.setImageUrl("http://7sby7r.com1.z0.glb.clouddn.com/CYSJ_02.jpg");//网络图片rul
                sp.setUrl(object.getEducVisitorUrl());   //网友点进链接后，可以看到分享的详情

                //3、非常重要：获取平台对象
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                wechat.setPlatformActionListener(ShareParams.this); // 设置分享事件回调
                // 执行分享
                wechat.share(sp);
            }
        });
    }

    @Override
    public void onShareWechatMomentsClickListener() {
        MyApplication.roomSession.getClientUrl(new GLRoomSession.Callback<ClientUrl>() {
            @Override
            public void onFinish(int code, ClientUrl object) {
                if (code != Code.SUCCESS)
                    return;
                //                ShareSDK.initSDK(mContext);
                //2、设置分享内容
                Platform.ShareParams sp = new Platform.ShareParams();
                sp.setShareType(Platform.SHARE_WEBPAGE); //非常重要：一定要设置分享属性
                sp.setTitle("我是分享标题");  //分享标题
                sp.setText(object.getEducVisitorUrl());   //分享文本
                sp.setImageUrl("http://7sby7r.com1.z0.glb.clouddn.com/CYSJ_02.jpg");//网络图片rul
                sp.setUrl(object.getEducVisitorUrl());
                //3、非常重要：获取平台对象
                Platform wechatMoments = ShareSDK.getPlatform(WechatMoments.NAME);
                wechatMoments.setPlatformActionListener(ShareParams.this); // 设置分享事件回调
                // 执行分享
                wechatMoments.share(sp);
            }
        });

    }

    @Override
    public void onShareSinaWeiboClickListener() {
        MyApplication.roomSession.getClientUrl(new GLRoomSession.Callback<ClientUrl>() {
            @Override
            public void onFinish(int code, ClientUrl object) {
                if (code != Code.SUCCESS)
                    return;
                //                ShareSDK.initSDK(mContext);
                //2、设置分享内容
                //2、设置分享内容
                Platform.ShareParams sp = new Platform.ShareParams();
                sp.setText(object.getEducVisitorUrl()); //分享文本
                sp.setImageUrl("http://7sby7r.com1.z0.glb.clouddn.com/CYSJ_02.jpg");//网络图片rul
                sp.setUrl(object.getEducVisitorUrl());

                //3、非常重要：获取平台对象
                Platform sinaWeibo = ShareSDK.getPlatform(SinaWeibo.NAME);
                sinaWeibo.setPlatformActionListener(ShareParams.this); // 设置分享事件回调
                // 执行分享
                sinaWeibo.share(sp);
            }
        });
    }


    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {

    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {

    }

    @Override
    public void onCancel(Platform platform, int i) {

    }
}
