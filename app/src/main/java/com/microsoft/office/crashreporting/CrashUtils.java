package com.microsoft.office.crashreporting;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Keep;
import static android.util.Log.getStackTraceString;

import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.crashes.ingestion.models.ManagedErrorLog;
import com.microsoft.appcenter.crashes.ingestion.models.StackFrame;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import com.microsoft.appcenter.crashes.model.NativeException;
import com.microsoft.appcenter.crashes.utils.ErrorLogHelper;
import com.microsoft.appcenter.utils.storage.FileManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Keep
public class CrashUtils {

    public static final String PENDINGJAVA_EXT = ".pendingjava";
    public static final String ATTACHMENT_LOGS_EXT = ".attachLogs";
    public static final String ATTACHMENT_METADATA_EXT = ".attachDesc";
    public static final String javaCrashFolder = "error";
    public static final int MAX_NUM_OF_STACKTRACE_LINES = 6;
    public static final int MAX_NUM_OF_CRASHES_PER_DAY = 5;
    public static final String MINIDUMP_FILE = "minidump";
    public static final String NUMBER_OF_CRASH_UPLOADED_TODAY = "NUMBER_OF_CRASH_UPLOADED_TODAY";
    public static final String TAG = "AppCenter";
    public static final String commonCrashFolder = "error";
    public static final String nativeCrashFolder = "error/minidump";
    public static final String JAVA_ATTACHMENT_EXT = ".desc";
    public static final String NATIVE_DUMP_EXT = ".dmp";
    public static final String PROCESS_EXT = ".processtxt"; //the extensions are in sync with the native code file : hockey_exception_handler.cpp, fn : saveCrashedProcessFile
    public static final String LOGS_EXT = ".logs";
    private final static String s_ls = System.getProperty("line.separator");


    public static String getExceptionDetails(Throwable exception)
    {
        StringBuffer sb = new StringBuffer();
        String ls = System.getProperty("line.separator");
        sb.append(exception.toString()).append(ls);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for(StackTraceElement element: stackTrace)
            sb.append(element.toString()).append(ls);

        return sb.toString();
    }
    public static String getCrashFilePathWithExtension(String folder, String reportId, String extension) {
        return Constants.FILES_PATH + File.separator + folder + File.separator  + reportId + extension;
    }

    @Keep
    public static void getAdditionalCrashInfo(String fileName,  Throwable pendingJavaEx)
    {
        Log.e("Project working!", "welcome");
        if(pendingJavaEx != null)
        {
            String pendingExFileName = fileName + PENDINGJAVA_EXT;
            writeToFile(pendingExFileName, getExceptionDetails(pendingJavaEx));
        }
    }

    public static void writeToFile(String fileName, String content) {
        File f = new File(fileName);
        try (BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            bwr.write(content);
        } catch (IOException e) {
        }
    }
    public static String getLogcatLogs(String numOfLines) {
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{
                    "logcat", "-v", "threadtime", "-t", numOfLines, "-d"
            });

            br = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
            for (String s = br.readLine(); s != null; s = br.readLine()) {
                sb.append(s).append('\n');
            }
        } catch (Throwable t) {
            //Do nothing
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (Exception e) {
                //Do nothing as the app anyways crashes after this
            }
        }
        return sb.toString();
    }
    private static int getTotalCrashesUploadedToday(Context context)
    {
        try {
            final SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPreference == null)
            {
                return 0;
            }
            String lastCrashInfo =  sharedPreference.getString(NUMBER_OF_CRASH_UPLOADED_TODAY, null);
            if (TextUtils.isEmpty(lastCrashInfo))
            {
                return 0;
            }
            String todayAsString = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String[] crashInfo = lastCrashInfo.split("-");
            if (crashInfo.length == 2 && todayAsString.compareTo(crashInfo[0]) == 0)
            {
                return Integer.parseInt(crashInfo[1]);
            } else
            {
                return 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting crashes for today");
            return 0;
        }

    }
    public static StringBuilder readFromFile(String fileName)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            File f = new File(fileName);
            if (f.exists())
            {
                BufferedReader reader = new BufferedReader( new FileReader(f));
                String line = null;

                while( ( line = reader.readLine() ) != null ) {
                    sb.append( line );
                    sb.append( s_ls );
                }
                reader.close();
            }
        }
        catch (Exception e)
        {
            Log.v(TAG, "Exception occured while reading from file" + e);
        }
        return sb;
    }

    public static String GetRunningProcessName(Context context) {
        String currentProcName = "";
        try {
            int pid = android.os.Process.myPid();
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    currentProcName = processInfo.processName;
                    break;
                }
            }
        }
        catch(Exception e) {
        }
        return currentProcName;
    }
    public static ErrorReport buildErrorReport(ManagedErrorLog log)
    {
        UUID id = log.getId();

        String stackTrace = null;

        /* If exception in the log doesn't have stack trace try get it from the .throwable file. */
        File file = ErrorLogHelper.getStoredThrowableFile(id);
        if (file != null)
        {
            if (file.length() > 0)
            {
                stackTrace = FileManager.read(file);
            }
        }
        if (stackTrace == null)
        {
            if (MINIDUMP_FILE.equals(log.getException().getType()))
            {
                stackTrace = getStackTraceString(new NativeException());
            } else
            {
                stackTrace = buildStackTrace(log.getException());
            }
        }
        ErrorReport report = ErrorLogHelper.getErrorReportFromErrorLog(log, stackTrace);
        return report;
    }
    public static String getPathIfExistsCommonCrashFilePathWithExtension(String reportId, String extension)
    {
        String filePath = getCrashFilePathWithExtension(commonCrashFolder, reportId, extension);
        File f = new File(filePath);
        if(f.exists())
            return filePath;
        return "";
    }
    public static boolean areCrashesUploadedPerDayExceeded(Context context, int totalProcessedCrashes)
    {
        if (totalProcessedCrashes >= MAX_NUM_OF_CRASHES_PER_DAY ||  getTotalCrashesUploadedToday(context) >= MAX_NUM_OF_CRASHES_PER_DAY)
        {
            return true;
        }
        return false;
    }

    public static String getPathIfExistsNativeCrashMetaFilePathWithExtension(String reportId, final String extension)
    {
        String filePath = getCrashFilePathWithExtension(nativeCrashFolder, reportId, extension);
        File f = new File(filePath);
        if(f.exists())
            return filePath;
        return "";
    }
    public static boolean isNativeCrash(ManagedErrorLog log)
    {
        try
        {
            if (MINIDUMP_FILE.equals(log.getException().getType()))
            {
                return true;
            }
        } catch (Exception e)
        {
            Log.e(TAG, "Exception in reading exception type from log file message: "+ e.getMessage());
        }
        return false;
    }


    public static String buildStackTrace(com.microsoft.appcenter.crashes.ingestion.models.Exception exception)
    {
        String stacktrace = String.format("%s", exception.getType());
        if (exception.getFrames() == null)
        {
            return stacktrace;
        }
        int currentNumberOfLines = 0;
        for (StackFrame frame : exception.getFrames())
        {
            if(currentNumberOfLines > MAX_NUM_OF_STACKTRACE_LINES)
            {
                break;
            }
            currentNumberOfLines++;
            stacktrace += String.format("\n at %s.%s(%s:%s)", frame.getClassName(), frame.getMethodName(), frame.getFileName(), frame.getLineNumber());
        }
        return stacktrace;
    }

}
