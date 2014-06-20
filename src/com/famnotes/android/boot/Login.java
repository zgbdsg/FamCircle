package com.famnotes.android.boot;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.famcircle.R;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.util.StringUtils;

public class Login extends BaseActivity {
	private EditText mUser; // 帐号编辑框
	private EditText mPassword; // 密码编辑框

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        mUser = (EditText)findViewById(R.id.login_user_edit);
        mPassword = (EditText)findViewById(R.id.login_passwd_edit);
        
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
