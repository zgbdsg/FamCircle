package com.android.famcircle.orderlist;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.Constants;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.vo.User;


public class OrderStatusListActivity extends BaseActivity{

	MySectionIndexer mIndexer;

	PicListAdapter mAdapter;
	PinnedHeaderListView mListView;
//	TextView back;
	String[] tagName;
	public static OrderStatusListHandler handler;
	String tagFormat = "MM-dd-yyyy";
	int currentLevel = 0;
/*	private String [][]tagName = {
			{"2010年", "2011年", "2012年", "2013年", "2014年", "2015年", "2016年"},
			{"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"},
			{"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"}
	};*/
	List<OrderListTag> orderListTag;
	int maxLevel = 3;
	int[] counts;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_list);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		//back = (TextView) findViewById(R.id.back_title);
		mListView = (PinnedHeaderListView) findViewById(R.id.mListView);
		
		RequestData requestData = new RequestData();
		handler = new OrderStatusListHandler(this, false);
		requestData.connect(handler);
		requestData.execute(0);
		
	}

	class OrderStatusListHandler extends BaseAsyncTaskHandler<BaseActivity, Integer>{

		public OrderStatusListHandler(OrderStatusListActivity context,
				boolean showProgressBar) {
			super(context, showProgressBar);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onTaskFailed(BaseActivity arg0, Exception arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onTaskSuccess(BaseActivity arg0, Integer arg1) {
			// TODO Auto-generated method stub
			switch (arg1) {
			case 0:
				if(mAdapter==null){
					mIndexer = new MySectionIndexer(tagName, counts);
					mAdapter = new PicListAdapter(orderListTag, mIndexer, getApplicationContext(), tagName, maxLevel);
					mListView.setAdapter(mAdapter);
//					mListView.setOnScrollListener(mAdapter);
					
					//設置頂部固定頭部
					mListView.setPinnedHeaderView(LayoutInflater.from(getApplicationContext()).inflate(  
			                R.layout.order_list_group_item, mListView, false));  
					
				} else if(mAdapter!=null){
//					mIndexer = new MySectionIndexer(tagName, counts);
//					mAdapter.setIndexer(tagName, counts);
					Log.v("halley", "handler tagName:" + tagName.length);
					mAdapter.setData(orderListTag,tagName, counts);
					if (orderListTag != null) {
						Log.v("halley", "tagSize:" + orderListTag.size());
					}
					Log.v("halley", "counts_case0:" + counts.length);
					mAdapter.notifyDataSetChanged();
				}
				
				break;

			}
			return true;
		}
		
	}
	
	class RequestData  extends BaseAsyncTask<BaseActivity, Integer, Integer>{

		@Override
		public Integer run(Integer... requestLevel) throws Exception {
			// TODO Auto-generated method stub
			switch (requestLevel[0]) {
			case 0:
				tagFormat = "yyyy" ;
				break;
			case 1:
				tagFormat = "MM-yyyy";
				break;
			case 2:
				tagFormat = "MM-dd-yyyy";
				break;
			case 3:
				tagFormat = "MM-dd-yyyy";
				break;
			default:
				tagFormat = "MM-dd-yyyy";
				break;
			}
			String json=null;
			try{
			PostData pdata = new PostData("share", "searchTime","{\"grpId\":"+User.Current.grpId+", \"type\":"+requestLevel+"}");
			 json = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			orderListTag = getOrderListTag(json);
			counts = new int[orderListTag.size()];
			Log.v("halley", "counts:run:" + counts.length + "   orderListTag.size:" + orderListTag.size());
			for (int i = 0; i < counts.length; i++) {
				counts[i] = 1;
			}
			
			return 0;
		}
		
	}
	
	
	public void updateDataByLevel(int level){
		RequestData requestData = new RequestData();
		requestData.connect(handler);
		requestData.execute(level);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.orderlist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			if (currentLevel> 0) {
				mAdapter.lowLevel();
//				
//				Message msg = new Message();
//				msg.what = 1;
//				msg.arg2 =mAdapter.getLevel();
//				handler.sendMessage(msg);
				
				RequestData newData = new RequestData();
				newData.execute(mAdapter.getLevel());
			}else{
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private List<OrderListTag>  getOrderListTag(String string) {
		// TODO Auto-generated method stub

		//initialStatuses();
		JSONObject allResult = JSON.parseObject(string);
		JSONObject jsonResult = allResult.getJSONObject("results");
		
		if(string == null || string.length() == 0 || allResult.getInteger("errCode") != 0){
			return null;
		}
		
		String smallPicPath = jsonResult.getString("smallPicPath");
		
		JSONArray statusArray = jsonResult.getJSONArray("status");
		List<OrderListTag> listmap = new ArrayList<OrderListTag>();
		tagName = new String[statusArray.size()];
		Log.v("halley", "tagName:" + tagName.length);
		for(int i=0;i<statusArray.size();i ++){
			OrderListTag orderList= new OrderListTag();
			JSONArray statusesOfTime = statusArray.getJSONArray(i);
			
			String tag = "";
			List<Picture> picItemList = new ArrayList<Picture>();

			String time = ((JSONObject)statusesOfTime.get(0)).getString("creatTime");
			Date dt = new Date(Long.parseLong(time+"000"));
//			SimpleDateFormat df;
//			if()
			SimpleDateFormat df=new SimpleDateFormat(tagFormat); 
			tag= df.format(dt);
			
			if(currentLevel == 2){
				Calendar calendar = Calendar.getInstance(); 
				calendar.setFirstDayOfWeek(Calendar.MONDAY); //America set sunday is the first day;
				calendar.setTime(dt); 
				tag = "the "+calendar.get(Calendar.WEEK_OF_YEAR)+"th week in "+new SimpleDateFormat("yyyy").format(dt);
			}
			tagName[i] = tag;
			
			for(int j=0;j<statusesOfTime.size();j ++){
				JSONObject statusItem = (JSONObject) statusesOfTime.get(j);
				String usrId = statusItem.getString("usrId");
				JSONObject resrc = statusItem.getJSONObject("resrc");
				JSONArray picList = resrc.getJSONArray("picArray");
				
				for(int k=0;k<picList.size();k ++){
					String picurl = picList.getString(k);
					Picture pic = new Picture();
					pic.setUrl("http://"+Constants.Server+smallPicPath +usrId+"/"+picurl);
					picItemList.add(pic);
				}
			}
			
			orderList.setTag(tag);
			orderList.setPicItemList(picItemList);
			
			listmap.add(orderList);
		}
		
		//onLoading.dismiss();
		return listmap;
	}
}
