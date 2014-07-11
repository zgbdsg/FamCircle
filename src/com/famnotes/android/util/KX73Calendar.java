package com.famnotes.android.util;
//package ywsl.util;

import java.util.Calendar;
import java.util.Date;

public class KX73Calendar {
    public Calendar calendar = null;
    
    public KX73Calendar() {
        calendar = Calendar.getInstance();
        //Date trialTime = new Date();
        //calendar.setTime(trialTime);
    }
    public KX73Calendar(Date date) {
        calendar = Calendar.getInstance();
        if(date!=null)
        	calendar.setTime(date);
    }    
    public KX73Calendar(String ymd, String hms) {
        if(ymd==null || ymd.length() !=8)
            return ;
        if(hms!=null && hms.length()!=6)
            return ;
        if(hms==null)
            hms="000000";
        calendar = Calendar.getInstance();
        int year=Integer.parseInt(ymd.substring(0, 4));
        int month=Integer.parseInt(ymd.substring(4, 6))-1;
        int date=Integer.parseInt(ymd.substring(6, 8));
        int hour=Integer.parseInt(hms.substring(0, 2));
        int minute=Integer.parseInt(hms.substring(2, 4));
        int second=Integer.parseInt(hms.substring(4, 6));
        calendar.set(year, month, date, hour, minute, second);
        //Date trialTime = new Date();
        //calendar.setTime(trialTime);
    }
    
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }
    
    public String getMonth() {
        int m = getMonthInt();
        String[] months = new String [] { "01", "02", "03",
        "04", "05", "06",
        "07", "08", "09",
        "10", "11", "12" };
        if (m > 12)
            return "Unknown to Man";
        
        return months[m - 1];
        
    }
    
    public String getDay() {
        int x = getDayOfWeek();
        String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday",
        "Thursday", "Friday", "Saturday"};
        
        if (x > 7)
            return "Unknown to Man";
        
        return days[x - 1];
        
    }
    
    public String getMonthDay() {
        int d=calendar.get(Calendar.DAY_OF_MONTH);
        if(d<10){
            return "0" + d;
        } else {
            return String.valueOf(d);
        }
    }
    
    public int getMonthInt() {
        return 1 + calendar.get(Calendar.MONTH);
    }
    
    public String getDate() {
        String year=String.valueOf(getYear());
        return  year + getMonth() + getMonthDay();
        
    }
    public String getDate10() {
        String year=String.valueOf(getYear());
        return  year +"-"+ getMonth() +"-"+  getMonthDay();
        
    }
    public String getTime() {
        return getHour() + ":" + getMinute() + ":" + getSecond();
    }
    public String getTime6() {
        String h=String.valueOf(getHour());
        if(h.length()==1)
            h="0"+h;
        String m=String.valueOf(getMinute());
        if(m.length()==1)
            m="0"+m;
        String s=String.valueOf(getSecond());
        if(s.length()==1)
            s="0"+s;
        return (h + m + s);
    }
    
    public int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    public int getDayOfYear() {
        return calendar.get(Calendar.DAY_OF_YEAR);
    }
    
    public int getWeekOfYear() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }
    
    public int getWeekOfMonth() {
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }
    
    public int getDayOfWeek() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    
    public int getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    
    public int getMinute() {
        return calendar.get(Calendar.MINUTE);
    }
    
    
    public int getSecond() {
        return calendar.get(Calendar.SECOND);
    }
    
    public static void main(String args[]) {
        //KX73Calendar db = new KX73Calendar();
        KX73Calendar db = new KX73Calendar("20020019", "174234");
        p("date: " + db.getDayOfMonth());//kongx73
        p("year: " + db.getYear());
        p("month: " + db.getMonth());
        p("time: " + db.getTime());
        p("time6: " + db.getTime6());
        p("date: " + db.getDate());
        p("Day: " + db.getDay());
        p("DayOfYear: " + db.getDayOfYear());
        p("WeekOfYear: " + db.getWeekOfYear());
        p("era: " + db.getEra());
        p("ampm: " + db.getAMPM());
        p("DST: " + db.getDSTOffset());
        p("ZONE Offset: " + db.getZoneOffset());
        //p("TIMEZONE: " + db.getUSTimeZone());
        //db.add(java.util.Calendar.MONTH, -25);
        p("getMonthDay():"+db.getMonthDay());
        p("date: " + db.getDate());
        p("longtime:"+ db.toLongString());
        boolean b1=false;
        String sTime6=db.getTime6();
        if(sTime6.compareTo("173000")>=0)
            b1=true;
        System.out.println("b1="+b1);
        
        KX73Calendar kx73Cale=new KX73Calendar();
        kx73Cale.calendar.set(Calendar.DATE, 0);
        String startDay=kx73Cale.getDate10();
        KX73Calendar newCale=new KX73Calendar();
        int month=kx73Cale.getMonthInt();
        while(kx73Cale.getMonthInt()==month){
        	String rq=kx73Cale.getDate10();
        	System.out.println(rq);
        	kx73Cale.add(Calendar.DATE, +1);
        	
        }
    }
    
    private static void p(String x) {
        System.out.println(x);
    }
    
    
    public int getEra() {
        return calendar.get(Calendar.ERA);
    }
    
    public String getUSTimeZone() {
        String[] zones = new String[] {"Hawaii", "Alaskan", "Pacific",
        "Mountain", "Central", "Eastern"};
        
        return zones[10 + getZoneOffset()];
    }
    
    public int getZoneOffset() {
        return calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000);
    }
    
    
    public int getDSTOffset() {
        return calendar.get(Calendar.DST_OFFSET)/(60*60*1000);
    }
    
    
    public int getAMPM() {
        return calendar.get(Calendar.AM_PM);
    }
    
    public void add(int field, int amount) {
        this.calendar.add(field, amount);
    }
    
    public String toLongString(){
    	return getDate10()+" "+getTime6();
    }
    
	public long getTimeInMillis(){
		return calendar.getTimeInMillis(); 
	}    
	
	
	public int compareTo(KX73Calendar another){
		return (int) ((this.getTimeInMillis()-another.getTimeInMillis())/1000);		
	}
	
	public Date fromChar10(String yyyy_mm_dd){
		try{
			if(yyyy_mm_dd.length()==10){
				int yyyy=Integer.parseInt(yyyy_mm_dd.substring(0, 4));
				int mm=Integer.parseInt(yyyy_mm_dd.substring(5, 7))-1;
				int dd=Integer.parseInt(yyyy_mm_dd.substring(8));
	//			Date d=new Date(yyyy, mm, dd);
	//			return d;
				this.calendar.set(yyyy, mm, dd, 0, 0, 0);
				return this.calendar.getTime();
			}else if(yyyy_mm_dd.length()==8){
				int yyyy=Integer.parseInt(yyyy_mm_dd.substring(0, 4));
				int mm=Integer.parseInt(yyyy_mm_dd.substring(4, 6))-1;
				int dd=Integer.parseInt(yyyy_mm_dd.substring(6));
	//			Date d=new Date(yyyy, mm, dd);
	//			return d;
				this.calendar.set(yyyy, mm, dd, 0, 0, 0);
				return this.calendar.getTime();
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}		
}


