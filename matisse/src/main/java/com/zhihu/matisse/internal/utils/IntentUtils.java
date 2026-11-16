package com.zhihu.matisse.internal.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import java.util.List;

public class IntentUtils {

    /** 삼성 갤러리가 피커 화면 안에서 최대 선택 수를 막을 때 읽는 extra 키 */
    private static final String EXTRA_SAMSUNG_PICK_MAX_ITEM = "pick-max-item";

    public static void callActionPicks(Activity activity, int requestCode, String packageName){
        callActionPicks(activity, requestCode, packageName, 1);
    }

    /**
     * 외부 갤러리 앱으로 사진 선택 화면을 연다.
     *
     * @param maxSelectableCount	더 고를 수 있는 최대 장수.
     *                          	2장 이상이면 여러 장 선택을 요청하고(EXTRA_ALLOW_MULTIPLE),
     *                          	삼성 갤러리에는 최대 장수도 함께 넘겨 피커 화면 안에서 초과 선택을 막는다.
     *                          	다른 갤러리는 최대 장수를 무시할 수 있으므로 결과 처리에서 한 번 더 잘라낸다.
     */
    public static void callActionPicks(Activity activity, int requestCode, String packageName, int maxSelectableCount){
        Intent intent =  new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (maxSelectableCount > 1) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.putExtra(EXTRA_SAMSUNG_PICK_MAX_ITEM, maxSelectableCount);
        }
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
