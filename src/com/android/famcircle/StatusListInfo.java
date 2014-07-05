package com.android.famcircle;

import java.io.Serializable;

public class StatusListInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 273031318471688450L;
	
	String statusId;
	String usrId;
	String name;
	String resrc_type;
	String creatTime;
	String status;
	String avatar;
	String smallPicPath;
	String bigPicPath;
	String[] picArray;
	String[] bigPicArray;
	
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public String getUsrId() {
		return usrId;
	}
	public void setUsrId(String usrId) {
		this.usrId = usrId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getResrc_type() {
		return resrc_type;
	}
	public void setResrc_type(String resrc_type) {
		this.resrc_type = resrc_type;
	}
	public String getCreatTime() {
		return creatTime;
	}
	public void setCreatTime(String creatTime) {
		this.creatTime = creatTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String[] getPicArray() {
		return picArray;
	}
	public void setPicArray(String[] picArray) {
		this.picArray = picArray;
	}
	public String[] getBigPicArray() {
		return bigPicArray;
	}
	public void setBigPicArray(String[] bigPicArray) {
		this.bigPicArray = bigPicArray;
	}
	
	public String getSmallPicPath() {
		return smallPicPath;
	}
	public void setSmallPicPath(String smallPicPath) {
		this.smallPicPath = smallPicPath;
	}
	public String getBigPicPath() {
		return bigPicPath;
	}
	public void setBigPicPath(String bigPicPath) {
		this.bigPicPath = bigPicPath;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+" "+statusId+" "+status;
	}
	
	
}
