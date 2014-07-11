package com.famnotes.android.vo;

import java.util.ArrayList;
import java.util.List;

//String sql = "CREATE TABLE IF NOT EXISTS `fn_user` ("  +
//		  "`id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,"  +
//		  "`grpId` integer,"  +
//		  "`loginId` varchar(32),"  +
//		  "`name` varchar(255),"  +
//		  "`email` varchar(255),"  +
//		  "`cellno` varchar(255),"  +
//		  "`password` varchar(255),"  +
//		  "`type` integer,"  +
//		  "`flag` integer,"  +
//		  "`role` integer"  +
//-- id 代理主键，自动+1
//-- loginId, 用户号， 即userID
//-- grpId 群id --
//-- name 显示的称呼， 如：小妹
//-- type 类型， 1 一家购物之主； 2 普通家庭成员  3 超市收单者  4 物流公司   8 广告投递人 
//-- flag 0 他人（家庭成员）， 1 本人
//-- role 1-father 2-mother 3-brother  4-sister  5-grandfather  6-grandmother
public class User implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public static User Current;
	public static List<User> Members=new ArrayList<User>();
	
	public static User getUserById(int usrId){
		if(usrId==Current.id)
			return Current;
		for(User usr :  Members){
			if(usr.id==usrId)
				return usr;
		}
		return null;
	}
	
	public User(){
	}

	public User(int id, String loginId, String userName, int grpId, String password, int flag) {
		this.id=id;
		this.loginId=loginId;
		this.name=userName;
		this.grpId=grpId;
		this.password=password;
		this.flag=flag;
	}

	public int id=0, type=0, flag=0, role=0;
	public int grpId=0;
	public String loginId, name, password,avatar;
	
	
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		this.role = role;
	}
	public int getGrpId() {
		return grpId;
	}
	public void setGrpId(int grpId) {
		this.grpId = grpId;
	}
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	} 
	
}

