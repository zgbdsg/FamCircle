package com.android.famcircle.ui;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.Constants;
import com.android.famcircle.linearlistview.LinearListView;
import com.famnotes.android.base.AppManager;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.boot.RegisterGroup;
import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.ACache;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.MyReceiver;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;
import com.nostra13.universalimageloader.core.ImageLoader;
//import android.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;


import com.readystatesoftware.viewbadger.BadgeView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {
	
	public static boolean isForeground = false;
	//for receive customer msg from jpush server
	
	private LinearListView lvGroup;
	private static ListViewGroupAdapter lvGroupAdaper;
	private List<Group> infos;
	private HashMap<Integer,Integer> msgNumber;
	
	public static Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			Log.i("main activity ", "refresh");
			lvGroupAdaper.notifyDataSetChanged();
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        Log.i("main activity ", "create");
		try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
		
		mCache = ACache.get(this);
		if(User.Current==null)
			User.Current=mCache.getAsObject("User.Current");
		if(Groups.lGroup==null || Groups.lGroup.isEmpty()){
			Groups.lGroup=mCache.getAsObject("Groups.lGroup");
		}
		
		Set<String> tags = new HashSet<String>();
		for(int i=0;i<Groups.lGroup.size();i++){
			tags.add(""+Groups.lGroup.get(i).grpId);
		}
		
		Boolean isSetAliasAndTags = mCache.getAsObject("isSetAliasAndTags");
		if(isSetAliasAndTags == null || isSetAliasAndTags == false){
			JPushInterface.setAliasAndTags(this, ""+User.Current.loginId, tags, new TagAliasCallback() {
				
				@Override
				public void gotResult(int arg0, String arg1, Set<String> arg2) {
					// TODO Auto-generated method stub
					mCache.put("isSetAliasAndTags", true);
				}
			});
		}
		
		Log.i("base path", getFilesDir().getPath());
		String baseFilePath  = getFilesDir().getPath()+"/zip";
		String compressFilepath = getFilesDir().getPath()+"/compress";
		
		File dir = new File(baseFilePath);
		if(!dir.exists())
			dir.mkdirs();
		
		dir = new File(compressFilepath);
		if(!dir.exists())
			dir.mkdirs();
		
		init();
	}

	
	private void init() {
		// TODO Auto-generated method stub
		
		infos=new ArrayList<Group>(8);
		infos.addAll(Groups.lGroup);
		Group pesudoGrp=new Group(-1, "+",  null);
		infos.add(pesudoGrp);
		
    	lvGroup = (LinearListView) findViewById(R.id.group_listview); // inflater.inflate(R.id.group_listview, container, false);
    	lvGroupAdaper=new ListViewGroupAdapter((LayoutInflater)LayoutInflater.from(this),this);
		lvGroup.setAdapter(lvGroupAdaper);
		isForeground = true;
		
	}

	
	@Override
	protected void onResume() {
		isForeground = true;
		Log.i("main activity ", "onResume true");
		super.onResume();
//		Intent intent = getIntent();
//		Bundle bundle = intent.getExtras();
//		Boolean hasMsg = false;
//		if(bundle != null)
//			hasMsg = bundle.getBoolean("push", false);
//		Log.i("main activity ", "hasMsg "+hasMsg);
//		if(hasMsg){
		mCache = ACache.get(this);
		if(User.Current==null)
			User.Current=mCache.getAsObject("User.Current");
		Groups.lGroup=mCache.getAsObject("Groups.lGroup");
		lvGroupAdaper.notifyDataSetChanged();
//		}
			
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		isForeground = false;
		Log.i("main activity ", "onStop false");
		super.onStop();
	}

//
//	@Override
//	protected void onPause() {
//		isForeground = false;
//		super.onPause();
//	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}else if(id == R.id.action_clearcache){
			mCache.clear();
		}
		
		if (id == R.id.menu_exit) {
			new AlertDialog.Builder(this).setTitle("").setMessage("Exit？")
			.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AppManager.getInstance().AppExit(getApplicationContext());
					ImageLoader.getInstance().clearMemoryCache();
				}
			})
			.setNegativeButton("CANCEL", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			})
			.show();
		}

		if (id ==R.id.menu_clear) {
		  new AlertDialog.Builder(this).setTitle("Delete User Data").setMessage("Delete User Data, then Exit")
			.setPositiveButton("OK",  new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							DBUtil.clearDatabase();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						AppManager.getInstance().AppExit(getApplicationContext());//System.exit(0);
					}
				})
			.setNegativeButton("CANCEL", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
			.show();
		}
		return super.onOptionsItemSelected(item);
	}

