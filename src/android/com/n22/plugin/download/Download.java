package com.n22.plugin.download;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.widget.Toast;
import com.n22.thread.SafeThread;
import com.n22.thread.ThreadPool;
import com.n22.utils.FileDownLoader;
import com.n22.utils.JsonUtil;
import com.n22.utils.ZipUtil;

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
	ProgressDialog pBar;
	String filePath;
	private CallbackContext currentCallbackContext;
	
	@Override
	public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
		// TODO Auto-generated method stub
		super.initialize(cordova, webView);
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
					pBar.dismiss();
					currentCallbackContext.success();
				}
				if(what == DOWNLOAD_END_FULLDOSE){
					
					Runtime runtime = Runtime.getRuntime();
		            String command1 = "chmod -R 777 " + cordova.getActivity().getFilesDir();
		            try {
						runtime.exec(command1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					File f = new File(filePath);
					if(f.exists()){
						System.out.println("存在文件");
					}
					
					try{
					    Thread thread = Thread.currentThread();
					    thread.sleep(1500);//暂停1.5秒后程序继续执行
					}catch (InterruptedException e) {
					    // TODO Auto-generated catch block
					    e.printStackTrace();
					}
					
					//下载完成准备安装00000000
					if (Build.VERSION.SDK_INT >= 24) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						 intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				        Uri apkUri = FileProvider.getUriForFile(cordova.getActivity(), cordova.getActivity().getPackageName()+".provider", new File(filePath));
				        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
				        cordova.getActivity().startActivity(intent);
					}else{
						Intent intent = new Intent(Intent.ACTION_VIEW);
				        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        intent.setDataAndType(Uri.fromFile(new File(filePath)),"application/vnd.android.package-archive");
				        cordova.getActivity().startActivity(intent);
					}
				}
				if (what == DOWNLOAD_FAIL) {
					pBar.dismiss();
					currentCallbackContext.error("error");
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
	
    @Override
    public boolean execute(String action, String args, CallbackContext callbackContext) throws JSONException {
    	Toast.makeText(cordova.getActivity(), "进入方法", Toast.LENGTH_LONG).show();
    	currentCallbackContext = callbackContext;
    	Map<String,String> map = (Map<String, String>) JsonUtil.jsonToObject(args, Map.class);
    	if (action.equals("file")) {
            this.file(map);
            return true;
        }else if(action.equals("unpack")){
        	this.unpack(map);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
	private void file(Map<String,String> message) {
    	Toast.makeText(cordova.getActivity(), "准备开始下载", Toast.LENGTH_LONG).show();
//		update("http://140.207.91.54:8098/jl_server/downFile.do?isAddSharePath=true&fileName=/data/kmss/resource/app/junlong_V3.75.zip","JL.zip",DOWNLOAD_END);
    	filePath = message.get("filePath");
    	if(message.get("type").equals("zip")){
        	update(message.get("url"),message.get("name")+"."+message.get("type"),DOWNLOAD_END);
    	}else if(message.get("type").equals("apk")){
        	update(message.get("url"),message.get("name")+"."+message.get("type"),DOWNLOAD_END_FULLDOSE);
    	}
//    	File f = new File(filePath);
//		if(f.exists()){
//			System.out.println("存在文件"+f.length());
//		}
//    	String cmd = "chmod 777 " + filePath;
//    	try {
//    	Runtime.getRuntime().exec(cmd);
//    	     } catch (IOException e) {
//    	e.printStackTrace();
//    	     }
//    	
//		
//		try{
//		    Thread thread = Thread.currentThread();
//		    thread.sleep(1500);//暂停1.5秒后程序继续执行
//		}catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//    	Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(Uri.fromFile(new File("/data/data/cn.com.junlong.kdlinssit/files/n22/download/ruihua.apk")),"application/vnd.android.package-archive");
//        cordova.getActivity().startActivity(intent);
    }
    @SuppressWarnings("unused")
   	private void unpack(Map<String,String> message) {
    	message.get("targetPath");
    	try {
			ZipUtil.commonUnZip(message.get("unpackPath"),message.get("targetPath"),cordova.getActivity());//解压路径，目标路径
			currentCallbackContext.success();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			currentCallbackContext.error("error");
		}
    }
	void update(final String url,final String filename,final int type) {
//		progressbar.setIndeterminate(false);
		pBar = new ProgressDialog(cordova.getActivity());
		pBar.setTitle("正在下载");
		pBar.setMessage("请稍候...");
		pBar.setIndeterminate(false);
		pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pBar.setProgress(100);
		pBar.setCancelable(false);
		pBar.setCanceledOnTouchOutside(false);
		pBar.show();
		registerBoradcastReceiver("SYS_UPDATE");
		ThreadPool.excute(new SafeThread(url) {
			@Override
			public void deal() {
				FileDownLoader downloader = new FileDownLoader();
				downloader.setContext(cordova.getActivity());
				try {
					String result = downloader.downloadFile(url, "/data/data/"+cordova.getActivity().getPackageName()+"/files/n22/download/", filename);
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
				pBar.setProgress(progress);
//				Toast.makeText(cordova.getActivity(), "更新完成="+progress, Toast.LENGTH_SHORT).show();
//				pBar.setProgress(progress);
//				hint.setText("正在更新"+appVersionStr+"版本："+progress+"%");
//				progressbar.setProgress(progress);
			}
		}
	};
	
//	private boolean systemUpdate() {
//		// /data/data/cn.com.junlong.kdlinssit/files/jl/download/junlong_V1.1.zip
//		try {//file:data/data/cn.com.junlong.kdlinssit/files/jl/www/web-app/index.html
//			ZipUtil.commonUnZip(Path.getRootPath()+"download/",Path.getRootPath() + "download/JL.zip",this);
//			FileUtil.copy(Path.getRootPath() + "download/www/junlong", Path.getRootPath() + "/www/web-app");
//			return true;
//		} catch (Exception e) {
//			System.out.println(e);
//			return false;
//		}
//	}
}
