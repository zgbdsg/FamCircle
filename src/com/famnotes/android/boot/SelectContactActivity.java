package com.famnotes.android.boot;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.android.famcircle.R;

public class SelectContactActivity extends Activity {
	private ListView lv;
	private List<ContactInfo> infos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_contact);

		infos = new ContactInfoService(this).getContactInfos();
//		for(int i=0; i<infos.size(); i++){
//			ContactInfo info=infos.get(i);
//			Log.i("SelectContactActivity", "pos="+i+", 联系人："+info.getName() +", 电话："+info.getPhone());
//		}
		
		lv = (ListView) findViewById(R.id.lv_select_contact);
		lv.setAdapter(new SelectContactAdapter());
//		lv.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				String number = infos.get(position).getPhone();
//				Intent intent = new Intent();
//				intent.putExtra("number", number);
//				setResult(1, intent);
//				finish();
//			}
//		});
		
	}

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
				ArrayList<ContactInfo> sel_infos=new ArrayList<ContactInfo>();
				for(ContactInfo iinfo : infos){
					if(iinfo.isSelected())
						sel_infos.add(iinfo);
				}
				Intent intent = new Intent();
				intent.putExtra("selInfos", sel_infos);
				setResult(1, intent);
				finish();
				return true;
			}
		});
         return true;
    }
    
	// =================================================================================

	private class SelectContactAdapter extends BaseAdapter {

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
			Log.i("SelectContactAdapter.getView", "pos="+position+", 联系人："+info.getName() +", 电话："+info.getPhone());
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(SelectContactActivity.this, R.layout.contact_item, null);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_contact_name);
				holder.tv_number = (TextView) convertView.findViewById(R.id.tv_contact_number);
				holder.cb_contact_select=(CheckBox) convertView.findViewById(R.id.cb_contact_select);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.cb_contact_select.setOnClickListener(null);
			holder.tv_name.setText("联系人：" + info.getName());
			holder.tv_number.setText("电话：" + info.getPhone());
			holder.cb_contact_select.setChecked(info.isSelected());
			holder.cb_contact_select.setTag(info);
			holder.cb_contact_select.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ContactInfo clicked_info =(ContactInfo) v.getTag( );
					Log.i("SelectContactAdapter.onCheckedChanged",  " 联系人："+clicked_info.getName() +", 电话："+clicked_info.getPhone());
					if(clicked_info.isSelected())
						clicked_info.setSelected(false);
					else
						clicked_info.setSelected(true);
				}
			});
			
			return convertView;
		}

	}

	private class ViewHolder {
		TextView tv_name;
		TextView tv_number;
		CheckBox cb_contact_select;
	}

}
