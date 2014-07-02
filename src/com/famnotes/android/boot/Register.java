package com.famnotes.android.boot;

import java.util.ArrayList;

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
import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

public class Register extends BaseActivity {
	String userId;
	//private EditText txtLoginCellno; // 帐号编辑框
	private EditText txtLoginEmail;
	private EditText txtPassword, txtPassword2; // 密码编辑框
	private EditText txtVerifyCode;
	private EditText txtGroupName; //群名称

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        Way way=(Way) getIntent().getSerializableExtra("way");  //savedInstanceState.getSerializable("way");
        userId=way.loginId;
        //txtLoginCellno = (EditText)findViewById(R.id.login_cellno);
        txtLoginEmail= (EditText)findViewById(R.id.login_email);
        
        txtPassword =   (EditText)findViewById(R.id.login_passwd_edit);
        txtPassword2 = (EditText)findViewById(R.id.login_passwd2_edit);
        
        txtVerifyCode= (EditText)findViewById(R.id.login_VerifyCode);
        txtGroupName= (EditText)findViewById(R.id.login_grp_name);
		//txtLoginCellno.setFocusable(true);
		//txtLoginCellno.setFocusableInTouchMode(true);  
		//txtPassword.setFocusable(true);
		//txtPassword.setFocusableInTouchMode(true);        
    }

    public void register_getVerifyCode(View v) {
    	DisplayLongToast("测试阶段，您就输入54321");
    }
    
    
	public void register_famnotes(View v) {
		//显示软键盘  imm.showSoftInputFromInputMethod(tv.getWindowToken(), 0);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        
//		String userId=txtLoginCellno.getText().toString().trim();
//		if(StringUtils.isEmpty(userId)) {
//			DisplayLongToast("Cell No. cannot be empty!");
//
//			txtLoginCellno.requestFocus();
//			imm.showSoftInput(txtLoginCellno, 0);
//			return;
//		}
		
		String userName=txtLoginEmail.getText().toString().trim();
		if(StringUtils.isEmpty(userName)) {
			DisplayLongToast("UserName cannot be empty!");

			txtLoginEmail.requestFocus();
			imm.showSoftInput(txtLoginEmail, 0);
			return;
		}
		
		String groupName=txtGroupName.getText().toString().trim();
		if(StringUtils.isEmpty(groupName)) {
			DisplayLongToast("GroupName cannot be empty!");

			txtGroupName.requestFocus();
			imm.showSoftInput(txtGroupName, 0);
			return;
		}
		
		String password=txtPassword.getText().toString().trim();
		String password2=txtPassword2.getText().toString().trim();
		if(StringUtils.isEmpty(password)){
			DisplayLongToast("密码不能为空");

			txtPassword.requestFocus();
			imm.showSoftInput(txtPassword, 0);
			return;
		}
		if(!password.equals(password2)) {
			DisplayLongToast("两次输入的密码不同");
			return;
		}
		
		String verifyCode=txtVerifyCode.getText().toString().trim();
		if(StringUtils.isEmpty(groupName)) {
			DisplayLongToast("VerifyCode cannot be empty!");

			txtVerifyCode.requestFocus();
			imm.showSoftInput(txtVerifyCode, 0);
			return;
		}
		
		//FamNotes可以自动成为FamPhoto的用户/群；但反过来，不行！
		/*
		 * 输入: ｛手机号即userId, userName, GroupName, VerifyCode, password｝ //?Email 暂时不要
		 * 输出： grpId
		 * 说明：后台建用户记录、群记录、群用户关系记录
		 */
		JSONObject obj=new JSONObject();
		obj.put("userId", userId);
		obj.put("userName", userName);
		obj.put("groupName", groupName);
		obj.put("verifyCode", verifyCode);
		obj.put("password", password);
		String reqJsonMsg=obj.toJSONString();

		RegisterHandler handler=new RegisterHandler(this);
		RegisterTask task=new RegisterTask();
		task.connect(handler);
		task.execute(reqJsonMsg, userId, userName, password, groupName);
		
	}

	public void login_back(View v) { // 标题栏 返回按钮
		this.finish();
	}

	public void login_forgot_password(View v) { // 忘记密码按钮
	// Uri uri = Uri.parse("http://3g.qq.com");
	// Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	// startActivity(intent);
	}
}

class RegisterHandler extends BaseAsyncTaskHandler<Register, Integer>{

	public RegisterHandler(Register context) {
		super(context);
	}

	@Override
	public boolean onTaskFailed(Register context, Exception ex) {
		// TODO Auto-generated method stub
		context.DisplayLongToast(ex.toString());
		return true;
	}

	@Override
	public boolean onTaskSuccess(Register arg0, Integer rCode) {
		// TODO Auto-generated method stub
		switch(rCode){
			case 1: 			
				getContext().openActivity(FamilyMemberSetting.class);
				getContext().finish();
				break;
				
			case 2: 

				//通过“过场”进入主界面
				getContext().openActivity(LoadingActivity.class);
				break;
				
		}
		return true;
	}
	
}
class RegisterTask  extends BaseAsyncTask<Register, String, Integer>{

	@Override
	public Integer run(String... reqJsonMsg) throws Exception {
		String json = null;
		
		if(Constants.isFamNotes()){
			//grpId  register_famnotes(群+用户信息+VerifyCode)
			PostData pdata=new PostData("user", "register_famnotes",  reqJsonMsg[0] );
			json = new FNHttpRequest(Constants.Usage_System).doPost(pdata);
		}else{
			//grpId  register_famphoto(群+用户信息+VerifyCode)
			PostData pdata=new PostData("user", "register_famphoto",  reqJsonMsg[0] );
			json = new FNHttpRequest(Constants.Usage_System).doPost(pdata);
		}
		
		if(json==null){
			throw new Exception("Register fails !"); 
		}
		

		JSONObject jsonResult = JSON.parseObject(json);
		if(jsonResult.getInteger("errCode") != 0) {
			throw new Exception("Register fails ! "+jsonResult.getString("errMesg")); 
		}
		
		int userId=jsonResult.getInteger("id"); 
		int grpId=jsonResult.getInteger("results"); 
		if(grpId<=0){
			throw new Exception("Register fails ! "+jsonResult.getString("errMesg"));
		}else{
			ArrayList<Group> lGrp=new ArrayList<Group>();
			Group grp=new Group(grpId, reqJsonMsg[4], grpId+".png");
			lGrp.add(grp);
			Groups.lGroup=lGrp; Groups.selectIdx=0;
		}
	
		
		String loginId=reqJsonMsg[1], userName=reqJsonMsg[2], password=reqJsonMsg[3];
		User user=new User(userId, loginId, userName, grpId, password, 1); 
		user.setAvatar(userId+".png");
		long rowid=DBUtil.insertUser(user);
		if(rowid<=0){
			throw new Exception("Register fails ! Cannot insertUser to local db.");
		}
		User.Current=user;
		return 1;
	}
}