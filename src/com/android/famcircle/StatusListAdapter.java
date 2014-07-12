package com.android.famcircle;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.famcircle.config.Constants;
import com.android.famcircle.linearlistview.LinearListView;
import com.android.famcircle.ui.ShareActivity;
import com.android.famcircle.ui.StatusImagePagerActivity;
import com.android.famcircle.ui.StatusOfPersonActivity;
import com.famnotes.android.base.BaseAsyncTask;
import com.famnotes.android.base.BaseAsyncTaskHandler;
import com.famnotes.android.util.ACache;
import com.famnotes.android.util.FNHttpRequest;
import com.famnotes.android.util.PostData;
import com.famnotes.android.util.StringUtils;
import com.famnotes.android.vo.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StatusListAdapter extends BaseAdapter{
	int[] location;
	
	class StatusListAdapterHandler extends BaseAsyncTaskHandler<ShareActivity, Message>{  //Handler handler = new Handler(){

		public StatusListAdapterHandler(ShareActivity context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public StatusListAdapterHandler(ShareActivity context, boolean showProgressBar) {
			super(context, showProgressBar);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public boolean onTaskFailed(ShareActivity arg0, Exception arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onTaskSuccess(ShareActivity context, Message msg) {
			// TODO Auto-generated method stub
			if(msg.arg1 == 2){
				if(commentPopupWindow != null)
					commentPopupWindow.dismiss();
			}else if(msg.arg1 == 1){
				commentPopupWindow.dismiss();
				int loc = msg.arg2;
				Bundle data = msg.getData();
				
				List<StatusZanInfo> zanList = (List<StatusZanInfo>) dataList.get(loc).get("zaninfo");

				StatusZanInfo addZan = new StatusZanInfo();
				Log.i("zanFromName", data.getString("fromUsrName"));
				addZan.setFromUsrId(data.getString("fromUsrId"));
				addZan.setFromUsrName(data.getString("fromUsrName"));
				zanList.add(addZan);
				
				mCache.put("statusResult"+User.Current.grpId+"---"+User.Current.id, (Serializable)dataList);
				context.notifyDataSetChanged();
			}else if(msg.arg1 == 0){
				replyTextContent.setText("");
				ShareActivity share = (ShareActivity)context;
				final RelativeLayout replyWindow = share.replyWindow;
				replyWindow.performClick();
				
				int loc = msg.arg2;
				Bundle data = msg.getData();
				List<StatusReplyInfo> replyList = (List<StatusReplyInfo>) dataList.get(loc).get("replyinfo");
				
				StatusReplyInfo replyInfo = new StatusReplyInfo();
				replyInfo.setFromUsrId(data.getString("fromUsrId"));
				replyInfo.setFromUsrName(data.getString("fromUsrName"));
				replyInfo.setReply(data.getString("reply"));
				replyInfo.setToUsrId(data.getString("toUsrId"));
				replyInfo.setToUsrName(data.getString("toUsrName"));
				replyInfo.setStatusId(data.getString("StatusId"));
				
				Log.i("add reply info ", data.getString("reply"));
				replyList.add(replyInfo);

				mCache.put("statusResult"+User.Current.grpId+"---"+User.Current.id, (Serializable)dataList);
				context.notifyDataSetChanged();
			}			
			return true;
		}
		
	}
	
	PopupWindow commentPopupWindow;
	EditText replyTextContent;
	StatusListAdapterHandler handler;
	
	private  class  ViewHolder {
		ImageView userLogo;
		TextView userName;
		LinearListView statusPics;
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
	
	private ACache mCache;
	
	public StatusListAdapter(Context context,
			List<HashMap<String, Object>> data) {
		
		// TODO Auto-generated constructor stub
		this.dataList = data;
		this.context = context;
		layoutInflater = (LayoutInflater)LayoutInflater.from(context);
		
		mCache = ACache.get(context);
		handler = new StatusListAdapterHandler((ShareActivity) context,false);
		
		Options sampleOpt = new Options();
		sampleOpt.inJustDecodeBounds = false;
		sampleOpt.inSampleSize = 16;
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.decodingOptions(sampleOpt)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		//Log.i("get count", ""+(this.dataList!=null? this.dataList.size(): 0));
		int num = this.dataList!=null? this.dataList.size(): 0;
		return (num>ShareActivity.showNum)?ShareActivity.showNum : num ;
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
//		Log.i("get view("+dataList.size()+") :", ""+position);
		statusInfo = (StatusListInfo)dataList.get(position).get("statusInfo");
		statusZanInfoList = (List<StatusZanInfo>)dataList.get(position).get("zaninfo");
		//Log.i("zan Len:", ""+statusZanInfoList.size()+"");
		statusReplyInfoList = (List<StatusReplyInfo>)dataList.get(position).get("replyinfo");
		//Log.i("reply Len:", ""+statusReplyInfoList.size()+"");
		
		if(convertView != null ){
			holder = (ViewHolder)convertView.getTag();
		}else {
			convertView = layoutInflater.inflate(R.layout.status_item_layout, null);
			holder = new ViewHolder();
			holder.publish_time = (TextView)convertView.findViewById(R.id.publish_time);
			holder.statusPics = (LinearListView)convertView.findViewById(R.id.status_content_pics);
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
		SimpleDateFormat df=new SimpleDateFormat("MMÔÂddÈÕ   a hhµã"); 
		holder.publish_time.setText(df.format(dt));
		ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+statusInfo.getAvatar(), holder.userLogo,options,null);
		holder.userLogo.setTag(position);
		holder.userLogo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int num = (Integer)arg0.getTag();
				final StatusListInfo statInfo=(StatusListInfo)dataList.get(num).get("statusInfo");
				startStatusOfPersonActivity(statInfo.getUsrId());
			}
		});
		
		holder.userName.setTag(position);
		holder.userName.setText(statusInfo.getName());
		holder.userName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int num = (Integer)arg0.getTag();
//				if(arg0.isHovered())
//					arg0.setBackgroundColor(Color.rgb(169,169,169));
				final StatusListInfo statInfo=(StatusListInfo)dataList.get(num).get("statusInfo");
				startStatusOfPersonActivity(statInfo.getUsrId());
			}
		});
		
		holder.status.setText(statusInfo.getStatus());
		
		
		if(resrc_type.equals("0")){
			 holder.statusPics.setVisibility(8);
		}else if(resrc_type.equals("1")){
			holder.statusPics.setVisibility(0);
			GridViewAdapter myGridViewAdapter = new GridViewAdapter(context,statusInfo.getPicArray(),layoutInflater,statusInfo,options);
			holder.statusPics.setAdapter(myGridViewAdapter);
			holder.statusPics.setTag(position);
			
		}
		
		holder.all_reply_component.setVisibility(8);
		holder.zanText.setText("");
		holder.zanText.setVisibility(View.VISIBLE);

		if(statusZanInfoList.size() > 0 || statusReplyInfoList.size() > 0){
			Log.i("reply and zan info :", "zan : "+statusZanInfoList.size()+"  reply   :"+statusReplyInfoList.size());
			holder.all_reply_component.setVisibility(0);
		}
		
		if(statusZanInfoList.size() > 0) {
			String zanText = "";
			for(int t=0;t<statusZanInfoList.size(); t++){
				zanText = zanText.concat(statusZanInfoList.get(t).getFromUsrName()+"  ");
			}
			holder.zanText.setText(zanText);
		}else{
				holder.zanText.setVisibility(View.GONE);
		}	
		
		ReplyListAdapter replyAdapter = new ReplyListAdapter(context , statusReplyInfoList, position);
		holder.replyList.setAdapter(replyAdapter);
		
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
				
				popupView.findViewById(R.id.zan_area).setTag(pos);
				popupView.findViewById(R.id.zan_area).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						int loc=(Integer) v.getTag();
						SendZan sendZan = new SendZan();
						sendZan.connect(handler);
						sendZan.execute(loc,ShareActivity.userId,statInfo.getUsrId(),statInfo.getStatusId(),ShareActivity.userName);
					}
				});
				
				popupView.findViewById(R.id.comment_area).setTag(pos);
				popupView.findViewById(R.id.comment_area).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.i("press reply", "!!!");
						int loc=(Integer) v.getTag();
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
						btnReplySend.setTag(loc);
						replyTextContent = (EditText)inputWindow.findViewById(R.id.reply_content);
						btnReplySend.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								int loc=(Integer) arg0.getTag();
								SendReply sendReply = new SendReply();
								sendReply.connect(handler);
								sendReply.execute(loc , ShareActivity.userId ,ShareActivity.userName , statInfo.getUsrId() , statInfo.getName() ,statInfo.getStatusId(),
										StringUtils.gbEncoding(replyTextContent.getText().toString()) , replyTextContent.getText().toString());
							}
						});
					}
				});
				
				location = new int[2];
				btnv.getLocationInWindow(location);
				Log.i("location :", location[0] +" "+location[1]);
				commentPopupWindow = new PopupWindow(popupView, 400, 90, true);
				
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

	private void startStatusOfPersonActivity(String  usrId) {
		Intent intent = new Intent(context, StatusOfPersonActivity.class);
		intent.putExtra("usrId", usrId);
		context.startActivity(intent);
	}
	public void setDataList(List<HashMap<String, Object>> dataList) {
		this.dataList = dataList;
	}