/**
 * 
 * @author zgb
 *
 */

    
	class ListViewGroupAdapter  extends BaseAdapter {
		private LayoutInflater inflater;
		private Context context;
		public ListViewGroupAdapter(LayoutInflater inflater,Context context) {
			// TODO Auto-generated constructor stub
			this.inflater = inflater;
			this.context = context;
		}

		@Override
		public int getCount() {
			int size = infos.size()/2;
			if(size*2 != infos.size())
				size = size + 1;
			return size;
		}
	
		@Override
		public Object getItem(int position) {
			return infos.get(position);
		}
	
		@Override
		public long getItemId(int position) {
			return position;
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
//			Group info = infos.get(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.fam_group_item, parent,false);
				holder.tv_name_left = (TextView) convertView.findViewById(R.id.name_left);
				holder.tv_name_right = (TextView) convertView.findViewById(R.id.name_right);
				holder.tv_image_left = (CircleImageView)convertView.findViewById(R.id.group_image_left);
				holder.tv_image_right = (CircleImageView)convertView.findViewById(R.id.group_image_right);
				holder.tv_left_block = (LinearLayout) convertView.findViewById(R.id.left_block);
				holder.tv_right_block = (LinearLayout) convertView.findViewById(R.id.right_block);
				holder.leftBadge =  (TextView) convertView.findViewById(R.id.push_left);
				holder.rightBadge =  (TextView) convertView.findViewById(R.id.push_right);
//				holder.leftBadge =  new BadgeView(context, holder.tv_left_block);
//				holder.leftBadge.setTextSize(16);
//				holder.leftBadge.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//				holder.leftBadge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
//				holder.rightBadge = new BadgeView(context, holder.tv_right_block);
//				holder.rightBadge.setTextSize(16);
//				holder.rightBadge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
//				holder.rightBadge.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_name_left.setTag(position*2);
			holder.tv_name_right.setTag(position*2+1);
			holder.tv_image_right.setTag(position*2+1);
			holder.tv_image_left.setTag(position*2);
			holder.tv_left_block.setTag(position*2);
			holder.tv_right_block.setTag(position*2+1);
			
//			BadgeView leftBadge = new BadgeView(context, holder.tv_left_block);
//			BadgeView rightBadge = new BadgeView(context, holder.tv_right_block);
			
			/*set the click listener*/
			View.OnClickListener listener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int id = (Integer) v.getTag();
					Group selGrp=infos.get(id);
					if( "+".equals(selGrp.name) ){
						openActivity(RegisterGroup.class);
						return; //?
					}
					
					//读取群成员后， enter ShareActivity
					Groups.selectIdx= id;
					User.Current.grpId=selGrp.getGrpId(); //=Groups.selectGrpId();
					User.Current.flag=1;
					try{
						DBUtil.insertUser(User.Current);
						ACache mCache=getACache();
						mCache.put("User.Current", User.Current);
						mCache.put("Groups.selectIdx", (Serializable)Groups.selectIdx);

					}catch(Exception ex){
						//getActivity().DisplayLongToast(ex.toString());
						ex.printStackTrace();
					}
					
					MemberTask memberTask=new MemberTask();
					MemberHandler memberHandler=new MemberHandler(MainActivity.this,false);
					memberTask.connect(memberHandler);
					memberTask.execute();
				}
			};
			
			View.OnLongClickListener longListener = new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					final int id = (Integer) v.getTag();
					Group selGrp=infos.get(id);
					if( "+".equals(selGrp.name) )
						return false; //?
					
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setMessage("Confirm unregister from this group ?");
					builder.setTitle("提示");
					builder.setPositiveButton("Yes", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
							Group info=infos.get(id);
							
							JSONObject obj=new JSONObject();
							obj.put("userId", User.Current.id);
							obj.put("grpId",  info.grpId);
							String reqJsonMsg=obj.toJSONString();

							UnRegHandler handler=new UnRegHandler(MainActivity.this);
							UnRegTask task=new UnRegTask();
							task.connect(handler);
							task.execute(reqJsonMsg, String.valueOf(id));
							
						}

					});
					builder.setNegativeButton("No", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
					return true;
				}
			};
			
			holder.tv_left_block.setOnClickListener(listener);
			holder.tv_left_block.setOnLongClickListener(longListener);	
			holder.tv_right_block.setOnClickListener(listener);
			holder.tv_right_block.setOnLongClickListener(longListener);
			
			/*change the view in the linear list view*/
			switch (infos.size() - (position*2+2)){
				case -1:
					Log.i("add group name:", infos.get(position*2).getName());
					Group groupInfo = infos.get(position*2);
					if(!groupInfo.getName().equals("+")){
						Log.i("group list push number ", ""+groupInfo.notificationNum);
						if(groupInfo.notificationNum > 0){
							holder.leftBadge.setText(""+groupInfo.notificationNum);
							holder.leftBadge.setTextColor(R.color.red);
							holder.leftBadge.setVisibility(View.VISIBLE);
						}else
							holder.leftBadge.setVisibility(View.GONE);
							
						holder.tv_name_left.setText(groupInfo.getName());
						String path ="http://"+Constants.Server+"/famnotes/Uploads/group/"+groupInfo.getGrpId()+"/"+ groupInfo.getCoverPhoto();
						ImageLoader.getInstance().displayImage(path, holder.tv_image_left);
					}else{
						holder.leftBadge.setVisibility(View.GONE);
						holder.tv_image_left.setVisibility(View.GONE);
						holder.tv_name_left.setText(groupInfo.getName());
						holder.tv_name_left.setTextSize(80);
						holder.tv_name_left.setPadding(0, 0, 0, 0);
					}
					
					holder.rightBadge.setVisibility(View.GONE);
					holder.tv_right_block.setVisibility(View.INVISIBLE);
					break;
				default:
					Log.i("add group name:", infos.get(position*2).getName()+"  "+infos.get(position*2+1).getName());
					Group groupInfoLeft = infos.get(position*2);
					holder.tv_name_left.setText(groupInfoLeft.getName());
					String pathLeft ="http://"+Constants.Server+"/famnotes/Uploads/group/"+groupInfoLeft.getGrpId()+"/"+ groupInfoLeft.getCoverPhoto();
					ImageLoader.getInstance().displayImage(pathLeft, holder.tv_image_left);
					Log.i("group list push number ", ""+groupInfoLeft.notificationNum);
					if(groupInfoLeft.notificationNum > 0){
						holder.leftBadge.setText(""+groupInfoLeft.notificationNum);
						holder.leftBadge.setTextColor(R.color.red);
						holder.leftBadge.setVisibility(View.VISIBLE);
					}else
						holder.leftBadge.setVisibility(View.GONE);
					
					Group groupInfoRight = infos.get(position*2+1);
					if(!groupInfoRight.getName().equals("+")){
						holder.tv_name_right.setText(groupInfoRight.getName());
						String pathRight ="http://"+Constants.Server+"/famnotes/Uploads/group/"+groupInfoRight.getGrpId()+"/"+ groupInfoRight.getCoverPhoto();
						ImageLoader.getInstance().displayImage(pathRight, holder.tv_image_right);
						Log.i("group list push number ", ""+groupInfoRight.notificationNum);
						if(groupInfoRight.notificationNum > 0){
							holder.rightBadge.setText(""+groupInfoRight.notificationNum);
							holder.rightBadge.setTextColor(R.color.red);
							holder.rightBadge.setVisibility(View.VISIBLE);
						}else
							holder.rightBadge.setVisibility(View.GONE);
						
					}else{
						holder.rightBadge.setVisibility(View.GONE);
						holder.tv_image_right.setVisibility(View.GONE);
						holder.tv_name_right.setText(groupInfoRight.getName());
						holder.tv_name_right.setTextSize(80);
						holder.tv_name_right.setPadding(0, 0, 0, 0);
					}
					break;
			}
			
			return convertView;
		}
	
	}
	

	class ViewHolder {
		//ImageView iv_pic;
		TextView tv_name_left;
		TextView tv_name_right;
		CircleImageView tv_image_left;
		CircleImageView tv_image_right;
		LinearLayout tv_left_block;
		LinearLayout tv_right_block;
		TextView leftBadge;
		TextView rightBadge;
	}	
	
	class MemberTask extends BaseAsyncTask<MainActivity, Void, Void> {
		@Override
		public Void run(Void... params) throws Exception {
			//取当前群成员
			PostData pdata=new PostData("user", "get_members");
			String json_members = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata); 
			if(!StringUtils.isEmpty(json_members)){
			JSONObject jsonObjectResult = JSON.parseObject(json_members);
				if(jsonObjectResult.getInteger("errCode") != 0) {
					throw new Exception("login fails ! Cannot get members"); 
				}else{
					JSONArray userArray = jsonObjectResult.getJSONArray("results");
					User.Members.clear();
					for(int i=0; i<userArray.size(); i++) {
						JSONObject  userJSON=(JSONObject) userArray.get(i);
						//User user=JSON.toJavaObject(userJSON, User.class);
						//(String userId, String userName, int grpId, String password, int flag)
						User iUser=new User(userJSON.getIntValue("id"),userJSON.getString("loginId"), userJSON.getString("name"),  User.Current.grpId, null, 0 );
						iUser.setAvatar(userJSON.getString("avatar"));
						User.Members.add(iUser);
					}
					mCache.put("User.Members", (Serializable)User.Members);
				}
			}
			return null;
		}

	}
	
	class MemberHandler extends BaseAsyncTaskHandler<MainActivity, Void>
	{
		public MemberHandler(MainActivity context, boolean showProgressBar) {
			super(context,showProgressBar);
			// TODO Auto-generated constructor stub
		}

		private static final String TAG = "MemberHandler";
		
		@Override
		public boolean onTaskSuccess(MainActivity context, Void result) {
			Log.i(TAG, "memberTask success");
			Group selGrp=infos.get(Groups.selectIdx);
			selGrp.notificationNum = 0;
			mCache.put("Groups.lGroup", (Serializable)Groups.lGroup);
			Message msg = new Message();
			msg.setTarget(handler);
			msg.sendToTarget();
			
			Intent intent = new Intent(MainActivity.this,ShareActivity.class);
			startActivity(intent);		
			
			return true;
		}
		
		@Override
		public boolean onTaskFailed(MainActivity context, Exception error) {
			Log.i(TAG, "Get Member Info Fail");
			context.DisplayLongToast(error.getMessage());
			
//			Intent intent = new Intent(getActivity(),ShareActivity.class);
//			startActivity(intent);			
			
			return true;
		}
	}
	
	class UnRegTask extends BaseAsyncTask<MainActivity, String, Integer> {
		@Override
		public Integer run(String... reqJsonMsg) throws Exception {
			//取当前群成员
			PostData pdata=new PostData("user", "unregister_from_group",  reqJsonMsg[0] );
			String json = new FNHttpRequest(Constants.Usage_System).doPost(pdata); 
			if(StringUtils.isEmpty(json))
				throw new Exception("unregister_from_group fails ! "); 

			JSONObject jsonObjectResult = JSON.parseObject(json);
			if(jsonObjectResult.getInteger("errCode") != 0) {
				throw new Exception("unregister_from_group fails !"); 
			}

			return Integer.parseInt(reqJsonMsg[1]);
		}

	}
	
	class UnRegHandler extends BaseAsyncTaskHandler<MainActivity, Integer>
	{
		public UnRegHandler(MainActivity context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		private static final String TAG = "UnRegHandler";
		
		@Override
		public boolean onTaskSuccess(MainActivity context, Integer result) {
			Log.i(TAG, "UnRegHandler success");
			
			infos.remove(result.intValue());
			lvGroupAdaper.notifyDataSetChanged();
			return true;
		}
		
		@Override
		public boolean onTaskFailed(MainActivity context, Exception error) {
			Log.i(TAG, "UnRegHandler fail");
			context.DisplayLongToast(error.getMessage());
			
			return true;
		}
	}
}
