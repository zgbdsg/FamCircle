package com.famnotes.android.boot;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.Constants;
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

public class Login extends BaseActivity {
	private EditText mUser; // 帐号编辑框
	private EditText mPassword; // 密码编辑框

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        //Bundle pBundle=getIntent().getExtras(); 
        //Way way0=(Way) pBundle.getSerializable("way");
        Way way=(Way) getIntent().getSerializableExtra("way");  //savedInstanceState.getSerializable("way");
        mUser = (EditText)findViewById(R.id.login_user_edit);
        mUser.setEnabled(false);
        mUser.setText(way.loginId);
        mPassword = (EditText)findViewById(R.id.login_passwd_edit);
        
        if(way.way==1){
	        TextView passwdHint=(TextView) findViewById(R.id.login_passwd_hint);
	        passwdHint.setText("");
        }
        
        mPassword.requestFocus();
		imm.showSoftInput(mPassword, 0);
    }

	public void login_famnotes(View v) {
		String loginId=mUser.getText().toString().trim();
		if(StringUtils.isEmpty(loginId)) {
			Toast.makeText(this, "loginId cannot be empty!", Toast.LENGTH_LONG).show(); 

			mUser.requestFocus();
			imm.showSoftInput(mUser, 0);
			return;
		}
		
		String password=mPassword.getText().toString().trim();
		if(StringUtils.isEmpty(password)){
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_LONG).show(); 

			mPassword.requestFocus();
			imm.showSoftInput(mPassword, 0);
			return;
		}

		
		LoginTask loginTask=new LoginTask();
		LoginHandler loingHandler=new LoginHandler(this);
		connect(loginTask, loingHandler);
		loginTask.execute(loginId, password);
		
			
//			new AlertDialog.Builder(Login.this)
//					.setIcon(getResources().getDrawable(R.drawable.login_error_icon))
//					.setTitle("登录失败").setMessage("帐号或者密码不正确，\n请检查后重新输入！")
//					.setPositiveButton("确认", new OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					})
//					.create().show();
	}
    
    public void login_back(View v) {     //标题栏 返回按钮
      	this.finish();
    }  
    
    public void login_pw(View v) {     //忘记密码按钮
//    	//发送短信验证吗，让其重置密码，并自动登录进系统
//    	Intent intent = new Intent(Intent.ACTION_VIEW, uri); 
//    	startActivity(intent);
      }  
}


class LoginHandler extends BaseAsyncTaskHandler<Login, Void>{

	protected static final String TAG = "LoginHandler";

	public LoginHandler(Login context) {
		super(context);
	}

	@Override
	public boolean onTaskSuccess(final Login context, Void result) {
		
//		final BaseAsyncTask<Login, Void, Void>  memberTask=new BaseAsyncTask<Login, Void, Void>(){
//			@Override
//			public Void run(Void... params) throws Exception {
//				//取当前群成员
//				PostData pdata=new PostData("user", "get_members");
//				String json_members = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata); 
//				if(!StringUtils.isEmpty(json_members)){
//				JSONObject jsonObjectResult = JSON.parseObject(json_members);
//					if(jsonObjectResult.getInteger("errCode") != 0) {
//						throw new Exception("login fails ! Cannot get members"); 
//					}else{
//						JSONArray userArray = jsonObjectResult.getJSONArray("results");
//						User.Members.clear();
//						for(int i=0; i<userArray.size(); i++) {
//							JSONObject  userJSON=(JSONObject) userArray.get(i);
//							//User user=JSON.toJavaObject(userJSON, User.class);
//							//(String userId, String userName, int grpId, String password, int flag)
//							User iUser=new User(userJSON.getIntValue("id"),userJSON.getString("loginId"), userJSON.getString("name"),  User.Current.grpId, null, 0 );
//							iUser.setAvatar(userJSON.getString("avatar"));
//							User.Members.add(iUser);
//						}
//					}
//				}
//				return null;
//			}
//
//		};
//		
//		final BaseAsyncTaskHandler<Login, Void> memberHandler=new BaseAsyncTaskHandler<Login, Void>(context) {
//			
//			@Override
//			public boolean onTaskSuccess(Login context, Void result) {
//				Log.i(TAG, "memberTask success");
//				
//				//通过“过场”进入主界面
//				context.openActivity(LoadingActivity.class);
//				context.finish();			
//				
//				return true;
//			}
//			
//			@Override
//			public boolean onTaskFailed(Login context, Exception error) {
//				context.DisplayLongToast(error.getMessage());
//				
//				//通过“过场”进入主界面
//				context.openActivity(LoadingActivity.class);
//				//context.finish();			
//				
//				return true;
//			}
//		};
//		
//		if(Groups.lGroup.size()>1){ //要求客户选一个
//			ArrayList<String> lItem=new ArrayList<String>(Groups.lGroup.size());
//			for(Group grp : Groups.lGroup){
//				lItem.add(grp.name);
//			}
//			
//			new AlertDialog.Builder(context).setTitle("您属于多个群，请选一个（日后可以切换）!")
//						.setSingleChoiceItems(lItem.toArray(new String[lItem.size()]), 0,
//								new DialogInterface.OnClickListener() {
//									public void onClick(DialogInterface dialog, int which) {
//										Groups.selectIdx=which;
//										dialog.dismiss();
//										
//										User.Current.grpId=Groups.selectGrpId();
//										User.Current.flag=1;
//										try{
//											DBUtil.insertUser(User.Current);
//										}catch(Exception ex){
//											context.DisplayLongToast(ex.toString());
//										}
//										
//										
//										memberTask.connect(memberHandler);
//										memberTask.execute();
//									}
//								})
//						.setNegativeButton("取消", null)
//						.show();
//			
//		} else {
//			Groups.selectIdx=0;
//			User.Current.grpId=Groups.selectGrpId();
//			User.Current.flag=1;
//			try{
//				DBUtil.insertUser(User.Current);
//			}catch(Exception ex){
//				context.DisplayLongToast(ex.toString());
//			}
//			memberTask.connect(memberHandler);
//			memberTask.execute();
//		}
		
		context.openActivity(LoadingActivity.class);
		context.finish();
		return true;
	}

