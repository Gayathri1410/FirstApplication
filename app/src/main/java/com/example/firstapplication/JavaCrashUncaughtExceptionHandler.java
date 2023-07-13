package com.example.firstapplication;

import android.content.Context;
import android.util.Log;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.utils.storage.FileManager;
import com.microsoft.office.crashreporting.CrashUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public  class JavaCrashUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
        private Context context;
        private HockeyAppCrashReporter.OfficeCrashListener listener;

        JavaCrashUncaughtExceptionHandler(Context context, HockeyAppCrashReporter.OfficeCrashListener listener) {

            this.context = context;
            this.listener = listener;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable exception) {
            // Handle the uncaught exception here if needed.
            UUID reportUUID = Crashes.getInstance().saveUncaughtException(thread, exception);
            ArrayList<String> customAttachments = listener.getCustomAttachments();
            if (reportUUID != null) {
                String filename = CrashUtils.getCrashFilePathWithExtension(CrashUtils.javaCrashFolder, reportUUID.toString(), "");
                if (customAttachments != null) {
                    int num_customAttachments = customAttachments.size();
                    File descriptionFile;
                    if (num_customAttachments % 2 == 0) {
                        for (int i = 0; i < num_customAttachments; i += 2) {
                            descriptionFile = new File(filename + customAttachments.get(i + 1));
                            try {
                                FileManager.write(descriptionFile, customAttachments.get(i));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d(Crashes.LOG_TAG, "Saved custom attachment file for ingestion.");
                        }
                    } else {
                        Log.e(Crashes.LOG_TAG, "Followed incorrect format for getCustomAttachments. ArrayList should contain file content followed by file extension for each file. The size of the ArrayList returned must be even.");
                    }
                }
            }
            defaultUncaughtExceptionHandler.uncaughtException(thread, exception);

        }

        void register() {
            defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

