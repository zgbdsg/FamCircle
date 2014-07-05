package com.android.famcircle.picselect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.R;
import com.android.famcircle.ui.ShareActivity;
import com.famnotes.android.util.ACache;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.ImageUtils;
import com.famnotes.android.util.PictureBody;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.User;

public class PublishedActivity extends Activity implements OnClickListener {
	
	private static final int MSG_SEND_SUC = 1;
	private static final int MSG_SEND_FAIL = 2;

	private LinearLayout screen;
	private GridView noScrollgridview;
	private GridAdapter adapter;
//	private TextView activitySelectimgSend;
//	private TextView cancel;
	private EditText contentEdit;
	private ACache mCache;
	
	private ProgressDialog progressDialog;  
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
		
			switch (msg.what) {
			case MSG_SEND_SUC:
				FileUtils.deleteDir();
				
				progressDialog.dismiss();
				Toast.makeText(PublishedActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
				
				emptyBimp();
				
				Message refreshMsg = new Message();
				refreshMsg.arg1 = 5;
//				refreshMsg.setTarget(ShareActivity.myhandler);
				mCache.clear();
//				refreshMsg.sendToTarget();
				
				Intent intent = new Intent();
				intent.putExtra("handlerCode", 5);
				setResult(0, intent);
				PublishedActivity.this.finish();
				
				break;
			case MSG_SEND_FAIL:
				progressDialog.dismiss();
				Toast.makeText(PublishedActivity.this, "上传失败！", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	protected void onCreate(Bundle savedInstanceState) {
		
		Log.v("halley", "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectimg);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		init();
	}

	public void init() {
		mCache = ACache.get(this);
		screen = (LinearLayout) findViewById(R.id.screen);
//		activitySelectimgSend = (TextView) findViewById(R.id.activity_selectimg_send);
//		cancel = (TextView) findViewById(R.id.cancel);
		
		contentEdit = (EditText) findViewById(R.id.contentEditText);
		noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
		noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new GridAdapter(this);
		adapter.update();
		noScrollgridview.setAdapter(adapter);
		noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				closeInput();
				if (arg2 == Bimp.bmp.size()) {
					new PopupWindows(PublishedActivity.this, noScrollgridview);
				} else {
					Intent intent = new Intent(PublishedActivity.this,
							PhotoDisplayActivity.class);
					intent.putExtra("ID", arg2);
					startActivity(intent);
				}
			}
		});
		
		screen.setOnClickListener(this);
//		cancel.setOnClickListener(this);
//		activitySelectimgSend.setOnClickListener(this);
		contentEdit.setOnClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.publish, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			closeInput();
			emptyBimp();
			finish();
		}else if(id == R.id.activity_selectimg_send){
			closeInput();
			progressDialog = ProgressDialog.show(PublishedActivity.this, "Sending...", "Please wait...", true, false);  
			
			ArrayList<String> uploadList = new ArrayList<String>();
			for (int i = 0; i < Bimp.drr.size(); i++) {
				uploadList.add(Bimp.drr.get(i));
			}
			upload(uploadList, contentEdit.getText().toString());
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	//上传数据
	private void upload(final ArrayList<String> all_path, final String text) {
		new Thread() {
			public void run() {
				PostData pdata = null;
				if(all_path != null) {
					ArrayList<PictureBody> pics = new ArrayList<PictureBody>();
					
					for(int a=0;a<all_path.size();a++){
							Log.i("file path:", all_path.get(a));
							Bitmap tmpmap = ImageUtils.compImageBySize(all_path.get(a));
							PictureBody pb = new PictureBody(tmpmap, Bitmap.CompressFormat.JPEG , all_path.get(a)+".jpg");
							pics.add(pb);
					}
					pdata = new PostData("share", "postStatus",
							"{\"usrId\":"+ShareActivity.userId+", \"grpId\":"+ShareActivity.groupId+", \"creatTime\":\""+ 
							System.currentTimeMillis() / 1000+ "\", \"status\":\""
							+StringUtils.gbEncoding(text)+"\"}", pics);
				}else{
					pdata = new PostData("share", "postStatus",
							"{\"usrId\":"+ShareActivity.userId+", \"grpId\":"+ShareActivity.groupId+", \"creatTime\":\""
									+ System.currentTimeMillis() / 1000
									+ "\", \"status\":\""
									+ StringUtils.gbEncoding(text) + "\"}");
				}
				
				String json;
				JSONObject responseJSONObject = null;
				try{
				json = new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
				responseJSONObject = JSON.parseObject(json);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				if (responseJSONObject.getIntValue("errCode") == 0) {
					mHandler.sendEmptyMessage(MSG_SEND_SUC);
				} else {
					mHandler.sendEmptyMessage(MSG_SEND_FAIL);
				}

			};
		}.start();
	}
	
	//关闭键盘
	private void closeInput() {
		InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		contentEdit.setCursorVisible(false);
		im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	//清空Bimp的内容
	private void emptyBimp() {
		Bimp.drr.clear();
		Bimp.bmp.clear();
		Bimp.max = 0;
	}

	@SuppressLint("HandlerLeak")
	public class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater; // 视图容器
		private int selectedPosition = -1;// 选中的位置
		private boolean shape;

		public boolean isShape() {
			return shape;
		}

		public void setShape(boolean shape) {
			this.shape = shape;
		}

		public GridAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public void update() {
			loading();
		}

		public int getCount() {
			return (Bimp.bmp.size() + 1);
		}

		public Object getItem(int arg0) {

			return null;
		}

		public long getItemId(int arg0) {

			return 0;
		}

		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}

		/**
		 * ListView Item设置
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			final int coord = position;
			ViewHolder holder = null;
			if (convertView == null) {

				convertView = inflater.inflate(R.layout.item_published_grida,
						parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position == Bimp.bmp.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.icon_addpic_unfocused));
				if (position == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(Bimp.bmp.get(position));
			}

			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};

		public void loading() {
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (Bimp.max == Bimp.drr.size()) {
							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
							break;
						} else {
							try {
								String path = Bimp.drr.get(Bimp.max);
								Bitmap bm = Bimp.revitionImageSize(path);
								Bimp.bmp.add(bm);
//								String newStr = path.substring(
//										path.lastIndexOf("/") + 1,
//										path.lastIndexOf("."));
//								FileUtils.saveBitmap(bm, "" + newStr);
								Bimp.max += 1;
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							} catch (IOException e) {

								e.printStackTrace();
							}
						}
					}
				}
			}).start();
		}
	}

	public String getString(String s) {
		String path = null;
		if (s == null)
			return "";
		for (int i = s.length() - 1; i > 0; i++) {
			s.charAt(i);
		}
		return path;
	}

	@Override
	protected void onRestart() {
		Log.v("halley", "onRestart");
		adapter.update();
		super.onRestart();
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
    }

	public class PopupWindows extends PopupWindow {

		public PopupWindows(Context mContext, View parent) {

			View view = View
					.inflate(mContext, R.layout.item_popupwindows, null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view
					.findViewById(R.id.ll_popup);
			ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view
					.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view
					.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view
					.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					photo();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(PublishedActivity.this,
							ImageBucketActivity.class);
					startActivity(intent);
					dismiss();
				}
			});
			bt3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
				}
			});

		}
	}

	private static final int TAKE_PICTURE = 0;
	private String path = "";
	private File file;
	

	public void photo() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		file = new File(Environment.getExternalStorageDirectory()
				+ "/myimage/" + String.valueOf(System.currentTimeMillis())
				+ ".jpg");
		if (!file.exists()) {
			File vDirPath = file.getParentFile(); 
			vDirPath.mkdirs();
		}
		path = file.getPath();
		Log.v("halley", "path_photo:" + path);
		Uri imageUri = Uri.fromFile(file);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(openCameraIntent, TAKE_PICTURE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);  
		switch (requestCode) {
		case TAKE_PICTURE:
			if (Bimp.drr.size() < 9 && resultCode == RESULT_OK) {
				Bimp.drr.add(file.getPath());
			}
			
			break;
		default:
			break;
		}
		
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen:
			closeInput();
			break;
//		case R.id.cancel:
//			closeInput();
//			emptyBimp();
//			PublishedActivity.this.finish();
//			break;
//		case R.id.activity_selectimg_send:
//			ArrayList<String> list = new ArrayList<String>();				
//			for (int i = 0; i < Bimp.drr.size(); i++) {
//				String Str = Bimp.drr.get(i).substring( 
//						Bimp.drr.get(i).lastIndexOf("/") + 1,
//						Bimp.drr.get(i).lastIndexOf("."));
//				list.add(FileUtils.SDPATH+Str+".jpg");				
//			}
//			closeInput();
//			progressDialog = ProgressDialog.show(PublishedActivity.this, "Sending...", "Please wait...", true, false);  
//			
//			ArrayList<String> uploadList = new ArrayList<String>();
//			for (int i = 0; i < Bimp.drr.size(); i++) {
//				uploadList.add(Bimp.drr.get(i));
//			}
//			upload(uploadList, contentEdit.getText().toString());
//			break;
		case R.id.contentEditText:
			contentEdit.setCursorVisible(true);
			break;
		default:
			break;
		}
	}

}
