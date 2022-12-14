package cn.tencent.DiscuzMob.ui.fragment.live;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.tencent.DiscuzMob.net.FileUploader;
import cn.tencent.DiscuzMob.ui.Event.RefrshLive;
import cn.tencent.DiscuzMob.ui.Event.RefrshLiveClean;
import cn.tencent.DiscuzMob.ui.adapter.LiveAdapter;
import cn.tencent.DiscuzMob.ui.dialog.ImageChooserDialog;
import cn.tencent.DiscuzMob.utils.DateUtils;
import cn.tencent.DiscuzMob.widget.NestedSwipeRefreshLayout;
import cn.tencent.DiscuzMob.base.RedNet;
import cn.tencent.DiscuzMob.base.RedNetApp;
import cn.tencent.DiscuzMob.model.DetailsBean;
import cn.tencent.DiscuzMob.model.Smiley;
import cn.tencent.DiscuzMob.net.AppNetConfig;
import cn.tencent.DiscuzMob.ui.Event.RefreshInteraction;
import cn.tencent.DiscuzMob.ui.Event.RefreshPicturePath;
import cn.tencent.DiscuzMob.ui.adapter.FaceAdapter;
import cn.tencent.DiscuzMob.ui.adapter.ViewPagerAdapter;
import cn.tencent.DiscuzMob.utils.RednetUtils;
import cn.tencent.DiscuzMob.utils.cache.CacheUtils;
import cn.tencent.DiscuzMob.widget.FooterForList;
import cn.tencent.DiscuzMob.widget.ILoadListener;
import cn.tencent.DiscuzMob.R;


/**
 * Created by cg on 2017/516.
 * ????????????
 */

