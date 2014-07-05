package com.android.famcircle;

import java.io.Serializable;

public class StatusReplyInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1432691977072300247L;
	String fromUsrId;
	String fromUsrName;
	String toUsrId;
	String toUsrName;
	String reply;
	
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
	public String getToUsrId() {
		return toUsrId;
	}
	public void setToUsrId(String toUsrId) {
		this.toUsrId = toUsrId;
	}
	public String getToUsrName() {
		return toUsrName;
	}
	public void setToUsrName(String toUsrName) {
		this.toUsrName = toUsrName;
	}
	public String getReply() {
		return reply;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	
}
