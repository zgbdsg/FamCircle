package com.famnotes.android.db;

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.famnotes.android.vo.User;
//import android.database.sqlite.SQLiteOpenHelper;


public class DBUtil {
	final public static String dbPath="/data/data/com.android.famcircle/famnotes.db";
	
	public static boolean detectDatabase() {
		File dbFile=new File(dbPath);
		
		return dbFile.exists();
	}
	public static User getCurrentUser() throws Exception {
		SQLiteDatabase sld = null;
		Cursor cur = null;
		try {
			// 打开数据库
			sld = openDatabase();
			String sql = "select * from fn_user where flag=1";
			cur = sld.rawQuery(sql, null);
			if (cur.moveToNext()) {
				User user= new User();
				user.id =getInt(cur, "id");
				user.grpId=getInt(cur, "grpId");
				user.loginId=getString(cur, "loginId");
				user.name=getString(cur, "name");
				user.password=getString(cur, "password");
				user.avatar=getString(cur, "avatar");
				user.flag=getInt(cur, "flag");
				return user;
			}
		} catch (Exception e) {
			Log.d("error", e.toString() + "=============getCurrentUser===============");
			throw new Exception("getCurrentUser fail：" + e.toString());
		} finally {
			try {
				cur.close();// 关闭结果集
				closeDatabase(sld);// 关闭数据库
			} catch (Exception e) {
				Log.d("error", e.toString() + "=============getCurrentUser===============");
			}
		}
		return null;
	}
	public static void clearDatabase() throws Exception {
		SQLiteDatabase sld = null;
		try {
			// 打开数据库
			sld = openDatabase();
			String sql = "delete from fn_user";
			sld.execSQL(sql);
		} catch (Exception e) {
			Log.d("error", e.toString() + "=============clearDatabase===============");
			throw new Exception("clearDatabase fail：" + e.toString());
		} finally {
			try {
				closeDatabase(sld);// 关闭数据库
			} catch (Exception e) {
				Log.d("error", e.toString() + "=============clearDatabase===============");
			}
		}
	}	
	
	public static long  insertUser(User user) throws Exception {
		SQLiteDatabase sldb = null;
		try {
			// 打开数据库
			sldb = openDatabase();
			if(user.flag==1){
				String sql = "delete from fn_user where flag=1";
				sldb.execSQL(sql);
			}
			ContentValues cvs=new ContentValues();
			cvs.put("id", user.id);
			cvs.put("grpId", user.grpId);
			cvs.put("loginId", user.loginId);
			cvs.put("name", user.name);
			cvs.put("password", user.password);
			cvs.put("avatar", user.avatar);
			cvs.put("flag", user.flag);
			cvs.put("type", user.type);
			cvs.put("flag", user.flag);
			cvs.put("role", user.role);
//			public int id=0, type=0, flag=0, role=0;
//			public int grpId=0;
//			public String loginId, name, password;			
			
			return sldb.insert("fn_user", null, cvs);
		} catch (Exception e) {
			Log.d("error", e.toString() + "=============insertUser===============");
			throw new Exception("getCurrentUser fail：" + e.toString());
		} finally {
			try {
				closeDatabase(sldb);// 关闭数据库
			} catch (Exception e) {
				Log.d("error", e.toString() + "=============insertUser===============");
			}
		}
	}		
	
	public static int getInt(Cursor cur, String columnName){
		int colIdx=cur.getColumnIndex(columnName);
		if(colIdx==-1)
			return -1;
		
		return cur.getInt(colIdx);
	}
	public static String getString(Cursor cur, String columnName){
		int colIdx=cur.getColumnIndex(columnName);
		if(colIdx==-1)
			return null;
		
		return cur.getString(colIdx);
	}
	
	public static boolean createDatabase() throws Exception {
		SQLiteDatabase sld = null;
		try {
			sld = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
			
			String sql = "CREATE TABLE IF NOT EXISTS `fn_user` ("  +
					  "`id` integer NOT NULL PRIMARY KEY,"  +
					  "`grpId` integer,"  +
					  "`loginId` varchar(32),"  +
					  "`name` varchar(255),"  +
					  "`email` varchar(255),"  +
					  "`cellno` varchar(255),"  +
					  "`password` varchar(255),"  +
					  "`avatar` varchar(255),"  +
					  "`type` integer,"  +
					  "`flag` integer,"  +
					  "`role` integer"  +
					");" ;
			sld.execSQL(sql);
			return true;
		} catch (Exception e) {
			Log.d("error", e.toString() + "=============createDatabase===============");
			throw new Exception("数据库创建错误：" + e.toString());
		} finally {
			if(sld!=null)
				sld.close();
		}
	}

