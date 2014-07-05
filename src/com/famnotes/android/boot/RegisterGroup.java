package com.famnotes.android.boot;


import java.io.Serializable;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.Constants;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.util.ACache;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

public class RegisterGroup extends BaseActivity {
	ACache mCache;
	String userId;
	//private EditText txtLoginCellno; // 帐号编辑框
	//private EditText txtLoginEmail;
	//private EditText txtPassword, txtPassword2; // 密码编辑框
	//private EditText txtVerifyCode;
	private EditText txtGroupName; //群名称

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        
        mCache=ACache.get(this);
        if(User.Current==null)
			User.Current=mCache.getAsObject("User.Current");
        userId=User.Current.loginId;
        
        txtGroupName= (EditText)findViewById(R.id.login_grp_name);
    }

    
	public void register_famnotes(View v) {
		//显示软键盘  imm.showSoftInputFromInputMethod(tv.getWindowToken(), 0);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        
		String groupName=txtGroupName.getText().toString().trim();
		if(StringUtils.isEmpty(groupName)) {
			DisplayLongToast("GroupName cannot be empty!");

			txtGroupName.requestFocus();
			imm.showSoftInput(txtGroupName, 0);
			return;
		}
		
//		String password=txtPassword.getText().toString().trim();
//		String password2=txtPassword2.getText().toString().trim();
//		if(StringUtils.isEmpty(password)){
//			DisplayLongToast("密码不能为空");
//
//			txtPassword.requestFocus();
//			imm.showSoftInput(txtPassword, 0);
//			return;
//		}
//		if(!password.equals(password2)) {
//			DisplayLongToast("两次输入的密码不同");
//			return;
//		}
//		
//		String verifyCode=txtVerifyCode.getText().toString().trim();
//		if(StringUtils.isEmpty(groupName)) {
//			DisplayLongToast("VerifyCode cannot be empty!");
//
//			txtVerifyCode.requestFocus();
//			imm.showSoftInput(txtVerifyCode, 0);
//			return;
//		}
		
		//FamNotes可以自动成为FamPhoto的用户/群；但反过来，不行！
		/*
		 * 输入: ｛手机号即userId, userName, GroupName, VerifyCode, password｝ //?Email 暂时不要
		 * 输出： grpId
		 * 说明：后台建用户记录、群记录、群用户关系记录
		 */
		JSONObject obj=new JSONObject();
		obj.put("userId", userId);
		obj.put("groupName", groupName);
		String reqJsonMsg=obj.toJSONString();

		RegisterHandler handler=new RegisterHandler(this);
		RegisterTask task=new RegisterTask();
		task.connect(handler);
		task.execute(reqJsonMsg, userId,  groupName);
		
	}

	public void login_back(View v) { // 标题栏 返回按钮
		this.finish();
	}

	class RegisterHandler extends BaseAsyncTaskHandler<RegisterGroup, Integer>{
	
		public RegisterHandler(RegisterGroup context) {
			super(context);
		}
	
		@Override
		public boolean onTaskFailed(RegisterGroup context, Exception ex) {
			// TODO Auto-generated method stub
			context.DisplayLongToast(ex.toString());
			return true;
		}
	
		@Override
		public boolean onTaskSuccess(RegisterGroup arg0, Integer grpId) {
			// TODO Auto-generated method stub
			Bundle bundle=new Bundle(); bundle.putInt("GroupId",  grpId); 
			getContext().openActivity(FamilyMemberSetting.class, bundle);
			getContext().finish();//?
			return true;
		}
		
	}
	class RegisterTask  extends BaseAsyncTask<RegisterGroup, String, Integer>{
	
		@Override
		public Integer run(String... reqJsonMsg) throws Exception {
			String json = null;
			
			PostData pdata=new PostData("user", "add_famphoto_group",  reqJsonMsg[0] );
			json = new FNHttpRequest(Constants.Usage_System).doPost(pdata);
			
			if(json==null){
				throw new Exception("Add_famphoto_group fails !"); 
			}
			
	
			JSONObject jsonResult = JSON.parseObject(json);
			if(jsonResult.getInteger("errCode") != 0) {
				throw new Exception("Add_famphoto_group  fails ! "+jsonResult.getString("errMesg")); 
			}
			
			int grpId=jsonResult.getInteger("results"); 
			if(grpId<=0){
				throw new Exception("Add_famphoto_group fails ! "+jsonResult.getString("errMesg"));
			}else{
				Group grp=new Group(grpId, reqJsonMsg[2], grpId+".png");
				Groups.lGroup.add(grp);
				mCache.put("Groups.selectIdx", Groups.selectIdx);
				mCache.put("Groups.lGroup", (Serializable)Groups.lGroup);
			}
		
			return grpId;
		}
	}

}