package cn.tencent.DiscuzMob.ui.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.common.LogUtil;
import com.google.gson.Gson;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.tencent.DiscuzMob.base.BaseActivity;
import cn.tencent.DiscuzMob.base.RedNet;
import cn.tencent.DiscuzMob.base.RedNetApp;
import cn.tencent.DiscuzMob.db.User;
import cn.tencent.DiscuzMob.model.UserInfoBean;
import cn.tencent.DiscuzMob.net.AppNetConfig;
import cn.tencent.DiscuzMob.request.HttpRequest;
import cn.tencent.DiscuzMob.request.OnRequestListener;
import cn.tencent.DiscuzMob.request.RequestParams;
import cn.tencent.DiscuzMob.ui.Event.RefreshUserInfo;
import cn.tencent.DiscuzMob.ui.bean.RegistBean;
import cn.tencent.DiscuzMob.utils.Callback;
import cn.tencent.DiscuzMob.utils.GsonUtil;
import cn.tencent.DiscuzMob.utils.LogUtils;
import cn.tencent.DiscuzMob.utils.RedNetPreferences;
import cn.tencent.DiscuzMob.utils.RednetUtils;
import cn.tencent.DiscuzMob.utils.StringUtil;
import cn.tencent.DiscuzMob.utils.ToastUtils;
import cn.tencent.DiscuzMob.utils.VerifyCode;
import cn.tencent.DiscuzMob.utils.cache.CacheUtils;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.tencent.DiscuzMob.R;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * ??????????????????   (?????????webview)
 */
