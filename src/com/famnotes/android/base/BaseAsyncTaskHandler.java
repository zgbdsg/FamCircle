package com.famnotes.android.base;

//import com.famnotes.android.famnotes.R;
import android.app.ProgressDialog;

import com.github.ignition.core.tasks.IgnitedAsyncTaskHandler;

public abstract class BaseAsyncTaskHandler<ContextT extends BaseActivity, ReturnT> implements IgnitedAsyncTaskHandler<ContextT, Integer, ReturnT> {
    private ContextT context;
    private boolean showProgressBar=true;
    public BaseAsyncTaskHandler(ContextT context) {
        this.context = context;
    }
    public BaseAsyncTaskHandler(ContextT context, boolean showProgressBar) {
        this.context = context;
        this.showProgressBar=showProgressBar;
    }

    @Override
    public final ContextT getContext() {
        return context;
    }

    
    @Override
    public final void setContext(ContextT context) {
        this.context = context;
    }

//================================================================================
	private ProgressDialog mPD;
	/**加载进度条*/
	private void showProgressDialog() {
		//ver 1
//		ProgressDialog progressDialog=new ProgressDialog(this);
//		Drawable drawable=getResources().getDrawable(R.drawable.loading_animation);
//		progressDialog.setIndeterminateDrawable(drawable);
//		progressDialog.setIndeterminate(true);
//		progressDialog.setCancelable(true);
//		progressDialog.setMessage("请稍候，正在努力加载。。");
//		progressDialog.show();
		
		//ver 2
		this.mPD = new ProgressDialog(context);
		this.mPD.setIndeterminate(false);
		this.mPD.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.mPD.show();
			
	}


	@Override
	public boolean onTaskStarted(ContextT context) {
		if(showProgressBar)
			showProgressDialog();
        return true;
	}


	@Override
	public boolean onTaskProgress(ContextT context, Integer... progress) {
		if(showProgressBar)
			mPD.setProgress(progress[0]);
		return true;
	}


	@Override
	public boolean onTaskCompleted(ContextT context, ReturnT result) {
		if(showProgressBar)
			mPD.dismiss();
		return true;
	}
	

//让程序员自己去实现
//	@Override
//	public boolean onTaskSuccess(ContextT context, ReturnT result) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean onTaskFailed(ContextT context, Exception error) {
//		// TODO Auto-generated method stub
//		return false;
//	}

}
