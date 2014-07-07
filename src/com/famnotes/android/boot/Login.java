package com.famnotes.android.boot;


import java.io.Serializable;
import java.util.ArrayList;

//import android.app.AlertDialog;
//import android.content.DialogInterface;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.Constants;
import com.android.famcircle.ui.MainActivity;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
//import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.ACache;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

public class Login extends BaseActivity {
	private EditText mUser; // 帐号编辑框
	private EditText mPassword; // 密码编辑框

	ACache mCache;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        mCache = ACache.get(this);
		
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



class LoginHandler extends BaseAsyncTaskHandler<Login, Integer>{

	protected static final String TAG = "LoginHandler";

	public LoginHandler(Login context) {
		super(context);
	}

	@Override
	public boolean onTaskSuccess(final Login context, Integer resultCode) {
		//? resultCode==3
		context.openActivity(MainActivity.class);
		context.finish();
		return true;
	}

	@Override
	public boolean onTaskFailed(Login context, Exception error) {
		context.DisplayLongToast(error.getMessage());
		return true;
	}

}
class LoginTask extends BaseAsyncTask<Login, String, Integer>{

	@Override
	public Integer run(String... params) throws Exception {
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
			
			JSONObject jsonResult=null;
			if(Constants.isFamNotes()){
				try {
					PostData pdata=new PostData("user", "login_famnotes", reqJsonMsg );
					json = new FNHttpRequest(Constants.Usage_System).doPost(pdata); //用第0Group登录，意味以后要把,所有用户与群有关的数据汇总到一台专门的登录服务器上.
					if(json==null)
						throw new Exception(" json==null"); 
					 jsonResult = JSON.parseObject(json);
				} catch (Exception e) {
					throw new Exception("login fails ! Network exception!"); 
				}
			}else{
				try {//grpId  register_famphoto(群+用户信息+VerifyCode)
					PostData pdata=new PostData("user", "login_famphoto", reqJsonMsg );
					json = new FNHttpRequest(Constants.Usage_System).doPost(pdata); //用第0Group登录，意味以后要把,所有用户与群有关的数据汇总到一台专门的登录服务器上.
					if(json==null)
						throw new Exception(" json==null"); 
					 jsonResult = JSON.parseObject(json);
				} catch (Exception e) {
					throw new Exception("login fails ! "+e.getMessage()); 
				}
			}

			int errCode=jsonResult.getInteger("errCode");
			
			if(errCode==3){
				//login success
				JSONObject userJSON = jsonResult.getJSONObject("user");
				User.Current=new User();
				User.Current.id=userJSON.getIntValue("id");
				User.Current.loginId=params[0];
				User.Current.password =params[1];
				User.Current.name=userJSON.getString("name");
				User.Current.grpId=userJSON.getIntValue("grpId"); //?还无用， 要选了后面的Groups.lGroup才有效
				User.Current.setAvatar(userJSON.getString("avatar"));
				Groups.lGroup=new ArrayList<Group>();
				mCache.put("User.Current", User.Current);
				mCache.put("Groups.lGroup", (Serializable)Groups.lGroup);
				return 3;
			}
			if( errCode!=0 ) {
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
			mCache.put("User.Current", User.Current);
			mCache.put("Groups.lGroup", (Serializable)Groups.lGroup);
			return 0;
	}

}

}
