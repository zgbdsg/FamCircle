package com.famnotes.android.base;

import java.util.ArrayList;

import com.famnotes.android.util.ACache;



import android.os.Bundle;


public abstract class BaseActivity  extends NoAsyncBaseActivity {

	public static final String TAG = BaseActivity.class.getSimpleName();

	protected ACache mCache;
	public ACache getACache(){
		if(mCache==null)
			mCache = ACache.get(this);
		return mCache;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		batchStop();
	}
	
//=========================以下异步任务处理==========================================================
	ArrayList<BaseTaskHandlerPair>  mTaskHandlerPair=new ArrayList<BaseTaskHandlerPair>();
	protected <ContextT extends BaseActivity, ParameterT, ReturnT> void  connect(BaseAsyncTask<ContextT, ParameterT, ReturnT> task,  BaseAsyncTaskHandler<ContextT, ReturnT> handler){
		task.connect(handler);
		mTaskHandlerPair.add(new BaseTaskHandlerPair(task, handler));
	}
	
//批量执行
//?不通的异步任务可能输入参数不同	
//	public void batchExec(){
//		for(BaseTaskHandlerPair  pair  : mTaskHandlerPair){
//			?? Object params;
//			pair.task.execute(params);
//		}
//	}

	
//批量执行终止
	public void batchStop(){
		for(BaseTaskHandlerPair  pair  : mTaskHandlerPair){
			pair.task.cancel(true);
		}
	}
	
}
