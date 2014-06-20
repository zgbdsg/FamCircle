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

	public PostData(String objId, String method,  String dataVal, ArrayList<PictureBody> pics){
		this.objId = objId;
		this.method = method;
		this.dataVal = dataVal; //jsonҵ�����ݰ�
		
		if(pics!=null){
			this.pics=pics;
		}
	}

	public static PostData newPostDataByPics(String objId, String method,  String dataVal, ArrayList<PictureBody> pics){
		PostData  post=new PostData(objId, method);
		post.dataVal = dataVal; //jsonҵ�����ݰ�
		
		if(post.pics!=null){
			post.pics=pics;
		}
		
		return post;
	}
	
	public static PostData newPostDataByFiles(String objId, String method,  String dataVal, ArrayList<String> uploadFiles){
		PostData  post=new PostData(objId, method);
		post.dataVal = dataVal; //jsonҵ�����ݰ�
		
		if(post.uploadFiles!=null){
			post.uploadFiles=uploadFiles;
		}
		
		return post;
	}
}
