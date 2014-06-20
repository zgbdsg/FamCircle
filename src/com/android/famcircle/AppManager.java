package com.android.famcircle;

import java.util.List;
import java.util.Stack;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;

public class AppManager {
	private static Stack<Activity> mActivityStack;
	private static AppManager mAppManager;

	private AppManager() {
	}

	/**
	 * 单一实例
	 */
	public static AppManager getInstance() {
		if (mAppManager == null) {
			mAppManager = new AppManager();
		}
		return mAppManager;
	}

	/**
	 * 添加Activity到堆�?
	 */
	public void addActivity(Activity activity) {
		if (mActivityStack == null) {
			mActivityStack = new Stack<Activity>();
		}
		mActivityStack.add(activity);
	}

	/**
	 * 获取栈顶Activity（堆栈中�?���?��压入的）
	 */
	public Activity getTopActivity() {
		Activity activity = mActivityStack.lastElement();
		return activity;
	}

	/**
	 * 结束栈顶Activity（堆栈中�?���?��压入的）
	 */
	public void killTopActivity() {
		Activity activity = mActivityStack.lastElement();
		killActivity(activity);
	}

	/**
	 * 结束指定的Activity
	 */
	public void killActivity(Activity activity) {
		if (activity != null) {
			mActivityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void killActivity(Class<?> cls) {
		for (Activity activity : mActivityStack) {
			if (activity.getClass().equals(cls)) {
				killActivity(activity);
			}
		}
	}

	/**
	 * 结束�?��Activity
	 */
	public void killAllActivity() {
		for (int i = 0, size = mActivityStack.size(); i < size; i++) {
			if (null != mActivityStack.get(i)) {
				mActivityStack.get(i).finish();
			}
		}
		mActivityStack.clear();
	}

	/**
	 * �?��应用程序
	 */
	public void AppExit(Context context) {
		try {
			killAllActivity();
			ActivityManager activityMgr = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.restartPackage(context.getPackageName());
			System.exit(0);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Checks if the application is in the background (i.e behind another application's Activity).
	 * 
	 * @param context
	 * @return true if another application is above this one.
	 */
	public static boolean isApplicationBroughtToBackground(final Context context) {
	    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningTaskInfo> tasks = am.getRunningTasks(1);
	    if (!tasks.isEmpty()) {
	        ComponentName topActivity = tasks.get(0).topActivity;
	        if (!topActivity.getPackageName().equals(context.getPackageName())) {
	            return true;
	        }
	    }

	    return false;
	}	
}



//在我们开发应用的时�?，经常会有很多很多的activity，这时�?，我们就�?���?��activity栈来帮忙管理activity的finish和start�?
//就拿OSC的安卓客户端为例，代码使用了�?��stack<Activity>来保存全部的activity�?
//
//
///**
// * 应用程序Activity管理类：用于Activity管理和应用程序�?�?
// */
//
//public class AppManager {
//
//    private static Stack<BaseActivity> activityStack;
//
//    private static AppManager instance;
//
// 
//
//    private AppManager() {
//
//    }
//
// 
//
//    /**
//
//     * 单实�?, UI无需考虑多线程同步问�?
//
//     */
//
//    public static AppManager getAppManager() {
//
//        if (instance == null) {
//            instance = new AppManager();
//        }
//
//        return instance;
//
//    }
//
// 
//
//    /**
//     * 添加Activity到栈
//     */
//
//    public void addActivity(BaseActivity activity) {
//
//        if (activityStack == null) {
//            activityStack = new Stack<BaseActivity>();
//        }
//
//        activityStack.add(activity);
//
//    }
//
// 
//
//    /**
//     * 获取当前Activity（栈顶Activity�?
//     */
//
//    public BaseActivity currentActivity() {
//
//        if (activityStack == null || activityStack.isEmpty()) {
//            return null;
//        }
//
//        BaseActivity activity = activityStack.lastElement();
//
//        return activity;
//
//    }
//
// 
//
//    /**
//
//     * 获取当前Activity（栈顶Activity�?没有找到则返回null
//
//     */
//
//    public BaseActivity findActivity(Class<?> cls) {
//
//        BaseActivity activity = null;
//
//        for (BaseActivity aty : activityStack) {
//
//            if (aty.getClass().equals(cls)) {
//
//                activity = aty;
//
//                break;
//
//            }
//
//        }
//
//        return activity;
//
//    }
//
// 
//
//    /**
//
//     * 结束当前Activity（栈顶Activity�?
//
//     */
//
//    public void finishActivity() {
//
//        BaseActivity activity = activityStack.lastElement();
//
//        finishActivity(activity);
//
//    }
//
// 
//
//    /**
//
//     * 结束指定的Activity(重载)
//
//     */
//
//    public void finishActivity(Activity activity) {
//
//        if (activity != null) {
//
//            activityStack.remove(activity);
//
//            activity.finish();
//
//            activity = null;
//
//        }
//
//    }
//
// 
//
//    /**
//
//     * 结束指定的Activity(重载)
//
//     */
//
//    public void finishActivity(Class<?> cls) {
//
//        for (BaseActivity activity : activityStack) {
//
//            if (activity.getClass().equals(cls)) {
//
//                finishActivity(activity);
//
//            }
//
//        }
//
//    }
//
// 
//
//    /**
//
//     * 关闭除了指定activity以外的全部activity 如果cls不存在于栈中，则栈全部清�?
//
//     * 
//
//     * @param cls
//
//     */
//
//    public void finishOthersActivity(Class<?> cls) {
//
//        for (BaseActivity activity : activityStack) {
//
//            if (!(activity.getClass().equals(cls))) {
//
//                finishActivity(activity);
//
//            }
//
//        }
//
//    }
//
// 
//
//    /**
//
//     * 结束�?��Activity
//
//     */
//
//    public void finishAllActivity() {
//
//        for (int i = 0, size = activityStack.size(); i < size; i++) {
//
//            if (null != activityStack.get(i)) {
//
//                activityStack.get(i).finish();
//
//            }
//
//        }
//
//        activityStack.clear();
//
//    }
//
// 
//
//    /**
//     * 应用程序�?��
//     */
//
//    public void AppExit(Context context) {
//
//        try {
//
//            finishAllActivity();
//
//            ActivityManager activityMgr = (ActivityManager) context
//
//                    .getSystemService(Context.ACTIVITY_SERVICE);
//
//            activityMgr.killBackgroundProcesses(context.getPackageName());
//
//            System.exit(0);
//
//        } catch (Exception e) {
//
//            System.exit(0);
//
//        }
//
//    }
//
//}
//这里是对整个应用的activity操作，可以看到，有�?出应用的方法，关闭指定activity的方法，关闭全部activity的方法，以及关闭除了指定activity以外的全部activity�?
//那么说一下这个类的作用吧，首先，该类使用�?��单例模式去管理，使得整个应用在任何地方都可以访问这个activity栈，这样就方便了应用的操作�?
//例如我们可以这样定义�?��Toast
//
//public static showMessage(String msg){
//    Toast.makeText(AppManager.getAppManager().currentActivity(), msg, Toast.LENGTH_SHORT).show();
//}
//可以看到，我们定义了�?��可以在全�?��用的Toast，不再受Context的限制，当然在使用之前你�?��首先确定你的应用没有被系统销毁�?
//再比如我们有时�?在一个service中做业务处理，然后想返回处理结果的时候，却不知道当时的activity是否依旧存在（它有可能已经被用户关闭），此时就可以使用activity栈获取到当前栈顶的activity通过instanceof关键字判断是否是我们想要的activity�?