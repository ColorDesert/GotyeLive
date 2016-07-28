package com.gotye.live.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gotye.live.demo.R;

import static android.view.View.*;

public class ThirdShare extends LinearLayout {

    private Context mContext;
    private OnShareCloseClick listener;

    public ThirdShare(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public ThirdShare(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public ThirdShare(Context context) {
        super(context);
        mContext = context;
        init();
    }
    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.share_title, null);
        addView(view);
        ImageView shutShare = (ImageView) view.findViewById(R.id.shut);
        shutShare.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onShareCloseClickListener();
                }
            }
        });
        ImageView Wechat,WechatMoments,SinaWeibo;
        Wechat = (ImageView)findViewById(R.id.wechat);
        WechatMoments = (ImageView)findViewById(R.id.wechatmoments);
        SinaWeibo = (ImageView)findViewById(R.id.sinaweibo);
        Wechat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onShareWechatClickListener();
            }
        });
        WechatMoments.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onShareWechatMomentsClickListener();
            }
        });
        SinaWeibo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onShareSinaWeiboClickListener();
            }
        });
    }

    public void setListener(OnShareCloseClick listener) {
        this.listener = listener;
    }

    public interface OnShareCloseClick {
        public void onShareCloseClickListener();
        public  void onShareWechatClickListener();
        public  void onShareWechatMomentsClickListener();
        public  void onShareSinaWeiboClickListener();
    }
}