///////////////////////////////////////////////////////////////////////////////////////////

class ReplyListAdapter extends BaseAdapter{
	
	 List<StatusReplyInfo> statusReplyInfoList;
	Context context;
	 int statusLocation;

	public ReplyListAdapter(Context context , List<StatusReplyInfo> statusReplyInfoList, int statusLocation){
		this.statusReplyInfoList = statusReplyInfoList;
		this.context = context;
		this.statusLocation = statusLocation;
		Log.i("status position:", statusLocation+"");
	}
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
			replyTextView.setBackgroundResource(R.drawable.reply_text_background);
		}
		
		replyTextView.setTag(position);
		replyTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("press reply", "!!!");
				int loc=(Integer) v.getTag();

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
				btnReplySend.setTag(loc);
				replyTextContent = (EditText)inputWindow.findViewById(R.id.reply_content);
				replyTextContent.setHint("reply to:"+statusReplyInfoList.get(loc).getFromUsrName());
				btnReplySend.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						int loc=(Integer) arg0.getTag();
						StatusReplyInfo reply = statusReplyInfoList.get(loc);
						SendReply sendReply = new SendReply();
						sendReply.connect(handler);
						sendReply.execute(statusLocation , ShareActivity.userId ,ShareActivity.userName , reply.getFromUsrId() , reply.getFromUsrName() ,reply.getStatusId(),
								StringUtils.gbEncoding(replyTextContent.getText().toString()) , replyTextContent.getText().toString());
					}
				});
			}
		});
		
		StatusReplyInfo reply = statusReplyInfoList.get(position);
		
		replyTextView.setText(reply.getFromUsrName()+" reply  "+reply.getToUsrName()+": "+reply.getReply());
		return replyTextView;
		}	
	}
}




