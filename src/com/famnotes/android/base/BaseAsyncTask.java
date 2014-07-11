package com.famnotes.android.base;

import com.github.ignition.core.tasks.IgnitedAsyncTask;

/**
 * @author kongx73
 * 请让程序员自己去实现run()
 * @param <ContextT>
 * @param <ParameterT>
 * @param <ReturnT>
 */
public abstract class BaseAsyncTask<ContextT extends BaseActivity, ParameterT, ReturnT> extends IgnitedAsyncTask<ContextT, ParameterT, Integer, ReturnT>  {

}
