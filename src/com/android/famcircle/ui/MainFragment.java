package com.android.famcircle.ui;

import java.util.ArrayList;
import java.util.List;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.boot.RegisterGroup;
import com.famnotes.android.db.DBUtil;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.Group;
import com.famnotes.android.vo.Groups;
import com.famnotes.android.vo.User;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class MainFragment extends Fragment{
	private ListView lvGroup;
	private ListViewGroupAdapter lvGroupAdaper;
	private List<Group> infos;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View root = inflater.inflate(R.layout.fragment_main, container, false);
		
//		TextView text = (TextView)root.findViewById(R.id.gotoshare);
//		Log.i("view ", ""+(text==null));
//		text.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(getActivity(),ShareActivity.class);
//				startActivity(intent);
//			}
//		});
		
		infos=new ArrayList<Group>(8);
		infos.addAll(Groups.lGroup);
		Group pesudoGrp=new Group(-1, "+",  null);
		infos.add(pesudoGrp);
		
    	lvGroup = (ListView)root.findViewById(R.id.group_listview); // inflater.inflate(R.id.group_listview, container, false);
    	lvGroupAdaper=new ListViewGroupAdapter();
		lvGroup.setAdapter(lvGroupAdaper);
        
		lvGroup.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
				//enter group's ShareActivity
				Group selGrp=infos.get((int) id);
				if( "+".equals(selGrp.name) ){
					//register新群，...
					((MainActivity)getActivity()).openActivity(RegisterGroup.class);
					
					return; //?
				}
				
				//读取群成员后， enter ShareActivity
				Groups.selectIdx=(int) id;
				User.Current.grpId=selGrp.getGrpId(); //=Groups.selectGrpId();
				User.Current.flag=1;
				try{
					DBUtil.insertUser(User.Current);
				}catch(Exception ex){
					//getActivity().DisplayLongToast(ex.toString());
					ex.printStackTrace();
				}
				
				MemberTask memberTask=new MemberTask();
				MemberHandler memberHandler=new MemberHandler((MainActivity) getActivity());
				memberTask.connect(memberHandler);
				memberTask.execute();

			}
				
		});
		
		//?registerForContextMenu(lvGroup);
		
		//为ListView加上长按事件
		lvGroup.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
				Group selGrp=infos.get((int) id);
				if( "+".equals(selGrp.name) )
					return false; //?
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Confirm unregister from this group ?");
				builder.setTitle("提示");
				builder.setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						Group info=infos.get((int) id);
						infos.remove(info);
						lvGroupAdaper.notifyDataSetChanged();
					}

				});
				builder.setNegativeButton("No", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				
				return true; //?
			}
			
		});
		
		return root;  
	}

	



    
	class ListViewGroupAdapter  extends BaseAdapter {
	
		@Override
		public int getCount() {
			return infos.size();
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
			Group info = infos.get(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(getActivity(), R.layout.fam_group_item, null);
				holder.tv_name = (TextView) convertView.findViewById(R.id.name);
	//			holder.cb_contact_select=(CheckBox) convertView.findViewById(R.id.cb_contact_select);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv_name.setText(info.getName());
			
			return convertView;
		}
	
	}

	class ViewHolder {
		//ImageView iv_pic;
		TextView tv_name;
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
		public MemberHandler(MainActivity context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		private static final String TAG = "MemberHandler";
		
		@Override
		public boolean onTaskSuccess(MainActivity context, Void result) {
			Log.i(TAG, "memberTask success");
			
			Intent intent = new Intent(getActivity(),ShareActivity.class);
			startActivity(intent);		
			
			return true;
		}
		
		@Override
		public boolean onTaskFailed(MainActivity context, Exception error) {
			Log.i(TAG, "memberTask fail");
			context.DisplayLongToast(error.getMessage());
			
			Intent intent = new Intent(getActivity(),ShareActivity.class);
			startActivity(intent);			
			
			return true;
		}
	}
}
