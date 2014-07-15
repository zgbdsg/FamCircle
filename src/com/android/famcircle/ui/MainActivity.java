package com.android.famcircle.ui;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
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
import com.android.famcircle.linearlistview.LinearListView.OnItemClickListener;
import com.android.famcircle.linearlistview.LinearListView.OnItemLongClickListener;
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

public class MainActivity extends BaseActivity {
	
	public static boolean isForeground = false;
	//for receive customer msg from jpush server
	private MyReceiver mMessageReceiver;
	public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";
	
	private LinearListView lvGroup;
	private ListViewGroupAdapter lvGroupAdaper;
	private List<Group> infos;
	private OnItemClickListener groupListClick;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
//        JPushInterface.init(this);     		// 初始化 JPush
		
        registerMessageReceiver();
        
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
			tags.add("grpId"+Groups.lGroup.get(i).grpId);
		}
		JPushInterface.setAliasAndTags(this, "usrId"+User.Current.loginId, tags, new TagAliasCallback() {
			
			@Override
			public void gotResult(int arg0, String arg1, Set<String> arg2) {
				// TODO Auto-generated method stub
				
			}
		});
		
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
    	lvGroupAdaper=new ListViewGroupAdapter((LayoutInflater)LayoutInflater.from(this));
		lvGroup.setAdapter(lvGroupAdaper);
        
		lvGroup.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(LinearListView parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				//enter group's ShareActivity
				Group selGrp=infos.get((int) id);
				if( "+".equals(selGrp.name) ){
					//register新群，...
					openActivity(RegisterGroup.class);
					
					return; //?
				}
				
				//读取群成员后， enter ShareActivity
				Groups.selectIdx=(int) id;
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
//				Intent intent = new Intent(getActivity(),ShareActivity.class);
//				startActivity(intent);
			}
				
		});
		
		//?registerForContextMenu(lvGroup);
		
		//为ListView加上长按事件
		lvGroup.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public void onItemLongClick(LinearListView parent, View view,
					int position, final long id) {
				// TODO Auto-generated method stub
				Group selGrp=infos.get((int) id);
				if( "+".equals(selGrp.name) )
					return; //?
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage("Confirm unregister from this group ?");
				builder.setTitle("提示");
				builder.setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						Group info=infos.get((int) id);
						
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
				
				return; //?
			}
			
		});
	}


	@Override
	protected void onResume() {
		isForeground = true;
		super.onResume();
	}


	@Override
	protected void onPause() {
		isForeground = false;
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}
	
	
	public void registerMessageReceiver() {
		mMessageReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_ACTION);
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		registerReceiver(mMessageReceiver, filter);
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
		public ListViewGroupAdapter(LayoutInflater inflater) {
			// TODO Auto-generated constructor stub
			this.inflater = inflater;
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
				holder.tv_left_block = (LinearLayout) convertView.findViewById(R.id.left_block);
				holder.tv_right_block = (LinearLayout) convertView.findViewById(R.id.right_block);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_name_left.setTag(position);
			holder.tv_name_right.setTag(position);
			holder.tv_left_block.setTag(position);
			holder.tv_right_block.setTag(position);
			
			switch (infos.size() - (position*2+2)){
				case -1:
					Log.i("add group name:", infos.get(position*2).getName());
					holder.tv_name_left.setText(infos.get(position*2).getName());
					holder.tv_right_block.setVisibility(View.GONE);
					break;
				default:
					Log.i("add group name:", infos.get(position*2).getName()+"  "+infos.get(position*2+1).getName());
					holder.tv_name_left.setText(infos.get(position*2).getName());
					holder.tv_name_right.setText(infos.get(position*2+1).getName());
					break;
			}
			
			return convertView;
		}
	
	}
	

	class ViewHolder {
		//ImageView iv_pic;
		TextView tv_name_left;
		TextView tv_name_right;
		LinearLayout tv_left_block;
		LinearLayout tv_right_block;
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
