package com.android.famcircle.util;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;


import com.famnotes.android.famnotes.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;


public class ImageLoaderConfig {

	/**
	 * 返回默认的参数配�?
	 * 
	 * @param isDefaultShow
	 *            true：显示默认的加载图片 false：不显示默认的加载图�?
	 * @return
	 */
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

	/**
	 * 返回修改图片大小的加载参数配�?
	 * 
	 * @return
	 */
	public static DisplayImageOptions initDisplayOptions(int targetWidth,
			boolean isShowDefault) {
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
			displayImageOptionsBuilder
					.showImageOnFail(R.drawable.no_image);
		}
		// �?��内存缓存
		displayImageOptionsBuilder.cacheInMemory(true);
		// �?��SDCard缓存
		displayImageOptionsBuilder.cacheOnDisc(true);
		// 设置图片的编码格式为RGB_565，此格式比ARGB_8888�?
		displayImageOptionsBuilder.bitmapConfig(Bitmap.Config.RGB_565);
		// 设置图片显示方式
		displayImageOptionsBuilder.displayer(new SimpleImageDisplayer(
				targetWidth));

		return displayImageOptionsBuilder.build();
	}

	/**
	 * 异步图片加载ImageLoader的初始化操作，在Application中调用此方法
	 * 
	 * @param context
	 *            上下文对�?
	 * @param cacheDisc
	 *            图片缓存到SDCard的目录，只需要传入SDCard根目录下的子目录即可，默认会建立在SDcard的根目录�?
	 */
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
}
