package com.android.famcircle;

import java.io.Serializable;

public class StatusZanInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8177546272336857090L;
	String fromUsrId;
	String fromUsrName;
	String statusId;
	
	
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public String getFromUsrId() {
		return fromUsrId;
	}
	public void setFromUsrId(String fromUsrId) {
		this.fromUsrId = fromUsrId;
	}
	public String getFromUsrName() {
		return fromUsrName;
	}
	public void setFromUsrName(String fromUsrName) {
		this.fromUsrName = fromUsrName;
	}
	
}
