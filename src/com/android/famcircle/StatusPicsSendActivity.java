package com.android.famcircle;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.famnotes.android.famnotes.R;
import com.famnotes.android.ui.BaseActivity;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StatusPicsSendActivity extends BaseActivity{

	private String[]  all_path;
	private Button add_pics;
	private EditText statusContent;
	private TextView statusSend;
	private GridView gridGallery;
	private GalleryAdapter adapter;
	private ProgressDialog progress;
	private ImageLoader  imageLoader = ImageLoader.getInstance();
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_send_pics);
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		
		statusContent = (EditText)findViewById(R.id.status_pics_text_content);
		handler = new Handler(){
			
			@Override
	        public void handleMessage(Message msg) {
				switch (msg.arg1) {
				case 0:
					progress.show();
					break;
				case 1:
					progress.dismiss();
					destroySelf();
					break;
				default:
					break;
				}
			}
		};
		gridGallery = (GridView)findViewById(R.id.gridGallery);
		gridGallery.setFastScrollEnabled(true);
		adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
		gridGallery.setAdapter(adapter);
		
		add_pics = (Button)findViewById(R.id.add_pics);
		add_pics.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				gridGallery.setVisibility(8);
				adapter.clear();
				Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
				startActivityForResult(i, 200);
			}
		});
		
		statusSend = (TextView)findViewById(R.id.status_pics_send);
		statusSend.setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new AsyncTask<String, String, String>() {

					@Override
					protected String doInBackground(String... params) {
						// TODO Auto-generated method stub
						Message mg1 = new Message();
						mg1.arg1 = 0;
						handler.sendMessage(mg1);
						PostData pdata = null;
						if(all_path != null) {
						for(int a=0;a<all_path.length;a++)
								Log.i("file path:", all_path[a]);
							ArrayList<String> upfiles = new ArrayList<String>();
							for(int i=0;i<all_path.length;i ++)
								upfiles.add(all_path[i]);
							pdata = new PostData("share", "postStatus",
									"{\"usrId\":2, \"grpId\":1, \"creatTime\":\""+ 
									System.currentTimeMillis() / 1000+ "\", \"status\":\""
									+StringUtils.gbEncoding(statusContent.getText().toString())+"\"}", upfiles);
						}else{
							pdata = new PostData("share", "postStatus",
									"{\"usrId\":2, \"grpId\":1, \"creatTime\":\""
											+ System.currentTimeMillis() / 1000
											+ "\", \"status\":\"Post Msg on "
											+ System.currentTimeMillis() / 1000 + "\"}");
						}
						Log.i("postdata", pdata.toString());
						String json = new FNHttpRequest().doPost(pdata).trim();
						Log.i("send status result", json);
						if(json.startsWith("{\"errCode\" : 0,")){
							Message mg2 = new Message();
							mg2.arg1 = 1;
							handler.sendMessage(mg2);
						}
						return null;
					}
				}.execute("");
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			all_path = data.getStringArrayExtra("all_path");

			ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

			for (String string : all_path) {
				CustomGallery item = new CustomGallery();
				item.sdcardPath = string;
				dataT.add(item);
			}
			gridGallery.setVisibility(0);
			adapter.addAll(dataT);
	}
	
	public void destroySelf(){
		this.finish();
	}
}
