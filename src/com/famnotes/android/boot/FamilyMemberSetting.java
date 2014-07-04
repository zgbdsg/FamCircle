package com.famnotes.android.boot;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.config.RequestCode;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.vo.User;

public class FamilyMemberSetting extends BaseActivity {
	//private User mySelf;
	private ListView lv;
	private List<ContactInfo> infos;
	private FamilyMemberSettingAdapter myAdaper;

	private int direction;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fam_member_setting);
        
        infos=new ArrayList<ContactInfo>(8);
        
        direction=getIntent().getIntExtra("direction", RequestCode.DirectionForword); 
        
    	lv = (ListView) findViewById(R.id.member_listview);
    	myAdaper=new FamilyMemberSettingAdapter();
		lv.setAdapter(myAdaper);
        
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(FamilyMemberSetting.this);
				builder.setMessage("Confirm delete？");
				builder.setTitle("提示");
				builder.setPositiveButton("确认", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						ContactInfo info=infos.get((int) id);
						infos.remove(info);
						myAdaper.notifyDataSetChanged();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
			
		});
		
		grpId=getIntent().getIntExtra("GroupId", User.Current.grpId); 
		//Intent intent = getIntent();
		//mySelf=(User) intent.getSerializableExtra("user");
    }

    private int grpId;
    public void enter_system(View view){
    	JSONObject obj=new JSONObject();
		obj.put("grpId", grpId); //User.Current.grpId);
		JSONArray array=new JSONArray(infos.size());
		obj.put("userInfos", array);
		for(ContactInfo info : infos){
			JSONObject subobj=new JSONObject();
			subobj.put("loginId", info.getPhone());
			subobj.put("userName", info.getName());
			array.add(subobj);
		}
		String reqJsonMsg=obj.toJSONString();
		FamilyMemberSettingHander handler=new FamilyMemberSettingHander(this);
		FamilyMemberSettingTask task=new FamilyMemberSettingTask();
		task.connect(handler);
		task.execute(reqJsonMsg);
    }
    
   //不根据xml的配置项生成Menu下的菜单项
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
         super.onCreateOptionsMenu(menu);
         
//         MenuInflater inmenu=getMenuInflater();//得到menuinflater,用于加载菜单项
//         inmenu.inflate(R.menu.menu, menu);//根据xml的定义生成菜单项
         
         
         
//         MenuItem item = menu.add("Search");
//         item.setIcon(android.R.drawable.ic_menu_search);
//         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
//                 | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
//         SearchView sv = new SearchView(FamilyMemberSetting.this);
////         sv.setOnQueryTextListener(this);
//         item.setActionView(sv);
         
         MenuItem itemAdd = menu.add("Add");
         itemAdd.setIcon(android.R.drawable.ic_menu_add);
         itemAdd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                 | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
         
		itemAdd.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent selectIntent = new Intent(FamilyMemberSetting.this, SelectContactActivity.class);
				startActivityForResult(selectIntent, 1);
				return true;
			}
		});
         return true;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(data != null){
			ArrayList<ContactInfo> sel_infos=(ArrayList<ContactInfo>) data.getSerializableExtra("selInfos");
			for(ContactInfo info : sel_infos){
				Log.i("FamilyMemberSetting", info.getName() +" " + info.getPhone());
				if(!infos.contains(info))
					infos.add(info);
			}
			
			myAdaper.notifyDataSetChanged();
			
		}
	}
    
	

	private class FamilyMemberSettingAdapter extends BaseAdapter {

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
			ContactInfo info = infos.get(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(FamilyMemberSetting.this, R.layout.fam_member_item, null);
				holder.tv_name = (TextView) convertView.findViewById(R.id.member_name);
				holder.tv_number = (TextView) convertView.findViewById(R.id.member_phone);
//				holder.cb_contact_select=(CheckBox) convertView.findViewById(R.id.cb_contact_select);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
//			holder.cb_contact_select.setOnClickListener(null);
			holder.tv_name.setText("联系人：" + info.getName());
			holder.tv_number.setText("电话：" + info.getPhone());
//			holder.cb_contact_select.setChecked(info.isSelected());
//			holder.cb_contact_select.setTag(info);
//			holder.cb_contact_select.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					ContactInfo clicked_info =(ContactInfo) v.getTag( );
//					Log.i("SelectContactAdapter.onCheckedChanged",  " 联系人："+clicked_info.getName() +", 电话："+clicked_info.getPhone());
//					if(clicked_info.isSelected())
//						clicked_info.setSelected(false);
//					else
//						clicked_info.setSelected(true);
//				}
//			});
			
			return convertView;
		}

	}

	private class ViewHolder {
		TextView tv_name;
		TextView tv_number;
		//CheckBox cb_contact_select;
	}	

	class FamilyMemberSettingHander extends BaseAsyncTaskHandler<FamilyMemberSetting, Integer>{
		public FamilyMemberSettingHander(FamilyMemberSetting context) {
			super(context);
		}


		@Override
		public boolean onTaskFailed(FamilyMemberSetting context, Exception ex) {
			// TODO Auto-generated method stub
			context.DisplayLongToast("Add Members fails ! "+ex.getMessage()); 
			return true;
		}

		@Override
		public boolean onTaskSuccess(FamilyMemberSetting context, Integer rCode) {
			// TODO Auto-generated method stub
			if(rCode==0){
				if(direction!=RequestCode.DirectionGoback){
					//通过“过场”进入主界面
					context.openActivity(LoadingActivity.class);
				}else{
					Intent intent = new Intent();
					intent.putExtra("isDirtyMembers", true);
					setResult(RESULT_OK, intent);
				}
				
				context.finish(); //返回打开我们的Activity
				return true; 
			}
			
			context.DisplayLongToast("Add Members fails ! You can try again. "); 
			return true;
		}
	}
	
	class FamilyMemberSettingTask  extends BaseAsyncTask<FamilyMemberSetting, String, Integer>{

		@Override
		public Integer run(String... params) throws Exception {
			String reqJsonMsg=params[0];
			String resJson=null;
			try {//grpId  register_famnotes(群+用户信息+VerifyCode)
				/*
				 * 输入: grpId, userInfo[]｛手机号即userId, userName｝
				 * 说明：后台建用户记录、群记录、群用户关系记录
				 * void  add_members({grpId:xxxx, userInfo[]})
				 */
				PostData pdata=new PostData("user", "add_members",  reqJsonMsg );
				resJson = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata);
			} catch (Exception e) {
				throw new Exception("Add Members fails !"); 
			}
			if(resJson==null){
				throw new Exception("Add Members fails !"); 
			}
			
			JSONObject jsonResult = JSON.parseObject(resJson);
			if(jsonResult.getInteger("errCode") != 0) {
				throw new Exception("Add Members fails ! "+jsonResult.getString("errMesg")); 
			}
			
			if(grpId==User.Current.grpId){
				User.Members.clear();
				for(ContactInfo info : infos){
					User user=new User();
					user.loginId=info.getPhone();
					user.name =info.getName();
					user.grpId=grpId; 
					User.Members.add(user);
				}
			}
			return 0;
		}
		
	}
}
