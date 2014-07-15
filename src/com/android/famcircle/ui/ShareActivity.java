package com.android.famcircle.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
//import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
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
import com.famnotes.android.util.CustomProgressDialog;
import com.android.famcircle.R;
import com.android.famcircle.StatusListAdapter;
import com.android.famcircle.StatusListInfo;
import com.android.famcircle.StatusReplyInfo;
import com.android.famcircle.StatusZanInfo;
import com.android.famcircle.config.Constants;
import com.android.famcircle.config.RequestCode;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.android.famcircle.orderlist.OrderStatusListActivity;
import com.android.famcircle.picselect.PublishedActivity;
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
	
	public static int showNum ;
	public static Boolean isNeedRefresh;
	private ACache mCache;
	
	String statusResult; //getStatusXXXX()  返回的json数据包
	List<HashMap<String, Object>> listMap; //statusResult的解析结果, 其中HashMap<String, Object>就是一个Status的所有结构
	HashMap<String, Object> newZanAndReplyOfHistory;
	HashMap<Integer,Integer> indexMap;
	ListView statuslist;
	View headview;
	PullToRefreshListView mPullRefreshListView;
	StatusListAdapter myadapter;
	public static ShareHandler myhandler, myhandlerPull;
	String[] imageUrls;
	GridView statusPics;
	//ImageView sendStatus;
	DisplayImageOptions options;
	
//	private CustomProgressDialog onLoading;
	int currentMode; ///getStatusXXXX ‘s flag， 表示刷新模式，上拉 or 下拉
	
	Context context;
	PopupWindow commentPopupWindow;
	public RelativeLayout replyWindow;
	public RelativeLayout inputWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		context = this;
		mCache = ACache.get(this);
		showNum = 8;
		
		if(User.Current==null)
			User.Current=mCache.getAsObject("User.Current");
		if(Groups.lGroup==null || Groups.lGroup.isEmpty()){
			Groups.selectIdx=mCache.getAsObject("Groups.selectIdx");
			Groups.lGroup=mCache.getAsObject("Groups.lGroup");
		}
		
		setTitle(Groups.selectGrp().name);
			
		myhandler = new ShareHandler(this, true); myhandlerPull= new ShareHandler(this, false);
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
					GetDataTask getDataTask = new GetDataTask();
					getDataTask.connect(myhandlerPull);
					getDataTask.execute(1);
				}else if(refreshView.getCurrentMode() == Mode.PULL_FROM_END){
					currentMode = 0;
					
					GetDataTask getDataTask = new GetDataTask();
					getDataTask.connect(myhandlerPull);
					getDataTask.execute(0);
				}
				
			}
		});

		// Add an end-of-list listener
//		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
//
//			@Override
//			public void onLastItemVisible() {
//				Toast.makeText(ShareActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
//			}
//		});
				
		statuslist = mPullRefreshListView.getRefreshableView();
		
		headview = LayoutInflater.from(this).inflate(R.layout.activity_share_header, null);
		statuslist.addHeaderView(headview);
		
		//listMap = getStatusListMaps("");
		myadapter = new StatusListAdapter(this, listMap );
		statuslist.setAdapter(myadapter);
		
