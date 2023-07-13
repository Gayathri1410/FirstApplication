package com.example.firstapplication;

import android.content.Context;

public class ContextContainer
{
    private static Context sAppContext;

    public static Context getContext()
    {
        return sAppContext;
    }

}
