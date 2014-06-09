package com.android.famcircle;

import java.io.Serializable;

public class StatusOfPersonListInfo implements Serializable{

	/**
	 * @author zgb
	 */
	
	private static final long serialVersionUID = 74904906746995617L;
	private String statusTime;
	private String[] statusTexts;
	private String[] statusPic;
	private String smallPicPath;
	private String bigPicpath;
	
	public String getStatusTime() {
		return statusTime;
	}
	public void setStatusTime(String statusTime) {
		this.statusTime = statusTime;
	}
	public String[] getStatusTexts() {
		return statusTexts;
	}
	public void setStatusTexts(String[] statusTexts) {
		this.statusTexts = statusTexts;
	}
	public String[] getStatusPic() {
		return statusPic;
	}
	public void setStatusPic(String[] statusPic) {
		this.statusPic = statusPic;
	}
	public String getSmallPicPath() {
		return smallPicPath;
	}
	public void setSmallPicPath(String smallPicPath) {
		this.smallPicPath = smallPicPath;
	}
	public String getBigPicpath() {
		return bigPicpath;
	}
	public void setBigPicpath(String bigPicpath) {
		this.bigPicpath = bigPicpath;
	}
	
}
