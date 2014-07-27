package com.famnotes.android.boot;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import com.android.famcircle.FamPushService;
import com.android.famcircle.config.Constants;
import com.famnotes.android.util.ImageLoaderConfig;
import com.famnotes.android.util.MyReceiver;


public class BaseApplication extends Application {
	private MyReceiver mMessageReceiver;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		ImageLoaderConfig.initImageLoader(this, Constants.BASE_IMAGE_CACHE);
		registerMessageReceiver();
		Intent service = new Intent(this,FamPushService.class);
		startService(service);
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		unregisterReceiver(mMessageReceiver);
		super.onTerminate();
	}

	public void registerMessageReceiver() {
		mMessageReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(Constants.MESSAGE_RECEIVED_ACTION);
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		registerReceiver(mMessageReceiver, filter);
	}
}
