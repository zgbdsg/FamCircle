package com.famnotes.android.vo;

import java.util.List;

public class Groups {
	public static List<Group> lGroup;
	public static int selectIdx=0;
	
	public static int selectGrpId(){
		return lGroup.get(selectIdx).grpId;
	}
}
