package com.famnotes.android.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.TelephonyManager;

/**
 * 字符串操作工具包
 */
public class StringUtils {

	private final static Pattern emailer = Pattern
			.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
	private final static Pattern phone = Pattern
			.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");

	private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	
	/**
	 * 中文转unicode
	 */

	public static String gbEncoding(final String gbString) {
		char[] utfBytes = gbString.toCharArray();
		StringBuffer unicodeBytes = new StringBuffer();
		for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {
			StringBuffer hexB = new StringBuffer(Integer.toHexString(utfBytes[byteIndex]));
			if (hexB.length() <= 2) {
				hexB.insert(0, "00");
				//hexB = "00" + hexB;
			}
			unicodeBytes.append("\\u");
			unicodeBytes.append(hexB);
		}
		System.out.println("unicodeBytes is: " + unicodeBytes);
		return unicodeBytes.toString();
	}
	
	/**
	 * 返回当前系统时间
	 */
	public static String getDataTime(String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(new Date());
	}

	/**
	 * 返回当前系统时间
	 */
	public static String getDataTime() {
		return getDataTime("HH:mm");
	}

	/**
	 * 
	 * 毫秒值转换为mm:ss
	 * 
	 * 
	 * 
	 * @author kymjs
	 * 
	 * @param ms
	 */

	public static String timeFormat(int ms) {

		StringBuilder time = new StringBuilder();

		time.delete(0, time.length());

		ms /= 0;

		int s = ms % 60;

		int min = ms / 60;

		if (min < 10) {

			time.append(0);

		}

		time.append(min).append(":");

		if (s < 10) {

			time.append(0);

		}

		time.append(s);

		return time.toString();

	}

	/**
	 * 
	 * 将字符串转位日期类型
	 * 
	 * 
	 * 
	 * @return
	 */

	public static Date toDate(String sdate) {

		try {

			return dateFormater.get().parse(sdate);

		} catch (ParseException e) {

			return null;

		}

	}

	/**
	 * 
	 * 判断给定字符串时间是否为今日
	 * 
	 * 
	 * 
	 * @param sdate
	 * 
	 * @return boolean
	 */

	public static boolean isToday(String sdate) {

		boolean b = false;

		Date time = toDate(sdate);

		Date today = new Date();

		if (time != null) {

			String nowDate = dateFormater2.get().format(today);

			String timeDate = dateFormater2.get().format(time);

			if (nowDate.equals(timeDate)) {

				b = true;

			}

		}

		return b;

	}

	/**
	 * 
	 * 判断给定字符串是否空白串 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 */

	public static boolean isEmpty(String input) {

		if (input == null || "".equals(input))

			return true;

		for (int i = 0; i < input.length(); i++) {

			char c = input.charAt(i);

			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {

				return false;

			}

		}

		return true;

	}

	/**
	 * 
	 * 判断是不是一个合法的电子邮件地址
	 */

	public static boolean isEmail(String email) {

		if (email == null || email.trim().length() == 0)

			return false;

		return emailer.matcher(email).matches();

	}

	/**
	 * 
	 * 判断是不是一个合法的手机号码
	 */

	public static boolean isPhone(String phoneNum) {

		if (phoneNum == null || phoneNum.trim().length() == 0)

			return false;

		return phone.matcher(phoneNum).matches();

	}

	/**
	 * 
	 * 字符串转整数
	 * 
	 * 
	 * 
	 * @param str
	 * 
	 * @param defValue
	 * 
	 * @return
	 */

	public static int toInt(String str, int defValue) {

		try {

			return Integer.parseInt(str);

		} catch (Exception e) {

		}

		return defValue;

	}

	/**
	 * 
	 * 对象转整
	 * 
	 * 
	 * 
	 * @param obj
	 * 
	 * @return 转换异常返回 0
	 */

	public static int toInt(Object obj) {

		if (obj == null)

			return 0;

		return toInt(obj.toString(), 0);

	}

	/**
	 * 
	 * String转long
	 * 
	 * 
	 * 
	 * @param obj
	 * 
	 * @return 转换异常返回 0
	 */

	public static long toLong(String obj) {

		try {

			return Long.parseLong(obj);

		} catch (Exception e) {

		}

		return 0;

	}

	/**
	 * 
	 * String转double
	 * 
	 * 
	 * 
	 * @param obj
	 * 
	 * @return 转换异常返回 0
	 */

	public static double toDouble(String obj) {

		try {

			return Double.parseDouble(obj);

		} catch (Exception e) {

		}

		return 0D;

	}

	/**
	 * 
	 * 字符串转布尔
	 * 
	 * 
	 * 
	 * @param b
	 * 
	 * @return 转换异常返回 false
	 */

	public static boolean toBool(String b) {

		try {

			return Boolean.parseBoolean(b);

		} catch (Exception e) {

		}

		return false;

	}

	/**
	 * 
	 * 判断一个字符串是不是数字
	 */

	public static boolean isNumber(String str) {

		try {

			Integer.parseInt(str);

		} catch (Exception e) {

			return false;

		}

		return true;

	}

	/**
	 * 
	 * 获取AppKey
	 */

	public static String getMetaValue(Context context, String metaKey) {

		Bundle metaData = null;

		String apiKey = null;

		if (context == null || metaKey == null) {

			return null;

		}

		try {

			ApplicationInfo ai = context.getPackageManager()

			.getApplicationInfo(context.getPackageName(),

			PackageManager.GET_META_DATA);

			if (null != ai) {

				metaData = ai.metaData;

			}

			if (null != metaData) {

				apiKey = metaData.getString(metaKey);

			}

		} catch (NameNotFoundException e) {

		}

		return apiKey;

	}

