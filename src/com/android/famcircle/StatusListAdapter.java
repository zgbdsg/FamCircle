package com.android.famcircle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.famcircle.ui.ShareActivity;
import com.android.famcircle.ui.StatusImagePagerActivity;
import com.android.famcircle.util.FNHttpRequest;
import com.android.famcircle.util.PostData;
import com.android.famcircle.util.StringUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StatusListAdapter extends BaseAdapter{
	int[] location;
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.arg1 == 1)
				commentPopupWindow.dismiss();
			else if(msg.arg1 == 0){
				ShareActivity share = (ShareActivity)context;
				final RelativeLayout replyWindow = share.replyWindow;
				replyWindow.performClick();
			}
		}
		
	};
	PopupWindow commentPopupWindow;
	
	private  class  ViewHolder {
		ImageView userLogo;
		TextView userName;
		GridView statusPics;
		TextView publish_time;
		TextView status;
		ImageButton comment;
		LinearLayout all_reply_component;
		TextView zanText;
		ListView replyList;
	}
	
	private List<HashMap<String, Object>> dataList;
	private LayoutInflater layoutInflater; 
	private ViewHolder holder;
	private Context context;
	private DisplayImageOptions options;
	
	private StatusListInfo statusInfo;
	private List<StatusZanInfo> statusZanInfoList;
	private List<StatusReplyInfo> statusReplyInfoList;
	
	public StatusListAdapter(Context context,
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
		//Log.i("get count", ""+(this.dataList!=null? this.dataList.size(): 0));
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
		statusInfo = (StatusListInfo)dataList.get(position).get("statusInfo");
		statusZanInfoList = (List<StatusZanInfo>)dataList.get(position).get("zaninfo");
		//Log.i("zan Len:", ""+statusZanInfoList.size()+"");
		statusReplyInfoList = (List<StatusReplyInfo>)dataList.get(position).get("replyinfo");
		//Log.i("reply Len:", ""+statusReplyInfoList.size()+"");
		
		class GridViewAdapter extends BaseAdapter{
			String[] imageUrls;
			
			public GridViewAdapter(String[] imageurls){
				this.imageUrls = imageurls;
			}
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return imageUrls.length;
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
				ImageView imageView;
				if(convertView != null) {
					imageView = (ImageView)convertView;
				}else {
					imageView = new ImageView(context);
					imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				}
				
				//imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				ImageLoader.getInstance().displayImage("http://114.215.180.229"+statusInfo.getSmallPicPath()+imageUrls[position], imageView,options,null);
				return imageView;
			}
			
		}
		
		
		class ReplyListAdapter extends BaseAdapter{

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return statusReplyInfoList.size();
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
				//Log.i("reply add :", ""+position);
				TextView replyTextView;
				if(convertView != null) {
					replyTextView = (TextView)convertView;
				}else{
					replyTextView = new TextView(context);
				}
				
				StatusReplyInfo reply = statusReplyInfoList.get(position);
				replyTextView.setText(reply.getFromUsrName()+" reply§ç "+reply.getToUsrName()+": "+reply.getReply());
				return replyTextView;
			}
			
		}
		
		if(convertView != null ){
			holder = (ViewHolder)convertView.getTag();
		}else {
			convertView = layoutInflater.inflate(R.layout.status_item_layout, null);
			holder = new ViewHolder();
			holder.publish_time = (TextView)convertView.findViewById(R.id.publish_time);
			holder.statusPics = (StatusGridView)convertView.findViewById(R.id.status_content_pics);
			holder.userLogo = (ImageView)convertView.findViewById(R.id.user_logo);
			holder.userName = (TextView)convertView.findViewById(R.id.username);
			holder.status = (TextView)convertView.findViewById(R.id.status_content_text);
			holder.comment = (ImageButton)convertView.findViewById(R.id.commentButton);
			holder.all_reply_component = (LinearLayout)convertView.findViewById(R.id.all_reply_component);
			holder.zanText = (TextView)convertView.findViewById(R.id.zan_text);
			holder.replyList = (ListView)convertView.findViewById(R.id.replyList);
			
			convertView.setTag(holder);
		}
		
		String resrc_type = statusInfo.getResrc_type();
		
		Date dt = new Date(Long.parseLong(statusInfo.getCreatTime()+"000"));
		SimpleDateFormat df=new SimpleDateFormat("MM‘¬dd»’   a hhµ„"); 
		holder.publish_time.setText(df.format(dt));
		ImageLoader.getInstance().displayImage("http://114.215.180.229"+statusInfo.getSmallPicPath()+statusInfo.getAvatar(), holder.userLogo,options,null);
		holder.userName.setText(statusInfo.getName());
		holder.status.setText(statusInfo.getStatus());
		
		
		if(resrc_type.equals("0")){
			 holder.statusPics.setVisibility(8);
		}else if(resrc_type.equals("1")){
			holder.statusPics.setVisibility(0);
			GridViewAdapter myGridViewAdapter = new GridViewAdapter(statusInfo.getPicArray());
			holder.statusPics.setAdapter(myGridViewAdapter);
			holder.statusPics.setTag(position);
			switch (statusInfo.getPicArray().length) {
			case 1:
				holder.statusPics.setNumColumns(1);
				break;
			case 2:
				holder.statusPics.setNumColumns(2);
				break;
			case 3:
				holder.statusPics.setNumColumns(3);
				break;
			case 4:
				holder.statusPics.setNumColumns(2);
				break;
			case 5:
				holder.statusPics.setNumColumns(3);
				break;
			case 6:
				holder.statusPics.setNumColumns(3);
				break;
			case 7:
				holder.statusPics.setNumColumns(3);
				break;
			case 8:
				holder.statusPics.setNumColumns(3);
				break;
			case 9:
				holder.statusPics.setNumColumns(3);
				break;
			default:
				holder.statusPics.setNumColumns(3);
				break;
			}
			holder.statusPics.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					int num = (Integer)parent.getTag();
					final StatusListInfo statInfo=(StatusListInfo)dataList.get(num).get("statusInfo");
					String[] imageUrls = statInfo.getPicArray();
					String[] bigImageUrl = new String[imageUrls.length];
					for(int i=0;i<imageUrls.length;i ++)
						bigImageUrl[i] = "http://114.215.180.229"+statInfo.getBigPicPath()+imageUrls[i];
					Log.i("start pager", "");
					startImagePagerActivity(position,statInfo.getStatusId() , bigImageUrl);
				}
			});
		}
		
		holder.all_reply_component.setVisibility(8);
		holder.zanText.setText("");
		//Log.i("all_reply_component :", "vi "+holder.all_reply_component.getVisibility());

		if(statusZanInfoList.size() > 0 | statusReplyInfoList.size() > 0){
			holder.all_reply_component.setVisibility(0);
			//Log.i("all_reply_component set after :", "vi "+holder.all_reply_component.getVisibility());
		}
		
		if(statusZanInfoList.size() > 0) {
			String zanText = "";
			for(int t=0;t<statusZanInfoList.size(); t++){
				zanText = zanText.concat(statusZanInfoList.get(t).getFromUsrName()+"  ");
			}
			holder.zanText.setText(zanText);
		}
		
		
		if(statusReplyInfoList.size() > 0) {
			ReplyListAdapter replyAdapter = new ReplyListAdapter();
			holder.replyList.setAdapter(replyAdapter);
		}
		
		holder.comment.setTag(position); //Log.i("get view position ", ""+position);
		holder.comment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View btnv) {
				int pos=(Integer) btnv.getTag();
				Log.i("get view position ", ""+pos);
				// TODO Auto-generated method stub
				final StatusListInfo statInfo=(StatusListInfo)dataList.get(pos).get("statusInfo");
				Log.i("pop on ", statInfo.getStatusId()+"  "+statInfo.getStatus());
				View popupView = layoutInflater.inflate(R.layout.status_popup_window, null);
				
				popupView.findViewById(R.id.zan_area).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sendZan(""+1,statInfo.getUsrId(),statInfo.getStatusId());
					}
				});
				
				popupView.findViewById(R.id.comment_area).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.i("press reply", "!!!");
						commentPopupWindow.dismiss();
						ShareActivity share = (ShareActivity)context;
						final RelativeLayout  inputWindow = share.inputWindow;
						final RelativeLayout replyWindow = share.replyWindow;
						Log.i("reply visual","  "+ inputWindow.getVisibility());
						inputWindow.setVisibility(View.VISIBLE);
						replyWindow.setVisibility(View.VISIBLE);
						inputWindow.requestFocus();
						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);  
						imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY); 
						
						Button btnReplySend = (Button)inputWindow.findViewById(R.id.btn_reply_send);
						final EditText replyContent = (EditText)inputWindow.findViewById(R.id.reply_content);
						btnReplySend.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								sendReply(""+1,statInfo.getUsrId(),statInfo.getStatusId(),
										StringUtils.gbEncoding(replyContent.getText().toString()));
							}
						});
					}
				});
				
				location = new int[2];
				btnv.getLocationInWindow(location);
				Log.i("location :", location[0] +" "+location[1]);
				commentPopupWindow = new PopupWindow(popupView, 360, 80, true);
				
				commentPopupWindow.setTouchable(true);
				commentPopupWindow.setOutsideTouchable(true);
				commentPopupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
				int xoffInDip = -commentPopupWindow.getWidth();
				int yoffInDip = -commentPopupWindow.getHeight();
				commentPopupWindow.showAsDropDown(btnv,xoffInDip,yoffInDip);
				Log.i("popup ", commentPopupWindow.getWidth()+"  "+commentPopupWindow.getHeight());
				commentPopupWindow.update();
				//commentPopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0]-commentPopupWindow.getWidth(), location[1]);
				
			}
		});
		
		return convertView;
	}
		
	private void startImagePagerActivity(int position,String statusId, String[] imageUrls) {
		Intent intent = new Intent(context, StatusImagePagerActivity.class);
		intent.putExtra("statusId", statusId);
		intent.putExtra("images", imageUrls);
		intent.putExtra("position", position);
		context.startActivity(intent);
	}

	public void setDataList(List<HashMap<String, Object>> dataList) {
		this.dataList = dataList;
	}
	
	
	public void sendZan(final String fromUsrId, final String toUsrId ,final String statusId){
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				Log.i("zan info:", fromUsrId+"  to "+toUsrId +" int status "+statusId);
				PostData pdata=new PostData("share", "postReply", "{\"statusId\":"+statusId+ ", \"fromUsrId\":"+fromUsrId+", \"toUsrId\":"+toUsrId+", \"type\":1, \"reply\":\"\"}");
				String json=new FNHttpRequest().doPost(pdata).trim();
				Message msg = new Message();
				msg.arg1 = 1;
				handler.sendMessage(msg);
				System.out.println(json);

				return null;
			}
		}.execute("");
	}
	
	public void sendReply(final String fromUsrId, final String toUsrId ,final String statusId,final String replyContent){
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				Log.i("reply  info:", fromUsrId+"  to "+toUsrId +" int status "+statusId);
				PostData pdata=new PostData("share", "postReply", "{\"statusId\":"+statusId+ ", \"fromUsrId\":"+fromUsrId+", \"toUsrId\":"+toUsrId+", \"type\":0, \"reply\":\""+replyContent+"\"}");
				String json=new FNHttpRequest().doPost(pdata).trim();
				Message msg = new Message();
				msg.arg1 = 0 ;
				handler.sendMessage(msg);
				System.out.println(json);

				return null;
			}
		}.execute("");
	}
}
