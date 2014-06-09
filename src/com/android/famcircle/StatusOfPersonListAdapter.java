package com.android.famcircle;

import java.util.HashMap;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
	
	private List<HashMap<String, Object>> dataList;
	private LayoutInflater layoutInflater; 
	private ViewHolder holder;
	private Context context;
	private DisplayImageOptions options;
	
	public StatusOfPersonListAdapter(Context context,
			List<HashMap<String, Object>> data) {
		
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
		return null;
	}
	
	public void setDataList(List<HashMap<String, Object>> dataList) {
		this.dataList = dataList;
	}
}