	@Override
	public boolean onTaskFailed(Login context, Exception error) {
		context.DisplayLongToast(error.getMessage());
		return true;
	}

}
class LoginTask extends BaseAsyncTask<Login, String, Void>{

	@Override
	public Void run(String... params) throws Exception {
//FamNotes可以自动成为FamPhoto的用户/群；但反过来，不行！
/*
 * 输入: ｛userId, password(被加密)｝
 * 输出：[{grpId:111,name：xxx}, {grpId:222,name：yyy}] 即 "results" : $dataJson 中 $dataJson是个对象数组, 
 * 客户端要把用户输入的userId、password(初始密码)与返回的此用户属于那些群，保存到数据库，并进入首群(第0个群)
 */
			JSONObject obj=new JSONObject();
			obj.put("loginId", params[0]);
			obj.put("password", params[1]);
			String reqJsonMsg=obj.toJSONString();
			String json = null;
			
			if(Constants.isFamNotes()){
				try {
					PostData pdata=new PostData("user", "login_famnotes", reqJsonMsg );
					json = new FNHttpRequest(Constants.Usage_System).doPost(pdata); //用第0Group登录，意味以后要把,所有用户与群有关的数据汇总到一台专门的登录服务器上.
				} catch (Exception e) {
					throw new Exception("login fails ! Network exception!"); 
				}
			}else{
				try {//grpId  register_famphoto(群+用户信息+VerifyCode)
					PostData pdata=new PostData("user", "login_famphoto", reqJsonMsg );
					json = new FNHttpRequest(Constants.Usage_System).doPost(pdata); //用第0Group登录，意味以后要把,所有用户与群有关的数据汇总到一台专门的登录服务器上.
				} catch (Exception e) {
					throw new Exception("login fails ! Network exception!"); 
				}
			}
			
			if(json==null){
				throw new Exception("login fails ! json==null!"); 
			}
			
			JSONObject jsonResult = JSON.parseObject(json);
			if(jsonResult.getInteger("errCode") != 0) {
				throw new Exception("login fails ! "+jsonResult.getString("errMesg")); 
			}

			//login success
			JSONObject userJSON = jsonResult.getJSONObject("user");
			//User.Current=new User(userJSON.getIntValue("id"), params[0], userJSON.getString("name"),  ??? , params[1], 0 );
			User.Current=new User();
			User.Current.id=userJSON.getIntValue("id");
			User.Current.loginId=params[0];
			User.Current.password =params[1];
			User.Current.name=userJSON.getString("name");
			User.Current.grpId=userJSON.getIntValue("grpId"); //?还无用， 要选了后面的Groups.lGroup才有效
			User.Current.setAvatar(userJSON.getString("avatar"));
			
			
			
			JSONArray groupArray = jsonResult.getJSONArray("results");
			ArrayList<Group> lGrp=new ArrayList<Group>();
			for(int i=0; i<groupArray.size(); i++) {
				JSONObject  groupJSON=(JSONObject) groupArray.get(i);
				Group grp=new Group(groupJSON.getInteger("grpId"),  groupJSON.getString("name"), groupJSON.getString("coverPhoto"));
				lGrp.add(grp);
			}
			Groups.lGroup=lGrp;
			return null;
	}

}
