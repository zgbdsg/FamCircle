package com.android.famcircle.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.StatusListInfo;
import com.android.famcircle.StatusOfPersonListAdapter;
import com.android.famcircle.StatusOfPersonListInfo;
import com.android.famcircle.config.Constants;
import com.android.famcircle.config.RequestCode;
import com.android.famcircle.orderlist.OrderStatusListActivity;
import com.android.famcircle.picselect.PublishedActivity;
import com.android.famcircle.ui.ShareActivity.GetDataTask;
import com.android.famcircle.ui.ShareActivity.InitialStatuses;
import com.android.famcircle.ui.ShareActivity.ShareHandler;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.util.ACache;
import com.famnotes.android.util.CustomProgressDialog;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StatusOfPersonActivity extends BaseActivity {

	Context context;
	private PullToRefreshListView mPullRefreshListView;
	private ListView statuslist;
	private StatusOfPersonListAdapter myAdapter;
	private List< Object> listMap;
	private String statusResult;
	private StatusOfPersonHandler myhandler;
	private StatusOfPersonHandler myhandlerPull;
	private int usrId ;
	private String userName;
	private String logoUrl;
	private String groupId;
	private ACache mCache;
	private View headview;
	private boolean isNeedRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_status);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Bundle info = this.getIntent().getExtras();
		usrId = Integer.parseInt(info.getString("usrId"));
		listMap = new ArrayList<Object>();
		myhandler = new StatusOfPersonHandler(this, true); 
		myhandlerPull= new StatusOfPersonHandler(this, false);
		context = this;
		mCache = ACache.get(this);
		
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.statuslist);
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);

		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(
								getApplicationContext(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("refresh at: " + label);
						refreshView.getLoadingLayoutProxy().setRefreshingLabel(getResources().getString(R.string.pull_to_refresh_refreshing_label));
						refreshView.getLoadingLayoutProxy().setReleaseLabel(getResources().getString(R.string.pull_to_refresh_release_label));
						refreshView.getLoadingLayoutProxy().setPullLabel(getResources().getString(R.string.pull_to_refresh_pull_label));

						GetPersonDataTask getDataTask = new GetPersonDataTask();
						getDataTask.connect(myhandlerPull);
						getDataTask.execute(0);
					}
				});

		// Add an end-of-list listener
		mPullRefreshListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						Toast.makeText(StatusOfPersonActivity.this,
								"End of List!", Toast.LENGTH_SHORT).show();
					}
				});

		statuslist = mPullRefreshListView.getRefreshableView();
		headview = LayoutInflater.from(this).inflate(
				R.layout.activity_personal_header, null);
		statuslist.addHeaderView(headview);
		
		myAdapter = new StatusOfPersonListAdapter(context,listMap );
		statuslist.setAdapter(myAdapter);
		
		User usr=User.getUserById(usrId);
		userName=usr.name;
		logoUrl=usr.avatar;
		groupId=String.valueOf(User.Current.grpId);
		updateProfile();
		Log.i("cache", "find cache usrId "+usrId);
		
		statusResult = mCache.getAsString("statusOfPersonResult"+groupId+"---"+usrId);
		if(statusResult == null){
			InitialPersonStatusesTask initialPerson = new InitialPersonStatusesTask();
			initialPerson.connect(myhandler);
			initialPerson.execute("");
		}else{
			isNeedRefresh = false;
			listMap = getStatusListMaps(statusResult);
			//Log.i("listMap length :", ""+listMap.size());
        	myAdapter.setDataList(listMap);
        	isNeedRefresh = false;
        	myAdapter.notifyDataSetChanged();
        	updateProfile();
		}

	}

	@Override
	protected void onStart(){
		super.onStart();

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == android.R.id.home){
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	class GetPersonDataTask extends BaseAsyncTask<StatusOfPersonActivity, Integer, Integer>{
		@Override
		public Integer run(Integer... reqJsonMsg) throws Exception {
			// Simulates a background job.
			String result = "";
			try{
			StatusOfPersonListInfo personStatusInfo = (StatusOfPersonListInfo) listMap.get(listMap.size()-1);
			String[] creatTime = personStatusInfo.getCreatTime();
			String lastCreatTime = creatTime[creatTime.length-1];
			PostData pdata=new PostData("share", "getStatusByUsrId","{\"usrId\":"+usrId+ ", \"grpId\":"+groupId+ ", \"type\":"+0+", \"creatTime\":"+lastCreatTime+"}");
			result=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
			
			Log.i("refresh data :", result);
			statusResult = result;

			}catch(Exception ex){
				ex.printStackTrace();
			}
			return 1;
		}
	}
	
	class InitialPersonStatusesTask  extends BaseAsyncTask<StatusOfPersonActivity, String, Integer>{
		@Override
		public Integer run(String... Msg) throws Exception {
			// TODO Auto-generated method stub
			statusResult = "";
			try{
				JSONObject obj=new JSONObject();
				obj.put("usrId", usrId);
				obj.put("grpId", User.Current.grpId);
				obj.put("type", 0); //? type 日月年
				String reqJsonMsg=obj.toJSONString();
				PostData pdata=new PostData("share","getStatusByUsrId", reqJsonMsg); //?"{\"usrId\":"+usrId+ ", \"grpId\":"+User.Current.grpId+", \"type\":"+0+"}"
				String json=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata);
	
				statusResult = json;
				JSONObject allResult = JSON.parseObject(json);
	
				if(allResult.getInteger("errCode") == 0){
					mCache.put("statusOfPersonResult"+User.Current.grpId+"---"+User.Current.id, statusResult);
				}

			}catch(Exception ex){
				ex.printStackTrace();
			}
			return 1;
		}
	}

	class StatusOfPersonHandler extends BaseAsyncTaskHandler<StatusOfPersonActivity, Integer>{

		public StatusOfPersonHandler(StatusOfPersonActivity context, boolean showProgressBar) {
			super(context, showProgressBar);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onTaskFailed(StatusOfPersonActivity arg0, Exception arg1) {
			// TODO Auto-generated method stub
			return true;
		}
	
		@Override
		public boolean onTaskSuccess(StatusOfPersonActivity arg0, Integer rCode) {
			// TODO Auto-generated method stub
			switch(rCode){
				case 1:
					List<Object> allList = new ArrayList<Object>();
					List<Object> resultList = getStatusListMaps(statusResult);
					
					allList.addAll(listMap);
					allList.addAll(resultList);

					listMap = allList;
					myAdapter.setDataList(listMap);
					Log.i("listMap length :", ""+listMap.size());
					myAdapter.notifyDataSetChanged();

					// Call onRefreshComplete when the list has been refreshed.
					mPullRefreshListView.onRefreshComplete();
					break;
			}
			return true;
		}
		
	}
	
	private List<Object> getStatusListMaps(String string) {
		// TODO Auto-generated method stub

		// initialStatuses();
		JSONObject allResult = JSON.parseObject(string);
		JSONObject jsonResult = allResult.getJSONObject("results");

		if (string == null || string.length() == 0
				|| allResult.getInteger("errCode") != 0) {
			return null;
		}

		JSONArray statusArray = jsonResult.getJSONArray("status");
		List<Object> listmap = new ArrayList<Object>();

		for (int i = 0; i < statusArray.size(); i++) {

			JSONArray statusesOfTime = statusArray.getJSONArray(i);
			StatusOfPersonListInfo personStatusInfo = new StatusOfPersonListInfo() ;
			
			JSONObject first = (JSONObject)statusesOfTime.get(0);
			String time = first.getString("creatTime");
			Date dt = new Date(Long.parseLong(time+"000"));
			SimpleDateFormat df=new SimpleDateFormat("MM-dd-yyyy"); 
			personStatusInfo.setStatusTime(df.format(dt));
			
			String[] picArray = new String[statusesOfTime.size()];
			String[] textArray = new String[statusesOfTime.size()];
			String[] creatTime = new String[statusesOfTime.size()];
			String[] numOfPics = new String[statusesOfTime.size()];
			String[][] bigPics = new String[statusesOfTime.size()][];
			
			for (int j = 0; j < statusesOfTime.size(); j++) {
				JSONObject status = (JSONObject)statusesOfTime.get(j);
				textArray[j] = status.getString("status");
				creatTime[j] = status.getString("creatTime");
				
				if(status.getString("resrc_type").equals("0")){
					picArray[j] = "";
					bigPics[j] = null;
				}else{
					JSONArray picUrls = status.getJSONObject("resrc").getJSONArray("picArray");
					String thumbUrl = status.getString("thumb");
					numOfPics[j] = "共有"+picUrls.size()+"张";
					picArray[j] = jsonResult.getIntValue("usrId") + "/"+thumbUrl;
					
					bigPics[j] = new String[picUrls.size()];
					for(int k=0;k<picUrls.size();k++){
						bigPics[j][k] = jsonResult.getIntValue("usrId") + "/"+picUrls.getString(k);
					}
				}
			}

			personStatusInfo.setBigPicpath(jsonResult.getString("bigPicPath"));
			personStatusInfo.setSmallPicPath(jsonResult.getString("smallPicPath"));
			personStatusInfo.setNumOfPics(numOfPics);
			personStatusInfo.setStatusPic(picArray);
			personStatusInfo.setStatusTexts(textArray);
			personStatusInfo.setCreatTime(creatTime);
			personStatusInfo.setBigPics(bigPics);

			listmap.add(personStatusInfo);
		}

		// onLoading.dismiss();
		return listmap;
	}
	
	private void updateProfile() {
		// TODO Auto-generated method stub

		TextView userNameView = (TextView) headview.findViewById(R.id.username);
		userNameView.setText(userName);
		ImageView avatar = (ImageView)headview.findViewById(R.id.headicon);
		ImageLoader.getInstance().displayImage("http://"+Constants.Server+"/famnotes/Uploads/smallPic/"+usrId+"/"+logoUrl, avatar);
		
		ImageView imageCover = (ImageView)headview.findViewById(R.id.imageCover);
		ImageLoader.getInstance().displayImage("http://"+Constants.Server+"/famnotes/Uploads/group/"+groupId+"/"+Groups.selectGrp().getCoverPhoto(), imageCover); //
				
		
	}
//	private void updateProfile() {
//		TextView userNameView = (TextView) headview.findViewById(R.id.username);
//		userNameView.setText(userName);
//		ImageView avatar = (ImageView)headview.findViewById(R.id.headicon);
//		ImageLoader.getInstance().displayImage("http://"+Constants.Server+"/famnotes/Uploads/smallPic/"+userId+"/"+logoUrl, avatar);
//		
//		ImageView imageCover = (ImageView)headview.findViewById(R.id.imageCover);
//		ImageLoader.getInstance().displayImage("http://"+Constants.Server+"/famnotes/Uploads/group/"+groupId+"/"+Groups.selectGrp().getCoverPhoto(), imageCover); //
//		
//	}	
}
