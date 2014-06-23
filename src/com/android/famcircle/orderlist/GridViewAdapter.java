package com.android.famcircle.orderlist;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.famcircle.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class GridViewAdapter extends BaseAdapter {
	private  class  ViewHolder {
		ImageView pic;
	}

	private Context context; 
	private LayoutInflater layoutInflater;    
	private List<Picture> list;
	private  ViewHolder holder;
	
	private int level = 0;
	private DisplayImageOptions options;

	public GridViewAdapter(Context context, List<Picture> list, int level)
	{
		this.context = context;        
		layoutInflater = LayoutInflater.from(context);        
		this.list = list;
		this.level = level;
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		if (!ImageLoader.getInstance().isInited()) {
			initImageLoader(context, Environment
					.getExternalStorageDirectory().getAbsolutePath()+ "/iTau/jingdong/"+ "cache/images/");
		}
	}

	//得到总的数量
	public int getCount() 
	{
		// TODO Auto-generated method stub
		return this.list!=null? this.list.size(): 0 ;
	}

	//根据ListView位置返回View
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return this.list.get(position);
	}

	//根据ListView位置得到List中的ID
	public long getItemId(int position) 
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView != null)
		{    
			holder = (ViewHolder) convertView.getTag ( ) ; 
		} else {
			convertView = layoutInflater.inflate(R.layout.order_list_grid_item, null);
			
			int height = 0;
			if (this.level == 0) {
				height = 60;
			} else if (this.level == 1) {
				height = 100;
			} else if (this.level == 2) {
				height = 140;
			} else if (this.level == 3) {
				height = 180;
			}
			
			AbsListView.LayoutParams param = new AbsListView.LayoutParams(
	                android.view.ViewGroup.LayoutParams.FILL_PARENT,
	                height);
	        convertView.setLayoutParams(param);

			holder = new ViewHolder();
			holder.pic = (ImageView) convertView.findViewById(R.id.image);
			
//			holder.pic.setImageResource(Integer.parseInt(list.get(position).get("picture").toString()));
//			holder.pic.setImageResource(list.get(position).getPicID());
			
			ImageLoader.getInstance().displayImage(list.get(position).getUrl(), holder.pic ,options);
			convertView.setTag(holder);           
		}

		
		return convertView;
	}

	class MyAdapterListener implements OnClickListener {
		private int position ;

		MyAdapterListener(int pos) {
			position = pos;
		}

		@Override
		public void onClick(View v) {
			int vid= v.getId ( ) ;
		}
	}
	
	public void addLevel() {
		level++;
	}
	
	public void lowLevel() {
		level--;
	}
	
	public static void initImageLoader(Context context, String cacheDisc) {
		// 配置ImageLoader
		// 获取本地缓存的目录，该目录在SDCard的根目录�?
		File cacheDir = StorageUtils.getOwnCacheDirectory(context, cacheDisc);
		// 实例化Builder
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context);
		// 设置线程数量
		builder.threadPoolSize(3);
		// 设定线程等级比普通低�?��
		builder.threadPriority(Thread.NORM_PRIORITY);
		// 设定内存缓存为弱缓存
		builder.memoryCache(new WeakMemoryCache());
		// 设定内存图片缓存大小限制，不设置默认为屏幕的宽高
		builder.memoryCacheExtraOptions(480, 800);
		// 设定只保存同�?��寸的图片在内�?
		builder.denyCacheImageMultipleSizesInMemory();
		// 设定缓存的SDcard目录，UnlimitDiscCache速度�?��
		builder.discCache(new UnlimitedDiscCache(cacheDir));
		// 设定缓存到SDCard目录的文件命�?
		builder.discCacheFileNameGenerator(new HashCodeFileNameGenerator());
		// 设定网络连接超时 timeout: 10s 读取网络连接超时read timeout: 60s
		builder.imageDownloader(new BaseImageDownloader(context, 10000, 60000));
		// 设置ImageLoader的配置参�?
		builder.defaultDisplayImageOptions(initDisplayOptions(true));

		// 初始化ImageLoader
		ImageLoader.getInstance().init(builder.build());
	}
	
	public static DisplayImageOptions initDisplayOptions(boolean isShowDefault) {
		DisplayImageOptions.Builder displayImageOptionsBuilder = new DisplayImageOptions.Builder();
		// 设置图片缩放方式
		// EXACTLY: 图像将完全按比例缩小的目标大�?
		// EXACTLY_STRETCHED: 图片会缩放到目标大小
		// IN_SAMPLE_INT: 图像将被二次采样的整数�?
		// IN_SAMPLE_POWER_OF_2: 图片将降�?倍，直到下一减少步骤，使图像更小的目标大�?
		// NONE: 图片不会调整
		displayImageOptionsBuilder.imageScaleType(ImageScaleType.EXACTLY);
		if (isShowDefault) {
			// 默认显示的图�?
			displayImageOptionsBuilder.showStubImage(R.drawable.no_image);
			// 地址为空的默认显示图�?
			displayImageOptionsBuilder
					.showImageForEmptyUri(R.drawable.no_image);
			// 加载失败的显示图�?
			displayImageOptionsBuilder.showImageOnFail(R.drawable.no_image);
		}
		// �?��内存缓存
		displayImageOptionsBuilder.cacheInMemory(true);
		// �?��SDCard缓存
		displayImageOptionsBuilder.cacheOnDisc(true);
		// 设置图片的编码格式为RGB_565，此格式比ARGB_8888�?
		displayImageOptionsBuilder.bitmapConfig(Bitmap.Config.RGB_565);

		return displayImageOptionsBuilder.build();
	}
}
