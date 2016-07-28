package com.gotye.live.demo.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gotye.live.demo.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBubble extends LinearLayout {

    private Context context;
    private LayoutInflater inflater;
    private int mHeight;
    private RelativeLayout.LayoutParams p;
    private ScrollView scrollView;
    private ArrayList<View> viewArrayList = new ArrayList<>();
    private Handler handler;
    private int height;
    private LinearLayout linearLayout;


    public ChatBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        this.context = context;
        init();
    }

    public ChatBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.context = context;
        init();
    }

    public ChatBubble(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
        init();
    }

    private void init() {
        inflater = (LayoutInflater) context
                .getSystemService(context.LAYOUT_INFLATER_SERVICE);
        handler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        addView(linearLayout);
                        break;
                    case 2:
                        View view = getChildAt(0);
                        view.setAlpha(0);
                        removeView(view);

                        try {
                            int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                            int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                            measure(w, h);
                            height = getMeasuredHeight();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        if (height != 0 && height > ViewHelper.getWindowHeight(context) / 2)
                            handler.sendEmptyMessage(2);
                        break;
                }
            }

        };

    }

    public void clear() {
        removeAllViews();
    }

    public void setHeight(int height) {
        mHeight = height;

    }

    public void addChat(String name, String information) {
        final LinearLayout linearLayout = (LinearLayout) inflater.inflate(
                R.layout.list_item, null);
        TextView infor = (TextView) linearLayout.findViewById(R.id.infor);
        TextView nickName = (TextView) linearLayout.findViewById(R.id.name);
        nickName.setText(name + " ");
        SpannableStringBuilder sb = handler(information);
        infor.setText(sb);

        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                addView(linearLayout);

            }
        });

        Animation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(5000);
        alphaAnimation.setStartOffset(2000);

        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        alphaAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearLayout.setVisibility(GONE);
            }
        });
        height = 0;
        int width;
        linearLayout.startAnimation(alphaAnimation);

        try {
            int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            measure(w, h);
            height = getMeasuredHeight();

        } catch (Exception e) {
            // TODO: handle exception
        }


        if (height != 0 && height > ViewHelper.getWindowHeight(context) / 2) {
            handler.sendEmptyMessage(2);

        }


    }

    private SpannableStringBuilder handler( String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "\\[s\\d{1,2}\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String tempText = m.group();
            String regEx="[^0-9]";
            Pattern p1 = Pattern.compile(regEx);
            Matcher m1 = p1.matcher(tempText);
            tempText = m1.replaceAll("").trim();
            String png = "face/png/smiley_"+tempText+".png";

            try {
                sb.setSpan(new ImageSpan(context, BitmapFactory.decodeStream(context.getAssets().open(png))), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return sb;
    }

}
