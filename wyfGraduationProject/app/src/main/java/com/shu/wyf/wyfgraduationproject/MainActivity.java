package com.shu.wyf.wyfgraduationproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoiceRecognition;

import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements
        SinVoiceRecognition.Listener {
    private final static String TAG = "MainActivityxx";

    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;
    private final static int MSG_PLAY_TEXT = 4;
    private final static int[] TOKENS = {32, 32, 32, 32, 32, 32};
    private final static int TOKEN_LEN = TOKENS.length;
    private static final int REQUEST_STORAGE_PERMISSION = 10;
    private static final OkHttpClient okHttpClient = new OkHttpClient();//okHttpClient is a instance of OkHttpClient

    private Handler mHanlder;
    private SinVoiceRecognition mRecognition;
    private boolean mIsReadFromFile;
    private PowerManager.WakeLock mWakeLock;
    private PostUtilClass postUtilClass = null;
    private CustomProgressDialog dialog = null;   //dialog is a instance of CustomProgressDialog
    private ParseFromJsonClass parseFromJsonClass = null;
    static TextView mRecognisedTextView;
    private char mRecgs[] = new char[100];
    private int mRecgCount;
    private static String strReg = "";
    private String str = null;
    private boolean flag_dopost;
    private boolean flag_setview;
    private String web_url = null;

    /*load jni*/
    static {
        System.loadLibrary("sinvoice");
        LogHelper.d(TAG, "sinvoice jnicall loadlibrary");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIsReadFromFile = false;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);

        mRecognition = new SinVoiceRecognition();
        mRecognition.init(this);
        mRecognition.setListener(this);

        mRecognisedTextView = (TextView) findViewById(R.id.regtext);
        mHanlder = new RegHandler(this);

        requestPermission();
        parseFromJsonClass = new ParseFromJsonClass();

        Button recognitionStart = (Button) findViewById(R.id.start_reg);
        recognitionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /******************************************************/
                mRecognition.start(TOKEN_LEN, mIsReadFromFile);
                /******************************************************/
            }
        });

        Button recognitionStop = (Button) findViewById(R.id.stop_reg);
        recognitionStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /******************************************************/
                mRecognition.stop();
                /******************************************************/
            }
        });
    }

    /*
    *   网络访问信息应在onResume里面加载
    * */
    @Override
    protected void onResume() {
        super.onResume();
        mWakeLock.acquire();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // try catch capture the exception of onPause,such as dialog and mWakeLock
        try {
            if (dialog != null)
                dialog.dismiss();
            if (mWakeLock.isHeld())
                mWakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /******************************************************/
        //when onPause ,the Recognition should stop,this is the method of stopping the recognition process
        mRecognition.stop();
        /******************************************************/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecognition.uninit();
    }

    private void requestPermission() {
        //判断系统版本
        if (Build.VERSION.SDK_INT >= 23) {
            //检测当前app是否拥有某个权限
            int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //判断这个权限是否已经授权过
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    Toast.makeText(this, "Need Storage permission.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_STORAGE_PERMISSION);
                return;
            } else {
            }
        } else {
        }
    }

    private class RegHandler extends Handler {
        private MainActivity mAct;

        public RegHandler(MainActivity act) {
            mAct = act;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_RECG_TEXT:
                    char ch = (char) msg.arg1;
                    mAct.mRecgs[mAct.mRecgCount++] = ch;
                    break;

                case MSG_RECG_START:
                    mAct.mRecgCount = 0;
                    break;

                case MSG_RECG_END:
                    LogHelper.d(TAG, "recognition end gIsError:" + msg.arg1);
                    if (mAct.mRecgCount > 0) {
                        byte[] strs = new byte[mAct.mRecgCount];
                        for (int i = 0; i < mAct.mRecgCount; ++i) {
                            strs[i] = (byte) mAct.mRecgs[i];
                        }
                        try {
                            strReg = new String(strs, "UTF8");
                            if (msg.arg1 < 0) {
                                Log.d(TAG, "reg ok!!!!!!!!!!!!");
                                if (null != mAct) {
                                    Log.d(TAG, strReg);
                                    str = strProcess(strReg);
                                    Log.d(TAG, str + "*<-*");
                                    //if the correct building information has been parsed,do post process
                                    if (str != null)
                                        flag_dopost = true;
                                    Log.d(TAG, flag_dopost + "");
                                    // do post process is the AsyncTask
                                    if (flag_dopost) {
                                        new Task().execute();
                                    }
                                }
                            } else {
                                Log.d(TAG, "reg error!!!!!!!!!!!!!");
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case MSG_PLAY_TEXT:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onSinVoiceRecognitionStart() {
        mHanlder.sendEmptyMessage(MSG_RECG_START);
    }

    @Override
    public void onSinVoiceRecognition(char ch) {
        mHanlder.sendMessage(mHanlder.obtainMessage(MSG_SET_RECG_TEXT, ch, 0));
    }

    @Override
    public void onSinVoiceRecognitionEnd(int result) {
        mHanlder.sendMessage(mHanlder.obtainMessage(MSG_RECG_END, result, 0));
    }

    private String strProcess(String strReg) {
        if (strReg.substring(0, 2).equals("qw") && strReg.substring(6, 8).equals("er")) {
            String key = strReg.substring(2, 6);
            return key;
        } else
            return "wyf error";
    }

    private void doPost() {
        postUtilClass = new PostUtilClass(handler);
        Log.d(TAG, "postUtilClass created");
        if (str != null) {
            Log.d(TAG, "do post start");
            postUtilClass.doPost(str, okHttpClient);
        }
    }

    private void goToWeb() {
        try {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, WebView.class);

            intent.putExtra("web_url", web_url);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x0005:
                    Log.d("wyf", "loading data form server finished!");
                    if (postUtilClass.getJsonString() != null) {
                        parseFromJsonClass.parseItemInfo(postUtilClass.getJsonString());
                        web_url = parseFromJsonClass.getUrl();
                        if (web_url != null)
                            goToWeb();
                        Log.d("wyf", "跳转至：" + parseFromJsonClass.getIntro());
                        postUtilClass.clrJsonString();
                    }
                    break;
                case 0x0006:
                    Log.d("wyf", "loading data form server failed!");
                    break;
                default: {
                    break;
                }
            }
        }
    };

    class Task extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            dialog = new CustomProgressDialog(MainActivity.this, "正在加载中", R.drawable.frame);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.d(TAG, "doShowListView start");
                doPost();
                /******************************************************/
                mRecognition.stop();
                /******************************************************/
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.d(TAG, "flag_dopost:" + flag_dopost);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (flag_setview) {
                dialog.dismiss();
                flag_setview = false;
            }
        }
    }
}
