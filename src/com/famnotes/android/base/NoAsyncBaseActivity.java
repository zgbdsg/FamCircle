package com.famnotes.android.base;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.famcircle.config.Constants;
import com.famnotes.android.util.ImageLoaderConfig;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class NoAsyncBaseActivity extends Activity  {

	public static final String TAG = NoAsyncBaseActivity.class.getSimpleName();

	protected Handler mHandler = null;
	protected InputMethodManager imm;
	protected TelephonyManager tManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		AppManager.getInstance().addActivity(this);
		if (!ImageLoader.getInstance().isInited()) {
			ImageLoaderConfig.initImageLoader(this, Constants.BASE_IMAGE_CACHE);
		}
		tManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		imm=(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	}

	
	/**
	 * 通过类名启动Activity
	 * 
	 * @param pClass
	 */
	public void openActivity(Class<?> pClass) {
		openActivity(pClass, null);
	}

	/**
	 * 通过类名启动Activity，并且含有Bundle数据
	 * 
	 * @param pClass
	 * @param pBundle
	 */
	protected void openActivity(Class<?> pClass, Bundle pBundle) {
		Intent intent = new Intent(this, pClass);
		if (pBundle != null) {
			intent.putExtras(pBundle);
		}
		startActivity(intent);
	}
	
	protected void openActivityForResult(Class<?> pClass, Bundle pBundle, int requestCode) {
		Intent intent = new Intent(this, pClass);
		if (pBundle != null) {
			intent.putExtras(pBundle);
		}
		startActivityForResult(intent, requestCode);
	}	

	/**
	 * 通过Action启动Activity
	 * 
	 * @param pAction
	 */
	protected void openActivity(String pAction) {
		openActivity(pAction, null);
	}

	/**
	 * 通过Action启动Activity，并且含有Bundle数据
	 * 
	 * @param pAction
	 * @param pBundle
	 */
	protected void openActivity(String pAction, Bundle pBundle) {
		Intent intent = new Intent(pAction);
		if (pBundle != null) {
			intent.putExtras(pBundle);
		}
		startActivity(intent);
	}
	

	public void DisplayLongToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	}
	public void DisplayShortToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}	
	
	protected void hideOrShowSoftInput(boolean isShowSoft,EditText editText) {
		if (isShowSoft) {
			imm.showSoftInput(editText, 0);
		}else {
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		}
	}
	
	//获得当前程序版本信息
	protected String getVersionName() throws Exception {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		return packInfo.versionName;
	}

	
//獲得設備信息
	protected String getDeviceId() throws Exception {
		String deviceId=tManager.getDeviceId();
		
		return deviceId;
		
	}
	
	/**
	 * 获取SIM卡序列号
	 * 
	 * @return
	 */
	protected String getToken() {
		return tManager.getSimSerialNumber();
	}

	/*獲得系統版本*/
	
	protected String getClientOs() {
		return android.os.Build.ID;
	}
	
	/*獲得系統版本號*/
	protected String getClientOsVer() {
		return android.os.Build.VERSION.RELEASE;
	}
	
	//獲得系統語言包
	protected String getLanguage() {
		return Locale.getDefault().getLanguage();
	}
	
	protected String getCountry() {
		
		return Locale.getDefault().getCountry();
	}
	
	
}
