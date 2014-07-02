package com.famnotes.android.boot;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.Constants;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;

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
		PrepLoginHandler prepLoginHandler=new PrepLoginHandler(this);
		prepLoginTask.connect(prepLoginHandler);
		prepLoginTask.execute(loginId);
	}
}

class Way implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public int way;
	public String loginId;
}

class PrepLoginTask extends BaseAsyncTask<PrepLogin, String, Way>{

	@Override
	public Way run(String... params) throws Exception {
//FamNotes可以自动成为FamPhoto的用户/群；但反过来，不行！
/*
 * 输入: ｛userId, password(被加密)｝
 * 输出：[{grpId:111,name：xxx}, {grpId:222,name：yyy}] 即 "results" : $dataJson 中 $dataJson是个对象数组, 
 * 客户端要把用户输入的userId、password(初始密码)与返回的此用户属于那些群，保存到数据库，并进入首群(第0个群)
 */
			JSONObject obj=new JSONObject();
			obj.put("loginId", params[0]);
			String reqJsonMsg=obj.toJSONString();
			String json = null;
			
			try {
				PostData pdata=new PostData("user", "login_prepare", reqJsonMsg );
				json = new FNHttpRequest(Constants.Usage_System).doPost(pdata); //用第0Group登录，意味以后要把,所有用户与群有关的数据汇总到一台专门的登录服务器上.
			} catch (Exception e) {
				throw new Exception("Network exception!"); 
			}
			
			if(json==null){
				throw new Exception("backend exception!"); 
			}
			
			JSONObject jsonResult = JSON.parseObject(json);
			if(jsonResult.getInteger("errCode") != 0) {
				throw new Exception("backend fails ! "+jsonResult.getString("errMesg")); 
			}

			//prepare login success  0-不存在此号码,  1-存在此号码且password不空,  2-存在此号码且password为空
			int state=jsonResult.getIntValue("results");
			
			Way way=new Way(); way.way=state; way.loginId=params[0];
			return way;
	}
}

class PrepLoginHandler extends BaseAsyncTaskHandler<PrepLogin, Way>{

	protected static final String TAG = "PrepLoginHandler";

	public PrepLoginHandler(PrepLogin context) {
		super(context);
	}

	@Override
	public boolean onTaskSuccess(final PrepLogin context, Way way) {
		//prepare login success  0-不存在此号码,  1-存在此号码且password不空,  2-存在此号码且password为空
		Bundle bundle=new Bundle(); bundle.putSerializable("way",  way); 
		switch(way.way) {
			case 0 :
				context.openActivity(Register.class, bundle);
				break;
			case 1 :
			case 2 :
				context.openActivity(Login.class, bundle);
				break;
		}
		
		return true;
	}

	@Override
	public boolean onTaskFailed(PrepLogin context, Exception error) {
		context.DisplayLongToast(error.getMessage());
		return true;
	}


}
