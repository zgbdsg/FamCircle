package com.android.famcircle.ui;

import com.android.famcircle.AppManager;
import com.android.famcircle.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class StatusOfPersonActivity extends BaseActivity{
	
	Context context;
	private PullToRefreshListView mPullRefreshListView;
	private ListView statuslist;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_status);
		
		AppManager.getInstance().addActivity(this);
		context = this;
		mPullRefreshListView = (PullToRefreshListView)findViewById(R.id.statuslist);
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);
		
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("refresh at: "+label);
				refreshView.getLoadingLayoutProxy().setRefreshingLabel(getResources().getString(R.string.pull_to_refresh_refreshing_label));
				refreshView.getLoadingLayoutProxy().setReleaseLabel(getResources().getString(R.string.pull_to_refresh_release_label));
				refreshView.getLoadingLayoutProxy().setPullLabel(getResources().getString(R.string.pull_to_refresh_pull_label));
				
			}
		});
		
		// Add an end-of-list listener
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				Toast.makeText(StatusOfPersonActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
			}
		});
		
		statuslist = mPullRefreshListView.getRefreshableView();
		View headview = LayoutInflater.from(this).inflate(R.layout.activity_share_header, null);
		statuslist.addHeaderView(headview);
	}
}
