package com.example.firstapplication;


import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import com.microsoft.appcenter.utils.async.AppCenterConsumer;
import net.hockeyapp.android.NativeCrashManager;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {

private static final String logcat = "FIRST app";

    private Button button1;
    private Button button2;

    static {
        System.loadLibrary("crash-lib");
        System.loadLibrary("hockey_exception_handler");
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        AppCenter.start(getApplication(),
                "6033671e-3930-47dc-b11a-7f1c0eb84844",
                Analytics.class,
                Crashes.class);
        Crashes.isEnabled().thenAccept(new AppCenterConsumer<Boolean>() {
            @Override
            public void accept(Boolean isEnabled) {
                if (isEnabled) {
                    new JavaCrashUncaughtExceptionHandler(getApplicationContext(), new HockeyAppCrashReporter.OfficeCrashListener(getApplicationContext(), true)).register();

                }
            }
        });

   NativeCrashManager.initialize(getApplicationContext());
        HockeyAppCrashReporter.OfficeCrashListener officeCrashListener = new HockeyAppCrashReporter.OfficeCrashListener(getApplicationContext(), true);
        try {
            officeCrashListener.watsonCode(WatsonStage.kWatsonStageOne);
        } catch (IOException | NoSuchAlgorithmException | JSONException e) {
            e.printStackTrace();
        }
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);

        String text="hello";
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               Log.d(logcat, "button 1 clicked");
                char c= text.charAt(5);

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(logcat, "button 2 clicked");
             NativeCrashHelper.generateNativeCrash();

            }
        });
    }


}
