package com.famnotes.android.boot;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

public class LoginHandler extends BaseAsyncTaskHandler<Login, Void>{

	protected static final String TAG = "LoginHandler";

	public LoginHandler(Login context) {
		super(context);
	}

	@Override
	public boolean onTaskSuccess(Login context, Void result) {
		if(Groups.lGroup.size()>1){ //要求客户选一个
			ArrayList<String> lItem=new ArrayList<String>(Groups.lGroup.size());
			for(Group grp : Groups.lGroup){
				lItem.add(grp.name);
			}
			new AlertDialog.Builder(context).setTitle("您属于多个群，请选一个（日后可以切换）!")
						.setSingleChoiceItems(lItem.toArray(new String[lItem.size()]), 0,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										Groups.selectIdx=which;
										dialog.dismiss();
									}
								})
						.setNegativeButton("取消", null)
						.show();
		}
		
		User.Current.grpId=Groups.selectGrpId();
		User.Current.flag=1;
		try{
			DBUtil.insertUser(User.Current);
		}catch(Exception ex){
			context.DisplayLongToast(ex.toString());
		}
		BaseAsyncTask<Login, Void, Void>  memberTask=new BaseAsyncTask<Login, Void, Void>(){
			@Override
			public Void run(Void... params) throws Exception {
				//取当前群成员
				PostData pdata=new PostData("user", "get_members");
				String json_members = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata); 
				if(!StringUtils.isEmpty(json_members)){
				JSONObject jsonObjectResult = JSON.parseObject(json_members);
					if(jsonObjectResult.getInteger("errCode") != 0) {
						throw new Exception("login fails ! Cannot get members"); 
					}else{
						JSONArray userArray = jsonObjectResult.getJSONArray("results");
						User.Members.clear();
						for(int i=0; i<userArray.size(); i++) {
							JSONObject  userJSON=(JSONObject) userArray.get(i);
							//User user=JSON.toJavaObject(userJSON, User.class);
							//(String userId, String userName, int grpId, String password, int flag)
							User iUser=new User(userJSON.getString("loginId"), userJSON.getString("name"),  User.Current.grpId, null, 0 );
							User.Members.add(iUser);
						}
					}
				}
				return null;
			}

		};
		
		BaseAsyncTaskHandler<Login, Void> memberHandler=new BaseAsyncTaskHandler<Login, Void>(context) {
			
			@Override
			public boolean onTaskSuccess(Login context, Void result) {
				Log.i(TAG, "memberTask success");
				
				//通过“过场”进入主界面
				context.openActivity(LoadingActivity.class);
				context.finish();			
				
				return true;
			}
			
			@Override
			public boolean onTaskFailed(Login context, Exception error) {
				context.DisplayLongToast(error.getMessage());
				
				//通过“过场”进入主界面
				context.openActivity(LoadingActivity.class);
				context.finish();			
				
				return true;
			}
		};
		
		memberTask.connect(memberHandler);
		memberTask.execute();
		
		return true;
	}

	@Override
	public boolean onTaskFailed(Login context, Exception error) {
		context.DisplayLongToast(error.getMessage());
		return true;
	}

}
