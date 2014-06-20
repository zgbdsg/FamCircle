package com.famnotes.android.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.famnotes.android.config.Constants;

import android.util.Log;

//var userid ='xxxx'; // $_GET ["userid"];
//var password='pureHtml'; //read from db;
//var timestamp= (new Date()).valueOf();   //$timestamp = $_GET ["timestamp"];
//var nonce=Math.random();  //$
public class FNHttpRequest {
	private String userId; //�ӿͻ�����ݿ��ȡ����
	private String password;
	public FNHttpRequest(String usage) {
		if("System".equals(usage)) {
			this.userId="xxxx"; //�ӿͻ�����ݿ��ȡ����
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
	private String localUrl = "http://192.168.0.73/famnotes/index.php/WS/boot?"; //"http://xbinfo.sinaapp.com/famnotes/index.php/WS/boot?";
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
			localUrl += "&XDEBUG_SESSION_START=ECLIPSE_DBGP&KEY=14014148248011"; //debug PHP������Ҫ
			
			Log.d("FNHttpRequest", localUrl);
			HttpPost httpPost = new HttpPost(localUrl);
			// HTTP Post֮multipart/form-data��application/x-www-form-urlencoded
			// ����HttpPost�����������ֿ�Post��������壬�ֱ���MultipartEntity��UrlEncodedFormEntity���������ߵĹ��Ժ����������½��ͺͱ���
			//
			// ���ԣ�
			// 1��������HTTP��POST����
			// 2��ʵ���˽ӿ�HttpEntity
			//
			// ���ԣ�
			// 1��Content-Type��ͬ���ֱ��ǣ�Content-Type:multipart/form-data;
			// boundary=***********��
			// Content-Type:application/x-www-form-urlencoded
			// 2��RequestBody��ͬ
			// ����MultipartEntity���ж����ݶ���ɣ�������ݶ����Լ���Content-Type��ContentBody
			// ����UrlEncodedFormEntityֻ��һ��Body������ʹ��UrlEncode���������ݡ��磺key1=******&key2=******&key3=******
			
			
			if ((postData.uploadFiles != null && !postData.uploadFiles.isEmpty()) || (postData.pics != null && !postData.pics.isEmpty()) ) {
				MultipartEntity mpEntity = new MultipartEntity(); //֧���ļ�����
				
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
					mpEntity.addPart("ups[]", cbFile);   // <input type="file" name="ups[]" /> ��Ӧ��
				}
				
				for(PictureBody  picBody : postData.pics){
					mpEntity.addPart("ups[]", picBody);   // <input type="file" name="ups[]" /> ��Ӧ��
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
			localHttpResponse = httpclient.execute(httpPost); // ?DefaultHttpClient �ѱ���̭��
			if (localHttpResponse.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(localHttpResponse.getEntity());
				//System.out.println(result);
				return result;
			}
//		} catch (ClientProtocolException localClientProtocolException) {
//			localClientProtocolException.printStackTrace();
//			return "�����쳣";
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return "�����쳣";
//		}
		} catch (Exception e) {
			Log.e("HttpClient", "�����쳣"+e.getMessage());
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
