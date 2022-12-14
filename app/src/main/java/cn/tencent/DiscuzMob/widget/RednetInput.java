package cn.tencent.DiscuzMob.widget;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.tencent.DiscuzMob.base.RedNetApp;
import cn.tencent.DiscuzMob.model.FileBean;
import cn.tencent.DiscuzMob.recordingutils.UPlayer;
import cn.tencent.DiscuzMob.recordingutils.URecorder;
import cn.tencent.DiscuzMob.ui.activity.SendThreadActivity;
import cn.tencent.DiscuzMob.ui.adapter.AudioAdapter;
import cn.tencent.DiscuzMob.ui.adapter.FaceAdapter;
import cn.tencent.DiscuzMob.ui.dialog.ImageChooserDialog;
import cn.tencent.DiscuzMob.utils.Callback;
import cn.tencent.DiscuzMob.widget.pagerindicator.CirclePageIndicator;
import cn.tencent.DiscuzMob.widget.transformer.ThumbnailTransformer;
import cn.tencent.DiscuzMob.base.RedNet;
import cn.tencent.DiscuzMob.model.Smiley;
import cn.tencent.DiscuzMob.ui.Event.SendAudio;
import cn.tencent.DiscuzMob.utils.BitmapUtils;
import cn.tencent.DiscuzMob.utils.Constant;
import cn.tencent.DiscuzMob.utils.RednetUtils;
import cn.tencent.DiscuzMob.utils.cache.CacheUtils;
import cn.tencent.DiscuzMob.R;

/**
 * Created by AiWei on 2016/7/1.
 */
public class RednetInput extends FrameLayout implements View.OnClickListener {

    public static final int FLAG_INPUT = 0x1;
    public static final int FLAG_EMOJ = 0x2;
    public static final int FLAG_PHOTO = 0x4;
    public static final int FLAG_RADIO = 0x8;

    private int flags;

    private View mOptions;
    private ViewPager mEmojPager;
    private CirclePageIndicator mPagerIndicator;

    private ImageChooserDialog mImageChooserDialog;
    private RecyclerView mPhotography;

    private CheckBox mEmoj;
    private View mPhoto;
    private EditText mEditor;
    private TextView mSubmit;
    private ImageView mRadio;
    private int mPadding;
    private LinearLayout ll_recording;
    private Handler mFileHandler;

    private Callback<String> mEmojListener;
    private boolean isRadio = false;
    private ImageView iv_recording;
    private TextView tv_send_record;
    private TextView tv_again;
    private TextView tv_explain;
    private TextView tv_recordingtime;
    private ImageView iv_playrecord;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;

    private static int count = 1;
    private boolean isPause = false;

