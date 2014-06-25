package com.android.famcircle.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.CustomProgressDialog;
import com.android.famcircle.R;
import com.android.famcircle.StatusListAdapter;
import com.android.famcircle.StatusListInfo;
import com.android.famcircle.StatusReplyInfo;
import com.android.famcircle.StatusZanInfo;
import com.android.famcircle.config.Constants;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.util.ACache;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class ShareActivity  extends BaseActivity {
	
	public static String userId;    //?  int userId; 
	public static String userName;
	public static String logoUrl;
	public static String groupId; //? int groupId; 
	
	private ACache mCache;
	
	String statusResult; //getStatusXXXX()  返回的json数据包
	List<HashMap<String, Object>> listMap; //statusResult的解析结果, 其中HashMap<String, Object>就是一个Status的所有结构
	ListView statuslist;
	View headview;
	PullToRefreshListView mPullRefreshListView;
	StatusListAdapter myadapter;
	public static Handler myhandler;
	String[] imageUrls;
	GridView statusPics;
	//ImageView sendStatus;
	DisplayImageOptions options;
	
	private CustomProgressDialog onLoading;
	Boolean isNeedRefresh;
	int currentMode; ///getStatusXXXX ‘s flag， 表示刷新模式，上拉 or 下拉
	
	Context context;
	PopupWindow commentPopupWindow;
	public RelativeLayout replyWindow;
	public RelativeLayout inputWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		context = this;
		mCache = ACache.get(this);
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

				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("refresh at: "+label);
				refreshView.getLoadingLayoutProxy().setRefreshingLabel(getResources().getString(R.string.pull_to_refresh_refreshing_label));
				refreshView.getLoadingLayoutProxy().setReleaseLabel(getResources().getString(R.string.pull_to_refresh_release_label));
				refreshView.getLoadingLayoutProxy().setPullLabel(getResources().getString(R.string.pull_to_refresh_pull_label));
				
				// Update the LastUpdatedLabel
				if(refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
					// Do work to refresh the list here.
					currentMode = 1;
					new GetDataTask().execute(1);
				}else if(refreshView.getCurrentMode() == Mode.PULL_FROM_END){
					currentMode = 0;
					new GetDataTask().execute(0);
				}
				
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
		
		headview = LayoutInflater.from(this).inflate(R.layout.activity_share_header, null);
		statuslist.addHeaderView(headview);
		
		//listMap = getStatusListMaps("");
		myadapter = new StatusListAdapter(this, listMap , myhandler );
		statuslist.setAdapter(myadapter);
		
		myhandler = new Handler(){
			@Override
	        public void handleMessage(Message msg) {
	            // TODO Auto-generated method stub
	            if(msg != null) {
	            	Log.i("handler ", "get a message");
	            	
	            	switch (msg.arg1) {
					case 1:
						//for onclik event
						commentPopupWindow.dismiss();
						break;
					case 2:
						//for  init the status
						onLoading.dismiss();
						listMap = getStatusListMaps(statusResult);
						Log.i("listMap length :", ""+listMap.size());
		            	myadapter.setDataList(listMap);
		            	isNeedRefresh = false;
		            	myadapter.notifyDataSetChanged();
		            	updateProfile();
						break;
					case 3:
						//for pull refresh
						List<HashMap<String, Object>> allList = new ArrayList<HashMap<String,Object>>();
						List<HashMap<String, Object>> resultList = getStatusListMaps(statusResult);
						
						if(currentMode == 0){ //上拉
							allList.addAll(listMap);
							allList.addAll(resultList);
						}else{ //下拉
							allList.addAll(resultList);
							allList.addAll(listMap);
						}
						listMap = allList;
						myadapter.setDataList(listMap);
						Log.i("listMap length :", ""+listMap.size());
						myadapter.notifyDataSetChanged();

						// Call onRefreshComplete when the list has been refreshed.
						mPullRefreshListView.onRefreshComplete();
						break;
					case 4:
						// for zan and reply
						myadapter.notifyDataSetChanged();
						break;
					case 5:
						//for status send finished;
						currentMode = 1;
						mPullRefreshListView.setRefreshing();
						break;
					default:
						//onLoading.dismiss();
						listMap = getStatusListMaps(statusResult);
						Log.i("listMap length :", ""+listMap.size());
		            	myadapter.setDataList(listMap);
		            	isNeedRefresh = false;
		            	myadapter.notifyDataSetChanged();
						break;
					}
	            }
	        }
		};
		onLoading = new CustomProgressDialog(this);
		onLoading.setCancelable(true);
		onLoading.setCanceledOnTouchOutside(true);
		isNeedRefresh = true;

//成为历史了  kx73		
//		JSONObject userProfile = mCache.getAsJSONObject("userProfile");
//		if(userProfile != null){
//			userId = userProfile.getString("usrId");
//			userName = userProfile.getString("name");
//			logoUrl = userProfile.getString("avatar");
//			groupId = userProfile.getString("grpId");
//			updateProfile();
//			Log.i("cache", "find cache usrId"+userId);
//		}else{
//			initialUserProfile();
//		}
		{
			userId =String.valueOf(User.Current.id);
			userName=User.Current.name;
			logoUrl=User.Current.avatar;
			groupId=String.valueOf(User.Current.grpId);
			updateProfile();
			Log.i("cache", "find cache usrId"+userId);
		}
		statusResult = mCache.getAsString("statusResult");
		if(statusResult == null)
			initialStatuses();
		else{
			isNeedRefresh = false;
			Message message = new Message();
			myhandler.sendMessage(message);
		}
	}

	@Override
	protected void onStart(){
		super.onStart();

		if(isNeedRefresh)
			onLoading.show();
		//myadapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.btn_send_status) {
			Intent i = new Intent(getApplicationContext(), StatusPicsSendActivity.class);
			startActivity(i);
			return true;
		}
		
		if(id==R.id.btn_group_setting){
			openActivity(SettingActivity.class);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private class GetDataTask extends AsyncTask<Integer, Void, String> {

		@Override
		protected String doInBackground(Integer... params) { //i.e. currentMode
			// Simulates a background job.
			String result = "";
			try{
			if(params[0] == 0){
				/*from end*/
				String statusId = "-1";
				if(listMap.size() != 0){
					StatusListInfo statusInfo = (StatusListInfo)listMap.get(listMap.size()-1).get("statusInfo");
					statusId = statusInfo.getStatusId();
				}
				PostData pdata=new PostData("share", "getStatusByGrpId", "{\"grpId\":"+User.Current.grpId+",\"statusId\":"+statusId+",\"flag\":0}");
				result=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
			}else if(params[0] == 1){
				/*from start*/
				String statusId = "-1";
				if(listMap.size() != 0){
					StatusListInfo statusInfo = (StatusListInfo)listMap.get(0).get("statusInfo");
					statusId = statusInfo.getStatusId();
				}
				PostData pdata=new PostData("share", "getStatusByGrpId", "{\"grpId\":"+User.Current.grpId+",\"statusId\":"+statusId+",\"flag\":1}");
				result=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
			}
			
			Log.i("refresh data :", result);
			statusResult = result;
			//listMap = getStatusListMaps(json);
			//onLoading.dismiss();
			Message message = new Message();
			message.arg1 = 3;
			myhandler.sendMessage(message);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return result;
		}

	}
	
//	private void initialUserProfile() {
//		
//		new AsyncTask<String, String, String >() {
//			
//			@Override
//			protected String doInBackground(String... params) {
//				// TODO Auto-generated method stub
//				try{
//				PostData pdata=new PostData("share", "getUsrByUsrId", "{\"usrId\":1}");
//				String json=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata);
//				Log.i("initialUserProfile  :", json);
//				
//				JSONObject jsonResult = JSON.parseObject(json);
//				JSONArray tmpArray = jsonResult.getJSONArray("results");
//				if(jsonResult.getInteger("errCode") == 0) {
//					JSONObject userProfile = (JSONObject) tmpArray.get(0);
//					userId = userProfile.getString("usrId");
//					userName = userProfile.getString("name");
//					logoUrl = userProfile.getString("avatar");
//					groupId = userProfile.getString("grpId");
//					Log.i("groupId", groupId);
//					mCache.put("userProfile", userProfile);
//				}
//				}catch(Exception ex){
//					ex.printStackTrace();
//				}
//				return null;
//			}
//		}.execute("");
//	}

	private void initialStatuses() {
		new AsyncTask<String, String, String >() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				statusResult = "";
				try{
				PostData pdata=new PostData("share", "getStatusByGrpId", "{\"grpId\":"+User.Current.grpId+"}");
				String json=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata);
				//Log.i("initialStatuses  :", json);

				statusResult = json;
				//listMap = getStatusListMaps(json);
				//onLoading.dismiss();
				JSONObject allResult = JSON.parseObject(json);

				if(allResult.getInteger("errCode") == 0){
					mCache.put("statusResult", statusResult);
				}
				Message message = new Message();
				message.arg1 = 2;
				myhandler.sendMessage(message);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				return null;
			}
		}.execute("");
	}
	private List<HashMap<String, Object>> getStatusListMaps(String string) {
		// TODO Auto-generated method stub

		//initialStatuses();
		JSONObject allResult = JSON.parseObject(string);
		JSONObject jsonResult = allResult.getJSONObject("results");
		
		if(string == null || string.length() == 0 || allResult.getInteger("errCode") != 0){
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
	
	private void updateProfile() {
		TextView userNameView = (TextView) headview.findViewById(R.id.username);
		userNameView.setText(userName);
		ImageView avatar = (ImageView)headview.findViewById(R.id.headicon);
		ImageLoader.getInstance().displayImage("http://"+Constants.Server+"/famnotes/Uploads/smallPic/"+userId+"/"+logoUrl, avatar);
		
		ImageView imageCover = (ImageView)headview.findViewById(R.id.imageCover);
		ImageLoader.getInstance().displayImage("http://"+Constants.Server+"/famnotes/Uploads/group/"+groupId+"/"+Groups.selectGrp().getCoverPhoto(), imageCover); //
		
	}
}
