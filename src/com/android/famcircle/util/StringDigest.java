package com.android.famcircle.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringDigest {

	public static String md5(String string) {  
	    byte[] hash;  
	    try {  
	        hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));  
	    } catch (NoSuchAlgorithmException e) {  
	        throw new RuntimeException("Huh, MD5 should be supported?", e);  
	    } catch (UnsupportedEncodingException e) {  
	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);  
	    }  
	  
	    StringBuilder hex = new StringBuilder(hash.length * 2);  
	    for (byte b : hash) {  
	        if ((b & 0xFF) < 0x10) hex.append("0");  
	        hex.append(Integer.toHexString(b & 0xFF));  
	    }  
	    return hex.toString();  
	}  
	
	public static String sha1(String string) {  
	    byte[] hash;  
	    try {  
	        hash = MessageDigest.getInstance("SHA1").digest(string.getBytes("UTF-8"));  
	    } catch (NoSuchAlgorithmException e) {  
	        throw new RuntimeException("Huh, SHA1 should be supported?", e);  
	    } catch (UnsupportedEncodingException e) {  
	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);  
	    }  
	  
	    StringBuilder hex = new StringBuilder(hash.length * 2);  
	    for (byte b : hash) {  
	        if ((b & 0xFF) < 0x10) hex.append("0");  
	        hex.append(Integer.toHexString(b & 0xFF));  
	    }  
	    return hex.toString();  
	} 	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