@SuppressLint("ValidFragment")
public class LivePager extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, ILoadListener, SwipeRefreshLayout.OnRefreshListener {
    String cookiepre_auth;
    String cookiepre_saltkey;
    private int mPage;
    private String fid;
    String uploadhash;
    public boolean isClick = true;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    RednetUtils.toast(RedNetApp.getInstance(), "????????????");
                    mProgressBar.setVisibility(View.GONE);
                    mTip.setDisplayedChild(0);
                    break;
                case 1:
                    RednetUtils.toast(RedNetApp.getInstance(), "????????????");
                    mProgressBar.setVisibility(View.GONE);
                    mTip.setDisplayedChild(0);
                    break;
                case 3:
                    postlist.remove(0);
                    liveAdapter.setData(postlist);
                    if (postlist.size() == 10) {
                        mFooter.reset();
                    } else {
                        mFooter.finishAll();
                    }
                    break;
                case 4:
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditor, InputMethodManager.SHOW_FORCED);
                    imm.hideSoftInputFromWindow(mEditor.getWindowToken(), 0); //??????????????????

                    final String result = (String) msg.obj;
                    mFaceViewLayout.setVisibility(View.GONE);
                    ll_photo.setVisibility(View.GONE);
                    RednetUtils.toast(getActivity(), result);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case 5:
                    ll_photo.setVisibility(View.GONE);
                    RednetUtils.hideSoftInput(mEditor.getApplicationWindowToken());
                    if (emojis == null || emojis.isEmpty()) {
                        emojis = RedNet.smileManager.initSmileData();
                        Init_viewPager();
                        Init_Point();
                        Init_Data();
                    }
                    smiley_btn.setImageResource(R.drawable.icon_key);
                    mFaceViewLayout.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    RednetUtils.hideSoftInput(mEditor.getApplicationWindowToken());
                    smiley_btn.setImageResource(R.drawable.icon_face_smile);
                    mFaceViewLayout.setVisibility(View.GONE);
                    ll_photo.setVisibility(View.GONE);
                    mEditor.requestFocus();
                    break;
                case 7:
                    RednetUtils.hideSoftInput(mEditor.getApplicationWindowToken());
                    smiley_btn.setImageResource(R.drawable.icon_face_smile);
                    mFaceViewLayout.setVisibility(View.GONE);
                    ll_photo.setVisibility(View.VISIBLE);
                    break;
                case 8:
                    final String s = String.valueOf(msg.obj);
                    Log.e("TAG", "s=" + s + ";uploadhash=" + uploadhash + ";fid=" + fid);
                    mFileUploader.submit(String.valueOf(msg.obj), uploadhash, fid, new FileUploader.CallbackImpl<Object>(String.valueOf(msg.obj)) {
                        @Override
                        public void onCallback(Object obj) {
                            mProgressBar.setVisibility(View.VISIBLE);
                            if (obj.toString().contains("java.lang.Exception: java.lang.Exception:")) {
                                Toast.makeText(getActivity(), obj.toString(), Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                                isClick = true;
                            } else {
                                aids.add(obj.toString());
                                showPhoto(s);
                            }

                        }
                    });
                    break;

            }
        }
    };
    private List<ImageView> imageViews = new ArrayList<>();

    private void showPhoto(final String path) {
        final View inflate = View.inflate(getActivity(), R.layout.picture_item, null);
        ImageView iv_item = (ImageView) inflate.findViewById(R.id.iv_item);
        final ImageView iv_del = (ImageView) inflate.findViewById(R.id.iv_del);
        iv_item.setScaleType(ImageView.ScaleType.FIT_XY);
        iv_item.setPadding(0, 0, 10, 0);
        EventBus.getDefault().post(new RefrshLiveClean(null));
        imageViews.add(iv_del);
        mProgressBar.setVisibility(View.GONE);
        if (imgPaths.size() == 10) {
            iv_addpicture.setVisibility(View.GONE);
        } else {
            iv_addpicture.setVisibility(View.VISIBLE);
        }
        iv_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i1 = imageViews.indexOf(iv_del);
                mFileUploader.cancel(imgPaths.get(i1), uploadhash);
                imageViews.remove(i1);
                imgPaths.remove(i1);
                aids.remove(i1);
                post_pic_container.removeView(inflate);
                iv_addpicture.setVisibility(View.VISIBLE);
            }
        });

        iv_item.setImageURI(Uri.parse(path));
        post_pic_container.addView(inflate, post_pic_container.getChildCount() - 1);
        isClick = true;
    }

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                RednetUtils.hideSoftInput(v.getApplicationWindowToken());
                tv_send.performClick();
                return true;
            }
            return false;
        }
    };

    private void Init_Data() {
        vp_face.setAdapter(new ViewPagerAdapter(pageViews));
        vp_face.setCurrentItem(1);
        current = 0;
        vp_face.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                current = arg0 - 1;
                draw_Point(arg0);
                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????.
                if (arg0 == pointViews.size() - 1 || arg0 == 0) {
                    if (arg0 == 0) {
                        vp_face.setCurrentItem(arg0 + 1);// ????????? ??????????????????????????????????????????.
                        pointViews.get(1).setBackgroundResource(R.drawable.d2);
                    } else {
                        vp_face.setCurrentItem(arg0 - 1);// ???????????????
                        pointViews.get(arg0 - 1).setBackgroundResource(
                                R.drawable.d2);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

    }

    public void draw_Point(int index) {//??????????????????
        for (int i = 1; i < pointViews.size(); i++) {
            if (index == i) {
                pointViews.get(i).setBackgroundResource(R.drawable.d2);
            } else {
                pointViews.get(i).setBackgroundResource(R.drawable.d1);
            }
        }
    }

    private void Init_Point() {
        pointViews = new ArrayList<>();
        ImageView imageView;
        for (int i = 0; i < pageViews.size(); i++) {
            imageView = new ImageView(getActivity());
            imageView.setBackgroundResource(R.drawable.d1);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            layoutParams.width = 15;
            layoutParams.height = 15;
            layout_point.addView(imageView, layoutParams);
            if (i == 0 || i == pageViews.size() - 1) {
                imageView.setVisibility(View.GONE);
            }
            if (i == 1) {
                imageView.setBackgroundResource(R.drawable.d2);
            }
            pointViews.add(imageView);
        }
    }


    private void Init_viewPager() {
        pageViews = new ArrayList<>();
        View emptyView = new View(getActivity());
        emptyView.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(emptyView);

        faceAdapters = new ArrayList<>();
        for (int i = 0; i < emojis.size(); i++) {
            GridView view = new GridView(getActivity());
            FaceAdapter adapter = new FaceAdapter(getActivity(), emojis.get(i));
            view.setAdapter(adapter);
            faceAdapters.add(adapter);
            view.setOnItemClickListener(this);
            view.setNumColumns(7);
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setHorizontalSpacing(1);
            view.setVerticalSpacing((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
            view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            view.setCacheColorHint(0);
            view.setPadding(10, 10, 10, 10);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setGravity(Gravity.CENTER);
            pageViews.add(view);
        }

        emptyView = new View(getActivity());
        emptyView.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(emptyView);
    }

    private String authorid;
    private TextView tv_send;
    public String tid;
    private ImageView iv_nothing;
    private int page;
    private ProgressBar mProgressBar;
    Integer replies;
    Integer ppp;
    private int current = 0;
    private String inputType = "text";
    private ArrayList<ArrayList<Smiley>> emojis;
    private RelativeLayout mFaceViewLayout;
    private ViewPager vp_face;
    private ArrayList<View> pageViews;
    private ArrayList<ImageView> pointViews;
    private LinearLayout layout_point;
    private List<FaceAdapter> faceAdapters;

    @SuppressLint("ValidFragment")
    public LivePager(String fid, String authorid, String tid) {
        this.fid = fid;
        this.authorid = authorid;
        this.tid = tid;
    }

    private EditText mEditor;
    private ImageView smiley_btn;
    private ImageView camera_btn;
    private RelativeLayout ll_photo;
    private ImageView iv_addpicture;
    public ImageChooserDialog mImageChooserDialog;
    private List<String> imgPaths = new ArrayList<>();
    private final Object mObj = new Object();
    private FileUploader mFileUploader;
    private List<String> aids = new ArrayList<>();
    private LinearLayout post_pic_container;
    protected ViewAnimator mTip;
    protected NestedSwipeRefreshLayout mRefreshView;
    protected ListView mListView;
    protected ImageView imageView;
    protected SwipeMenuListView listView;
    protected RelativeLayout rl_threads;
    private FooterForList mFooter;
    private ListView list;
    private LiveAdapter liveAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = View.inflate(container.getContext(), R.layout.live_fragment, null);
        EventBus.getDefault().register(this);
        post_pic_container = (LinearLayout) inflate.findViewById(R.id.post_pic_container);
        mImageChooserDialog = this != null ? new ImageChooserDialog(getActivity()) : null;
        mFileUploader = FileUploader.newInstance(getActivity(), mObj);
        smiley_btn = (ImageView) inflate.findViewById(R.id.smiley_btn);
        camera_btn = (ImageView) inflate.findViewById(R.id.camera_btn);
        ll_photo = (RelativeLayout) inflate.findViewById(R.id.ll_photo);
        mFaceViewLayout = (RelativeLayout) inflate.findViewById(R.id.ll_facechoose);
        vp_face = (ViewPager) inflate.findViewById(R.id.vp_contains);
        layout_point = (LinearLayout) inflate.findViewById(R.id.iv_image);
        iv_addpicture = (ImageView) inflate.findViewById(R.id.iv_addpicture);
        camera_btn.setOnClickListener(this);
        smiley_btn.setOnClickListener(this);
        iv_addpicture.setOnClickListener(this);
        iv_nothing = (ImageView) inflate.findViewById(R.id.iv_nothing);
        tv_send = (TextView) inflate.findViewById(R.id.tv_send);
        tv_send.setOnClickListener(this);
        mEditor = (EditText) inflate.findViewById(R.id.post_content);
        mEditor.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEditor.setImeActionLabel("??????", KeyEvent.KEYCODE_SEARCH);
        mEditor.setOnClickListener(this);
        mEditor.setOnEditorActionListener(mOnEditorActionListener);
        mProgressBar = (ProgressBar) inflate.findViewById(R.id.progress);
        mProgressBar.setVisibility(View.VISIBLE);
        cookiepre_auth = CacheUtils.getString(getActivity(), "cookiepre_auth");
        cookiepre_saltkey = CacheUtils.getString(getActivity(), "cookiepre_saltkey");

        mTip = (ViewAnimator) inflate.findViewById(R.id.tip);
        mRefreshView = (NestedSwipeRefreshLayout) inflate.findViewById(R.id.refresh);
        mListView = (ListView) inflate.findViewById(R.id.list);
        imageView = (ImageView) inflate.findViewById(R.id.iv_nothing);
        liveAdapter = new LiveAdapter(getActivity());
        list = (ListView) inflate.findViewById(R.id.list);
        list.setDividerHeight(0);
        list.setAdapter(liveAdapter);
        listView = (SwipeMenuListView) inflate.findViewById(R.id.listView);
        listView.setVisibility(View.GONE);
        rl_threads = (RelativeLayout) inflate.findViewById(R.id.rl_threads);
        mFooter = new FooterForList(mListView);
        mFooter.setOnLoadListener(this);
        mRefreshView.setOnRefreshListener(this);
        mTip.setDisplayedChild(1);
        mPage = 1;
        mEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//????????????
                    ll_photo.setVisibility(View.GONE);
                    mFaceViewLayout.setVisibility(View.GONE);
                } else {//????????????
                    hideFaceView();
                    hidePotoView();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditor.getWindowToken(), 0); //??????????????????
                }
            }
        });
        if (authorid.equals(RedNetApp.getInstance().getUid())) {
            mEditor.setHint("???????????????????????????");
        } else {
            mEditor.setHint("???????????????????????????");
        }
        getPushData();
        removePhotos();
        onRefresh();
        return inflate;
    }

    private void getPushData() {
        Pusher pusher = new Pusher("30dc23e721dbeb25fa3b");

        Channel channel = pusher.subscribe("livethread");

        channel.bind("thread", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {
                int i = data.indexOf("tid");
                int i1 = data.indexOf("pid");
                String substring = data.substring(i + 8, i1 - 5);
                if (tid.equals(substring)) {
                    handler.sendEmptyMessage(3);
                }
            }
        });

        pusher.connect();
    }

    private boolean hidePotoView() {
        if (ll_photo.getVisibility() == View.VISIBLE) {
            ll_photo.setVisibility(View.GONE);
            inputType = "text";
            return true;
        }
        return false;
    }

    private boolean hideFaceView() {
        if (mFaceViewLayout.getVisibility() == View.VISIBLE) {
            mFaceViewLayout.setVisibility(View.GONE);
            smiley_btn.setImageResource(R.drawable.icon_face_smile);
            inputType = "text";
            return true;
        }
        return false;
    }

    String formhash;
    JSONArray postlist;

    private void getDataFromnet(final boolean b) {
        RedNet.mHttpClient.newCall(new Request.Builder()
                .addHeader("Cookie", cookiepre_auth + ";" + cookiepre_saltkey)
                .url(AppNetConfig.LIVEDETIAL + "&authorid=" + authorid + "&tid=" + tid + "&page=" + String.valueOf(page))
                .cacheControl(new CacheControl.Builder().noStore().noCache().build()).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        handler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        final JSONObject jso = JSON.parseObject(response.body().string());
                        Log.e("TAG", "?????????url=" + AppNetConfig.LIVEDETIAL + "&authorid=" + authorid + "&tid=" + tid + "&page=" + page);
                        if (null != jso) {
                            JSONObject data = (JSONObject) jso.get("data");
                            JSONObject variables = jso.getJSONObject("Variables");
                            if (null != variables) {
                                formhash = variables.getString("formhash");

                                postlist = variables.getJSONArray("postlist");
                                replies = variables.getJSONObject("thread").getInteger("replies");
                                ppp = variables.getInteger("ppp");
                                uploadhash = variables.getJSONObject("allowperm").getString("uploadhash");
                                if (null != postlist && postlist.size() > 0) {
                                    if (b) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                liveAdapter.addData(postlist);
                                                if (postlist.size() == 10) {
                                                    mFooter.reset();
                                                } else {
                                                    mFooter.finishAll();
                                                }
                                            }
                                        });

                                    } else {
                                        handler.sendEmptyMessage(3);
//                                        getActivity().runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                postlist.remove(0);
//                                                liveAdapter.setData(postlist);
//                                                if (postlist.size() == 10) {
//                                                    mFooter.reset();
//                                                } else {
//                                                    mFooter.finishAll();
//                                                }
//                                            }
//                                        });

                                    }
                                } else {
                                    handler.sendMessage(Message.obtain(handler, 2, jso.toString()));
                                }
                            } else {
                                handler.sendEmptyMessage(0);
                            }

                        } else {
                            handler.sendEmptyMessage(0);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                mRefreshView.setRefreshing(false);
                                mTip.setDisplayedChild(0);
                            }
                        });

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send:
                if (RednetUtils.hasLogin(getActivity())) {
                    sendMsg();
                }
                break;
            case R.id.smiley_btn:
                if (inputType.equals("smiley")) {
                    inputType = "text";
                    handler.sendEmptyMessage(6);
                } else if (inputType.equals("text")) {
                    inputType = "smiley";
                    handler.sendEmptyMessage(5);
                } else if (inputType.equals("photo")) {
                    inputType = "text";
                    handler.sendEmptyMessage(6);
                }
                break;
            case R.id.camera_btn:
                if (inputType.equals("photo")) {
                    inputType = "text";
                    handler.sendEmptyMessage(6);
                } else if (inputType.equals("text")) {
                    inputType = "photo";
                    handler.sendEmptyMessage(7);
                } else if (inputType.equals("smiley")) {
                    inputType = "text";
                    handler.sendEmptyMessage(7);
                }
                break;
            case R.id.post_content:
                hideFaceView();
                hidePotoView();
                break;
            case R.id.iv_addpicture:
                if (isClick == true) {
                    if (RednetUtils.hasLogin(getActivity())) {
                        CacheUtils.putString(RedNetApp.getInstance(), "from", "live");
                        if (mImageChooserDialog != null) mImageChooserDialog.show();
                    }
                } else {
                    Toast.makeText(RedNetApp.getInstance(), "??????????????????????????????", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private void sendMsg() {

        final String message = mEditor.getText().toString();
        if (null == message || TextUtils.isEmpty(message.trim())) {
            RednetUtils.toast(getActivity(), "??????????????????");
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        FormEncodingBuilder builder = new FormEncodingBuilder()
                .add("formhash", formhash)
                .add("tid", tid)
                .add("message", message)
                .add("mobiletype", "2")
                .add("allownoticeauthor", "1");
        for (int i = 0; i < aids.size(); i++) {
            builder.add("attachnew[" + aids.get(i) + "][description]", aids.get(i));

        }
        RedNet.mHttpClient.newCall(new Request.Builder()
                .addHeader("Cookie", cookiepre_auth + ";" + cookiepre_saltkey + ";")
                .url(AppNetConfig.COMMENT)
                .post(builder.build()).build())
                .enqueue(new com.squareup.okhttp.Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        handler.sendMessage(Message.obtain(handler, 4, "??????????????????"));
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        try {
                            if (response != null) {
                                DetailsBean detailsBean = new Gson().fromJson(response.body().string(), DetailsBean.class);
                                DetailsBean.VariablesBean variables = detailsBean.getVariables();
                                DetailsBean.MessageBean message1 = detailsBean.getMessage();
                                String messageval = message1.getMessageval();
                                String messagestr = message1.getMessagestr();
                                if ("post_reply_succeed".equals(messageval)) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            onRefresh();
                                            JSONObject lan1 = new JSONObject();
                                            lan1.put("message", message);
                                            String currentTime = DateUtils.getCurrentTime_Today();
                                            String s = DateUtils.dataOne(currentTime);
                                            lan1.put("dbdateline", s);
                                            if (authorid.equals(RedNetApp.getInstance().getUid())) {
                                                JSONArray objects = new JSONArray();
                                                Log.e("TAG", "imgPaths =" + imgPaths.size());
                                                Map<String, JSONObject> map = null;
                                                map = new HashMap<String, JSONObject>();
                                                for (int i = 0; i < imgPaths.size(); i++) {
                                                    Log.e("TAG", "i=" + i);
                                                    JSONObject object = new JSONObject();
                                                    object.put("url", "data/attachment/forum/");
                                                    objects.add("filename" + i);
                                                    String path = imgPaths.get(i);
                                                    object.put("attachment", path);
                                                    lan1.put("imagelist", objects);

                                                    map.put("filename" + i, object);
                                                    lan1.put("attachments", map);
                                                }
                                                Log.e("TAG", "map.size()" + map.size());
                                                postlist.add(0, lan1);
                                                liveAdapter.clearData();
                                                liveAdapter.setData(postlist);

                                            } else {
                                                String todayDateTime = DateUtils.getTodayDateTime();
                                                lan1.put("dateline", todayDateTime);
                                                String username = CacheUtils.getString(RedNetApp.getInstance(), "username");
                                                lan1.put("author", username);
                                                if (aids.size() > 0) {
                                                    JSONArray objects = new JSONArray();
                                                    for (int i = 0; i < imgPaths.size(); i++) {
                                                        JSONObject jsonObject = new JSONObject();
                                                        jsonObject.put("src", imgPaths.get(i));
                                                        jsonObject.put("url", "from");
                                                        objects.add(i, jsonObject);
                                                    }
                                                    lan1.put("imagelists", objects);
                                                }
                                                EventBus.getDefault().post(new RefreshInteraction(lan1));
                                            }
                                            if (imgPaths.size() > 0) {
                                                removePhotos();

                                            }
                                            mEditor.setText("");
                                            imgPaths.clear();
                                            aids.clear();
                                            mProgressBar.setVisibility(View.GONE);

                                        }
                                    });

                                    handler.sendMessage(Message.obtain(handler, 4, "????????????"));
                                } else {
                                    handler.sendMessage(Message.obtain(handler, 4, messagestr));
                                }
                            } else {
                                handler.sendMessage(Message.obtain(handler, 4, "??????????????????"));
                            }
                        } catch (JsonSyntaxException e) {
                            handler.sendMessage(Message.obtain(handler, 4, "????????????"));
                            e.printStackTrace();
                        }

                    }
                });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Smiley emoji = (Smiley) faceAdapters.get(current).getItem(position);
        if (!TextUtils.isEmpty(emoji.getCode())) {
            mEditor.append(emoji.getCode());
        }
    }

    private void addPath(String path) {
        if (null != path && !TextUtils.isEmpty(path)) {
            isClick = false;
            if (!imgPaths.contains(path)) {
                imgPaths.add(path);
                if (imgPaths.size() == 10) {
                    iv_addpicture.setVisibility(View.GONE);
                } else {
                    iv_addpicture.setVisibility(View.VISIBLE);
                }
                Log.e("TAG", "path=" + path);
                handler.sendMessage(handler.obtainMessage(8, path));
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(RedNetApp.getInstance(), "????????????????????????", Toast.LENGTH_SHORT).show();
            }

//            }
        }

    }

    private void dissmissChooser() {
        if (mImageChooserDialog != null) mImageChooserDialog.dismiss();
    }


    @Subscribe
    public void onEventMainThread(RefreshPicturePath mObj) {
        String path = mObj.getPath();
        addPath(path);
        dissmissChooser();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().
                unregister(this);
    }

    @Subscribe
    public void onEventMainThread(RefrshLive mObj) {
        if (imgPaths.size() > 0) {
            removePhotos();
        }
        JSONObject jsonObject = mObj.getJsonObject();
        postlist.add(0, jsonObject);
        liveAdapter.clearData();
        liveAdapter.setData(postlist);
    }

    private void removePhotos() {
        post_pic_container.removeAllViews();
        final View inflate = View.inflate(getActivity(), R.layout.picture_item, null);
        iv_addpicture = (ImageView) inflate.findViewById(R.id.iv_item);
        final ImageView iv_del = (ImageView) inflate.findViewById(R.id.iv_del);
        iv_del.setVisibility(View.GONE);
        iv_addpicture.setScaleType(ImageView.ScaleType.FIT_XY);
        iv_addpicture.setImageResource(R.drawable.addp);
        post_pic_container.addView(inflate);
        inflate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RednetUtils.hasLogin(getActivity())) {
                    CacheUtils.putString(RedNetApp.getInstance(), "from", "live");
                    if (mImageChooserDialog != null) mImageChooserDialog.show();
                }
            }
        });
    }

//    @Subscribe
//    public void onEventMainThread(RefrshLiveClean1 mObj) {
//        if (imgPaths.size() > 0) {
//            removePhotos();
//        }
//
//
//    }

    @Override
    public void onLoad() {
        if (mRefreshView.isRefreshing()) {
            RednetUtils.toast(RedNetApp.getInstance(), "????????????");

        } else if (RednetUtils.networkIsOk(RedNetApp.getInstance())) {
            page++;
            getDataFromnet(true);
        } else {
            mFooter.finish();
        }
    }

    @Override
    public void onRefresh() {
        if (mFooter != null && mFooter.isLoading()) {
            RednetUtils.toast(RedNetApp.getInstance(), "????????????");
            mRefreshView.setRefreshing(false);
        } else if (RednetUtils.networkIsOk(RedNetApp.getInstance())) {
            if (liveAdapter != null) {
                liveAdapter.clearData();
            }
            page = 1;
            getDataFromnet(false);
        } else {
            mRefreshView.setRefreshing(false);
        }
    }

}
