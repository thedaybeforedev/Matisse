package com.zhihu.matisse.internal.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import java.util.List;

public class IntentUtils {

    public static void callActionPicks(Activity activity, int requestCode, String packageName){
        Intent intent =  new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (!TextUtils.isEmpty(packageName)){
            intent = getIntentByPackageName(activity, intent, packageName);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public static Intent getIntentByPackageName(Activity activity, Intent intent, String calledPackageName){
        List<ResolveInfo> resolveInfoList = activity.getPackageManager().queryIntentActivities(intent, 0);
        for (int i = 0; i < resolveInfoList.size(); i++) {
            if (resolveInfoList.get(i) != null) {
                String packageName = resolveInfoList.get(i).activityInfo.packageName;
                if (calledPackageName.equals(packageName)) {
                    intent.setComponent(new ComponentName(packageName, resolveInfoList.get(i).activityInfo.name));
                    return intent;
                }
            }
        }
        return intent;
    }
}
