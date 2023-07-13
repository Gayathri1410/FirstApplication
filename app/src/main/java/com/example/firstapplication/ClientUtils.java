package com.example.firstapplication;

import android.text.TextUtils;
import androidx.annotation.Keep;

@Keep

public class ClientUtils {

    public static final String UserIdPrefix = "USR_";

    public static String sanitizeUserId(String userId) {

        if (!TextUtils.isEmpty(userId) && userId.startsWith(UserIdPrefix)) {

            userId = userId.replace(UserIdPrefix, "");

        } else if (!TextUtils.isEmpty(userId) && userId.startsWith(SharePreferenceKaizalaSHelper.MobileServiceUserIdPrefix)) {

            userId = userId.replace(SharePreferenceKaizalaSHelper.MobileServiceUserIdPrefix, "");

        }

        return userId;

    }
}