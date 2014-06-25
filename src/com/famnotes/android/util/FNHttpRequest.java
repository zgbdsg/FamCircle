package com.famnotes.android.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.android.famcircle.config.Constants;

import android.util.Log;
//import org.apache.commons.codec.digest.DigestUtils;

//var userid ='xxxx'; // $_GET ["userid"];
//var password='pureHtml'; //read from db;
//var timestamp= (new Date()).valueOf();   //$timestamp = $_GET ["timestamp"];
//var nonce=Math.random();  //$
public class FNHttpRequest {
	private String userId; //从客户端数据库获取密码
	private String password;
	public FNHttpRequest(String usage) {
		if("System".equals(usage)) {
			this.userId="xxxx"; //从客户端数据库获取密码
			this.password="pureHtml";
		}
	}
	public FNHttpRequest(String userId, String password, int grpId) {
		super();
		this.userId = userId;
		this.password = password;
		this.grpId=grpId;
	}


	private int grpId=0;
	private long timeStamp;
	private double nonce;
	private HttpResponse localHttpResponse;
	private String signature;
	private String localUrl = "http://"+Constants.Server+"/famnotes/index.php/WS/boot?"; //"http://xbinfo.sinaapp.com/famnotes/index.php/WS/boot?";
//	private String localUrl = "http://xbinfo.sinaapp.com/famnotes/index.php/WS/boot?";
    HttpContext httpContext;
    
    
	protected void initialParameters(){
		timeStamp = System.currentTimeMillis();
		nonce = Math.random();
		//signature = DigestUtils.shaHex(userId + password + timeStamp + nonce);
		signature = StringDigest.sha1(userId + password + timeStamp + nonce);
	}
	
	public String doPost(PostData postData) throws Exception{
		try{
			initialParameters();
			
			localUrl += "userid=" + userId;
			localUrl += "&timestamp=" + timeStamp;
			localUrl += "&nonce=" + nonce;
			localUrl += "&signature=" + signature;
			localUrl += "&grpId=" + grpId;
			localUrl += "&XDEBUG_SESSION_START=ECLIPSE_DBGP&KEY=14014148248011"; //debug PHP跟踪需要
			
			Log.d("FNHttpRequest", localUrl);
			HttpPost httpPost = new HttpPost(localUrl);
			// HTTP Post之multipart/form-data和application/x-www-form-urlencoded
			// 关于HttpPost，有这样两种可Post的数据载体，分别是MultipartEntity和UrlEncodedFormEntity，对这两者的共性和异性做如下解释和备忘：
			//
			// 共性：
			// 1、都属于HTTP的POST范畴
			// 2、实现了接口HttpEntity
			//
			// 异性：
			// 1、Content-Type不同。分别是：Content-Type:multipart/form-data;
			// boundary=***********，
			// Content-Type:application/x-www-form-urlencoded
			// 2、RequestBody不同
			// 　　MultipartEntity是有多个数据段组成，各个数据段有自己的Content-Type和ContentBody
			// 　　UrlEncodedFormEntity只有一个Body，还是使用UrlEncode处理过的内容。如：key1=******&key2=******&key3=******
			
			
			if ((postData.uploadFiles != null && !postData.uploadFiles.isEmpty()) || (postData.pics != null && !postData.pics.isEmpty()) ) {
				MultipartEntity mpEntity = new MultipartEntity(); //支持文件传输
				
				StringBody objIdBody = new StringBody(postData.objId); 
				mpEntity.addPart("objId", objIdBody);
				
				StringBody methodBody = new StringBody(postData.method); 
				mpEntity.addPart("method", methodBody);
				
				if (postData.dataVal != null){
					StringBody jsonBody = new StringBody(postData.dataVal); 
					mpEntity.addPart("params", jsonBody);
				}
				
				for(String fullPath : postData.uploadFiles){
					File file=new File(fullPath);
					FileBody cbFile = new FileBody(file);
					mpEntity.addPart("ups[]", cbFile);   // <input type="file" name="ups[]" /> 对应的
				}
				
				for(PictureBody  picBody : postData.pics){
					mpEntity.addPart("ups[]", picBody);   // <input type="file" name="ups[]" /> 对应的
				}
				
				httpPost.setEntity(mpEntity);
			}else{

				Map<String, String> params = new HashMap<String, String>();
				params.put("objId", postData.objId);
				params.put("method", postData.method);
				if (postData.dataVal != null)
					params.put("params", postData.dataVal);
	
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				if (params != null && !params.isEmpty()) {
					for (Map.Entry<String, String> entry : params.entrySet()) {
						list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
					}
				}
			
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,"utf-8");
				httpPost.setEntity(entity);
			}

			HttpClient httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			localHttpResponse = httpclient.execute(httpPost); // ?DefaultHttpClient 已被淘汰了
			if (localHttpResponse.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(localHttpResponse.getEntity());
				Log.d("FNHttpRequest", "Recv: "+result);
				
				int bIdx=result.indexOf( "{\"errCode\" :");
				if(bIdx==-1){
					bIdx=result.indexOf( "{\"errCode\":");
				}
				if(bIdx==-1){
					return  "{\"errCode\" : -1, \"errMesg\" : \"return msg is null or illegal\"}";
				}
				if(bIdx>0)
					result=result.substring(bIdx);
					
				return result;
			}
//		} catch (ClientProtocolException localClientProtocolException) {
//			localClientProtocolException.printStackTrace();
//			return "网络异常";
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return "网络异常";
//		}
		} catch (Exception e) {
			Log.e("HttpClient", "网络异常"+e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return null;
		
	}
	
	
	
	public int getGrpId() {
		return grpId;
	}
	public void setGrpId(int grpId) {
		this.grpId = grpId;
	}

}
