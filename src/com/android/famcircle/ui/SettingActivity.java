package com.android.famcircle.ui;



import com.android.famcircle.R;
import com.famnotes.android.base.BaseActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingActivity extends BaseActivity{

	private AlertDialog.Builder  builder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		builder=new AlertDialog.Builder(this)
		.setTitle("提示")
		.setPositiveButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
					}

				}).setNegativeButton("确定", null);
		
		
		setContentView(R.layout.setting); //View view			=  inflater.inflate(R.layout.setting, container, false);
		TextView textView	=(TextView)findViewById(R.id.title_text);
		textView.setText("设置");  //我的设置 --> 设置
		RelativeLayout  re= (RelativeLayout)findViewById(R.id.clear_map);
		re.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				builder.setMessage("确定要清楚地图缓存吗？").show();
			}
		});
		
	}

	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

//		((RelativeLayout) view.findViewById(R.id.setting_hotel)).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				View hell		=inflater.inflate(R.layout.seek_bar, null);
//				SeekBar seekBar		=(SeekBar) hell.findViewById(R.id.seekBar1);
//				//设置变化范围,有个问题没有解决，就是seekbar的背景和前景不能正常出来
//				seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//					
//					int currentProgress;
//					
//					@Override
//					public void onStopTrackingTouch(SeekBar seekBar) {
//						seekBar.setProgress(currentProgress);
//					}
//					
//					@Override
//					public void onStartTrackingTouch(SeekBar seekBar) {
//						
//					}
//					
//					@Override
//					public void onProgressChanged(SeekBar seekBar, int progress,
//							boolean fromUser) {
//						 if(progress>0&&progress<seekBar.getMax()/6){
//							 progress=0;
//						 }else if(progress<seekBar.getMax()/2){
//							 progress=seekBar.getMax()/3;
//						 }else if(progress<seekBar.getMax()*5/6){
//							 progress=seekBar.getMax()*2/3;
//						 }else{
//							 progress=seekBar.getMax();
//						 }
//						 currentProgress=progress;
//					}
//				});
//				
//				new AlertDialog.Builder(getActivity())
//				.setTitle("选择身边酒店的距离")
//				.setNegativeButton("确定", null)
//				.setView(hell)
//				.show();
//			}
//		});
//		return view;
		
		return null;
	}

}
