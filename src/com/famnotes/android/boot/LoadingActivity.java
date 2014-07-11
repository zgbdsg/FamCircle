package com.famnotes.android.boot;



//import com.famnotes.android.ui.HomeActivity;

import com.android.famcircle.R;
import com.android.famcircle.ui.MainActivity;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

//过场类, 目前仅是个空架子，没有动画、多任务等
public class LoadingActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.loading);
			
	new Handler().postDelayed(new Runnable(){
		@Override
		public void run(){
			Intent intent = new Intent(LoadingActivity.this, MainActivity.class);			
			startActivity(intent);			
			LoadingActivity.this.finish();
			Toast.makeText(getApplicationContext(), "Login success!", Toast.LENGTH_SHORT).show();
		}
	}, 100);
   }
}