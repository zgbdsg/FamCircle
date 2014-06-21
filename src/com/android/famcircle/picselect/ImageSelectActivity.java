package com.android.famcircle.picselect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.famcircle.R;
import com.android.famcircle.picselect.ImageSelectAdapter.TextCallback;

public class ImageSelectActivity extends Activity implements OnClickListener {
	public static final String EXTRA_IMAGE_LIST = "imagelist";

	private List<ImageItem> dataList;
	private GridView gridView;
	private ImageSelectAdapter adapter;
	private AlbumHelper helper;
	private Button finish;
	TextView cancel;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(ImageSelectActivity.this, "最多选择9张图片", 400).show();
				break;
			default:
				break;
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_grid);

		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		dataList = (List<ImageItem>) getIntent().getSerializableExtra(
				EXTRA_IMAGE_LIST);

		initView();
		finish = (Button) findViewById(R.id.finish);
		finish.setOnClickListener(this);
		
		cancel = (TextView) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
	}

	private void initView() {
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageSelectAdapter(ImageSelectActivity.this, dataList,
				mHandler);
		gridView.setAdapter(adapter);
		adapter.setTextCallback(new TextCallback() {
			public void onListen(int count) {
				finish.setText("完成" + "(" + count + ")");
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				// if(dataList.get(position).isSelected()){
				// dataList.get(position).setSelected(false);
				// }else{
				// dataList.get(position).setSelected(true);
				// }
				adapter.notifyDataSetChanged();
			}

		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel:
			this.finish();
			break;
		case R.id.finish:
			ArrayList<String> list = new ArrayList<String>();
			Collection<String> c = adapter.map.values();
			Iterator<String> it = c.iterator();
			for (; it.hasNext();) {
				list.add(it.next());
			}
			
			
			for (int i = 0; i < list.size(); i++) {
				if (Bimp.drr.size() < 9) {
					Bimp.drr.add(list.get(i));
				}
			}

//			if (Bimp.act_bool) {
				Intent intent = new Intent(ImageSelectActivity.this,
						PublishedActivity.class);
				startActivity(intent);
//				Bimp.act_bool = false;
//			}
//			for (int i = 0; i < list.size(); i++) {
//				if (Bimp.drr.size() < 9) {
//					Bimp.drr.add(list.get(i));
//				}
//			}
//			finish();
			break;
		}
	}
}
