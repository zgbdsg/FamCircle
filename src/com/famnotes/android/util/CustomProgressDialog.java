package com.famnotes.android.util;

import com.android.famcircle.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class CustomProgressDialog extends ProgressDialog{
	Context context;
	public CustomProgressDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	public CustomProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.customprogressdialog);
	}
	
	public  CustomProgressDialog show(Context ctx){
//		d = new CustomProgressDialog(ctx);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(true);
		Window wd= this.getWindow();
		LayoutParams lp = wd.getAttributes();
		lp.alpha = 0.7f;
		wd.setAttributes(lp);
		this.show();
		return this;
	}
	
	public void dismissMyDialog(){
		if(this!=null && this.isShowing())
			this.dismiss();
	}
}
