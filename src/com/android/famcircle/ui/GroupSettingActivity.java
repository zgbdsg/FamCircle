package com.android.famcircle.ui;


import java.util.ArrayList;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.RequestCode;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.boot.FamilyMemberSetting;
import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.ImageUtils;
import com.famnotes.android.util.PictureBody;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;
import com.famnotes.crop.CropImageActivity;

public class GroupSettingActivity extends BaseActivity{

	private AlertDialog.Builder  builder;
	private int grpId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		grpId=getIntent().getIntExtra("GroupId", User.Current.grpId); 
		
		builder=new AlertDialog.Builder(this)
		.setTitle("提示")
		.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//清除此群缓存
					}

				}).setNegativeButton("取消", null);
		
		
		setContentView(R.layout.activity_group_setting); //View view			=  inflater.inflate(R.layout.setting, container, false);
		
		RelativeLayout  re= (RelativeLayout)findViewById(R.id.clear_map);
		re.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				builder.setMessage("确定要清除此群缓存吗？").show();
			}
		});
		
	}

	public View onCreateView(final LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {

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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			go_back(null);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	public void setting_group_head(View v) {
		Bundle bundle=new Bundle(); bundle.putInt("modal",  1);  bundle.putInt("direction", RequestCode.DirectionGoback);
		openActivityForResult(CropImageActivity.class, bundle, RequestCode.GroupPictureSetting);
	}
	public void setting_person_head(View v) {
		Bundle bundle=new Bundle(); bundle.putInt("modal",  0);  bundle.putInt("direction", RequestCode.DirectionGoback);
		openActivityForResult(CropImageActivity.class, bundle, RequestCode.PersonPhotoSetting);
	}
	
	public void setting_group_member(View v) {
		Bundle bundle=new Bundle(); bundle.putInt("GroupId",  grpId);  bundle.putInt("direction", RequestCode.DirectionGoback);
		openActivityForResult(FamilyMemberSetting.class, bundle, RequestCode.GroupMemberSetting);
	}
	
	Intent  resultIntent=new Intent();
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_OK  && data != null){
			resultIntent.fillIn(data, Intent.FILL_IN_DATA); //?好不好
		}
		
		switch(requestCode){
			case RequestCode.PersonPhotoSetting : {
				if(data!=null){
					//byte[] dataBm=data.getByteArrayExtra("cropPic");
					//Bitmap cropedBitmap=BitmapFactory.decodeByteArray(dataBm, 0, dataBm.length);
					
					String path=data.getStringExtra("picPath");
					PersonPhotoSettingTask  task=new PersonPhotoSettingTask();
					PersonPhotoSettingHandler handler=new PersonPhotoSettingHandler(this);
					task.connect(handler);
					task.execute(path);
				}
				break;
			}
			
			case RequestCode.GroupPictureSetting : {
				if(data!=null){
					//byte[] dataBm=data.getByteArrayExtra("cropPic");
					//Bitmap cropedBitmap=BitmapFactory.decodeByteArray(dataBm, 0, dataBm.length);
					
					String path=data.getStringExtra("picPath");
					GroupPictureSettingTask  task=new GroupPictureSettingTask();
					GroupPictureSettingHandler handler=new GroupPictureSettingHandler(this);
					task.connect(handler);
					task.execute(path);
				}
				break;
			}
			
			default :
				break;
		}
	}
	
	public void go_back(View v) {
		setResult(RESULT_OK,  resultIntent);
		finish();
	}
	
	class PersonPhotoSettingHandler extends BaseAsyncTaskHandler<GroupSettingActivity, Integer>{

		public PersonPhotoSettingHandler(GroupSettingActivity context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onTaskFailed(GroupSettingActivity context, Exception arg1) {
			// TODO Auto-generated method stub
			context.DisplayLongToast(""+arg1.getMessage());
			return true;
		}

		@Override
		public boolean onTaskSuccess(GroupSettingActivity context, Integer arg1) {
			// TODO 更新数据库 Avatar
			if(0==arg1)
				context.DisplayShortToast("Success");
			return true;
		}
		
		
	}

	class PersonPhotoSettingTask extends BaseAsyncTask<GroupSettingActivity, String, Integer>{  //<GroupSettingActivity, Bitmap, Integer>

		@Override
		public Integer run(String... path) throws Exception {
			// TODO Auto-generated method stub
			ArrayList<PictureBody> pics = new ArrayList<PictureBody>();
				
			Bitmap tmpmap = ImageUtils.compImageBySize(path[0]);
			String filename=StringUtils.getFileNameFromPath(path[0]);
			PictureBody pb = new PictureBody(tmpmap, Bitmap.CompressFormat.JPEG , filename);
			pics.add(pb);
			PostData	pdata = new PostData("share", "uploadUsrAvatar", "{\"usrId\":"+ShareActivity.userId+"}", pics);
			
			JSONObject responseJSONObject = null;
			try{
				String json = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
				responseJSONObject = JSON.parseObject(json);
			}catch(Exception ex){
				ex.printStackTrace();
				throw ex;
			}
			if (responseJSONObject.getIntValue("errCode") == 0) {
				try{
					JSONObject  resultsJO= responseJSONObject.getJSONObject("results");
					User.Current.avatar=resultsJO.getString("avatar");

					DBUtil.insertUser(User.Current);
				}catch(Exception ex){
					ex.printStackTrace();
					return 1;
				}
				return 0;
			} else {
				throw new Exception(responseJSONObject.getString("errMesg"));
			}
		}
		
	}


	class GroupPictureSettingHandler extends BaseAsyncTaskHandler<GroupSettingActivity, Integer>{

		public GroupPictureSettingHandler(GroupSettingActivity context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onTaskFailed(GroupSettingActivity context, Exception arg1) {
			// TODO Auto-generated method stub
			context.DisplayLongToast(""+arg1.getMessage());
			return true;
		}

		@Override
		public boolean onTaskSuccess(GroupSettingActivity context, Integer arg1) {
			// TODO 更新...
			if(0==arg1)
				context.DisplayShortToast("Success");
			return true;
		}
		
		
	}

	class GroupPictureSettingTask extends BaseAsyncTask<GroupSettingActivity, String, Integer>{  //<GroupSettingActivity, Bitmap, Integer>

		@Override
		public Integer run(String... path) throws Exception {
			// TODO Auto-generated method stub
			ArrayList<PictureBody> pics = new ArrayList<PictureBody>();
				
			Bitmap tmpmap = ImageUtils.compImageBySize(path[0]);
			String filename=StringUtils.getFileNameFromPath(path[0]);
			PictureBody pb = new PictureBody(tmpmap, Bitmap.CompressFormat.JPEG , filename);
			pics.add(pb);
			PostData	pdata = new PostData("share", "uploadGrpCoverPhoto", "{\"grpId\":"+grpId+"}", pics);
			
			JSONObject responseJSONObject = null;
			try{
				String json = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
				responseJSONObject = JSON.parseObject(json);
			}catch(Exception ex){
				ex.printStackTrace();
				throw ex;
			}
			if (responseJSONObject.getIntValue("errCode") == 0) {
				try{
					JSONObject  resultsJO= responseJSONObject.getJSONObject("results");
					Group grp=Groups.select(grpId);
					grp.coverPhoto=resultsJO.getString("coverPhoto");
					
				}catch(Exception ex){
					ex.printStackTrace();
					return 1;
				}
				return 0;
			} else {
				throw new Exception(responseJSONObject.getString("errMesg"));
			}
		}
		
	}	
	
}

