package com.famnotes.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileRW{

	
	public static String readFullFile(File path, String encoding){
		try {
			StringBuffer sb=new StringBuffer();
			FileInputStream fis = new FileInputStream(path); 
	        InputStreamReader isr = new InputStreamReader(fis, encoding); //"UTF-8"
	        
			BufferedReader fr=new BufferedReader(isr);
			
			String line=null;
			do{
				line=fr.readLine();
				if(line!=null)
					sb.append(line);
			}while(line!=null);
				
			isr.close();
			return sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
			
		return "0001";
	}

	public static void writeFullFile(File path, String encoding, String data){
		try {
			FileOutputStream fis = new FileOutputStream(path); 
	        OutputStreamWriter osw = new OutputStreamWriter(fis, encoding); //"UTF-8"
	        
	        osw.write(data);
	        osw.flush();
	        osw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
			
	}
	
}