	/**
	 * 
	 * 获取手机IMEI码
	 */

	public static String getPhoneIMEI(Activity aty) {

		TelephonyManager tm = (TelephonyManager) aty

		.getSystemService(Activity.TELEPHONY_SERVICE);

		return tm.getDeviceId();

	}

	/**
	 * 
	 * MD5加密
	 */

	public static String md5(String string) {

		byte[] hash;

		try {

			hash = MessageDigest.getInstance("MD5").digest(

			string.getBytes("UTF-8"));

		} catch (NoSuchAlgorithmException e) {

			throw new RuntimeException("Huh, MD5 should be supported?", e);

		} catch (UnsupportedEncodingException e) {

			throw new RuntimeException("Huh, UTF-8 should be supported?", e);

		}

		StringBuilder hex = new StringBuilder(hash.length * 2);

		for (byte b : hash) {

			if ((b & 0xFF) < 0x10)

				hex.append("0");

			hex.append(Integer.toHexString(b & 0xFF));

		}

		return hex.toString();

	}

	/**
	 * 
	 * KJ加密
	 */

	public static String KJencrypt(String str) {

		char[] cstr = str.toCharArray();

		StringBuilder hex = new StringBuilder();

		for (char c : cstr) {
			hex.append((char) (c + 5));
		}

		return hex.toString();
	}

	/**
	 * 
	 * KJ解密
	 */

	public static String KJdecipher(String str) {

		char[] cstr = str.toCharArray();

		StringBuilder hex = new StringBuilder();

		for (char c : cstr) {
			hex.append((char) (c - 5));
		}

		return hex.toString();

	}

	
	public static int toInt(String str){
		if(str==null || str.length()==0)
			return 0;
		str=str.trim();
		return Integer.parseInt(str);
	}
	
	public static BigDecimal toBigDecimal(String str){
		if(str==null || str.length()==0)
			return new BigDecimal(0.00);
		
		return new BigDecimal(str.trim());
	}
	
	public static String toYyyyMMdd(String rq){
		return rq.replaceAll("-|/", "");
	}
	
	public static String stdStr(Object o){
		if(o==null)
			return "";
		
		if(o instanceof String)
			return ((String)o).trim();
		
		return String.valueOf(o);
	}	
	public static String std金额(Object obj){
		if(obj!=null || obj instanceof BigDecimal)
			return std金额((BigDecimal)obj);
		
    	String src=stdStr(obj);
    	return std金额(src);
	}	
	public static String std金额(String m1){
		if(m1==null)
			return "0.00";
		
		m1=m1.trim();
		if(m1.length()==0)
			return "0.00";
		
		if(isZero(m1))
			return "0.00";
		
		BigDecimal bd=new BigDecimal(m1);
//    	//System.out.println(bd.toString());
//    	String std金额=bd.toString();
//    	
//    	int dotIndex=std金额.indexOf(".");
//    	if(dotIndex==-1)
//    		return std金额+".00";

    	String rs=String.format("%.2f", bd);
    	return rs;
	}
	
	public static String std金额(BigDecimal bd){
    	String rs=String.format("%.2f", bd);
    	return rs;
	}	
	
	public static String addStr金额(String m1, String m2){
		BigDecimal bd1=toBigDecimal(m1);
		BigDecimal bd2=toBigDecimal(m2);
		
		BigDecimal bd3=bd1.add(bd2);
		
		return std金额(bd3);
	}
	public static BigDecimal addStr金额(BigDecimal bd1, String m2){
		BigDecimal bd2=toBigDecimal(m2);
		
		BigDecimal bd3=bd1.add(bd2);
		
		return bd3;
	}
	public static String addStr金额(String m1, BigDecimal bd2){
		BigDecimal bd1=toBigDecimal(m1);
		
		BigDecimal bd3=bd1.add(bd2);
		
		return std金额(bd3);
	}
	
	public static boolean isPositive(String m0){
			BigDecimal bd=new BigDecimal(m0);
			BigDecimal feng1=new BigDecimal("0.01");
			int rs=bd.compareTo(feng1);
			return rs>0;
	}
	public static boolean isPositive(BigDecimal bd){
		BigDecimal feng1=new BigDecimal("0.01");
		int rs=bd.compareTo(feng1);
		return rs>0;
}	
	
	public static boolean isZero(String m0){
		//都对 "000000000.00", ".00", m0="0";
			BigDecimal bd=new BigDecimal(m0);
			int rs=bd.compareTo(BigDecimal.ZERO);
			return 0==rs;
	}
	public static boolean isZero(BigDecimal bd){
		try{
			BigDecimal feng1=new BigDecimal("0.01");
			int rs=bd.abs().compareTo(feng1);
			return rs<=0;
		}catch (Exception e) {
			return false;
		}
	}
	public static boolean isGtEqZero(BigDecimal bd){
		try{
			int rs=bd.compareTo(BigDecimal.ZERO);
			return rs>=0;
		}catch (Exception e) {
			return false;
		}
	}	
	
//	public static boolean isNullOrWhiteSpace(String str){
//		if(str==null)
//			return true;
//		
//		if(str.trim().length()==0)
//			return true;
//		
//		return false;
//	}
	
	public static String getFileNameFromPath(String path){
		if(isEmpty(path))
			return null;
		
		path=path.trim();
		
		if(path.endsWith("/"))
			return null;
		
		int idx=path.lastIndexOf('/');
		if(idx==-1)
			return path;
		
		return path.substring(idx+1);
		
	}
}
