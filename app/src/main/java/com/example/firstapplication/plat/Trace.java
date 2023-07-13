package com.example.firstapplication.plat;




import android.util.Log;




public class Trace {




    /**

     * Log Levels

     */

    public static final int VERBOSE = Log.VERBOSE;

    public static final int DEBUG = Log.DEBUG;

    public static final int INFO = Log.INFO;

    public static final int WARN = Log.WARN;

    public static final int ERROR = Log.ERROR;

    public static final int ASSERT = Log.ASSERT;

    //Variable to handle logs as per the build

    private static int traceBuildlevel = VERBOSE;




    public static void setTraceBuildlevel(int traceBuildlevel){

        Trace.traceBuildlevel = traceBuildlevel;

    }




    private static boolean isLoggableLevel(int level){

        return (level >= traceBuildlevel);

    }




    /**

     * Logging methods for different levels

     */

    public static int a(String tag, String message){

        return print(ASSERT, tag, message);

    }




    public static int a(String tag, String message, Throwable t){

        return print(ASSERT, tag, message, t);

    }




    private static int print(int level, String tag, String message){

        return print(level, tag, message, null);

    }




    private static int print(int level, String tag, String message, Throwable t){

        if(isLoggableLevel(level)){

            switch (level){

                case VERBOSE:

                    return Log.v(tag, message, t);

                case DEBUG:

                    return Log.d(tag, message, t);

                case INFO:

                    return Log.i(tag, message, t);

                case WARN:

                    return Log.w(tag, message, t);

                case ERROR:

                    return Log.e(tag, message, t);

                case ASSERT:

                    /* ideally Log.wtf should be called to handle ASSERT log level but since that can cause the app to

                       terminate, calling Log.e instead. */

                    return Log.e(tag, message, t);

            }

        }

        return 0;

    }

}