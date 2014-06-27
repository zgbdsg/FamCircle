package com.famnotes.android.boot;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.famnotes.android.base.AppManager;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

public class Appstart extends BaseActivity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.appstart);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);   //全屏显示
		
//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//			.detectDiskReads().detectDiskWrites().detectNetwork()
//			.penaltyLog().build());
		
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//			.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
//			.build());
		
		AppstartTask task=new AppstartTask();
		AppstartHandler handler=new AppstartHandler(this);
		task.connect(handler);
		task.execute();
    }
	

}

class AppstartHandler extends BaseAsyncTaskHandler<Appstart, Integer>{

	public AppstartHandler(Appstart context) {
		super(context);
	}

	@Override
	public boolean onTaskFailed(Appstart arg0, Exception arg1) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(getContext())
		.setIcon(getContext().getResources().getDrawable(R.drawable.login_error_icon))
		.setTitle("Sorry").setMessage(arg1.getMessage()+"！ App will exit.")
			.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				AppManager.getInstance().AppExit(getContext());//System.exit(0);
			}
		})
		.create().show();
		return true;
	}

	@Override
	public boolean onTaskSuccess(Appstart arg0, Integer rCode) {
		// TODO Auto-generated method stub
		switch(rCode){
			case 0: //有库但无此人
			case 1: //有此人, json包错				
				getContext().openActivity(Welcome.class);
				break;
			
			case 2: //有此人, json包错				
				//不通过“过场”, 直接进入主界面				
//				getContext().openActivity(HomeActivity.class);
				
				//通过“过场”进入主界面
				getContext().openActivity(LoadingActivity.class);
				break;
		}
		arg0.finish();
		return true;
	}
	
	
	
}
class AppstartTask  extends BaseAsyncTask<Appstart, Void, Integer>{

	@Override
	public Integer run(Void... arg0) throws Exception {
		int status=DBUtil.detectDatabase();  //boolean dbExist=DBUtil.detectDatabase();
		if(status!=1) {
			boolean success=DBUtil.createDatabase(status);
			if(!success)
				throw new Exception("fail createDatabase");
			
			return 0;
		}

		User user=DBUtil.getCurrentUser();
		if(user==null){
			return 0;
		}else{
			User.Current=user;
			//取当前群成员
			PostData pdata=new PostData("user", "get_members");
			String json_members = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata); 
			
			if(StringUtils.isEmpty(json_members))
				return 1;
			
			JSONObject jsonObjectResult = JSON.parseObject(json_members);
			if(jsonObjectResult.getInteger("errCode") != 0) {
				return 1;	
			}else{
				JSONArray userArray = jsonObjectResult.getJSONArray("results");
				User.Members.clear();
				for(int i=0; i<userArray.size(); i++) {
					JSONObject  userJSON=(JSONObject) userArray.get(i);
					//User iUser=JSON.toJavaObject(userJSON, User.class);
					//(String userId, String userName, int grpId, String password, int flag)
					User iUser=new User(userJSON.getIntValue("id"), userJSON.getString("loginId"), userJSON.getString("name"),  User.Current.grpId, null, 0 );
					iUser.setAvatar(userJSON.getString("avatar"));
					User.Members.add(iUser);
				}
			}
			
			pdata=new PostData("user", "get_groups");
			String json_groups = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata); 
			JSONObject  grpJO = JSON.parseObject(json_groups);
			if(grpJO.getInteger("errCode") != 0) {
				throw new Exception("get_members fail"+grpJO.getInteger("errMesg"));
			}else{
				JSONArray groupArray = grpJO.getJSONArray("results");
				ArrayList<Group> lGrp=new ArrayList<Group>();
				for(int i=0; i<groupArray.size(); i++) {
					JSONObject  groupJSON=(JSONObject) groupArray.get(i);
					Group grp=new Group(groupJSON.getInteger("grpId"),  groupJSON.getString("name"), groupJSON.getString("coverPhoto"));
					lGrp.add(grp);
				}
				Groups.lGroup=lGrp;
				Groups.select(User.Current.grpId);
				return 2;
			}
			
		}
	}
	   
}