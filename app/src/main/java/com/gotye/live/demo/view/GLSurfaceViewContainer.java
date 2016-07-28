package com.gotye.live.demo.view;

import android.content.Context;

import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.gotye.live.player.GLSurfaceView;


/**
 * Created by psp on 2015/12/11.
 */
public class GLSurfaceViewContainer extends RelativeLayout {
    private GLSurfaceView surfaceView;
    private Context context;
    public GLSurfaceViewContainer(Context context) {
        super(context);
        this.context = context ;
        init();
    }

    public GLSurfaceViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context ;
        init();
    }

    public GLSurfaceViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context ;
        init();
    }
    private void init()
    {
        setBackgroundColor(getResources().getColor(android.R.color.black));
        this.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        surfaceView = new com.gotye.live.player.GLSurfaceView(context);
        surfaceView.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT) );
        addView(surfaceView);
    }
    public GLSurfaceView getSurfaceView()
    {
        return  surfaceView;
    }


}