    private static int delay = 1000;  //1s
    private static int period = 1000;  //1s
    private static final int UPDATE_TEXTVIEW = 0;
    private String path = null;
    private URecorder recorder;
    private UPlayer player;
    String cookiepre_auth;
    String cookiepre_saltkey;
    String formhash;
    //    private Handler mHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UPDATE_TEXTVIEW:
//                    updateTextView();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
//????????????
    private ExecutorService mExecutorService;
    //??????API
    private MediaRecorder mMediaRecorder;
    //?????????????????????????????????
    private long startTime, endTime;
    //????????????????????????
    private File mAudioFile;
    //??????????????????
    private List<FileBean> dataList;
    //?????????????????????????????????
    private AudioAdapter mAudioAdapter;
    //????????????????????????
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio/";
    //????????????????????????
    private volatile boolean isPlaying;
    //??????????????????API
    private MediaPlayer mediaPlayer;
    //????????????????????????
    private boolean isPlay = false;
    //??????Handler??????UI??????
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.RECORD_SUCCESS:
//                    //???????????????????????????
//                    if (null == mAudioAdapter) {
//                        mAudioAdapter = new AudioAdapter(RedNetApp.getInstance(), dataList, R.layout.file_item_layout);
//                    }
//                    listView.setAdapter(mAudioAdapter);
                    break;
                //????????????
                case Constant.RECORD_FAIL:
                    showToastMsg(String.valueOf(R.string.record_fail));
                    break;
                //??????????????????
                case Constant.RECORD_TOO_SHORT:
                    showToastMsg(String.valueOf(R.string.time_too_short));
                    break;
                case Constant.PLAY_COMPLETION:
                    showToastMsg(String.valueOf(R.string.play_over));
                    break;
                case Constant.PLAY_ERROR:
                    showToastMsg(String.valueOf(R.string.play_error));
                    break;

            }
        }
    };

    protected void showToastMsg(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateTextView() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_recordingtime.setText(String.valueOf(count) + "''");
            }
        });

    }

    public RednetInput(Context context) {
        super(context);
        init(context);
    }

    public RednetInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RednetInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public RednetInput(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private Context mContext;

    void init(Context context) {
        mContext = context;
        dataList = new ArrayList<>();
        mExecutorService = Executors.newSingleThreadExecutor();
        cookiepre_auth = CacheUtils.getString(RedNetApp.getInstance(), "cookiepre_auth");
        cookiepre_saltkey = CacheUtils.getString(RedNetApp.getInstance(), "cookiepre_saltkey");
        formhash = CacheUtils.getString(RedNetApp.getInstance(), "formhash");
        path = Environment.getExternalStorageDirectory().getAbsolutePath();
        mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        LayoutInflater.from(context).inflate(R.layout.rednet_input_layout, this, true);
        mOptions = findViewById(R.id.options);
        mEmoj = (CheckBox) findViewById(R.id.emoj);
        mPhoto = findViewById(R.id.photo);
        mRadio = (ImageView) findViewById(R.id.radio);
        mEditor = (EditText) findViewById(R.id.editor);
        mSubmit = (TextView) findViewById(R.id.submit);
        mEmojPager = (ViewPager) findViewById(R.id.pager);
        iv_recording = (ImageView) findViewById(R.id.iv_recording);
        mPagerIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mPhotography = (RecyclerView) findViewById(R.id.recycler);
        ll_recording = (LinearLayout) findViewById(R.id.ll_recording);
        tv_send_record = (TextView) findViewById(R.id.tv_send_record);
        tv_again = (TextView) findViewById(R.id.tv_again);
        tv_explain = (TextView) findViewById(R.id.tv_explain);
        tv_recordingtime = (TextView) findViewById(R.id.tv_recordingtime);
        iv_playrecord = (ImageView) findViewById(R.id.iv_playrecord);
        mPhotography.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mPhotography.setAdapter(new PhotographyAdapter());
        mPhotography.getAdapter().registerAdapterDataObserver(mDataObserver);
        mPhoto.setOnClickListener(this);
        mEditor.setOnClickListener(this);
        mRadio.setOnClickListener(this);
        iv_playrecord.setOnClickListener(this);
        tv_send_record.setOnClickListener(this);
        tv_again.setOnClickListener(this);
        mEditor.setOnEditorActionListener(mOnEditorActionListener);
        mEmoj.setOnCheckedChangeListener(mOnEmojCheckedChangeListner);
        iv_recording.setOnTouchListener(mRecordingTouchListener);

    }

    public OnTouchListener mRecordingTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                long timecurrentTimeMillis = System.currentTimeMillis();
                if (Build.VERSION.SDK_INT > 22) {
                    permissionForM();
                } else {
                    startRecord();
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (count == 1) {
                    tv_recordingtime.setVisibility(GONE);
                    iv_recording.setVisibility(VISIBLE);
                    stopTimer();
                    tv_explain.setText("??????????????????");
                    Toast.makeText(RedNetApp.getInstance(), "????????????", Toast.LENGTH_SHORT).show();
                } else {
                    stopRecord();
                }

                return true;
            } else {
                return false;
            }
        }
    };

    /**
     * @description ??????????????????
     * @author ldm
     * @time 2017/2/9 9:18
     */
    FileBean bean;

    private void stopRecord() {
        stopTimer();
        iv_recording.setVisibility(GONE);
        iv_playrecord.setVisibility(VISIBLE);
        tv_again.setVisibility(VISIBLE);
        tv_send_record.setVisibility(VISIBLE);
        tv_explain.setText("????????????");
        //????????????
        mMediaRecorder.stop();
        //??????????????????
        endTime = System.currentTimeMillis();
        //???????????????????????????????????????2????????????????????????
        int time = (int) ((endTime - startTime) / 1000);
        if (time >= 1) {
            //????????????,????????????
            bean = new FileBean();
            bean.setFile(mAudioFile);
            bean.setFileLength(time);
            dataList.add(bean);
            //????????????,???Message
            mHandler.sendEmptyMessage(Constant.RECORD_SUCCESS);
        } else {
            mAudioFile = null;
            mHandler.sendEmptyMessage(Constant.RECORD_TOO_SHORT);
        }
        //????????????????????????
        releaseRecorder();
    }

    private void startRecord() {
        tv_recordingtime.setVisibility(VISIBLE);
        startTimer();
        tv_explain.setText("??????????????????");
        //??????????????????????????????
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //?????????????????????
                releaseRecorder();
                //??????????????????
                recordOperation();
            }
        });
    }

    /**
     * @description ????????????????????????
     * @author ldm
     * @time 2017/2/9 9:33
     */
    private void releaseRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * @description ????????????
     * @author ldm
     * @time 2017/2/9 9:34
     */
    private void recordOperation() {
        //??????MediaRecorder??????
        mMediaRecorder = new MediaRecorder();
        //??????????????????,.m4a???MPEG-4?????????????????????????????????
        mAudioFile = new File(mFilePath + System.currentTimeMillis() + ".mp3");

        //??????????????????
        mAudioFile.getParentFile().mkdirs();
        try {
            //????????????
            mAudioFile.createNewFile();
            //??????mMediaRecorder????????????
            //??????????????????????????????
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //???????????????????????????MP4
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //??????????????????,44100???????????????????????????????????????,????????????????????????????????????????????????
            mMediaRecorder.setAudioSamplingRate(44100);
            //??????????????????????????????,?????????????????????AAC
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //??????????????????
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //???????????????????????????
            mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
            //????????????
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            //????????????????????????
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            recordFail();
        }
    }

    private void recordFail() {
        mAudioFile = null;
        mHandler.sendEmptyMessage(Constant.RECORD_FAIL);
    }

    private void permissionForM() {
        if (ContextCompat.checkSelfPermission(RedNetApp.getInstance(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(RedNetApp.getInstance(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constant.PERMISSIONS_REQUEST_FOR_AUDIO);
        } else {
            startRecord();
        }
    }


    private void stopTimer() {
        count = 1;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }

    private void startTimer() {
        count = 1;
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
//                    sendMessage(UPDATE_TEXTVIEW);
                    updateTextView();
                    do {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);

                    count++;
                }
            };
        }

        if (mTimer != null && mTimerTask != null)
            mTimer.schedule(mTimerTask, delay, period);
    }

    void onSetFlags() {
        if (mEmoj != null)
            mEmoj.setVisibility((flags & FLAG_EMOJ) != 0 ? View.VISIBLE : View.GONE);
        if (mPhoto != null)
            mPhoto.setVisibility((flags & FLAG_PHOTO) != 0 ? View.VISIBLE : View.GONE);
        if (mEditor != null)
            mEditor.setVisibility((flags & FLAG_INPUT) != 0 ? View.VISIBLE : View.INVISIBLE);
        if (mSubmit != null)
            mSubmit.setVisibility((flags & FLAG_INPUT) != 0 ? View.VISIBLE : View.INVISIBLE);
        if (mRadio != null)
            mRadio.setVisibility((flags & FLAG_RADIO) != 0 ? View.VISIBLE : View.INVISIBLE);
    }

    public void setFlags(int flag, int mask) {
        if (flag == 0) {
            flags &= ~mask;
        } else {
            mask &= ~flag;
            flags &= ~mask;
            flags |= flag;
        }
        onSetFlags();
    }

    /**
     * @param activity
     * @param submitText
     * @param flags
     */
    public void setFlags(Activity activity, CharSequence submitText, View.OnClickListener listener, int flags) {
        setOwnerActivity(activity);
        setActionLabel(submitText, listener);
        this.flags = flags;
        onSetFlags();
    }

    private Activity activity;

    public void setFileUploadHandler(Handler handler, SendThreadActivity sendThreadActivity) {
        this.mFileHandler = handler;
        this.activity = sendThreadActivity;
    }

    /**
     * @param activity
     */
    public void setOwnerActivity(Activity activity) {
        mImageChooserDialog = activity != null ? new ImageChooserDialog(activity) : null;
    }

    /**
     * ??????????????????
     *
     * @param submitText
     */
    public void setActionLabel(CharSequence submitText, View.OnClickListener listener) {
        if (!TextUtils.isEmpty(submitText)) {
            mSubmit.setText(submitText);
            mSubmit.setOnClickListener(listener);
            mEditor.setImeOptions(EditorInfo.IME_ACTION_SEND);
            mEditor.setImeActionLabel(submitText, EditorInfo.IME_ACTION_SEND);
        }
    }

    /**
     * ????????????
     *
     * @param context
     * @param path
     */
    public void appendPhoto(Context context, String path) {
        if (TextUtils.isEmpty(RednetUtils.getMimeTypeOfImageFile(path))) {
            RednetUtils.toast(context, "?????????????????????");
        } else {
            ((PhotographyAdapter) mPhotography.getAdapter()).append(path);
        }
    }

    public void setEmojListener(Callback<String> listener) {
        this.mEmojListener = listener;
    }

    /**
     * ????????????????????????
     *
     * @param path
     * @param code
     */
    public void notifyUploadState(String path, int code) {
        ((PhotographyAdapter) mPhotography.getAdapter()).notifyItemChanged(path, code);
    }

    public String[] photos() {
        return ((PhotographyAdapter) mPhotography.getAdapter()).paths();
    }

    public void dissmissChooser() {
        if (mImageChooserDialog != null) mImageChooserDialog.dismiss();
    }

    public boolean canBack() {
        if (mPhoto.getVisibility() == View.VISIBLE && mEmoj.isChecked()) {
            mEmoj.setChecked(false);
            return false;
        } else {
            return true;
        }
    }

    private AdapterView.OnItemClickListener mEmojItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Smiley emoji = (Smiley) parent.getItemAtPosition(position);
            if (!TextUtils.isEmpty(emoji.getCode())) {
                mEditor.append(emoji.getCode());
                if (mEmojListener != null) {
                    mEmojListener.onCallback(emoji.getCode());
                }
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener mOnEmojCheckedChangeListner = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                RednetUtils.hideSoftInput(mEditor.getApplicationWindowToken());
                if (mEmojPager.getAdapter() == null || mEmojPager.getAdapter().getCount() == 0) {
                    mEmojPager.setAdapter(new EmojPagerAdater());
                    mPagerIndicator.setViewPager(mEmojPager);
                }
            } else {
                if (!mEditor.isFocusable()) {
                    mEditor.requestFocus();
                }
            }
        }
    };

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                RednetUtils.hideSoftInput(v.getApplicationWindowToken());
                mSubmit.performClick();
            }
            return false;
        }
    };

    private class EmojPagerAdater extends PagerAdapter {

        ArrayList<ArrayList<Smiley>> emojis;

        public EmojPagerAdater() {
            emojis = RedNet.smileManager.initSmileData();
        }

        @Override
        public int getCount() {
            return emojis != null ? emojis.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            GridView view = new GridView(container.getContext());
            view.setAdapter(new FaceAdapter(container.getContext(), emojis.get(position)));
            view.setOnItemClickListener(mEmojItemClickListener);
            view.setNumColumns(7);
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setVerticalSpacing(mPadding);
            view.setCacheColorHint(Color.TRANSPARENT);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setGravity(Gravity.CENTER);
            container.addView(view);
            return view;
        }

    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        void hasEmpty() {
            boolean isEmpty = mPhotography.getAdapter().getItemCount() == 0;
            mPhotography.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            if (isEmpty) {
                ((PhotographyAdapter) mPhotography.getAdapter()).showDel(false, false);
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            hasEmpty();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            hasEmpty();
        }

    };

    static final class FileMeta {

        int code;//0?????????1????????????2????????????3????????????
        int action;//0??????1??????
        String path;

        private FileMeta(int code, int action, String path) {
            this.code = code;
            this.action = action;
            this.path = path;
        }

        @Override
        public int hashCode() {
            return 201607100;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FileMeta && path.equals(((FileMeta) o).path)
                    || (o instanceof String && o.equals(path));
        }

    }

    static FileMeta newFileMeta(int code, int action, String path) {
        return new FileMeta(code, action, path);
    }

    private class PhotographyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        //msg.what= msg.obj= the image's path
        final ArrayList<FileMeta> imagePathList = new ArrayList<>();
        boolean show;

        class CoverHolder extends RecyclerView.ViewHolder implements OnClickListener {
            View layout, del;
            TextView tip;
            AsyncImageView cover;

            View.OnLongClickListener mOnPhotographyLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((PhotographyAdapter) mPhotography.getAdapter()).showDel(true, true);
                    return true;
                }
            };

            public CoverHolder(View itemView) {
                super(itemView);
                layout = itemView;
                del = itemView.findViewById(R.id.del);
                tip = (TextView) itemView.findViewById(R.id.tip);
                cover = (AsyncImageView) itemView.findViewById(R.id.cover);
                del.setOnClickListener(this);
                tip.setOnClickListener(this);
                tip.setOnLongClickListener(mOnPhotographyLongClickListener);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(mOnPhotographyLongClickListener);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.del: {
                        int position = getLayoutPosition();
                        sendDeleteMessage(position);
                        imagePathList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemChanged(position);
                        break;
                    }
                    case R.id.tip:
                        sendUploadMessage(getLayoutPosition(), 3, true); //retry uploading the path image
                    default:
                        ((PhotographyAdapter) mPhotography.getAdapter()).showDel(false, true);
                        return;
                }
            }

        }

        //
        void sendUploadMessage(int position, int what, boolean notify) {
            if (mFileHandler != null && imagePathList.get(position).code == what) {
                mFileHandler.sendMessage(Message.obtain(mFileHandler, position, String.valueOf(imagePathList.get(position).path)));
                imagePathList.get(position).code = 1;
                if (notify) notifyDataSetChanged();
            }
        }

        //
        void sendDeleteMessage(int position) {
            if (mFileHandler != null) {
                mFileHandler.sendMessage(Message.obtain(mFileHandler, position, 1, 0, String.valueOf(imagePathList.get(position).path)));
            }
        }

        //??????????????????
        void showDel(boolean show, boolean notify) {
            if (!(this.show && show)) {
                this.show = show;
                if (notify) notifyDataSetChanged();
            }
        }

        //????????????
        void append(String path) {
            if (mFileHandler != null && !TextUtils.isEmpty(path)) {
                FileMeta meta = newFileMeta(0, 0, path);
                imagePathList.add(meta);
                notifyDataSetChanged();
                sendUploadMessage(imagePathList.indexOf(meta), 0, false);
            }
        }

        String[] paths() {
            String[] array = new String[imagePathList.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = String.valueOf(imagePathList.get(i).path);
            }
            return array;
        }

        //
        void notifyItemChanged(String path, int code) {
            int position = imagePathList.indexOf(newFileMeta(0, 0, path));
            if (position != -1) {
                imagePathList.get(position).code = code;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return imagePathList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CoverHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rednet_input_photo, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
            CoverHolder holder = (CoverHolder) h;
            FileMeta fileMeta = imagePathList.get(position);
            holder.cover.load("file:///" + fileMeta.path, new ThumbnailTransformer());
            holder.tip.setText(fileMeta.code == 3 ? "????????????" : "");
            holder.tip.setVisibility(fileMeta.code == 2 ? View.GONE : View.VISIBLE);
            ((MarginLayoutParams) holder.layout.getLayoutParams()).rightMargin = position == getItemCount() - 1 ? 0 : mPadding;
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo:
                if (mImageChooserDialog != null) mImageChooserDialog.show();
                isRadio = false;
            case R.id.editor:
                mEmoj.setChecked(false);
                isRadio = false;
                break;
            case R.id.radio:
                if (isRadio == false) {
                    ll_recording.setVisibility(VISIBLE);
                    isRadio = true;
                } else {
                    ll_recording.setVisibility(GONE);
                    isRadio = false;
                }
                break;
            case R.id.tv_again:
                path = null;
                tv_again.setVisibility(GONE);
                tv_send_record.setVisibility(GONE);
                count = 0;
                tv_explain.setText("??????????????????");
                tv_recordingtime.setVisibility(GONE);
//                mHandler.sendEmptyMessage(UPDATE_TEXTVIEW);
                updateTextView();
                iv_recording.setVisibility(VISIBLE);
                iv_playrecord.setVisibility(GONE);
                break;
            case R.id.iv_playrecord:
                if (isPlaying) {
                    //????????????????????????????????????
                    stopPlay();
                } else {
                    //??????MediaPlayer??????????????????
                    iv_playrecord.setImageResource(R.drawable.pause);
                    startPlay(bean.getFile());
                }

                break;
            case R.id.tv_send_record:
                if (bean.getFile() != null) {
                    CacheUtils.putString(RedNetApp.getInstance(), "audioPath", path);
                    EventBus.getDefault().post(new SendAudio(bean));
                }

                break;
        }
    }

    private void stopPlay() {
        isPlaying = false;
        iv_playrecord.setImageResource(R.drawable.playrecord);
        //????????????
        mediaPlayer.stop();
    }

    /**
     * @description ????????????????????????
     * @author ldm
     * @time 2017/2/9 16:56
     */
    private void startPlay(File mFile) {
        try {
            isPlaying = true;
            //??????????????????
            mediaPlayer = new MediaPlayer();
            //??????????????????????????????
            mediaPlayer.setDataSource(mFile.getAbsolutePath());
            //????????????????????????
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //????????????
                    playEndOrFail(true);
                }
            });
            //??????????????????????????????
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    playEndOrFail(false);
                    return true;
                }
            });
            //?????????????????????
            mediaPlayer.setVolume(1, 1);
            //??????????????????
            mediaPlayer.setLooping(false);
            //???????????????
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            //??????????????????
            playEndOrFail(false);
        }

    }


    /**
     * @description ?????????????????????????????????
     * @author ldm
     * @time 2017/2/9 16:58
     */
    private void playEndOrFail(boolean isEnd) {
        iv_playrecord.setImageResource(R.drawable.playrecord);
        isPlaying = false;
        if (isEnd) {
            mHandler.sendEmptyMessage(Constant.PLAY_COMPLETION);
        } else {
            mHandler.sendEmptyMessage(Constant.PLAY_ERROR);
        }
        if (null != mediaPlayer) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnErrorListener(null);
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public interface IChain {
        boolean doChain(String path);
    }

    /**
     * @param activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void handleOnActivityResult(Activity activity, IChain chain, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String path;
            if (requestCode == ImageChooserDialog.REQUEST_CODE_PICK) {
                if (data != null && data.getData() != null) {
                     path = BitmapUtils.getRealFilePath(mContext, data.getData());
                    appendPhoto(activity, path);
//                    Cursor cursor = activity.getContentResolver()
//                            .query(data.getData(), new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
//                    if (cursor != null) {
//                        try {
//                            cursor.moveToFirst();
//                            path = String.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
//                            if (chain.doChain(path)) {
//                                appendPhoto(activity, path);
//                            }
//                        } finally {
//                            cursor.close();
//                        }
//                    }
                }
            } else if (requestCode == ImageChooserDialog.REQUEST_CODE_CAMERA) {
                path = String.valueOf(mImageChooserDialog.getCurrentPhotoPath());
                Log.e("TAG", "path=" + path);
                if (chain.doChain(path)) {
                    appendPhoto(activity, path);
                }
            }/* else if (requestCode == ImageChooserDialog.REQUEST_CODE_CROP) {
                path = String.valueOf(mImageChooserDialog.getFixPhotoPath());
                if (chain.doChain(path)) {
                    appendPhoto(activity, path);
                }
            }*/ else {
            }
            dissmissChooser();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mPhotography.getAdapter().unregisterAdapterDataObserver(mDataObserver);
        super.onDetachedFromWindow();
    }


}
