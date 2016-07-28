package com.gotye.live.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gotye.live.demo.R;
import com.gotye.live.player.VideoQuality;
import com.gotye.live.publisher.VideoPreset;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/28.
 */
public class ListVideoPreset extends LinearLayout {

    private Context context;
    private ListView listView;
    private Button button;
    private final static String[] data = new String[]{"480x272  (16:9)", "640x360  (16:9)", "854x480  (16:9)", "320x240  (4:3)", "640x480  (4:3)", "768x576  (4:3)"};
    public interface changePreset {
        public void changePreset(String preset, VideoPreset videoPreset);

        public void cancel();
    }

    public interface ChangeVideoQuality {
        public void changeQuality(VideoQuality videoQuality);

        public void cancel();
    }

    private changePreset changePreset;
    private ChangeVideoQuality changeVideoQuality;

    public void setChangeUrlCallBack(final ArrayList<VideoQuality> videoQualities, final ChangeVideoQuality changeVideoQuality) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeVideoQuality.changeQuality(videoQualities.get(position));
            }
        });
        String[] data = new String[videoQualities.size()];
        for (int i = 0; i < videoQualities.size(); i++) {
            data[i] = videoQualities.get(i).toString();
        }
        listView.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, data));

        this.changeVideoQuality = changeVideoQuality;
    }

    public void setChangePresetCallBack(final changePreset changePreset) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < VideoPreset.values().length; i++) {
                    if (VideoPreset.values()[i].toString().contains(data[position].split(" ")[0])) {
                        changePreset.changePreset(data[position].split(" ")[0], VideoPreset.values()[i]);
                        break;
                    }
                }
            }
        });
        listView.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, data));
//        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.changePreset = changePreset;
    }

    public ListVideoPreset(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ListVideoPreset(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ListVideoPreset(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        View view = View.inflate(context, R.layout.view_resolution, null);
        addView(view);
        button = (Button) findViewById(R.id.cancel_action);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changePreset != null)
                    changePreset.cancel();
                if (changeVideoQuality != null)
                    changeVideoQuality.cancel();
            }
        });

        listView = (ListView) findViewById(R.id.listView);

    }

    private ListView getListView() {
        return listView;
    }

}
