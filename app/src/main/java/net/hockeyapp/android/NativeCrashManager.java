package net.hockeyapp.android;

import android.content.Context;
import androidx.annotation.Keep;

import com.microsoft.appcenter.Constants;
import com.microsoft.office.crashreporting.CrashUtils;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.utils.async.AppCenterConsumer;


public class NativeCrashManager {


    // Initializing native crash reporter is a costly operation. It was adding load in boot time. To overcome this, separated out java and native initialization.

    // Java initialization continues on main thread whereas native initialization gets completed in app thread after the native so files are loaded.

    public static void initialize(Context context) {

            Constants.loadFromContext(context);
            final String appProcessName = CrashUtils.GetRunningProcessName(context);
            Crashes.getMinidumpDirectory().thenAccept(new AppCenterConsumer<String>() {
                @Override
                public void accept(String path) {
                    if (path != null) {
                        setUpBreakpad(path, false, null, appProcessName);
                    }

                }

            });




            // TODO: Appcenter takes different paths for both java and C/CPP code. Currently, CrashUtils takes filepath as "Constants.FILES_PATH",

            // which is specifically for ".dmp" files. Modify crashUtils methods and add trimCrashDumps functionality.

        }





    // The function definition of setUpBreakpad is present in the binary libhockey_exception_handler.so, which we have picked up from

    // the office devmain location: \\modidc-shdw\shadow\16.0\13605.10000\store\droidarm64\ship\android\x-none

    @Keep
    public static native void setUpBreakpad(String filepath, boolean isCurrentSessionAnAppSession, String customLogStatement, String appProcessName);

}