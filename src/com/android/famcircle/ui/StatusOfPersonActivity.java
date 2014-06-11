package com.android.famcircle.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.AppManager;
import com.android.famcircle.CustomProgressDialog;
import com.android.famcircle.R;
import com.android.famcircle.StatusListInfo;
import com.android.famcircle.StatusOfPersonListAdapter;
import com.android.famcircle.StatusOfPersonListInfo;
import com.android.famcircle.util.FNHttpRequest;
import com.android.famcircle.util.PostData;
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
	private Handler myhandler;
	private String usrId ;
	private String userName;
	private String logoUrl;
	private String groupId;
	private View headview;;
	private CustomProgressDialog onLoading;
	private boolean isNeedRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_status);

		Bundle info = this.getIntent().getExtras();
		usrId = info.getString("usrId");

		AppManager.getInstance().addActivity(this);
		context = this;
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

						new GetDataTask().execute();
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
				R.layout.activity_share_header, null);
		statuslist.addHeaderView(headview);
		
		myAdapter = new StatusOfPersonListAdapter(context,listMap );
		statuslist.setAdapter(myAdapter);
		
		myhandler = new Handler(){
			@Override
	        public void handleMessage(Message msg) {
	            // TODO Auto-generated method stub
	            if(msg != null) {
	            	Log.i("handler ", "get a message");
	            	
	            	switch (msg.arg1) {
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
					default:
						onLoading.dismiss();
						listMap = getStatusListMaps(statusResult);
						//Log.i("listMap length :", ""+listMap.size());
		            	myAdapter.setDataList(listMap);
		            	isNeedRefresh = false;
		            	myAdapter.notifyDataSetChanged();
		            	updateProfile();
						break;
	            	}
	            }
			}
		};
		
		
		onLoading = new CustomProgressDialog(this);
		onLoading.setCancelable(true);
		onLoading.setCanceledOnTouchOutside(true);
		isNeedRefresh = true;
		
		initialUserProfile();
		initialStatuses();

	}

	@Override
	protected void onStart(){
		super.onStart();

		if(isNeedRefresh)
			onLoading.show();
		//myadapter.notifyDataSetChanged();
	}
	
	private class GetDataTask extends AsyncTask<Integer, Void, String> {

		@Override
		protected String doInBackground(Integer... params) {
			// Simulates a background job.
			String result = "";
			StatusOfPersonListInfo personStatusInfo = (StatusOfPersonListInfo) listMap.get(listMap.size()-1);
			String[] creatTime = personStatusInfo.getCreatTime();
			String lastCreatTime = creatTime[creatTime.length-1];
			PostData pdata=new PostData("share", "getStatusByUsrId","{\"usrId\":"+usrId+ ", \"type\":"+0+", \"creatTime\":"+lastCreatTime+"}");
			result=new FNHttpRequest().doPost(pdata).trim();
			
			Log.i("refresh data :", result);
			statusResult = result;
			//listMap = getStatusListMaps(json);
			//onLoading.dismiss();
			Message message = new Message();
			message.arg1 = 1;
			myhandler.sendMessage(message);
			return result;
		}
	}
	
	private void initialStatuses() {
		new AsyncTask<String, String, String >() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				statusResult = "";
				PostData pdata=new PostData("share","getStatusByUsrId","{\"usrId\":"+usrId+ ", \"type\":"+0+"}");
				String json=new FNHttpRequest().doPost(pdata);
				//Log.i("initialStatuses  :", json);

				statusResult = json;
				//listMap = getStatusListMaps(json);
				//onLoading.dismiss();
				Message message = new Message();
				message.arg1 = 0;
				myhandler.sendMessage(message);
				return null;
			}
		}.execute("");
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
			String[][] bigPics = new String[statusesOfTime.size()][];
			
			for (int j = 0; j < statusesOfTime.size(); j++) {
				JSONObject status = (JSONObject)statusesOfTime.get(j);
				
				if(status.getString("resrc_type").equals("0")){
					picArray[j] = "";
					bigPics[j] = null;
				}else{
					JSONArray picUrls = status.getJSONObject("resrc").getJSONArray("picArray");
					picArray[j] = jsonResult.getIntValue("usrId") + "/"+picUrls.getString(0);
					
					bigPics[j] = new String[picUrls.size()];
					for(int k=0;k<picUrls.size();k++){
						bigPics[j][k] = jsonResult.getIntValue("usrId") + "/"+picUrls.getString(k);
					}
				}
				textArray[j] = status.getString("status");
				
				creatTime[j] = status.getString("creatTime");
			}

			personStatusInfo.setBigPicpath(jsonResult.getString("bigPicPath"));
			personStatusInfo.setSmallPicPath(jsonResult.getString("smallPicPath"));
			personStatusInfo.setStatusPic(picArray);
			personStatusInfo.setStatusTexts(textArray);
			personStatusInfo.setCreatTime(creatTime);
			personStatusInfo.setBigPics(bigPics);

			listmap.add(personStatusInfo);
		}

		// onLoading.dismiss();
		return listmap;
	}
	
	private void initialUserProfile() {
		// TODO Auto-generated method stub

		new AsyncTask<String, String, String >() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				
				PostData pdata=new PostData("share", "getUsrByUsrId", "{\"usrId\":"+usrId+"}");
				String json=new FNHttpRequest().doPost(pdata);
				Log.i("initialUserProfile  :", json);
				
				JSONObject jsonResult = JSON.parseObject(json);
				JSONArray tmpArray = jsonResult.getJSONArray("results");
				if(jsonResult.getInteger("errCode") == 0) {
					JSONObject userProfile = (JSONObject) tmpArray.get(0);
					userName = userProfile.getString("name");
					logoUrl = userProfile.getString("avatar");
					groupId = userProfile.getString("grpId");
					Log.i("groupId", groupId);
				}
				return null;
			}
		}.execute("");
	}
	
	private void updateProfile() {
		// TODO Auto-generated method stub

		TextView userNameView = (TextView) headview.findViewById(R.id.username);
		userNameView.setText(userName);
		ImageView avatar = (ImageView)headview.findViewById(R.id.headicon);
		ImageLoader.getInstance().displayImage("http://114.215.180.229/famnotes/Uploads/smallPic/"+usrId+"/"+logoUrl, avatar);
		
	}
}