///////////////////////////////////////////////////////////////////////////////////////



class SendZan  extends BaseAsyncTask<ShareActivity, Object, Message>{
	@Override
	public Message run(Object... params) throws Exception {
		Message msg = new Message();
		try{
				Log.i("zan info:", params[1]+"  to "+params[2] +" int status "+params[3]);
				PostData pdata=new PostData("share", "postReply", "{\"statusId\":"+params[3]+ ", \"fromUsrId\":"+params[1]+", \"toUsrId\":"+params[2]+", \"type\":1, \"reply\":\"\"}");
				String json=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();
				
				JSONObject result = JSON.parseObject(json);
				if(result.getIntValue("errCode") != 0)
					msg.arg1 = 2;
				else
					msg.arg1 = 1;

				msg.arg2 = (Integer) params[0];
				Bundle data = new Bundle();
				Log.i("zanFromName", (String) params[4]);
				data.putString("fromUsrName", (String) params[4]);
				data.putString("fromUsrId", (String) params[1]);
				
				msg.setData(data);
				System.out.println(json);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return msg;
	}
}


class SendReply  extends BaseAsyncTask<ShareActivity, Object, Message>{
	@Override
	public Message run(Object... params) throws Exception {
//		sendReply.execute(loc , ShareActivity.userId ,ShareActivity.userName , statInfo.getUsrId() , statInfo.getName() ,statInfo.getStatusId(),
//				StringUtils.gbEncoding(replyTextContent.getText().toString()) , replyTextContent.getText().toString());
		Message msg = new Message();
		try{
			Log.i("reply  info:", params[1]+"  to "+params[3] +" int status "+params[5] +"at position  "+params[0]);
			PostData pdata=new PostData("share", "postReply", "{\"statusId\":"+params[5]+ ", \"fromUsrId\":"+params[1]+", \"toUsrId\":"+params[3]+", \"type\":0, \"reply\":\""+StringUtils.gbEncoding((String)params[7])+"\"}");
			String json=new FNHttpRequest(User.Current.loginId, User.Current.password, User.Current.grpId).doPost(pdata).trim();

			JSONObject result = JSON.parseObject(json);
			if(result.getIntValue("errCode") != 0)
				msg.arg1 = 2;
			else
				msg.arg1 = 0 ;
			msg.arg2 = (Integer) params[0];
			Bundle data = new Bundle();
			data.putString("fromUsrName", (String) params[2]);
			data.putString("fromUsrId", (String) params[1]);
			data.putString("toUsrName", (String) params[4]);
			data.putString("toUsrId", (String) params[3]);
			data.putString("reply", (String) params[7]);
			data.putString("StatusId", (String) params[5]);
			
			msg.setData(data);
			System.out.println(json);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return msg;
	}
}

/////////////////////////////////////////////////////////////////////////////////////////////

class GridViewAdapter extends BaseAdapter{
	String[] imageUrls;
	String[] bigImageUrl;
	LayoutInflater layoutInflater; 
	StatusListInfo statusInfo;
	DisplayImageOptions options;
	Context context;
	
