package com.famnotes.android.vo;

import java.util.List;

public class Group implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	
	public Group(){
	}
	
	public Group(int grpId, String name) {
		super();
		this.grpId = grpId;
		this.name = name;
	}
	
	
	public int grpId;
	public String name;
	
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
}
