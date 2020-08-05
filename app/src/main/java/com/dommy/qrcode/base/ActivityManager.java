package com.zcy.valine.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于处理退出程序时可以退出所有的activity
 * Created by yhl on 2018/3/8.
 */

public class ActivityManager {
    private List<Activity> activityList;
    private static ActivityManager instance;

    private ActivityManager() {
        this.activityList = new ArrayList<>();
    }

    public static ActivityManager getInstance() {
        if (null == instance) {
            instance = new ActivityManager();
        }
        return instance;
    }

    public void add(Activity activity) {
        /*if (activityList.size() > 0) {
            if (!activity.getClass().getName().equals(activityList.get(activityList.size() - 1).getClass().getName()))
                activityList.add(activity);
        } else*/
        activityList.add(activity);
    }

    public int activitySize() {
        if (null != activityList) {
            return activityList.size();
        }
        return 0;
    }

    public Activity getCurrentActivity() {
        if (null != activityList && activityList.size() > 0) {
            return activityList.get(activitySize() - 1);
        }
        return null;
    }

    public void activityFinish() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.finish();
            activityList.remove(activitySize() - 1);
        }
    }

    /**
     * 遍历所有Activity并finish.
     */
    public void clearAllActivity() {
        try {//不加异常捕捉，由搜索股票页面返回后，连按两次back键，模拟器崩溃，手机不会崩溃，待解决   add by yhl
            for (Activity activity : activityList) {
                if (null != activity) {
                    activityFinish();
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}