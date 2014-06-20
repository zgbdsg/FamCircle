package com.android.famcircle.util;

import java.util.ArrayList;

public class PostData {
	public  String objId, method;
	public  String dataVal;//jsonҵ�����ݰ�
	
	public ArrayList<String> uploadFiles=new ArrayList<String>();
	public ArrayList<PictureBody> pics=new ArrayList<PictureBody>();
	
	public PostData(String objId, String method){
		this.objId = objId;
		this.method = method;
	}
	public PostData(String objId, String method, String dataVal) {
		super();
		this.objId = objId;
		this.method = method;
		this.dataVal = dataVal; //jsonҵ�����ݰ�
	}

	public PostData(String objId, String method,  String dataVal, ArrayList<PictureBody> uploadFiles){
		this.objId = objId;
		this.method = method;
		this.dataVal = dataVal; //jsonҵ�����ݰ�
		
		if(uploadFiles!=null){
			this.pics=uploadFiles;
		}
	}

	public PostData(String objId, String method,  String dataVal, ArrayList<String> uploadFiles){
		this.objId = objId;
		this.method = method;
		this.dataVal = dataVal; //jsonҵ�����ݰ�
		
		if(uploadFiles!=null){
			this.uploadFiles=uploadFiles;
		}
	}
}