	class GridHolder{
		ImageView iamgeView1;
		ImageView iamgeView2;
		ImageView iamgeView3;
		ImageView realImage;
		LinearLayout itemsInLine;
	}
	
	public GridViewAdapter(Context context ,String[] imageurls,LayoutInflater layoutInflater,StatusListInfo statusInfo,DisplayImageOptions options){
		this.context = context;
		this.imageUrls = imageurls;
		this.layoutInflater = layoutInflater;
		this.statusInfo = statusInfo;
		this.options = options;
		
		bigImageUrl = new String[imageUrls.length];
		for(int i=0;i<imageUrls.length;i ++)
			bigImageUrl[i] = "http://"+Constants.Server+""+statusInfo.getBigPicPath()+imageUrls[i];

	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(imageUrls.length < 4)
			return 1;
		else if(imageUrls.length < 7)
			return 2;
		else
			return 3;
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
		GridHolder gridHolder;

		final String statusId = statusInfo.getStatusId();
		if(convertView != null) {
			gridHolder = (GridHolder) convertView.getTag();
		}else {
			convertView = layoutInflater.inflate(R.layout.item_grid_image, parent,false);
			gridHolder = new GridHolder();
			gridHolder.iamgeView1 = (ImageView) convertView.findViewById(R.id.grid_image_item_1);
			gridHolder.iamgeView2 = (ImageView) convertView.findViewById(R.id.grid_image_item_2);
			gridHolder.iamgeView3 = (ImageView) convertView.findViewById(R.id.grid_image_item_3);
			gridHolder.realImage = (ImageView) convertView.findViewById(R.id.grid_image_realitem);
			gridHolder.itemsInLine = (LinearLayout)convertView.findViewById(R.id.items_line);
			convertView.setTag(gridHolder);
		}
		
		gridHolder.iamgeView1.setTag(position);
		gridHolder.iamgeView2.setTag(position);
		gridHolder.iamgeView3.setTag(position);
		gridHolder.realImage.setTag(position);
		
		gridHolder.iamgeView1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int pos = (Integer) v.getTag();
				if(imageUrls.length ==4)
					startImagePagerActivity(pos*2,statusId, bigImageUrl);
				else
					startImagePagerActivity(pos*3,statusId, bigImageUrl);
			}
		});
		
		gridHolder.iamgeView2.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						int pos = (Integer) v.getTag();
						if(imageUrls.length ==4)
							startImagePagerActivity(pos*2+1,statusId, bigImageUrl);
						else
							startImagePagerActivity(pos*3+1,statusId, bigImageUrl);
					}
				});
		
		gridHolder.iamgeView3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int pos = (Integer) v.getTag();
				startImagePagerActivity(pos*3+2,statusId, bigImageUrl);
			}
		});
		
		gridHolder.realImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startImagePagerActivity(0,statusId, bigImageUrl);
			}
		});
		
		if(imageUrls.length == 1){
			gridHolder.iamgeView1.setVisibility(View.GONE);
			gridHolder.iamgeView2.setVisibility(View.GONE);
			gridHolder.iamgeView3.setVisibility(View.GONE);
			gridHolder.realImage.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[0], gridHolder.realImage,options,null);
			
			return convertView;
		}
		//gridHolder.iamgeView.setScaleType(ImageView.ScaleType.FIT_XY);
		//imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		if(imageUrls.length == 4) {
			gridHolder.iamgeView3.setVisibility(View.GONE);
			ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position*2], gridHolder.iamgeView1,options,null);
			ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position+1], gridHolder.iamgeView2,options,null);
			
		}else {
			switch (imageUrls.length - (position*3+3)) {
			case -2:
				ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position*3], gridHolder.iamgeView1,options,null);
				gridHolder.iamgeView3.setVisibility(View.GONE);
				gridHolder.iamgeView2.setVisibility(View.GONE);
				break;

			case -1:
				ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position*3], gridHolder.iamgeView1,options,null);
				ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position*3+1], gridHolder.iamgeView2,options,null);
				gridHolder.iamgeView3.setVisibility(View.GONE);
				break;
				
			default:
				ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position*3], gridHolder.iamgeView1,options,null);
				ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position*3+1], gridHolder.iamgeView2,options,null);
				ImageLoader.getInstance().displayImage("http://"+Constants.Server+""+statusInfo.getSmallPicPath()+imageUrls[position*3+2], gridHolder.iamgeView3,options,null);
				
				break;
			}
		}
		return convertView;
	}
	
	
	public void startImagePagerActivity(int position,String statusId, String[] imageUrls) {
		Intent intent = new Intent(context, StatusImagePagerActivity.class);
		intent.putExtra("statusId", statusId);
		intent.putExtra("images", imageUrls);
		intent.putExtra("position", position);
		context.startActivity(intent);
	}
}
