package com.famnotes.android.boot;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class PrepLogin extends BaseActivity {
	private EditText mUser; // 帐号编辑框

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_prep);
        
        mUser = (EditText)findViewById(R.id.login_user_edit);
        
    }

	public void prep_login_famnotes(View v) {
		String loginId=mUser.getText().toString().trim();
		if(StringUtils.isEmpty(loginId)) {
			Toast.makeText(this, "loginId cannot be empty!", Toast.LENGTH_LONG).show(); 

			mUser.requestFocus();
			imm.showSoftInput(mUser, 0);
			return;
		}
		

		
		PrepLoginTask prepLoginTask=new PrepLoginTask();
		PrepLoginHandler loingHandler=new PrepLoginHandler(this);
		prepLoginTask.connect(loingHandler);
		prepLoginTask.execute(loginId);
	}
}

class PrepLoginTask extends BaseAsyncTask<PrepLogin, String, Void>{

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

class PrepLoginHandler extends BaseAsyncTaskHandler<PrepLogin, Void>{

	protected static final String TAG = "PrepLoginHandler";

	public PrepLoginHandler(PrepLogin context) {
		super(context);
	}

	@Override
	public boolean onTaskSuccess(final PrepLogin context, Void result) {
		final BaseAsyncTask<Login, Void, Void>  memberTask=new BaseAsyncTask<Login, Void, Void>(){
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
							User iUser=new User(userJSON.getIntValue("id"),userJSON.getString("loginId"), userJSON.getString("name"),  User.Current.grpId, null, 0 );
							iUser.setAvatar(userJSON.getString("avatar"));
							User.Members.add(iUser);
						}
					}
				}
				return null;
			}

		};
		
		
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
										
										User.Current.grpId=Groups.selectGrpId();
										User.Current.flag=1;
										try{
											DBUtil.insertUser(User.Current);
										}catch(Exception ex){
											context.DisplayLongToast(ex.toString());
										}
										
									}
								})
						.setNegativeButton("取消", null)
						.show();
			
		} else {
			Groups.selectIdx=0;
			User.Current.grpId=Groups.selectGrpId();
			User.Current.flag=1;
			try{
				DBUtil.insertUser(User.Current);
			}catch(Exception ex){
				context.DisplayLongToast(ex.toString());
			}
		}
		
		
		return true;
	}

	@Override
	public boolean onTaskFailed(PrepLogin context, Exception error) {
		context.DisplayLongToast(error.getMessage());
		return true;
	}


}
