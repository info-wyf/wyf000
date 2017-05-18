package com.shu.wyf.wyfgraduationproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebViewClient;

import java.lang.reflect.InvocationTargetException;

public class WebView extends Activity {
	public android.webkit.WebView wv_web;
	public String web_url;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		wv_web = (android.webkit.WebView) findViewById(R.id.wv_web);
		wv_web.getSettings().setJavaScriptEnabled(true);
		try {
			Intent intent = getIntent();//
			Bundle bundle = intent.getExtras();// .getExtras()
			web_url = bundle.getString("web_url");
			Log.d("test", web_url);
		} catch (Exception e) {
			Log.d("test", "web_url");
		}
		wv_web.loadUrl(web_url);
		wv_web.setWebViewClient(new HelloWebViewClient());
		wv_web.setWebChromeClient(new WebChromeClient());
		wv_web.requestFocus();
		wv_web.getSettings().setDefaultTextEncodingName("utf-8");
		wv_web.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		
	}
	@Override
	protected void onPause() {
		try {
			wv_web.getClass().getMethod("onPause").invoke(wv_web, (Object[])null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}
	@Override
	protected void onResume() {
		try {
			wv_web.getClass().getMethod("onResume").invoke(wv_web, (Object[])null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onResume();
	}
    @Override
    //
    //
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wv_web.canGoBack()) {
        	wv_web.goBack(); //goBack()
            return true;  
        } 
        else
        {
        	finish();
        	return true;
        }
    }  
	private class HelloWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(android.webkit.WebView view,String url){
			view.loadUrl(url);
			return true;
		}
	}
}