	public static SQLiteDatabase openDatabase() throws Exception {
		SQLiteDatabase sld = null;
		try {
			sld = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

		} catch (Exception e) {
			Log.d("error", e.toString() + "=============open===============");
			throw new Exception("数据库打开错误：" + e.toString());
		}
		return sld;
	}
	
	// 关闭数据库的方法
	public static void closeDatabase(SQLiteDatabase sld) throws Exception {
		try {
			sld.close();
		} catch (Exception e) {
			Log.d("error", e.toString() + "=============close===============");
			throw new Exception("数据库关闭失败：" + e.toString());
		}
	}

//	// ===================获得分类列表==================
//	public static List<String> getTypeList(Context context) {
//		SQLiteDatabase sld = null;
//		List<String> typelist = new ArrayList<String>();
//		Cursor cur = null;
//		try {
//			// 打开数据库
//			sld = openDatabase(context);
//			// String test="delete from typetable";
//			// sld.execSQL(test);
//			// 查询语句
//			String sql = "select id,type from typetable";
//			cur = sld.rawQuery(sql, null);// 执行语句得到结果集
//			while (cur.moveToNext()) {
//				StringBuffer cursb = new StringBuffer();
//				cursb.append(cur.getInt(0) + "#");// 将一条记录整理成中间以#分隔的字符串
//				cursb.append(cur.getString(1));
//				// 添加到列表中去
//				// 打印
//				Log.d("type", cursb.toString() + "=");
//				typelist.add(cursb.toString());
//			}
//
//		} catch (Exception e) {
//			Log.d("error", e.toString() + "===============type=============");
//			throw new Exception("数据库查询失败：" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		} finally {
//			try {
//				cur.close();// 关闭结果集
//				closeDatabase(sld, context);// 关闭数据库
//			} catch (Exception e) {
//				Log.d("error", e.toString()
//						+ "============type================");
//			}
//
//		}
//		return typelist;
//	}
//
//	// 删除相应id的日程及timetable和alerttable中的相关记录
//	public static void deleteSchdule(Context context, String eventid) {
//		// 先删除alerttable的记录再删除timetable的记录，最后删除eventtable中的记录
//		SQLiteDatabase sld = null;
//		Cursor cur = null;
//		try {
//			// 打开数据库
//			sld = openDatabase(context);
//			String sql = "select id,alerttime from timetable where eventid=?";
//			cur = sld.rawQuery(sql, new String[] { eventid });// 先通过timetable找到相应alerttable的id
//			while (cur.moveToNext()) {
//				// 删除提醒表中的内容
//				sql = "delete from alerttable where alerttime=?";
//				sld.execSQL(sql, new String[] { cur.getInt(1) + "" });
//				// 删除timetable中的相应内容
//				sql = "delete from timetable where id=?";
//				sld.execSQL(sql, new String[] { cur.getInt(0) + "" });
//			}
//			cur.close();
//			// 再查询相应日程对应的内容表中的id
//			sql = "select content from eventtable where id=?";
//			cur = sld.rawQuery(sql, new String[] { eventid });
//			// 删除事件表的日程记录
//			sql = "delete from eventtable where id=?";
//			sld.execSQL(sql, new String[] { eventid });
//			// 删除内容表中的记录
//			sql = "delete from contenttable where id=?";
//			sld.execSQL(sql, new String[] { eventid });
//			/*
//			 * if(cur.moveToNext()) { sql="delete from contenttable where id=?";
//			 * sld.execSQL(sql,new String[]{cur.getInt(0)+""});
//			 * throw new Exception("删除成功"); }else{
//			 * throw new Exception("删除失败"); }
//			 */
//
//		} catch (Exception e) {
//			Log.d("error", e.toString() + "=============delete===============");
//			throw new Exception("删除失败：" + e.toString(), Toast.LENGTH_LONG)
//					.show();
//		} finally {
//			try {
//				cur.close();// 关闭结果集
//				closeDatabase(sld, context);// 关闭数据库
//			} catch (Exception e) {
//				Log.d("error", e.toString()
//						+ "=============delete2===============");
//			}
//		}
//	}
//
//	// 通过事件的id查询其标题
//	public static String getTitleById(Context context, int id) {
//		String title = null;
//		SQLiteDatabase sld = null;
//		Cursor cur = null;
//		try {
//			// 打开数据库
//			sld = openDatabase(context);
//			String sql = "select title from eventtable where id=" + id;
//			cur = sld.rawQuery(sql, null);
//			if (cur.moveToNext()) {
//				title = cur.getString(0);// 取得标题
//			}
//		} catch (Exception e) {
//			Log.d("error", e.toString()
//					+ "=============selectTitleById===============");
//			throw new Exception("通过id查询标题失败：" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		} finally {
//			try {
//				cur.close();// 关闭结果集
//				closeDatabase(sld, context);// 关闭数据库
//			} catch (Exception e) {
//				Log.d("error", e.toString()
//						+ "=============selectTitleById===============");
//			}
//		}
//		return title;
//	}
//
//	public static ArrayList<String> gettype(Context ff) // 获取日程类型、
//	{
//		SQLiteDatabase sld = null;
//		String sql = null;
//		ArrayList<String> al = new ArrayList<String>();
//		al.clear();
//		sld = openDatabase(ff);
//		try {
//			sql = "select id ,type from typetable";
//			Cursor cur = sld.rawQuery(sql, null);
//			al.add((-1) + "#全部");
//			while (cur.moveToNext()) {
//				al.add(cur.getInt(0) + "#" + cur.getString(1));
//			}
//			cur.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				closeDatabase(sld, ff);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return al;
//	}
//
//	public static ArrayList<String> gettypeContent(Context ff, int typeid) // 获取相应类型的内容。
//	{
//		SQLiteDatabase sld = null;
//		sld = openDatabase(ff);
//		String sql = null;
//		ArrayList<String> al = new ArrayList<String>();
//		al.clear();
//		try {
//
//			if (typeid == (-1)) {
//				sql = "select id ,title from eventtable";
//				Cursor cur = sld.rawQuery(sql, null);
//				while (cur.moveToNext()) {
//					al.add(cur.getInt(0) + "#" + cur.getString(1));
//				}
//				cur.close();
//			} else {
//				sql = "select id ,title from eventtable where type=(select id from typetable where id='"
//						+ typeid + "')";
//				Cursor cur = sld.rawQuery(sql, null);
//				while (cur.moveToNext()) {
//					al.add(cur.getInt(0) + "#" + cur.getString(1));
//				}
//				cur.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				closeDatabase(sld, ff);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return al;
//	}
//
//	public static ArrayList<String> searchContent(Context fa, String edit) // 通过输入文本框中的内容进行查询相关的信息
//	{
//		SQLiteDatabase sld = null;
//		sld = openDatabase(fa);
//		ArrayList<String> al = new ArrayList<String>();
//		String sql = null;
//		al.clear();
//		try {
//			sql = "select id ,title from eventtable where title like '%" + edit
//					+ "%'";
//			Cursor cur = sld.rawQuery(sql, null);
//			while (cur.moveToNext()) {
//				al.add(cur.getInt(0) + "#" + cur.getString(1));
//			}
//			cur.close();
//		} catch (Exception e) {
//			throw new Exception("数据库错误 delete：" + e.toString(),
//					Toast.LENGTH_LONG).show();
//			;
//		} finally {
//			try {
//				closeDatabase(sld, fa);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return al;
//	}
//
//	// ==========================================================
//	// ===从数据库中读取eventid,使得传进来的id能有一个值，并且返回==========
//	@SuppressWarnings("unused")
//	public int geteventID(Context context, int id) {
//		SQLiteDatabase sld = null;
//		Cursor cursor = null;
//		try {
//			sld = openDatabase(context);
//
//			String sql = "select max(id) from eventtable ";
//			cursor = sld
//					.query("eventtable", null, null, null, null, null, "id");// 执行语句得到结果集
//			if (cursor.moveToLast()) {
//				id = cursor.getInt(0) + 1;
//			}
//			// throw new Exception("id:"+id);
//			cursor.close();
//		} catch (Exception e) {
//			throw new Exception("ID赋值失败" + e.toString(), Toast.LENGTH_LONG)
//					.show();
//		}
//		closeDatabase(sld, context);
//		return id;
//	}
//
//	public int gettypeID(Context fa, String selectedtype) {
//		SQLiteDatabase sld = null;
//		Cursor cursor = null;
//		int typeid = 0;
//		try {
//			sld = openDatabase(fa);
//
//			String sql = "select id,type from typetable";
//			cursor = sld.rawQuery(sql, null);
//
//			if (cursor.moveToLast()) {
//				typeid = cursor.getInt(0);
//			}
//			cursor.close();
//			closeDatabase(sld, fa);
//
//		} catch (Exception e) {
//			throw new Exception("读取失败" + e.toString());
//
//		}
//
//		return typeid;
//
//	}
//
//	// 像内容表中添加内容
//	public void insertcontent(Context applicationContext, int id, String text,
//			String picpath, String soundpath) {
//		SQLiteDatabase sld = null;
//		try {
//			sld = openDatabase(applicationContext);
//			String sql = "insert into  contenttable(id,text,picpath,soundpath) values ("
//					+ id
//					+ ",'"
//					+ text
//					+ "','"
//					+ picpath
//					+ "','"
//					+ soundpath
//					+ "')";
//
//			sld.execSQL(sql);
//			closeDatabase(sld, applicationContext);
//
//		} catch (Exception e) {
//			Toast.makeText(applicationContext, "添加失败" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		}
//	}
//
//	// ====================获取内容
//	// 根据ID查询内容表的内容
//	public ArrayList<String> getcontent(Context context, int inid) {
//
//		ArrayList<String> contentarray = new ArrayList<String>();
//		SQLiteDatabase sld = null;
//		Cursor cursor = null;
//		contentarray.clear();
//		try {
//			/*
//			 * String drop="drop table contenttable"; sld.execSQL(drop);
//			 */
//			sld = openDatabase(context);
//			String sql = "select id,text,picpath,soundpath from contenttable where id="
//					+ inid;
//			cursor = sld.rawQuery(sql, null);
//			if (cursor.moveToNext()) {
//				contentarray
//						.add(cursor.getInt(0) + "#" + cursor.getString(1) + "#"
//								+ cursor.getString(2) + "#"
//								+ cursor.getString(3));
//
//			}
//			cursor.close();
//			closeDatabase(sld, context);
//		} catch (Exception e) {
//			throw new Exception("内容读取失败" + e.toString(), Toast.LENGTH_LONG)
//					.show();
//		}
//
//		return contentarray;
//	}
//
//	public ArrayList<String> getevent(Context applicationContext, int inid) {
//		ArrayList<String> eventarray = new ArrayList<String>();
//		SQLiteDatabase sld = null;
//		Cursor cursor = null;
//		eventarray.clear();
//		try {
//			sld = openDatabase(applicationContext);
//			String sql = "select id,title,content,type,person,place,alertstyle from eventtable where id="
//					+ inid;
//
//			cursor = sld.rawQuery(sql, null);
//			if (cursor.moveToNext()) {
//				eventarray.add(cursor.getInt(0) + "#" + cursor.getString(1)
//						+ "#" + cursor.getInt(2) + "#" + cursor.getInt(3) + "#"
//						+ cursor.getString(4) + "#" + cursor.getString(5) + "#"
//						+ cursor.getString(6));
//			}
//			cursor.close();
//			closeDatabase(sld, applicationContext);
//
//		} catch (Exception e) {
//			Toast.makeText(applicationContext, "event读取失败" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		}
//
//		return eventarray;
//	}
//
//	// 根据id获取类型
//	public ArrayList<String> gettype(Context applicationContext, int typeid) {
//		ArrayList<String> typearray = new ArrayList<String>();
//		SQLiteDatabase sld = null;
//		Cursor cursor = null;
//		typearray.clear();// TODO Auto-generated method stub
//		try {
//			sld = openDatabase(applicationContext);
//			String sql = "select id,type from typetable where id=" + typeid;
//			cursor = sld.rawQuery(sql, null);
//			if (cursor.moveToNext()) {
//				typearray.add(cursor.getInt(0) + "#" + cursor.getString(1));
//			}
//			cursor.close();
//			closeDatabase(sld, applicationContext);
//
//		} catch (Exception e) {
//			Toast.makeText(applicationContext, "type读取失败" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		}
//		return typearray;
//	}
//
//	public ArrayList<String> gettimes(Context applicationContext, int eventid) {
//		ArrayList<String> timesarray = new ArrayList<String>();
//		SQLiteDatabase sld = null;
//		Cursor cursor = null;
//		timesarray.clear();
//		try {
//			sld = openDatabase(applicationContext);
//			String sql = "select alerttime,nowtime,alertone,alerttwo,alertthree,alertfour,alertfive,alertsix,alertseven from alerttable where alerttime="
//					+ eventid;
//			cursor = sld.rawQuery(sql, null);
//			if (cursor.moveToLast()) {
//				timesarray.add(cursor.getInt(0) + "#" + cursor.getString(1)
//						+ "#" + cursor.getString(2) + "#" + cursor.getString(3)
//						+ "#" + cursor.getString(4) + "#" + cursor.getString(5)
//						+ "#" + cursor.getString(6) + "#" + cursor.getString(7)
//						+ "#" + cursor.getString(8));
//			}
//			cursor.close();
//			closeDatabase(sld, applicationContext);
//
//		} catch (Exception e) {
//			Toast.makeText(applicationContext, "alerttime读取失败" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		}
//
//		return timesarray;
//	}
//
//	// 更新相关的数据
//	public void updatealert(Context applicationContext, int eventid,
//			String nowtime, String alerone, String alerttwo, String alertthree,
//			String alertfour, String alertfive, String alertsix,
//			String aldertseven) {
//		SQLiteDatabase sld = null;
//		try {
//			sld = openDatabase(applicationContext);
//			String sql = "update  alerttable set nowtime='" + nowtime
//					+ "',alertone='" + alerone + "',alerttwo='" + alerttwo
//					+ "',alertthree='" + alertthree + "',alertfour='"
//					+ alertfour + "',alertfive='" + alertfive + "',alertsix='"
//					+ alertsix + "',alertseven='" + aldertseven
//					+ "'  where alerttime=" + eventid;
//			sld.execSQL(sql);
//			closeDatabase(sld, applicationContext);
//			// Toast.makeText(applicationContext, "alerttable更新成功",
//			// Toast.LENGTH_LONG).show();
//		} catch (Exception e) {
//			Toast.makeText(applicationContext, "alerttable更新失败" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		}
//
//	}
//
//	public void updateschedule(Context applicationContext, int eventid,
//			String tilte, int contentid, int typeid, String person,
//			String place, String alertstyle) {
//		SQLiteDatabase sld = null;
//		try {
//			sld = openDatabase(applicationContext);
//			String sql = "update eventtable set title='" + tilte + "',content="
//					+ contentid + ",type=" + typeid + ",person='" + person
//					+ "',place='" + place + "',alertstyle='" + alertstyle
//					+ "' where id=" + eventid;
//			sld.execSQL(sql);
//			closeDatabase(sld, applicationContext);
//			// Toast.makeText(applicationContext, "eventtable更新成功",
//			// Toast.LENGTH_LONG).show();
//		} catch (Exception e) {
//			Toast.makeText(applicationContext, "eventtable更新失败" + e.toString(),
//					Toast.LENGTH_LONG).show();
//		}
//
//	}
//
//	public void updatecontent(Context applicationContext, int eventid,
//			String text, String picpath, String soundpath) {
//		SQLiteDatabase sld = null;
//		try {
//			sld = openDatabase(applicationContext);
//			String sql = "update contenttable set text='" + text
//					+ "',picpath='" + picpath + "',soundpath='" + soundpath
//					+ "' where id=" + eventid;
//			sld.execSQL(sql);
//			closeDatabase(sld, applicationContext);
//			// Toast.makeText(applicationContext, "contenttable更新成功",
//			// Toast.LENGTH_LONG).show();
//		} catch (Exception e) {
//			Toast.makeText(applicationContext,
//					"contenttable更新失败" + e.toString(), Toast.LENGTH_LONG)
//					.show();
//		}
//
//	}
//
//	public void inserttimetable(Context applicationContext, int id, int id2) {
//		SQLiteDatabase sld = null;
//		Cursor cursor = null;
//		int tempid = 0;
//		try {
//			sld = openDatabase(applicationContext);
//			cursor = sld.query("timetable", null, null, null, null, null, "id");// 执行语句得到结果集
//			if (cursor.moveToLast()) {
//				tempid = cursor.getInt(0) + 1;
//			}
//			String sql = "insert into timetable  values ( " + tempid + "," + id
//					+ "," + id2 + ")";
//			sld.execSQL(sql);
//			cursor.close();
//			closeDatabase(sld, applicationContext);
//
//		} catch (Exception e) {
//			Toast.makeText(applicationContext,
//					"contenttable更新失败" + e.toString(), Toast.LENGTH_LONG)
//					.show();
//		}
//
//	}

}
