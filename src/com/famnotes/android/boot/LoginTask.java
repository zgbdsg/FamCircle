package com.famnotes.android.boot;

import java.util.ArrayList;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.config.Constants;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

public class LoginTask extends BaseAsyncTask<Login, String, Void>{

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
				Group grp=new Group(groupJSON.getInteger("grpId"),  groupJSON.getString("name"));
				lGrp.add(grp);
			}
			Groups.lGroup=lGrp;
			return null;
	}

}
