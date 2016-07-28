package com.gotye.live.demo.view;

import com.gotye.live.chat.Ack;
import com.gotye.live.chat.Code;
import com.gotye.live.chat.Message;
import com.gotye.live.chat.SendMsgAck;
import com.gotye.live.chat.TextMessage;
import com.gotye.live.demo.Fragment.PlayVerticalFragment;
import com.gotye.live.demo.MyApplication;
import com.gotye.live.demo.R;
import com.gotye.live.demo.adapter.FaceGVAdapter;
import com.gotye.live.demo.adapter.FaceVPAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRoom extends LinearLayout {

    private boolean isChatShow = false;
    private Context mContext;
    private ChatBubble chatBubble;
    private Button btSubmit;
    private EditText contentEdit;
    private LinearLayout llinput, mDotsLayout;
    private LinearLayout chat_face_container;
    private List<String> staticFacesList;
    private int columns = 5;
    private int rows = 4;
    private ViewPager mViewPager;
    private List<View> views = new ArrayList<View>();

    public ChatRoom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public ChatRoom(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public ChatRoom(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public View getllinput() {
        return llinput;
    }

    public View getEditView() {
        return contentEdit;
    }

    private void sendMessage() {
        String text = contentEdit.getText().toString();
        if (TextUtils.isEmpty(text)) {
//			contentEdit.setError(getString(R.string.error_field_required));
            return;
        }
        final Message message = new TextMessage();
        message.setStatus(0);
        message.setText(text);
        message.setSenderNickname(MyApplication.im.getNickname());
        message.setSenderId(MyApplication.im.getAccount());
        MyApplication.im.sendMessage(message, new Ack<SendMsgAck>() {
            @Override
            public void ack(SendMsgAck data) {
                if (data.getCode() == Code.SUCCESS) {
                    message.setStatus(1);
                } else {
                    message.setStatus(2);
                }

            }
        });
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(contentEdit.getWindowToken(), 0); //强制隐藏键盘
        contentEdit.setText("");
        chatBubble.addChat(MyApplication.im.getNickname(), text);
        chat_face_container.setVisibility(View.GONE);
    }

    private void init() {

        View view = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.chat_room, null);
        addView(view);
        llinput = (LinearLayout) view.findViewById(R.id.llinput);
        chatBubble = (ChatBubble) view.findViewById(R.id.chatRoom);
        btSubmit = (Button) view.findViewById(R.id.submit);
        contentEdit = (EditText) view.findViewById(R.id.content);

        contentEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                chat_face_container.setVisibility(View.GONE);
                contentEdit.setFocusable(true);
                contentEdit.setFocusableInTouchMode(true);
                contentEdit.requestFocus();
                InputMethodManager imm = (InputMethodManager)mContext. getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(contentEdit,InputMethodManager.SHOW_FORCED);
                return false;
            }
        });
//        contentEdit.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                chat_face_container.setVisibility(View.GONE);
//            }
//        });
        chat_face_container = (LinearLayout) view.findViewById(R.id.chat_face_container);
        mViewPager = (ViewPager) findViewById(R.id.face_viewpager);
        mViewPager.setOnPageChangeListener(new PageChange());
        ImageView iv_face = (ImageView) view.findViewById(R.id.iv_face);
        iv_face.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(contentEdit.getWindowToken(), 0); //强制隐藏键盘
                if (chat_face_container.getVisibility() == View.GONE) {
                    chat_face_container.setVisibility(View.VISIBLE);
                } else {
                    chat_face_container.setVisibility(View.GONE);
                }
            }
        });
        btSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mDotsLayout = (LinearLayout) view.findViewById(R.id.face_dots_container);
        initStaticFaces();
        InitViewPager();

    }

    public ChatBubble getChatBubble()

    {
        return chatBubble;
    }

    public boolean isShow() {
        return isChatShow;
    }

    public void shutChatRoom() {
        isChatShow = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
//    private int getPagerCount() {
//        int count = staticFacesList.size();
//        return count % (columns * rows - 1) == 0 ? count / (columns * rows - 1)
//                : count / (columns * rows - 1) + 1;
//    }

    private void InitViewPager() {
        // 获取页数
        for (int i = 0; i < 2; i++) {
            views.add(viewPagerItem(i));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(16, 16);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewPager.setAdapter(mVpAdapter);
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    private void initStaticFaces() {
        try {
            staticFacesList = new ArrayList<String>();
            String[] faces = mContext.getAssets().list("face/png");
            //将Assets中的表情名称转为字符串一一添加进staticFacesList
            for (int i = 0; i < faces.length; i++) {
                staticFacesList.add(faces[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ImageView dotsItem(int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    private View viewPagerItem(int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.face_gridview, null);//表情布局
        GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
        /**
         * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
         * */
        List<String> subList = new ArrayList<String>();
        subList.addAll(staticFacesList
                .subList(position * (columns * rows),
                        (columns * rows) * (position + 1) > staticFacesList
                                .size() ? staticFacesList.size() : (columns
                                * rows)
                                * (position + 1)));
        /**
         * 末尾添加删除图标
         * */
        FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, mContext);
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(columns);
        // 单击表情执行的操作
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String png = ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString();
                    insert(getFace(png));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return gridview;
    }

    private SpannableStringBuilder getFace(String png) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        try {
            /**
             * 经过测试，虽然这里tempText被替换为png显示，但是但我单击发送按钮时，获取到輸入框的内容是tempText的值而不是png
             * 所以这里对这个tempText值做特殊处理
             * 格式：#[face/png/f_static_000.png]#，以方便判斷當前圖片是哪一個
             * */
            String regEx = "[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(png);
            String tempText = m.replaceAll("").trim();
            sb.append("[s" + tempText + "]");
            sb.setSpan(
                    new ImageSpan(mContext, BitmapFactory
                            .decodeStream(mContext.getAssets().open(png))), sb.length()
                            - tempText.length() - 3, sb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb;
    }

    /**
     * 向输入框里添加表情
     */
    private void insert(CharSequence text) {
        int iCursorStart = Selection.getSelectionStart((contentEdit.getText()));
        int iCursorEnd = Selection.getSelectionEnd((contentEdit.getText()));
        if (iCursorStart != iCursorEnd) {
            ((Editable) contentEdit.getText()).replace(iCursorStart, iCursorEnd, "");
        }
        int iCursor = Selection.getSelectionEnd((contentEdit.getText()));
        ((Editable) contentEdit.getText()).insert(iCursor, text);
    }

    class PageChange implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }

    }

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (((Activity) mContext).getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (((Activity) mContext).getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(((Activity) mContext).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
