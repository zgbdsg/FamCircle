package com.android.famcircle.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.android.famcircle.Action;
import com.android.famcircle.CustomGallery;
import com.android.famcircle.GalleryAdapter;
import com.android.famcircle.R;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.ImageUtils;
import com.famnotes.android.util.PictureBody;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.User;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StatusPicsSendActivity extends BaseActivity{

	private String[]  all_path;
	private Button add_pics;
	private EditText statusContent;
	private TextView statusSend;
	private GridView gridGallery;
	private GalleryAdapter adapter;
	private ProgressDialog progress;
	public static ArrayList<CustomGallery> dataT;
	private ImageLoader  imageLoader = ImageLoader.getInstance();
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_send_pics);
		dataT = new ArrayList<CustomGallery>();
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
					Message refreshMsg = new Message();
					refreshMsg.arg1 = 5;
					refreshMsg.setTarget(ShareActivity.myhandler);
					
					refreshMsg.sendToTarget();
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
				//gridGallery.setVisibility(8);
				//adapter.clear();

				Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
				startActivityForResult(i, 200);
			}
		});
		
/*		statusSend = (TextView)findViewById(R.id.status_pics_send);
		statusSend.setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendStatusWithPics();
			}
		});*/
	}
	
	
	public void sendStatusWithPics(){
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				try{
				Message mg1 = new Message();
				mg1.arg1 = 0;
				handler.sendMessage(mg1);
				PostData pdata = null;
				if(all_path != null) {
					ArrayList<PictureBody> pics = new ArrayList<PictureBody>();
					
					for(int a=0;a<all_path.length;a++){
							Log.i("file path:", all_path[a]);
							Bitmap tmpmap = ImageUtils.compImageBySize(all_path[a]);
							PictureBody pb = new PictureBody(tmpmap, Bitmap.CompressFormat.JPEG , all_path[a]+".jpg");
							pics.add(pb);
					}

					pdata = new PostData("share", "postStatus",
							"{\"usrId\":"+ShareActivity.userId+", \"grpId\":"+ShareActivity.groupId+", \"creatTime\":\""+ 
							System.currentTimeMillis() / 1000+ "\", \"status\":\""
							+StringUtils.gbEncoding(statusContent.getText().toString())+"\"}", pics);
				}else{
					pdata = new PostData("share", "postStatus",
							"{\"usrId\":"+ShareActivity.userId+", \"grpId\":"+ShareActivity.groupId+", \"creatTime\":\""
									+ System.currentTimeMillis() / 1000
									+ "\", \"status\":\""
									+ StringUtils.gbEncoding(statusContent.getText().toString()) + "\"}");
				}
				Log.i("postdata", pdata.toString());
				String json = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
				Log.i("send status result", json);
				if(json.startsWith("{\"errCode\" : 0,")){
					Message mg2 = new Message();
					mg2.arg1 = 1;
					handler.sendMessage(mg2);
				}
				}catch(Exception ex){
					ex.printStackTrace();
				}
				return null;
			}
		}.execute("");
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			all_path = data.getStringArrayExtra("all_path");

			//ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();
			dataT.clear();
			for (String string : all_path) {
				CustomGallery item = new CustomGallery();
				item.sdcardPath = string;
				item.isSeleted = true;
				dataT.add(item);
			}
			gridGallery.setVisibility(0);
			adapter.addAll(dataT);
	}
	
	public void destroySelf(){
		this.finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.picssend, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.btn_send_status) {
			sendStatusWithPics();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
