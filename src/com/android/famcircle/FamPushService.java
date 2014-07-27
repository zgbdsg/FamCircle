package com.android.famcircle;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

public class FamPushService extends Service{

	AlarmManager mAlarmManager = null;
	PendingIntent mPendingIntent = null;
//	private MyReceiver mMessageReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Toast.makeText(getApplicationContext(), "Push service !", Toast.LENGTH_SHORT).show();
		Log.i("famphoto push service", "create !");
		JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush
        
        Intent intent = new Intent(getApplicationContext(), FamPushService.class);
        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		mPendingIntent = PendingIntent.getService(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		long now = System.currentTimeMillis();
		mAlarmManager.setInexactRepeating(AlarmManager.RTC, now, 60000, mPendingIntent);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		Toast.makeText(getApplicationContext(), "Callback Successed!", Toast.LENGTH_SHORT).show();  
		Log.i("famphoto push service", "start !");
		return START_STICKY;
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTrimMemory(int level) {
		// TODO Auto-generated method stub
		super.onTrimMemory(level);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Intent service = new Intent(this,FamPushService.class);
		startService(service);
		super.onDestroy();
//		unregisterReceiver(mMessageReceiver);
	}

//	public void registerMessageReceiver() {
//		mMessageReceiver = new MyReceiver();
//		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
//		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
//		filter.addAction(Constants.MESSAGE_RECEIVED_ACTION);
//		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
//		registerReceiver(mMessageReceiver, filter);
//	}
}
