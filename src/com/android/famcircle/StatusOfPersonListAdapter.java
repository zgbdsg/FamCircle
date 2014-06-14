package com.android.famcircle;

import java.util.HashMap;
import java.util.List;

import com.android.famcircle.ui.StatusImagePagerActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class StatusOfPersonListAdapter extends BaseAdapter{
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}
		
	};
	
	private  class  ViewHolder {
		TextView statusTime;
		ListView listview;
	}
	
	private List<Object> dataList;
	private LayoutInflater layoutInflater; 
	private ViewHolder holder;
	private Context context;
	private DisplayImageOptions options;
	
	public StatusOfPersonListAdapter(Context context,
			List<Object> data) {
		
		// TODO Auto-generated constructor stub
		this.dataList = data;
		this.context = context;
		layoutInflater = (LayoutInflater)LayoutInflater.from(context);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.dataList!=null? this.dataList.size(): 0 ;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		/*init data of the position*/
		StatusOfPersonListInfo personStatusInfo = (StatusOfPersonListInfo)dataList.get(position);
		
		if(convertView != null ){
			holder = (ViewHolder)convertView.getTag();
		}else {
			convertView = layoutInflater.inflate(R.layout.personal_status_list_item, null);
			holder = new ViewHolder();
			holder.statusTime = (TextView)convertView.findViewById(R.id.day_of_time);
			holder.listview = (ListView)convertView.findViewById(R.id.list_of_statuses_at_the_time);
			
			convertView.setTag(holder);
		}
		
		holder.statusTime.setText(personStatusInfo.getStatusTime());
		PersonalStatusListAdapter statusListAdapter = new PersonalStatusListAdapter(layoutInflater, options,
				personStatusInfo.getSmallPicPath(), personStatusInfo.getStatusTexts(), personStatusInfo.getStatusPic(),personStatusInfo.getNumOfPics());
		holder.listview.setAdapter(statusListAdapter);
		holder.listview.setTag(position);
		holder.listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				int num = (Integer)parent.getTag();
				StatusOfPersonListInfo statInfo=(StatusOfPersonListInfo)dataList.get(num);
				String[] imageUrls = statInfo.getBigPics()[position];
				if(imageUrls == null)
					return;
				
				String[] bigImageUrl = new String[imageUrls.length];
				for(int i=0;i<imageUrls.length;i ++)
					bigImageUrl[i] = "http://114.215.180.229"+statInfo.getBigPicpath()+imageUrls[i];
				Log.i("start pager", "");
				startImagePagerActivity(0, "", bigImageUrl);
			}
		});
		
		return convertView;
	}
	
	public void setDataList(List<Object> dataList) {
		this.dataList = dataList;
	}
	
	private void startImagePagerActivity(int position,String statusId, String[] imageUrls) {
		Intent intent = new Intent(context, StatusImagePagerActivity.class);
		intent.putExtra("statusId", statusId);
		intent.putExtra("images", imageUrls);
		intent.putExtra("position", position);
		context.startActivity(intent);
	}
}

class PersonalStatusListAdapter extends BaseAdapter{
	String[] statusTexts;
	String[] statusPic;
	String[] numOfPics;
	String smallPicPath;
	DisplayImageOptions options;
	LayoutInflater layoutInflater; 
	
	class ListHolder{
		TextView text_status;
		ImageView pics;
		TextView pics_discript;
		TextView numOfPics;
		LinearLayout pics_status;
	}
	
	public PersonalStatusListAdapter(LayoutInflater layoutInflater,DisplayImageOptions options,String smallPicPath,String[] statusTexts,String[] statusPic,String[] numOfPics){
		this.statusTexts = statusTexts;
		this.statusPic = statusPic;
		this.smallPicPath = smallPicPath;
		this.numOfPics = numOfPics;
		this.layoutInflater = layoutInflater;
		this.options = options;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return statusPic.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ListHolder listHolder;
		if(convertView != null){
			listHolder = (ListHolder)convertView.getTag();
		}else{
			convertView = layoutInflater.inflate(R.layout.item_of_status_at_the_time, parent,false);
			listHolder = new ListHolder();
			listHolder.pics = (ImageView)convertView.findViewById(R.id.pics);
			listHolder.pics_discript = (TextView)convertView.findViewById(R.id.pics_discript);
			listHolder.pics_status = (LinearLayout)convertView.findViewById(R.id.pics_status);
			listHolder.text_status = (TextView)convertView.findViewById(R.id.text_status);
			listHolder.numOfPics = (TextView)convertView.findViewById(R.id.num_of_pics);
			
			convertView.setTag(listHolder);
		}
		
		if(statusPic[position].equals("")){
			listHolder.pics_status.setVisibility(View.GONE);
			listHolder.text_status.setVisibility(View.VISIBLE);
			listHolder.text_status.setText(statusTexts[position]);
		}else{
			listHolder.text_status.setVisibility(View.GONE);
			listHolder.pics_status.setVisibility(View.VISIBLE);
			listHolder.pics_discript.setText(statusTexts[position]);
			listHolder.numOfPics.setText(numOfPics[position]);
			ImageLoader.getInstance().displayImage("http://114.215.180.229"+smallPicPath+statusPic[position], listHolder.pics,options,null);
		}
		
		return convertView;
	}
	
}
