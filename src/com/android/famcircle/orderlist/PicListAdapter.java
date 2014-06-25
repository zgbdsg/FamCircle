package com.android.famcircle.orderlist;

import java.util.List;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.famcircle.R;
import com.android.famcircle.orderlist.PinnedHeaderListView.PinnedHeaderAdapter;

public class PicListAdapter extends BaseAdapter implements
		PinnedHeaderAdapter, OnScrollListener {
	private List<OrderListTag> mList;
	private MySectionIndexer mIndexer;
	private Context mContext;
	private int mLocationPosition = -1;
	private LayoutInflater mInflater;
	private String []tagName;
	
	//几级显示
	private int level;
	private int maxLevel;
	
//	private String [][]tagName = {
//			{"2010年", "2011年", "2012年", "2013年", "2014年", "2015年", "2016年"},
//			{"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"},
//			{"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"}
//	};

	public PicListAdapter(List<OrderListTag> mList, MySectionIndexer mIndexer,
			Context mContext, String []tagName, int maxLevel) {
		this.mList = mList;
		this.mIndexer = mIndexer;
		this.mContext = mContext;
		this.tagName = tagName;
		this.level = 0;
		this.maxLevel = maxLevel;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList == null ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.order_list_pic_item, null);

			holder = new ViewHolder();
			holder.group_title = (TextView) view.findViewById(R.id.group_title);
			holder.grid_view = (MyGridView) view.findViewById(R.id.grid_view);

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}	
		
//		根据不同的等级，设置不同的gridview列宽
		if (this.level == 0) {
			holder.grid_view.setColumnWidth(60);
		} else if (this.level == 1) {
			holder.grid_view.setColumnWidth(100);
		} else if (this.level == 2) {
			holder.grid_view.setColumnWidth(140);
		} else if (this.level == 3) {
			holder.grid_view.setColumnWidth(180);
		}
		
		OrderListTag orderListTag = (OrderListTag)mList.get(position);
		//设置分组的标题
		int section = mIndexer.getSectionForPosition(position);
		if (mIndexer.getPositionForSection(section) == position) {
			Log.v("google ", ""+position+" "+section+"~~~~~~");
			holder.group_title.setVisibility(View.VISIBLE);
			holder.group_title.setText(tagName[position]);
		} else {
			Log.v("google ", ""+position+" "+section+"!!!!!!!!!");
			holder.group_title.setVisibility(View.GONE);
		}

		holder.adapter = new GridViewAdapter(mContext, orderListTag.getPicItemList(), level);
		holder.adapter.notifyDataSetChanged();
		holder.grid_view.setAdapter(holder.adapter);

		holder.grid_view.setOnItemClickListener(new MyAdapterListener(position));
		
		return view;
	}

	public static class ViewHolder {
		public TextView group_title;
		public MyGridView grid_view;
		public GridViewAdapter adapter;
	}

	@Override
	public int getPinnedHeaderState(int position) {
		int realPosition = position;
		if (realPosition < 0
				|| (mLocationPosition != -1 && mLocationPosition == realPosition)) {
			return PINNED_HEADER_GONE;
		}
		mLocationPosition = -1;
		int section = mIndexer.getSectionForPosition(realPosition);
		int nextSectionPosition = mIndexer.getPositionForSection(section + 1);
		if (nextSectionPosition != -1
				&& realPosition == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}
		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		// TODO Auto-generated method stub
		int realPosition = position;
		int section = mIndexer.getSectionForPosition(realPosition);
		String title = (String) mIndexer.getSections()[section];
		((TextView) header.findViewById(R.id.group_title)).setText(title);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}

	}
	
	//对gridview进行监听
	class MyAdapterListener implements OnItemClickListener {
		private int position ;

		MyAdapterListener(int pos) {
			position = pos;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			int vid= arg1.getId ( ) ;
			
			if (level < maxLevel) {
				Message msg = new Message();
				msg.what = 1;
				level = level + 1;
				msg.arg2 =level;
				Log.v("halley", "maxL:" + maxLevel);
				msg.setTarget(OrderStatusListActivity.handler);
				msg.sendToTarget();
				
				Log.v("halley", "level:" + level);
			}
		}
	}
	
	//升级
	public void addLevel() {
		level++;
	}
	
	//降级
	public void lowLevel() {
		level--;
	}
	
	public int getLevel()	{
		return level;
	}
	
	public void setData(List<OrderListTag>mList ,String []list, int []counts) {
		this.mList.clear();
		this.tagName = null;
		this.mList = mList;
		this.tagName = list;

		mIndexer.setData(list, counts);
	}
}
