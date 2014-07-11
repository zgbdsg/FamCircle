package com.famnotes.crop;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.famcircle.R;
import com.famnotes.android.base.BaseActivity;
import com.famnotes.android.util.ImageUtils;
import com.famnotes.crop.util.CropImage;
import com.famnotes.crop.view.CropImageView;

/**
 * 裁剪界面
 *
 */
public class CropImageActivity extends BaseActivity {
	public static final int FLAG_CHOOSE=1;
	public static final int FLAG_HANDLEBACK=2;
	
	private CropImageView mImageView;
	private Bitmap mBitmap;
	
	private CropImage mCrop;
	private int modal;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        
        String path = getIntent().getStringExtra("path");
        modal=getIntent().getIntExtra("modal", 0);
        Log.d("may", "path="+path);
        init(path);
    }
    
    private void init(String path) {
    	
        if(path!=null &&  path.trim().length()>0) {
        	if(mBitmap!=null)
        		mBitmap.recycle();
	        mBitmap = ImageUtils.compImageBySize(path); //BitmapFactory.decodeFile(path);
	        
	        mImageView = (CropImageView) findViewById(R.id.image);
	        mImageView.setImageBitmap(mBitmap);
	        mImageView.setImageBitmapResetBase(mBitmap, true);
	        
	        mCrop = new CropImage(this, mImageView, modal);
	        mCrop.detectionFace(mBitmap);
        }
    }
    
    public void onClick(View v)
    {
    	switch (v.getId()) {
			case R.id.select: {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(intent, FLAG_CHOOSE);
				break;
			}
	    		
	    	case R.id.detect:
	    		
	    		mCrop.detectionFace(mBitmap);
	    		break;
	    		
	    	case R.id.save:  {//先保存到 mBitmap中然后，//?再保存到sdcard目录中
	    		Intent intent = new Intent();
	    		Bitmap cropedBitmap=mCrop.cropAndSave(mBitmap);
	    		Bitmap sndBm=ImageUtils.compressImage(cropedBitmap);//? 没起作用
	    		//?mBitmap.recycle();
	    		String path = mCrop.saveToLocal(sndBm);
	    		cropedBitmap.recycle(); //释放ram
	    		sndBm.recycle();
	    		intent.putExtra("picPath", path);
	    		setResult(RESULT_OK, intent);
	    		finish();
	    		break;
	    	}
    		
    	}
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && null != data) {
			switch (requestCode) {
			case FLAG_CHOOSE:
				Uri uri = data.getData();
				if (!TextUtils.isEmpty(uri.getAuthority())) {
					Cursor cursor = getContentResolver().query(uri,
							new String[] { MediaStore.Images.Media.DATA },
							null, null, null);
					
					if (null == cursor) {
						Toast.makeText(this, R.string.no_found,  Toast.LENGTH_SHORT).show();
						return;
					}
					cursor.moveToFirst();
					String path = cursor.getString(cursor .getColumnIndex(MediaStore.Images.Media.DATA));
					Log.d("may", "path=" + path);
					init(path);
				} 
				break;
			
		    default :
				break;
			}
		}
		
	}    
}