package com.android.famcircle.ui;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.famcircle.R;
import com.famnotes.android.base.AppManager;
import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.ACache;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MainActivity extends Activity {
	private ACache mCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mCache = ACache.get(this);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new MainFragment()).commit();
		}
		
		Log.i("base path", getFilesDir().getPath());
		String baseFilePath  = getFilesDir().getPath()+"/zip";
		String compressFilepath = getFilesDir().getPath()+"/compress";
		
		File dir = new File(baseFilePath);
		if(!dir.exists())
			dir.mkdirs();
		
		dir = new File(compressFilepath);
		if(!dir.exists())
			dir.mkdirs();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}else if(id == R.id.action_clearcache){
			mCache.clear();
		}
		
		if (id == R.id.menu_exit) {
			new AlertDialog.Builder(this).setTitle("").setMessage("退出吗？")
			.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AppManager.getInstance().AppExit(getApplicationContext());
					ImageLoader.getInstance().clearMemoryCache();
				}
			})
			.setNegativeButton("CANCEL", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			})
			.show();
		}

		if (id ==R.id.menu_clear) {
		  new AlertDialog.Builder(this).setTitle("Delete User Data").setMessage("Delete User Data, then Exit")
			.setPositiveButton("OK",  new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							DBUtil.clearDatabase();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						AppManager.getInstance().AppExit(getApplicationContext());//System.exit(0);
					}
				})
			.setNegativeButton("CANCEL", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
			.show();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

}
