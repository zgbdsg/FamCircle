package com.famnotes.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import com.android.famcircle.FamPushService;
import com.android.famcircle.config.Constants;
import com.android.famcircle.ui.MainActivity;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";

	@Override
	public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) { 
        	Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction());
        	if(!JPushInterface.getConnectionState(context)){
	            JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
	            JPushInterface.init(context);     		// 初始化 JPush
        	}
            
//        	Intent iPush= new Intent(context,PushService.class);
//        	context.startService(iPush);
//        	Intent iDown = new Intent(context,DownloadService.class);
//        	context.startService(iDown);
        	
        }else if(intent.getAction().equals(Intent.ACTION_TIME_TICK)){
        	Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction()+"  state :"+JPushInterface.getConnectionState(context));
        	if(!JPushInterface.getConnectionState(context)){
        		Log.d(TAG, "[MyReceiver] init jpush "+intent.getAction());
	            JPushInterface.init(context); 
        	}
//        	boolean isPushServiceRunning = false; 
//        	boolean isDownServiceRunning = false;
//        	ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
//        	
//        	for (RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) { 
//        		if("cn.jpush.android.service.PushService".equals(service.service.getClassName()))
//        			isPushServiceRunning = true;
//        		if("cn.jpush.android.service.DownloadService".equals(service.service.getClassName()))
//        			isDownServiceRunning = true;
//        		
//        		if(isDownServiceRunning && isPushServiceRunning)
//        			break;
//        	}
//        	if (!isPushServiceRunning) { 
//        		Intent i= new Intent(context,PushService.class);
//            	context.startService(i);
//        	}
//        	
//        	if (!isDownServiceRunning) { 
//        		Intent i= new Intent(context,DownloadService.class);
//            	context.startService(i);
//        	}
        }
        else if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...
                        
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	processCustomMessage(context, bundle);
        
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
            receivingNotification(context,bundle);
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            
//            JPushInterface.reportNotificationOpened(context, bundle.getString(JPushInterface.EXTRA_MSG_ID));
            
        	//打开自定义的Activity
        	Intent i = new Intent(context, MainActivity.class);
        	Bundle intentBundle = new Bundle();
        	intentBundle.putBoolean("push", true);
        	i.putExtras(intentBundle);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        	context.startActivity(i);
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.e(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	
        	if(!JPushInterface.getConnectionState(context)){
        		Log.d(TAG, "[MyReceiver] init jpush "+intent.getAction());
	            JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
	            JPushInterface.init(context);     		// 初始化 JPush
        	}
        	
        	Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} 
			else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
	
	private void receivingNotification(Context context, Bundle bundle){
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        Log.d(TAG, " title : " + title);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        Log.d(TAG, "message : " + message);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        Log.d(TAG, "extras : " + extras);
    } 
	
	//send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
//		if (MainActivity.isForeground) {
			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Log.i("custom message:", extras);
			Intent msgIntent = new Intent(context,FamPushService.class);
			msgIntent.setAction(Constants.MESSAGE_RECEIVED_ACTION);
			msgIntent.putExtra(Constants.KEY_MESSAGE, message);
			msgIntent.putExtra(Constants.KEY_EXTRAS, extras);
			context.startService(msgIntent);
//			context.sendBroadcast(msgIntent);
//		}
	}
	
}