public class NewRegisterActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private ImageView login_back;
    private WebView iv_showCode;
    private EditText et_username;
    private EditText et_password;
    private EditText et_query_password;
    private EditText et_email;
    private EditText et_verifycode;
    //??????????????????
    private String realCode;
    private TextView tv_change_code;
    private TextView tv_register;
    private String inputcode;
    private String inputusername;
    private String inputpassword;
    private String inputquerypassword;
    private String inputemail;
    private ImageView iv_username;
    private ImageView iv_password;
    private ImageView iv_querypassword;
    private ImageView iv_email;
    private String nickname;
    private String type;
    private String openId;
    private int task, task1 = 1 << 1, task2 = 2 << 1;
    private TextView to_login;
    private LinearLayout ll_terms;
    private CheckBox cb_terms;
    private String TYPE = "register";
    private String cookie2;
    private String seccode;
    private String sechash;
    private String formhash;
    private String cookiepre;
    private String saltkey;
    private LinearLayout ll_question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_register);
        initView();
        try {
            openId = getIntent().getStringExtra("openId");
            type = getIntent().getStringExtra("type");
            if (type != null) {
                ll_question.setVisibility(View.GONE);
                if (QQ.NAME.equals(type)) {
                    type = "qq";
                } else if (Wechat.NAME.equals(type)) {
                    type = "weixin";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (type == null) {
        getQuestion();
//        }

    }

    String regname;
    RegistBean registBean;

    private void getQuestion() {
//        Request request = new Request.Builder()
//                .url(AppNetConfig.QUESTION + TYPE)
//                .cacheControl(new CacheControl.Builder().noStore().noCache().build()).build();
//        RedNet.mHttpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
//            @Override
//            public void onFailure(Request request, IOException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        RednetUtils.toast(NewRegisterActivity.this, "?????????????????????");
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Response response) throws IOException {
////                Log.e("TAG", "???????????????=" + response.body().string());
//                cookie2 = response.request().headers().get("Cookie");
//                try {
//                    JSONObject object = new JSONObject(response.body().string());
//                    JSONObject variables = object.getJSONObject("Variables");
//                    if (null != variables && !TextUtils.isEmpty(variables.toString())) {
//                        seccode = variables.getString("seccode");
//                        sechash = variables.getString("sechash");
//                        formhash = variables.getString("formhash");
//                        cookiepre = variables.getString("cookiepre");
//                        Log.e("TAG", "seccode=" + seccode);
//                        saltkey = variables.getString("saltkey");
//                        syncCookie();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ll_question.setVisibility(View.VISIBLE);
//                                iv_showCode.setWebViewClient(mWebViewClientBase);
//                                iv_showCode.loadUrl(seccode);
//                            }
//                        });
//
//
//                    } else {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ll_question.setVisibility(View.GONE);
//                            }
//                        });
//                    }
//
//                } catch (JSONException e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ll_question.setVisibility(View.GONE);
//                        }
//                    });
//                    e.printStackTrace();
//                }
//            }
//        });
        HttpRequest.getInstance().okhttpGetRequest(AppNetConfig.GETREGISTER, new OnRequestListener() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    regname = jsonObject.optString("regname");
                    LogUtils.i(AppNetConfig.REGISTER + "mod=" + regname);
                    HttpRequest.getInstance().okhttpGetRequest(AppNetConfig.REGISTER + "mod=" + regname, new OnRequestListener() {
                        @Override
                        public void onResponse(String result) {
                            LogUtils.i(result);
                            registBean = GsonUtil.fromJson(result, RegistBean.class);
                            cookiepre = registBean.getVariables().getCookiepre();
                            saltkey =  registBean.getVariables().getSaltkey();
                            syncCookie();
                        }

                        @Override
                        public void onError(Exception error) {

                        }
                    });
                } catch (Exception e) {

                }
            }

            @Override
            public void onError(Exception error) {

            }
        });
    }

    private WebViewClientBase mWebViewClientBase = new WebViewClientBase();

    private class WebViewClientBase extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            CookieSyncManager syncManager = CookieSyncManager.createInstance(NewRegisterActivity.this);
            syncManager.sync();
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(url);
            CacheUtils.putString(NewRegisterActivity.this, "cookie", cookie);
            LogUtils.i(cookie);
            String[] cookies = cookie.split(";");
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].contains(cookiepre + "seccode")) {
                    RedNetPreferences.setVcodeCookie(cookies[i]);
                }
                if (cookies[i].contains(cookiepre + "saltkey")) {
                    RedNetPreferences.setSaltkey(cookies[i]);
                }
                if (cookies[i].contains(cookiepre + "seccode")) {
                    RedNetPreferences.setSeccode(cookies[i]);
                }
            }
            super.onPageFinished(view, url);
        }

    }

    private void syncCookie() {
        try {
            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();// ??????
            cookieManager.removeAllCookie();//????????????cookie
            Map<String, List<String>> map = new HashMap<>();
            Map<String, List<String>> cookieData = null;
            try {
                cookieData = RedNet.mHttpClient.getCookieHandler().get(URI.create(AppNetConfig.BASEURL), map);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> cookieStrList = cookieData.get("Cookie");

//            LogUtils.i("alan1995", "cookieStrList=          " + cookieStrList.toString());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < cookieStrList.size(); i++) {
                String[] cookies = cookieStrList.get(i).split(";");
                for (int j = 0; j < cookies.length; j++) {
                    sb.append(cookies[j]+";");
                    cookieManager.setCookie(AppNetConfig.BASEURL, cookies[j]);
                    CookieSyncManager.getInstance().sync(); //cookies??????HttpClient????????????cookie
                }
            }
            LogUtils.i(" ????????????SB: "+sb.toString());
            CacheUtils.putString(NewRegisterActivity.this, "cookie",sb.toString());
            //?????? - ????????????
//            cookieManager.setCookie(seccode, cookiepre + "seccode;");
//            cookieManager.setCookie(seccode, saltkey+ ";");
            //?????????????????????
            CookieSyncManager.getInstance().sync();

        } catch (Exception e) {
//            Log.e("Nat: webView.syncCookie failed", e.toString());
        }
    }

    private void initView() {
        ll_question = (LinearLayout) findViewById(R.id.ll_question);
        ll_terms = (LinearLayout) findViewById(R.id.ll_terms);
        login_back = (ImageView) findViewById(R.id.login_back);
        cb_terms = (CheckBox) findViewById(R.id.cb_terms);
//        iv_username = (ImageView) findViewById(R.id.iv_username);//??????????????????????????????
        et_username = (EditText) findViewById(R.id.et_username);
        et_username.setOnFocusChangeListener(this);
//        iv_password = (ImageView) findViewById(R.id.iv_password);//????????????????????????
        et_password = (EditText) findViewById(R.id.et_password);
        et_password.setOnFocusChangeListener(this);
//        iv_querypassword = (ImageView) findViewById(R.id.iv_querypassword);//??????????????????????????????
        et_query_password = (EditText) findViewById(R.id.et_query_password);
        et_query_password.setOnFocusChangeListener(this);
//        iv_email = (ImageView) findViewById(R.id.iv_email);//?????????????????????
        et_email = (EditText) findViewById(R.id.et_email);
        et_email.setOnFocusChangeListener(this);
        tv_change_code = (TextView) findViewById(R.id.tv_change_code);//???????????????
        tv_change_code.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //?????????
        et_verifycode = (EditText) findViewById(R.id.et_verifycode);//???????????????
        et_verifycode.setOnFocusChangeListener(this);
        iv_showCode = (WebView) findViewById(R.id.iv_showCode);//???????????????
        tv_register = (TextView) findViewById(R.id.tv_register);//??????
        to_login = (TextView) findViewById(R.id.to_login);
        to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //??????????????????????????????????????????
//        iv_showCode.setImageBitmap(VerifyCode.getInstance().createBitmap());
        realCode = VerifyCode.getInstance().getCode();//??????????????????

        login_back.setOnClickListener(this);
        tv_change_code.setOnClickListener(this);
        tv_register.setOnClickListener(this);
        ll_terms.setOnClickListener(this);
        iv_showCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getQuestion();
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_back:
                finish();
                break;
            case R.id.tv_change_code: //???????????????
//                iv_showCode.setImageBitmap(VerifyCode.getInstance().createBitmap());
//                realCode = VerifyCode.getInstance().getCode();//??????????????????
                break;
            case R.id.tv_register://??????
                getTextValue();
                if (inputusername == null || inputusername.equals("")) {
                    RednetUtils.toast(this, "??????????????????");
                    return;
                }
                if (inputpassword == null || inputpassword.equals("")) {
                    RednetUtils.toast(this, "???????????????");
                    return;
                }
                if (inputquerypassword == null || inputquerypassword.equals("")) {
                    RednetUtils.toast(this, "?????????????????????");
                    return;
                }

                if (!inputpassword.equals(inputquerypassword)) {
                    RednetUtils.toast(this, "?????????????????????");
                    return;
                }

                if (inputemail == null || inputemail.equals("")) {
                    RednetUtils.toast(this, "???????????????");
                    return;
                }
                if (cb_terms.isChecked() == false) {
                    RednetUtils.toast(this, "?????????????????????");
                    return;
                }
                if (ll_question.getVisibility() == View.GONE) {
                    toRegister();
                } else if (ll_question.getVisibility() == View.VISIBLE && inputcode != null && !inputcode.equals("")) {
                    if (inputcode != null && !inputcode.equals("")) {
                        toRegister();
                    } else {
                        RednetUtils.toast(this, "??????????????????");
                    }
//                    //??????????????????????????????
//                    if (realCode.equalsIgnoreCase(inputcode)) {
//                        toRegister();
//                    } else {
//                        RednetUtils.toast(this, "???????????????");
//                    }
                }
                break;
            case R.id.ll_terms:
                Intent intent = new Intent(this, TermsActivity.class);
                startActivity(intent);
                break;
        }
    }

    //?????????
    private String unionid = "";

    private void toRegister() {

        if (type != null && type.equals("weixin")) {
            Platform weChat = ShareSDK.getPlatform(Wechat.NAME);
            unionid = weChat.getDb().get("unionid");
        } else {
            unionid = "";
        }
        sechash = sechash == null ? "" : sechash;
        inputcode = inputcode == null ? "" : inputcode;
        if (type == null) {
            LogUtils.i("???????????????cookie   " + CacheUtils.getString(NewRegisterActivity.this, "cookie"));
            RequestParams requestParams = new RequestParams();
            requestParams.addParam(registBean.getVariables().getReginput().getUsername(), inputusername);
            requestParams.addParam(registBean.getVariables().getReginput().getPassword(), inputpassword);
            requestParams.addParam(registBean.getVariables().getReginput().getPassword2(), inputquerypassword);
            requestParams.addParam(registBean.getVariables().getReginput().getEmail(), inputemail);
            requestParams.addParam("regsubmit", "yes");
            requestParams.addParam("formhash", registBean.getVariables().getFormhash());
            HttpRequest.getInstance().okhttpPostRequest(AppNetConfig.BASEURL + "?version=5&module=register&mod=" + regname, requestParams, new OnRequestListener() {
                @Override
                public void onResponse(String result) {
                    LogUtils.i(result);
                    UserInfoBean userInfoBean = GsonUtil.fromJson(result, UserInfoBean.class);
                    if (StringUtil.isEmpty(userInfoBean.getMessage().getMessagestr())){
                        CacheUtils.putBoolean(NewRegisterActivity.this, "login", true);
                        CacheUtils.putString(NewRegisterActivity.this, "formhash1", userInfoBean.getVariables().getFormhash());
                        CacheUtils.putString(NewRegisterActivity.this, "username", inputusername);
                        CacheUtils.putString(NewRegisterActivity.this, "password", inputpassword);
                        CacheUtils.putString(NewRegisterActivity.this, "questionId", "0");
                        CacheUtils.putString(NewRegisterActivity.this, "answer", "");
                        CacheUtils.putString(NewRegisterActivity.this, "userId", userInfoBean.getVariables().getMember_uid());
                        ContentValues values = User.getContentValues(userInfoBean.getVariables());
                        getContentResolver().insert(User.URI, values);//??????????????????
                        prepareUserData();
                        onTaskFinish();
                        EventBus.getDefault().post(new RefreshUserInfo("0"));
                        ToastUtils.showToast(userInfoBean.getMessage().getMessagestr());
                        setResult(RESULT_OK);
                    }else {
                        ToastUtils.showToast(userInfoBean.getMessage().getMessagestr());
                    }

//                    try {
//                        JSONObject jsonObject = new JSONObject(result);
//                        JSONObject message = jsonObject.getJSONObject("Message");
//                        if (message != null) {
//                            String messagestatus = message.getString("messagestatus");
//                            final String messagestr = message.getString("messagestr");
//                            if (messagestatus.equals("1")) {//??????
//                                JSONObject variables = jsonObject.getJSONObject("Variables");
//                                String member_uid = variables.getString("member_uid");
//                                String formhash1 = variables.getString("formhash");
//                                String cookiepre = variables.getString("cookiepre");
//                                RednetUtils.login(NewRegisterActivity.this);
//                                CacheUtils.putBoolean(NewRegisterActivity.this, "login", true);
//                                CacheUtils.putString(NewRegisterActivity.this, "formhash1", formhash1);
//                                CacheUtils.putString(NewRegisterActivity.this, "username", inputusername);
//                                CacheUtils.putString(NewRegisterActivity.this, "password", inputpassword);
//                                CacheUtils.putString(NewRegisterActivity.this, "questionId", "0");
//                                CacheUtils.putString(NewRegisterActivity.this, "answer", "");
//
//                                CacheUtils.putString(NewRegisterActivity.this, "Cookie", variables.getString("cookiepre"));
//                                CacheUtils.putString(NewRegisterActivity.this, "saltkey", variables.getString("saltkey"));
//                                CacheUtils.putString(NewRegisterActivity.this, "userId", member_uid);
//                                MobclickAgent.onProfileSignIn("Official", inputusername);
//                                String auth1 = URLEncoder.encode(variables.getString("auth"), "UTF-8");
//                                String saltkey1 = URLEncoder.encode(variables.getString("saltkey"), "UTF-8");
//                                String cookiepre_auth = cookiepre + "auth=" + auth1;
//                                String cookiepre_saltkey = cookiepre + "saltkey=" + saltkey1;
//                                CacheUtils.putString(NewRegisterActivity.this, "cookiepre_auth", cookiepre_auth);
//                                CacheUtils.putString(NewRegisterActivity.this, "cookiepre_saltkey", cookiepre_saltkey);
//                                CacheUtils.putString(NewRegisterActivity.this, "userId", member_uid);
//                                UserInfoBean userInfoBean = new Gson().fromJson(jsonObject.toString(), UserInfoBean.class);
//                                ContentValues values = User.getContentValues(userInfoBean.getVariables());
//                                getContentResolver().insert(User.URI, values);//??????????????????
//                                prepareUserData();
//                                onTaskFinish();
//                                EventBus.getDefault().post(new RefreshUserInfo("0"));
//                                setResult(RESULT_OK);
//                            } else {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        RednetUtils.toast(NewRegisterActivity.this, messagestr);
//                                    }
//                                });
//
//                            }
//                        }
//                    } catch (Exception e){
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                RednetUtils.toast(NewRegisterActivity.this, "???????????????");
//                            }
//                        });
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void onError(Exception error) {

                }
            });
//            RedNet.mHttpClient.newCall(new Request.Builder()
////                    .addHeader("Cookie", CacheUtils.getString(NewRegisterActivity.this,"cookie1"))
//                    .url(AppNetConfig.GETREGISTER)
//                    .cacheControl(new CacheControl.Builder().noStore().noCache().build()).build())
//                    .enqueue(new com.squareup.okhttp.Callback() {
//                        @Override
//                        public void onFailure(Request request, IOException e) {
//
//                        }
//
//                        @Override
//                        public void onResponse(Response response) throws IOException {
//                            String s = response.body().string();
//                            try {
//                                JSONObject jsonObject = new JSONObject(s);
//                                final String regname = jsonObject.optString("regname");
//                                LogUtils.i(AppNetConfig.REGISTER + "mod=" + regname);
//                                RedNet.mHttpClient.newCall(new Request.Builder()
////                                        .addHeader("Cookie", CacheUtils.getString(NewRegisterActivity.this,"cookie1"))
//                                        .url(AppNetConfig.REGISTER + "mod=" + regname)
//                                        .cacheControl(new CacheControl.Builder().noStore().noCache().build()).build())
//                                        .enqueue(new com.squareup.okhttp.Callback() {
//                                            @Override
//                                            public void onFailure(Request request, IOException e) {
//
//                                            }
//
//                                            @Override
//                                            public void onResponse(Response response) throws IOException {
//                                                String string = response.body().string();
//                                                RegistBean registBean = GsonUtil.fromJson(string, RegistBean.class);
//                                                FormBody formBody = new FormBody.Builder()
//                                                        .add(registBean.getVariables().getReginput().getUsername(), inputusername)
//                                                        .add(registBean.getVariables().getReginput().getPassword(), inputpassword)
//                                                        .add(registBean.getVariables().getReginput().getPassword2(), inputquerypassword)
//                                                        .add(registBean.getVariables().getReginput().getEmail(), inputemail)
////                                                        .add("sechash", sechash)
////                                                        .add("seccodeverify", inputcode)
//                                                        .add("regsubmit", "yes")
//                                                        .add("formhash", registBean.getVariables().getFormhash())
//                                                        .build();
////                                                RequestParams requestParams = new RequestParams();
////
////                                                HttpRequest.getInstance().okhttpPostRequest(NewRegisterActivity.this,AppNetConfig.BASEURL + "?version=5&module=register&mod=" + regname),requestParams, new OnRequestListener() {
////                                                    @Override
////                                                    public void onResponse(String result) {
////
////                                                    }
////
////                                                    @Override
////                                                    public void onError(Exception error) {
////
////                                                    }
////                                                });
//                                                RequestParams requestParams = new RequestParams();
//                                                requestParams.addParam(registBean.getVariables().getReginput().getUsername(), inputusername);
//                                                requestParams.addParam(registBean.getVariables().getReginput().getPassword(), inputpassword);
//                                                requestParams.addParam(registBean.getVariables().getReginput().getPassword2(), inputquerypassword);
//                                                requestParams.addParam(registBean.getVariables().getReginput().getEmail(), inputemail);
//                                                requestParams.addParam("regsubmit", "yes");
//                                                requestParams.addParam("formhash", registBean.getVariables().getFormhash());
////                                                HttpRequest.getInstance().okhttpPostRequest(AppNetConfig.GETREGISTER, requestParams, new OnRequestListener() {
////                                                    @Override
////                                                    public void onResponse(String result) {
////                                                        LogUtils.i(result);
////                                                    }
////
////                                                    @Override
////                                                    public void onError(Exception error) {
////
////                                                    }
////                                                });
//                                                okhttp3.Request request = new okhttp3.Request.Builder()
////                                                        .addHeader("Cookie", RedNetPreferences.getVcodeCookie() + ";" + RedNetPreferences.getSaltkey() + ";" + RedNetPreferences.getSeccodeCookie() + ";")
//                                                        .url(AppNetConfig.BASEURL + "?version=5&module=register&mod=" + regname)
//                                                        .post(formBody)
//                                                        .build();
//                                                OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                                                        .connectTimeout(5, TimeUnit.MINUTES)
//                                                        .readTimeout(5, TimeUnit.MINUTES)
//                                                        .build();
//                                                Call call = okHttpClient.newCall(request);
//                                                call.enqueue(new okhttp3.Callback() {
//                                                    @Override
//                                                    public void onFailure(Call call, IOException e) {
//                                                        runOnUiThread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                RednetUtils.toast(NewRegisterActivity.this, "??????????????????");
//                                                            }
//                                                        });
//                                                    }
//
//                                                    @Override
//                                                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                                                        String s = response.body().string();
//                                                        try {
//                                                            okhttp3.Response.Builder builder = response.newBuilder();
//                                                            okhttp3.Headers headers = builder.build().headers();
//                                                            List<String> cookies = headers.values("Set-Cookie");
//                                                            String cookie1 = headers.get("Set-Cookie");
//                                                            for (int i = 0, count = headers.size(); i < count; i++) {
//                                                                LogUtils.i("\t" + headers.name(i) + ": " + headers.value(i));
//                                                            }
//                                                            if (cookies.size() > 0) {
//                                                                String session = cookies.get(0);
//                                                                String result = session.substring(0, session.indexOf(";"));
//                                                                CacheUtils.putString(NewRegisterActivity.this, "cookie1", result);
//                                                                LogUtils.i("Cookie=" + cookie1);
//                                                            }
////                                                            CookieSyncManager.createInstance(getApplicationContext());
////                                                            if (!StringUtil.isEmpty(cookie1)) {
////                                                                CacheUtils.putString(NewRegisterActivity.this, "cookie1", cookie1);
////                                                            }
//
//                                                            JSONObject jsonObject = new JSONObject(s);
//                                                            JSONObject message = jsonObject.getJSONObject("Message");
//
//                                                            if (message != null) {
//                                                                String messagestatus = message.getString("messagestatus");
//                                                                final String messagestr = message.getString("messagestr");
//                                                                if (messagestatus.equals("1")) {//??????
//                                                                    JSONObject variables = jsonObject.getJSONObject("Variables");
//                                                                    String member_uid = variables.getString("member_uid");
//                                                                    String formhash1 = variables.getString("formhash");
//                                                                    String cookiepre = variables.getString("cookiepre");
//                                                                    RednetUtils.login(NewRegisterActivity.this);
//                                                                    CacheUtils.putBoolean(NewRegisterActivity.this, "login", true);
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "formhash1", formhash1);
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "username", inputusername);
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "password", inputpassword);
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "questionId", "0");
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "answer", "");
//
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "Cookie", variables.getString("cookiepre"));
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "saltkey", variables.getString("saltkey"));
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "userId", member_uid);
//                                                                    MobclickAgent.onProfileSignIn("Official", inputusername);
//                                                                    String auth1 = URLEncoder.encode(variables.getString("auth"), "UTF-8");
//                                                                    String saltkey1 = URLEncoder.encode(variables.getString("saltkey"), "UTF-8");
//                                                                    String cookiepre_auth = cookiepre + "auth=" + auth1;
//                                                                    String cookiepre_saltkey = cookiepre + "saltkey=" + saltkey1;
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "cookiepre_auth", cookiepre_auth);
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "cookiepre_saltkey", cookiepre_saltkey);
//                                                                    CacheUtils.putString(NewRegisterActivity.this, "userId", member_uid);
//                                                                    UserInfoBean userInfoBean = new Gson().fromJson(jsonObject.toString(), UserInfoBean.class);
//                                                                    ContentValues values = User.getContentValues(userInfoBean.getVariables());
//                                                                    getContentResolver().insert(User.URI, values);//??????????????????
//                                                                    prepareUserData();
//                                                                    onTaskFinish();
//                                                                    EventBus.getDefault().post(new RefreshUserInfo("0"));
//                                                                    setResult(RESULT_OK);
//                                                                } else {
//                                                                    runOnUiThread(new Runnable() {
//                                                                        @Override
//                                                                        public void run() {
//                                                                            RednetUtils.toast(NewRegisterActivity.this, messagestr);
//                                                                        }
//                                                                    });
//
//                                                                }
//                                                            }
////                                }
//                                                        } catch (JSONException e) {
//                                                            runOnUiThread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    RednetUtils.toast(NewRegisterActivity.this, "???????????????");
//                                                                }
//                                                            });
//
//                                                            e.printStackTrace();
//                                                        } catch (UnsupportedEncodingException e) {
//                                                            runOnUiThread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    RednetUtils.toast(NewRegisterActivity.this, "???????????????");
//                                                                }
//                                                            });
//                                                            e.printStackTrace();
//                                                        }
//                                                    }
//                                                });
//                                            }
//                                        });
//
//
//                            } catch (Exception e) {
//
//                            }
//
//                        }
//                    });


        } else

        {
            RedNet.mHttpClient.newCall(new Request.Builder()
                    .addHeader("Cookie", RedNetPreferences.getVcodeCookie() + ";" + RedNetPreferences.getSaltkey() + ";")
                    .url(AppNetConfig.REGISTERBANGDING + "&username=" + inputusername + "&password=" + inputpassword + "&password2=" + inputquerypassword
                            + "&email=" + inputemail + "&openid=" + openId + "&type=" + type + "&unionid=" + unionid + sechash + "&seccodeverify=" + inputcode)
                    .cacheControl(new CacheControl.Builder().noStore().noCache().build()).build())
                    .enqueue(new com.squareup.okhttp.Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RednetUtils.toast(NewRegisterActivity.this, "??????????????????");
                                }
                            });

                        }

                        @Override
                        public void onResponse(final Response response) throws IOException {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                Headers headers = response.request().headers();
                                String cookie1 = headers.get("Cookie");
                                if (!StringUtil.isEmpty(cookie1)) {
                                    CacheUtils.putString(NewRegisterActivity.this, "cookie1", cookie1);
                                }
                                JSONObject variables = jsonObject.getJSONObject("Variables");
                                String member_uid = variables.getString("member_uid");
                                String formhash1 = variables.getString("formhash");
                                String cookiepre = variables.getString("cookiepre");
                                if (variables != null) {
                                    JSONObject message = jsonObject.getJSONObject("Message");
                                    if (message != null) {
                                        String messagestatus = message.getString("messagestatus");
                                        String messagestr = message.getString("messagestr");
                                        if (messagestatus.equals("1")) {//??????
                                            RednetUtils.login(NewRegisterActivity.this);
                                            CacheUtils.putBoolean(NewRegisterActivity.this, "login", true);
                                            CacheUtils.putString(NewRegisterActivity.this, "formhash1", formhash1);
                                            Log.e("TAG", "??????Cookie=" + variables.getString("cookiepre"));
                                            Log.e("TAG", "??????saltkey=" + variables.getString("saltkey"));
                                            Log.e("TAG", "messagestr = " + messagestr);
                                            CacheUtils.putString(NewRegisterActivity.this, "username", inputusername);
                                            CacheUtils.putString(NewRegisterActivity.this, "password", inputpassword);
                                            CacheUtils.putString(NewRegisterActivity.this, "questionId", "0");
                                            CacheUtils.putString(NewRegisterActivity.this, "answer", "");

                                            CacheUtils.putString(NewRegisterActivity.this, "Cookie", variables.getString("cookiepre"));
                                            CacheUtils.putString(NewRegisterActivity.this, "saltkey", variables.getString("saltkey"));
                                            CacheUtils.putString(NewRegisterActivity.this, "userId", member_uid);
                                            MobclickAgent.onProfileSignIn("Official", inputusername);
                                            String platform = CacheUtils.getString(NewRegisterActivity.this, "platform");
                                            if (type != null && !TextUtils.isEmpty(type)) {
                                                CacheUtils.putString(NewRegisterActivity.this, "platform", platform);
                                                if (type.equals("qq")) {
                                                    CacheUtils.putString(NewRegisterActivity.this, "member_loginstatus", "qq");
                                                } else {
                                                    CacheUtils.putString(NewRegisterActivity.this, "member_loginstatus", "weixin");
                                                }
                                            } else {
                                                RedNetApp.getInstance().getUserLogin(true).setMember_loginstatus("1");
                                            }
                                            String auth1 = URLEncoder.encode(variables.getString("auth"), "UTF-8");
                                            String saltkey1 = URLEncoder.encode(variables.getString("saltkey"), "UTF-8");
                                            String cookiepre_auth = cookie1 + "auth=" + auth1;
                                            String cookiepre_saltkey = cookie1 + "saltkey=" + saltkey1;
                                            CacheUtils.putString(NewRegisterActivity.this, "cookiepre_auth", cookiepre_auth);
                                            CacheUtils.putString(NewRegisterActivity.this, "cookiepre_saltkey", cookiepre_saltkey);
                                            CacheUtils.putString(NewRegisterActivity.this, "userId", member_uid);
                                            UserInfoBean userInfoBean = new Gson().fromJson(jsonObject.toString(), UserInfoBean.class);
                                            ContentValues values = User.getContentValues(userInfoBean.getVariables());
                                            getContentResolver().insert(User.URI, values);//??????????????????
                                            prepareUserData();
                                            onTaskFinish();
                                            EventBus.getDefault().post(new RefreshUserInfo("0"));
                                            setResult(RESULT_OK);
                                        } else {
                                            RednetUtils.toast(NewRegisterActivity.this, messagestr);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        RednetUtils.toast(NewRegisterActivity.this, "????????????");
                                    }
                                });
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        RednetUtils.toast(NewRegisterActivity.this, "????????????");
                                    }
                                });
                                e.printStackTrace();
                            } catch (IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        RednetUtils.toast(NewRegisterActivity.this, "????????????");
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    });
        }


    }

    //??????????????????
    private void getTextValue() {
        inputusername = et_username.getText().toString();
        inputpassword = et_password.getText().toString();
        inputquerypassword = et_query_password.getText().toString();
        inputemail = et_email.getText().toString();
        inputcode = et_verifycode.getText().toString();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_username:
//                if (hasFocus) {
//                    iv_username.setBackgroundResource(R.drawable.new_head_light);
//                    iv_password.setBackgroundResource(R.drawable.lock_dark);
//                    iv_querypassword.setBackgroundResource(R.drawable.lock_dark);
//                    iv_email.setBackgroundResource(R.drawable.email_dark);
//                }
                break;
            case R.id.et_password:
//                if (hasFocus) {
//                    iv_username.setBackgroundResource(R.drawable.new_head_dark);
//                    iv_password.setBackgroundResource(R.drawable.lock_light);
//                    iv_querypassword.setBackgroundResource(R.drawable.lock_dark);
//                    iv_email.setBackgroundResource(R.drawable.email_dark);
//                }
                break;
            case R.id.et_query_password:
//                if (hasFocus) {
//                    iv_username.setBackgroundResource(R.drawable.new_head_dark);
//                    iv_password.setBackgroundResource(R.drawable.lock_dark);
//                    iv_querypassword.setBackgroundResource(R.drawable.lock_light);
//                    iv_email.setBackgroundResource(R.drawable.email_dark);
//                }
                break;
            case R.id.et_email:
//                if (hasFocus) {
//                    iv_username.setBackgroundResource(R.drawable.new_head_dark);
//                    iv_password.setBackgroundResource(R.drawable.lock_dark);
//                    iv_querypassword.setBackgroundResource(R.drawable.lock_dark);
//                    iv_email.setBackgroundResource(R.drawable.email_light);
//                }
                break;
            case R.id.et_verifycode:
//                if (hasFocus) {
//                    iv_username.setBackgroundResource(R.drawable.new_head_dark);
//                    iv_password.setBackgroundResource(R.drawable.lock_dark);
//                    iv_querypassword.setBackgroundResource(R.drawable.lock_dark);
//                    iv_email.setBackgroundResource(R.drawable.email_dark);
//                }
                break;
        }
    }

    void prepareUserData() {
        //task = 2 | 4  = 6
        task = task1 | task2;
        User.prepareForum(this, new Callback() {
            @Override
            public void onCallback(Object obj) {
                //??????(^)?????????????????????????????????????????? a^=b?????????a = a^b
                task ^= task1;
                onTaskFinish();
            }
        });
        User.prepareThread(this, new Callback() {
            @Override
            public void onCallback(Object obj) {
                task ^= task2;
                onTaskFinish();
            }
        });
    }

    void onTaskFinish() {
//        if ((task & task1) == 0 && (task & task2) == 0) {
        Intent i = new Intent(NewRegisterActivity.this, RednetMainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("NewRegisterActivity(??????)");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("NewRegisterActivity(??????)");
    }

}
