package com.android.famcircle.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.api.LOG;
//import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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

//var userid ='xxxx'; // $_GET ["userid"];
//var password='pureHtml'; //read from db;
//var timestamp= (new Date()).valueOf();   //$timestamp = $_GET ["timestamp"];
//var nonce=Math.random();  //$
public class FNHttpRequest {
	private String userId="xxxx"; //从客户端数据库获取密码
	private String password="pureHtml";
	private long timeStamp;
	private double nonce;
	private HttpResponse localHttpResponse;
	private String signature;
	private String localUrl = "http://114.215.180.229/famnotes/index.php/WS/boot?";
    HttpContext httpContext;
    
    
	protected void initialParameters(){
		timeStamp = System.currentTimeMillis();
		nonce = Math.random();
		//signature = DigestUtils.shaHex(userId + password + timeStamp + nonce);
		signature = StringDigest.sha1(userId + password + timeStamp + nonce);
	}
	
	public String doPost(PostData postData){
		try{
			initialParameters();
			
			localUrl += "userid=" + userId;
			localUrl += "&timestamp=" + timeStamp;
			localUrl += "&nonce=" + nonce;
			localUrl += "&signature=" + signature;
			localUrl += "&XDEBUG_SESSION_START=ECLIPSE_DBGP&KEY=14005670183461"; //debug PHP跟踪需要
			
			LOG.d("FNHttpRequest", localUrl);
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
			
			
			if (postData.uploadFiles != null && !postData.uploadFiles.isEmpty()) {
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
				System.out.println(result);
				return result;
			}
		} catch (ClientProtocolException localClientProtocolException) {
			localClientProtocolException.printStackTrace();
			return "网络异常";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return "网络异常";
		}
		return null;
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		PostData pdata=new PostData("recipe", "insertRecipe", "this is a recipe");
		
//		PostData pdata1=new PostData("recipe", "query");
//		String json1=new FNHttpRequest().doPost(pdata1);
		
		ArrayList<String> upfiles=new ArrayList<String>();
		upfiles.add("D:/D_misc/pngtest.png");
		upfiles.add("D:/D_misc/opj_logo.png");
		PostData pdata=new PostData("recipe", "testUploads", " { \"userId\" : \"xxxx\",  \"comment\" : \"Very beatiful!\" }", upfiles );
		String json=new FNHttpRequest().doPost(pdata);
		
		System.out.println(json);
	}

	
}
