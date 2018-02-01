package com.n22.plugin.download;

import java.io.File;
import java.io.IOException;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import com.n22.thread.SafeThread;
import com.n22.thread.ThreadPool;
import com.n22.utils.FileDownLoader;

/**
 * This class echoes a string called from JavaScript.
 */
public class Download extends CordovaPlugin {
	Handler handler;
	int DOWNLOAD_END = 1;
	int DOWNLOAD_FAIL = 2;
	int UPDATE = 3;
	int NOUPDATE = 4;
	int UPDATEFULLDOSE = 5;
	int DOWNLOAD_END_FULLDOSE = 6;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
    	handler = new Handler(Looper.getMainLooper()) {
			@SuppressWarnings("static-access")
			@Override
			public void handleMessage(android.os.Message msg) {
				int what = msg.what;
				if(what == UPDATE){
					//开始更新
					update(msg.obj+"","JL.zip",DOWNLOAD_END);
				}
				if(what == UPDATEFULLDOSE){
//					开始更新
					update(msg.obj+"","android.apk",DOWNLOAD_END_FULLDOSE);
				}
				if (what == DOWNLOAD_END) {
//					下载完成解压
				}
				if(what == DOWNLOAD_END_FULLDOSE){
					//下载完成准备安装
				}
				if (what == DOWNLOAD_FAIL) {
//					pBar.dismiss();
//					progressbar.setVisibility(View.VISIBLE);
//					hint.setText("下载失败");
					Toast.makeText(cordova.getActivity(), "下载失败", Toast.LENGTH_LONG).show();
				}
				if(what == NOUPDATE){
//					hint.setText("已是最新版本，无需升级");
//					Intent t = new Intent();
//					t.setClass(LoginActivity.this, MainActivity.class);
//					startActivity(t);
//					finish();  
				}
			}
		};
    }
    
	void update(final String url,final String filename,final int type) {
//		progressbar.setIndeterminate(false);
		registerBoradcastReceiver("SYS_UPDATE");
		ThreadPool.excute(new SafeThread(url) {
			@Override
			public void deal() {
				FileDownLoader downloader = new FileDownLoader();
				downloader.setContext(cordova.getActivity());
				try {
					String result = downloader.downloadFile(url, "/data/data/"+cordova.getActivity().getPackageName()+"/files/n22/download/", filename);
//					String result = downloader.downloadFile(url, Environment.getExternalStorageDirectory()+"/dadongfang/download/", filename);
					if(result.equals(FileDownLoader.RETURNSUCCESS)){
						System.out.println("下载完成");
						android.os.Message message = android.os.Message.obtain();
						message.what = type;
						handler.sendMessage(message);
					}else{
						System.out.println("下载程序异常1");
						android.os.Message message = android.os.Message.obtain();
						message.what = DOWNLOAD_FAIL;
						handler.sendMessage(message);
					}
					
				} catch (Exception e) {
					System.out.println("下载程序异常");
					android.os.Message message = android.os.Message.obtain();
					message.what = DOWNLOAD_FAIL;
					handler.sendMessage(message);
					e.printStackTrace();
				}

			}
		});
	}
	
	public void registerBoradcastReceiver(String action) {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(action);
		System.out.println("reg:");
		cordova.getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
	}
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("SYS_UPDATE")) {
				int progress = intent.getExtras().getInt("progress");
//				pBar.setProgress(progress);
//				hint.setText("正在更新"+appVersionStr+"版本："+progress+"%");
//				progressbar.setProgress(progress);
			}
		}
	};
}
