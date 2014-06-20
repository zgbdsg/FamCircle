package com.famnotes.android.base;

public class BaseTaskHandlerPair  {
	public BaseAsyncTask<?, ?, ?> task;
	public BaseAsyncTaskHandler<?, ?> handler;
	
	public BaseTaskHandlerPair(BaseAsyncTask<?, ?, ?> task, BaseAsyncTaskHandler<?, ?> handler) {
		this.task = task;
		this.handler = handler;
	}
	
	
}