//		onLoading = new CustomProgressDialog(this);
//		onLoading.setCancelable(true);
//		onLoading.setCanceledOnTouchOutside(true);
		isNeedRefresh = false;

		//init user profile ;
		userId =String.valueOf(User.Current.id);
		userName=User.Current.name;
		logoUrl=User.Current.avatar;
		groupId=String.valueOf(User.Current.grpId);
		updateProfile();
		Log.i("cache", "find cache usrId"+userId);

		listMap = (List<HashMap<String, Object>>) mCache.getAsObject("statusResult"+User.Current.grpId+"---"+User.Current.id);
		if(listMap == null){
			InitialStatuses initial = new InitialStatuses();
			initial.connect(myhandler);
			initial.execute("");
		}else{
//			listMap = getStatusListMaps(statusResult);
			Log.i("listMap length :", ""+listMap.size());
        	myadapter.setDataList(listMap);
        	isNeedRefresh = false;
        	myadapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onStart(){
		super.onStart();

//		if(isNeedRefresh)
//			onLoading.show();
//		myadapter.notifyDataSetChanged();
		if(isNeedRefresh)
			listNotifyDataSetChanged();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(isNeedRefresh)
			listNotifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		Log.i("onresult ", ""+Constants.publishResult);
		switch(requestCode){
		
			case RequestCode.GroupSetting :
				//? 如何刷新界面，如组的封面、个人头像、新增的组成员
				logoUrl=User.Current.getAvatar();
				updateProfile();
				break;
			case RequestCode.RefreshStatusByPull:
				if(Constants.publishResult == 0){
					listNotifyDataSetChanged();
				}else if(Constants.publishResult == -1)
					Toast.makeText(
							this, "上传取消！", Toast.LENGTH_SHORT).show();
				break;
			default :
				if(resultCode == RESULT_OK){
					Log.i("publish success", "!!!!!");
					listNotifyDataSetChanged();
				}
				break;
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.btn_send_status) {
			Constants.publishResult = -2;
			Intent i = new Intent(getApplicationContext(), PublishedActivity.class);
			startActivityForResult(i, RequestCode.RefreshStatusByPull);//startActivity(i);
			return true;
		}else if(id == R.id.orderbytime){
			Intent i = new Intent(getApplicationContext(), OrderStatusListActivity.class);
			startActivity(i);
			return true;
		}else if(id==R.id.btn_group_setting){ //群设置
			Bundle pBundle = new Bundle();  pBundle.putInt("GroupId",  User.Current.grpId); 
			openActivityForResult(GroupSettingActivity.class, pBundle, RequestCode.GroupSetting);
			return true;
		}else if(id == android.R.id.home){
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	class GetDataTask  extends BaseAsyncTask<ShareActivity, Integer, Integer>{
		@Override
		public Integer run(Integer... reqJsonMsg) throws Exception {
			// Simulates a background job.
			String result = "";
			try{
			if(reqJsonMsg[0] == 0){
				/*from end*/
				
				if(listMap.size() > showNum){
					showNum = 2*showNum;
					return 6;
				}
				
				String statusId = "-1";
				if(listMap.size() != 0){
					StatusListInfo statusInfo = (StatusListInfo)listMap.get(listMap.size()-1).get("statusInfo");
					statusId = statusInfo.getStatusId();
				}
				PostData pdata=new PostData("share", "getStatusByGrpId", "{\"grpId\":"+User.Current.grpId+",\"statusId\":"+statusId+",\"flag\":0}");
				result=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
			}else if(reqJsonMsg[0] == 1){
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

			}catch(Exception ex){
				ex.printStackTrace();
			}
			return 3;
		}
	}
	
	class InitialStatuses  extends BaseAsyncTask<ShareActivity, String, Integer>{
		@Override
		public Integer run(String... reqJsonMsg) throws Exception {
				statusResult = "";
				try{
					PostData pdata=new PostData("share", "getStatusByGrpId", "{\"grpId\":"+User.Current.grpId+"}");
					String json=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata);
		
					statusResult = json;
					JSONObject allResult = JSON.parseObject(json);
		
//					if(allResult.getInteger("errCode") == 0){
//						mCache.put("statusResult"+User.Current.grpId+"---"+User.Current.id, statusResult);
//					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
				return 2;
		}
	}
	
	class ShareHandler extends BaseAsyncTaskHandler<ShareActivity, Integer>{

		public ShareHandler(ShareActivity shareActivity, boolean showProgressBar) {
			super(shareActivity, showProgressBar);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public boolean onTaskFailed(ShareActivity context, Exception ex) {
			// TODO Auto-generated method stub
			context.DisplayLongToast(ex.toString());
			return true;
		}

		@Override
		public boolean onTaskSuccess(ShareActivity arg0, Integer rCode) {
			// TODO Auto-generated method stub
			switch(rCode){
					case 1:
						//for onclik event
						commentPopupWindow.dismiss();
						break;
					case 2:
						//for  init the status
//						onLoading.dismiss();
						listMap = getStatusListMaps(statusResult);
						mCache.put("statusResult"+User.Current.grpId+"---"+User.Current.id, (Serializable)listMap);
						
						updateIndexMap(listMap);
						updateNewZanAndReply();
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
						showNum = 2*showNum;
						
						mCache.put("statusResult"+User.Current.grpId+"---"+User.Current.id, (Serializable)listMap);
						updateIndexMap(listMap);
						updateNewZanAndReply();
						myadapter.setDataList(listMap);
						Log.i("listMap length :", ""+listMap.size());
						myadapter.notifyDataSetChanged();
		
						// Call onRefreshComplete when the list has been refreshed.
						mPullRefreshListView.onRefreshComplete();
						break;
					case 4:
						// for zan and reply
						notifyDataSetChanged() ; //myadapter.notifyDataSetChanged();
						break;
					case 5:
						//for status send finished;
						listNotifyDataSetChanged();
						break;
					case 6:
						mPullRefreshListView.onRefreshComplete();
						notifyDataSetChanged() ; 
						break;
					default:
//						onLoading.dismiss();
						listMap = getStatusListMaps(statusResult);
						updateIndexMap(listMap);
						Log.i("listMap length :", ""+listMap.size());
		            	myadapter.setDataList(listMap);
		            	isNeedRefresh = false;
		            	myadapter.notifyDataSetChanged();
						break;	
			}
			return true;
		}
	}

	public void notifyDataSetChanged(){
		if(myadapter!=null)
			myadapter.notifyDataSetChanged();
	}
	
	public void updateNewZanAndReply() {
		// TODO Auto-generated method stub
		List<StatusReplyInfo> replyList = (List<StatusReplyInfo>) newZanAndReplyOfHistory.get("replyList");
		
		if(replyList != null){
			for(int i=0;i<replyList.size();i++){
				StatusReplyInfo info = replyList.get(i);
				int index = indexMap.get(info.getStatusId());
				((List<StatusReplyInfo>)listMap.get(index).get("replyinfo")).add(info);
			}
		}
		
		List<StatusZanInfo> zanList = (List<StatusZanInfo>) newZanAndReplyOfHistory.get("zanList");
		if(zanList != null){
			for(int i=0;i<replyList.size();i++){
				StatusZanInfo info = zanList.get(i);
				int index = indexMap.get(info.getStatusId());
				((List<StatusZanInfo>)listMap.get(index).get("zaninfo")).add(info);
			}
		}
	}

	public void updateIndexMap(List<HashMap<String, Object>> list) {
		// TODO Auto-generated method stub
		if(indexMap == null){
			indexMap = new HashMap<Integer, Integer>();
		}
		
		for(int i=0;i<list.size();i++){
			StatusListInfo info = (StatusListInfo) list.get(i).get("statusInfo");
			indexMap.put(Integer.parseInt(info.getStatusId()), i);
		}
	}
	
	public void updateZanAndReply(){
		
	}

	public void listNotifyDataSetChanged(){
		currentMode = 1;
		mPullRefreshListView.setRefreshing();
		isNeedRefresh = false;
		Log.i("send status need refresh", "");
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
				reply.setStatusId(info.getStatusId());
				replyList.add(reply);
			}
			map.put("replyinfo", replyList);
			
			listmap.add(map);
		}
		
		JSONArray replys = allResult.getJSONArray("replys");
		JSONArray zans = allResult.getJSONArray("zans");
		if(newZanAndReplyOfHistory == null)
			newZanAndReplyOfHistory = new HashMap<String, Object>();
		else
			newZanAndReplyOfHistory.clear();
		if(replys != null){
			 List<StatusReplyInfo> replyList = new ArrayList<StatusReplyInfo>();
			 for(int i=0;i<replys.size();i++){
				 StatusReplyInfo reply = new StatusReplyInfo();
				 reply.setFromUsrId(replys.getJSONObject(i).getString("fromUsrId"));
				 reply.setFromUsrName(replys.getJSONObject(i).getString("fromUsrName"));
				 reply.setToUsrId(replys.getJSONObject(i).getString("toUsrId"));
				 reply.setToUsrName(replys.getJSONObject(i).getString("toUsrName"));
				 reply.setReply(replys.getJSONObject(i).getString("reply"));
				 reply.setStatusId(replys.getJSONObject(i).getString("id"));
				 replyList.add(reply);
			 }
			 
			 newZanAndReplyOfHistory.put("replyList", replyList);
		}
		
		if(zans != null){
			 List<StatusZanInfo> zanList = new ArrayList<StatusZanInfo>();
			 for(int i=0;i<zans.size();i++){
				 StatusZanInfo zan = new StatusZanInfo();
				 zan.setFromUsrId(zans.getJSONObject(i).getString("fromUsrId"));
				 zan.setFromUsrName(zans.getJSONObject(i).getString("fromUsrName"));
				 zan.setStatusId(zans.getJSONObject(i).getString("id"));
				 zanList.add(zan);
			 }
			 
			 newZanAndReplyOfHistory.put("zanList", zanList);
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
