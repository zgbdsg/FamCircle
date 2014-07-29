package com.android.famcircle;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.config.Constants;
import com.android.famcircle.util.ACache;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

public class FamPushService extends Service{

	public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
	AlarmManager mAlarmManager = null;
	PendingIntent mPendingIntent = null;
	List<String> msgList;
	ACache mCache;
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
        
        init();
        
        Intent intent = new Intent(getApplicationContext(), FamPushService.class);
        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		mPendingIntent = PendingIntent.getService(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		long now = System.currentTimeMillis();
		mAlarmManager.setInexactRepeating(AlarmManager.RTC, now, 60000, mPendingIntent);
	}

	private void init(){
        mCache = ACache.get(this);
        msgList = (List<String>) mCache.getAsObject("FamPush-msgList");
        if(msgList==null){
        	msgList = new ArrayList<String>();
        }
		if(User.Current==null)
			User.Current=(User) mCache.getAsObject("User.Current");
		if(Groups.lGroup==null || Groups.lGroup.isEmpty()){
			Groups.selectIdx=(Integer) mCache.getAsObject("Groups.selectIdx");
			Groups.lGroup=(List<Group>) mCache.getAsObject("Groups.lGroup");
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
//		Toast.makeText(getApplicationContext(), "Callback Successed!", Toast.LENGTH_SHORT).show();
		Toast.makeText(getApplicationContext(), MESSAGE_RECEIVED_ACTION, Toast.LENGTH_SHORT).show();
		Log.i("famphoto push service", ""+(intent==null)+intent.getAction());
		if(null != intent && null !=intent.getAction() && intent.getAction().equals(MESSAGE_RECEIVED_ACTION)){
			Toast.makeText(getApplicationContext(), "Get new message Successed!", Toast.LENGTH_SHORT).show();
			String extras = intent.getStringExtra(Constants.KEY_EXTRAS);
			doNotification(extras);
		}
		Log.i("famphoto push service", "start !");
		return START_STICKY;
	}

	private void doNotification(String extras) {
		// TODO Auto-generated method stub
		JSONObject extraJson = JSON.parseObject(extras);
		int type = Integer.parseInt(extraJson.getString("type"));
		int usrId = Integer.parseInt(extraJson.getString("usrId"));
		int grpId = Integer.parseInt(extraJson.getString("grpId"));
//		String mess = extraJson.getString("message");					
		User postUser;
		Group postGroup;
		String notificationMessage;					
		
		switch (type) {
		/*
		 * 1-postStatus
		 * 2-postReply
		 * 3-postZan（postReply里type= 1）
		 * 4-removeStatus
		 * 5-removeReply
		 * 6-removeZan（postReply里type= 1）
		 */
			
		case 1:
			Log.i("custom message:", ""+1);
			postUser = User.getUserById(usrId);
			postGroup = Groups.getGroup(grpId);
			notificationMessage = postUser.getName()+" post a status in Group "+postGroup.name;
			notification(FamPushService.this,notificationMessage);
			break;
		case 2:
			if(User.Current.id != usrId){
				Log.i("custom message:", ""+2);
				postUser = User.getUserById(usrId);
				postGroup = Groups.getGroup(grpId);
				notificationMessage = postUser.getName()+" post a reply in Group "+postGroup.name;
				notification(FamPushService.this,notificationMessage);
			}
			break;
		case 3:
			if(User.Current.id != usrId){
				Log.i("custom message:", ""+3);
				postUser = User.getUserById(usrId);
				postGroup = Groups.getGroup(grpId);
				notificationMessage = postUser.getName()+" post a like in Group "+postGroup.name;
				notification(FamPushService.this,notificationMessage);
			}
			break;
		case 4:
			break;
		case 5:
			break;
		case 6:
			break;
		}
	}

	private void notification(Context context,String message){
		Log.i("custom message:", "notification");
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.ic_launcher)
			.setTicker(message)
			.setContentTitle("FamCircle")
			.setContentText(message)
			.setAutoCancel(true);
		
		Notification notification = builder.build();
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |=Notification.DEFAULT_SOUND;
		
		NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationmanager.notify(0, notification);
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
