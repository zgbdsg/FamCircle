package com.android.famcircle;

import android.app.Application;
import android.content.res.Configuration;

import com.android.famcircle.config.Constants;
import com.android.famcircle.util.ImageLoaderConfig;


public class BaseApplication extends Application {

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
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

}
