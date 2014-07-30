package com.famnotes.android.vo;

public class Group implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	
	public Group(){
		this.notificationNum = 1;
	}
	
	public Group(int grpId, String name, String coverPhoto) {
		super();
		this.grpId = grpId;
		this.name = name;
		this.coverPhoto=coverPhoto;
		this.notificationNum = 1;
	}
	
	
	public int grpId;
	public int notificationNum;
	public String name, coverPhoto;
	
	public String getCoverPhoto() {
		return coverPhoto;
	}

	public void setCoverPhoto(String coverPhoto) {
		this.coverPhoto = coverPhoto;
	}

	public int getGrpId() {
		return grpId;
	}
	public void setGrpId(int grpId) {
		this.grpId = grpId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getNotificationNum() {
		return notificationNum;
	}

	public void setNotificationNum(int notificationNum) {
		this.notificationNum = notificationNum;
	}
	
}
