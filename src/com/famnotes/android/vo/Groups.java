package com.famnotes.android.vo;

import java.util.List;

public class Groups {
	public static List<Group> lGroup;
	public static int selectIdx=0;
	
	public static int selectGrpId(){
		return lGroup.get(selectIdx).grpId;
	}
	public static Group selectGrp(){
		return lGroup.get(selectIdx);
	}
	public static Group select(int grpId){
		for(int i=0; i<lGroup.size(); i++){
			Group grp=lGroup.get(i);
			if(grp.grpId==grpId){
				selectIdx=i;
				return grp;
			}
		}
		return null;
	}
}
