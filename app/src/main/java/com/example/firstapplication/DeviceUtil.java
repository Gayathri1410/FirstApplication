package com.example.firstapplication;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import androidx.annotation.NonNull;
public class DeviceUtil {

    private static String mDeviceId = "";
    private static long NUMBER_OF_BYTES_IN_MEGABYTE = 1048576L; // 1024 * 1024;

    /**
     * we have synced with Office team to understand threat , and they have exempted hardware ID from being PII
     */
    @SuppressLint("HardwareIds")
    public static @NonNull
    String getDeviceId(Context context) {
        if (context == null) return "";
        // if already set, return early as the call has associated perf cost

        if (!Strings.isNullOrEmpty(mDeviceId)) return mDeviceId;

        mDeviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return (mDeviceId == null) ? "" : mDeviceId;
    }

    /**
     * This method will return total memory in device in MB
     * @return total memory in device
     */
    public static long getTotalMemory()
    {
        Context context = ContextContainer.getContext();
        ActivityManager activityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
        if (activityManager == null) {
            return -1;
        }
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.totalMem / NUMBER_OF_BYTES_IN_MEGABYTE;
    }

}
class Strings {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
