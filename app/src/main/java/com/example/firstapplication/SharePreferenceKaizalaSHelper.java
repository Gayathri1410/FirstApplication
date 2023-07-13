package com.example.firstapplication;

import android.content.Context;

import java.net.MalformedURLException;

public class SharePreferenceKaizalaSHelper {

    private static volatile ISharedPrefAdapter sSharedPrefAdapter;
    private static String sLoggedInUserId;
    private Context mContext;
    public static final String MobileServiceUserIdPrefix = "MobileAppsService:";
    public static final String USER_ID_PREF_KEY = "userId";

    private SharePreferenceKaizalaSHelper(Context context) throws MalformedURLException {
        mContext = context;
    }

    private static class SharePreferenceKaizalasHelperInstanceHolder {
        static final SharePreferenceKaizalaSHelper Instance = createInstance();


        private static SharePreferenceKaizalaSHelper createInstance() {
            try {
                return new SharePreferenceKaizalaSHelper(ContextContainer.getContext());
            } catch (MalformedURLException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static SharePreferenceKaizalaSHelper getInstance() {
        if (sSharedPrefAdapter == null) {
            sSharedPrefAdapter = new DefaultSharedPrefAdapter();
        }
        return SharePreferenceKaizalasHelperInstanceHolder.Instance;
    }

    public static String getCurrentUserId() {
        if (sLoggedInUserId == null && sSharedPrefAdapter != null) {
            String loggedUserId = AppSettings.get(AppSettings.USER_ID_PREF_KEY);
            loggedUserId = (loggedUserId == null) ? sSharedPrefAdapter.getString(USER_ID_PREF_KEY) : loggedUserId;
            if (loggedUserId != null) {
                sLoggedInUserId = ClientUtils.UserIdPrefix + loggedUserId.substring(MobileServiceUserIdPrefix.length());
            }
        }
        return sLoggedInUserId;
    }
}

