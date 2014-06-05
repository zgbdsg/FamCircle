package com.android.famcircle.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.AppManager;
import com.android.famcircle.R;
import com.android.famcircle.StatusListAdapter;
import com.android.famcircle.StatusListInfo;
import com.android.famcircle.StatusReplyInfo;
import com.android.famcircle.StatusZanInfo;
import com.android.famcircle.util.FNHttpRequest;
import com.android.famcircle.util.PostData;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;


public class ShareActivity  extends BaseActivity {
	
	String userId;
	String userName;
	String logoUrl;
	String groupId;
	
	String statusResult;
	List<HashMap<String, Object>> listMap;
	ListView statuslist;
	PullToRefreshListView mPullRefreshListView;
	StatusListAdapter myadapter;
	Handler myhandler;
	String[] imageUrls;
	GridView statusPics;
	ImageView sendStatus;
	DisplayImageOptions options;
	
	ProgressDialog onLoading;
	Boolean isNeedRefresh;
	
	Context context;
	PopupWindow commentPopupWindow;
	public RelativeLayout replyWindow;
	public RelativeLayout inputWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		AppManager.getInstance().addActivity(this);
		context = this;
		Log.i("activity ", "shared!!!!!!");
		replyWindow = (RelativeLayout)findViewById(R.id.relative_pop_up_input_window);
		replyWindow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputWindow.getWindowToken(), 0);
				inputWindow.setVisibility(View.INVISIBLE);
				replyWindow.setVisibility(View.INVISIBLE);
				
			}
		});
		inputWindow = (RelativeLayout)findViewById(R.id.pop_up_input_window);
		mPullRefreshListView = (PullToRefreshListView)findViewById(R.id.statuslist);
		mPullRefreshListView.setMode(Mode.BOTH);
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("refresh at: "+label);
				refreshView.getLoadingLayoutProxy().setRefreshingLabel(getResources().getString(R.string.pull_to_refresh_refreshing_label));
				refreshView.getLoadingLayoutProxy().setReleaseLabel(getResources().getString(R.string.pull_to_refresh_release_label));
				refreshView.getLoadingLayoutProxy().setPullLabel(getResources().getString(R.string.pull_to_refresh_pull_label));
				
				// Do work to refresh the list here.
				new GetDataTask().execute();
			}
		});

		// Add an end-of-list listener
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				Toast.makeText(ShareActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
			}
		});
				
		statuslist = mPullRefreshListView.getRefreshableView();

		initialUserProfile();
		initialStatuses();
		
		View headview = LayoutInflater.from(this).inflate(R.layout.activity_share_header, null);
		statuslist.addHeaderView(headview);
		
		//listMap = getStatusListMaps("");
		myadapter = new StatusListAdapter(this, listMap);
		statuslist.setAdapter(myadapter);
		
		myhandler = new Handler(){
			@Override
	        public void handleMessage(Message msg) {
	            // TODO Auto-generated method stub
	            if(msg != null) {
	            	Log.i("handler ", "get a message");
	            	
	            	switch (msg.arg1) {
					case 1:
						commentPopupWindow.dismiss();
						break;

					default:
						listMap = getStatusListMaps("");
		            	myadapter.setDataList(listMap);
		            	Log.i("data", listMap.get(0).get("statusInfo").toString());
		            	onLoading.dismiss();
		            	isNeedRefresh = false;
		            	myadapter.notifyDataSetChanged();
						break;
					}
	            }
	        }
		};
		onLoading = new ProgressDialog(this);
		onLoading.setCancelable(true);
		onLoading.setCanceledOnTouchOutside(true);
		isNeedRefresh = true;
		
		sendStatus = (ImageView)findViewById(R.id.btn_send_status);
		sendStatus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(), StatusPicsSendActivity.class);
				startActivity(i);
				//Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
				//startActivityForResult(i, 200);
			}
		});
	}

	@Override
	protected void onStart(){
		super.onStart();

		if(isNeedRefresh)
			onLoading.show();
		//myadapter.notifyDataSetChanged();
	}
	

	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			//listMap.add(null);
			myadapter.notifyDataSetChanged();

			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();

			super.onPostExecute(result);
		}
	}
	
	public void sendZan(final String fromUsrId, final String toUsrId ,final String statusId){
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				Log.i("zan info:", fromUsrId+"  to "+toUsrId +" int status "+statusId);
				PostData pdata=new PostData("share", "postReply", "{\"statusId\":"+statusId+ ", \"fromUsrId\":"+fromUsrId+", \"toUsrId\":"+toUsrId+", \"type\":1, \"reply\":\"\"}");
				String json=new FNHttpRequest().doPost(pdata).trim();
				Message msg = new Message();
				msg.arg1 = 1;
				myhandler.sendMessage(msg);
				System.out.println(json);

				return null;
			}
		}.execute("");
	}
	
	private void initialUserProfile() {
		// TODO Auto-generated method stub

		new AsyncTask<String, String, String >() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				
				PostData pdata=new PostData("share", "getUsrByUsrId", "{\"usrId\":1}");
				String json=new FNHttpRequest().doPost(pdata);
				Log.i("initialUserProfile  :", json);
				
				JSONObject jsonResult = JSON.parseObject(json);
				JSONArray tmpArray = jsonResult.getJSONArray("results");
				if(jsonResult.getInteger("errCode") == 0) {
					JSONObject userProfile = (JSONObject) tmpArray.get(0);
					userId = userProfile.getString("usrId");
					userName = userProfile.getString("name");
					logoUrl = userProfile.getString("avatar");
					groupId = userProfile.getString("groupId");
				}
				return null;
			}
		}.execute("");
	}

	private void initialStatuses() {
		new AsyncTask<String, String, String >() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				statusResult = "";
				PostData pdata=new PostData("share", "getStatusByGrpId", "{\"grpId\":1}");
				String json=new FNHttpRequest().doPost(pdata);
				//Log.i("initialStatuses  :", json);

				statusResult = json;
				//listMap = getStatusListMaps(json);
				//onLoading.dismiss();
				Message message = new Message();
				message.arg1 = 2;
				myhandler.sendMessage(message);
				return null;
			}
		}.execute("");
	}
	private List<HashMap<String, Object>> getStatusListMaps(String string) {
		// TODO Auto-generated method stub

		//initialStatuses();
		JSONObject allResult = JSON.parseObject(statusResult);
		JSONObject jsonResult = allResult.getJSONObject("results");
		
		if(statusResult == null || statusResult.length() == 0 || allResult.getInteger("errCode") != 0){
			return null;
		}
				
		JSONArray statusArray = jsonResult.getJSONArray("status");
		List<HashMap<String,Object>> listmap = new ArrayList<HashMap<String,Object>>();
		
		for(int i=0;i<statusArray.size();i ++){
			HashMap<String,Object> map= new HashMap<String, Object>();
			JSONObject status = statusArray.getJSONObject(i);
			
			StatusListInfo info = new StatusListInfo();
			info.setStatusId(status.getString("statusId"));
			info.setName(status.getString("name"));
			info.setAvatar(status.getString("usrId")+"/"+status.getString("avatar"));
			info.setCreatTime(status.getString("creatTime"));
			info.setStatus(status.getString("status"));
			info.setUsrId(status.getString("usrId"));
			info.setResrc_type(status.getString("resrc_type"));
			info.setBigPicPath(jsonResult.getString("bigPicPath"));
			info.setSmallPicPath(jsonResult.getString("smallPicPath"));
			
			JSONArray picUrls = status.getJSONObject("resrc").getJSONArray("picArray");
			String[] picArray = new String[picUrls.size()];
			for(int a=0;a<picUrls.size();a ++){
				picArray[a] = status.getString("usrId")+"/"+picUrls.getString(a);
			}
			
			info.setPicArray(picArray);
			map.put("statusInfo", info);
			
			JSONArray zanInfo = status.getJSONArray("zan");
			//Log.i("json zan Len:", ""+zanInfo.size()+"");
			List<StatusZanInfo> zanList = new ArrayList<StatusZanInfo>();
			for(int a=0;a<zanInfo.size();a ++){
				StatusZanInfo zan = new StatusZanInfo();
				zan.setFromUsrId(zanInfo.getJSONObject(a).getString("fromUsrId"));
				zan.setFromUsrName(zanInfo.getJSONObject(a).getString("fromUsrName"));
				zanList.add(zan);
			}
			map.put("zaninfo", zanList);
			
			JSONArray replyInfo = status.getJSONArray("reply");
			//Log.i("json reply Len:", ""+replyInfo.size()+"");
			List<StatusReplyInfo> replyList = new ArrayList<StatusReplyInfo>();
			for(int a=0;a<replyInfo.size();a ++){
				StatusReplyInfo reply = new StatusReplyInfo();
				reply.setFromUsrId(replyInfo.getJSONObject(a).getString("fromUsrId"));
				reply.setFromUsrName(replyInfo.getJSONObject(a).getString("fromUsrName"));
				reply.setToUsrId(replyInfo.getJSONObject(a).getString("toUsrId"));
				reply.setToUsrName(replyInfo.getJSONObject(a).getString("toUsrName"));
				reply.setReply(replyInfo.getJSONObject(a).getString("reply"));
				replyList.add(reply);
			}
			map.put("replyinfo", replyList);
			
			listmap.add(map);
		}
		
		//onLoading.dismiss();
		return listmap;
	}
	
}
