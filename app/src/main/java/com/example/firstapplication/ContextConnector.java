package com.example.firstapplication;

import android.app.Activity;
import android.content.Context;
import com.example.firstapplication.plat.KeepClassAndMembers;
//import com.microsoft.office.plat.annotation.*;
//import com.microsoft.office.plat.logging.Trace;

/**
 * <p>
 * This class contains the application context information for plat.
 * </p>
 */
@KeepClassAndMembers


public class ContextConnector
{
    private static final String LOG_TAG = "ContextConnector";
    /**
     * The singleton instance.
     */
    private static final ContextConnector connector = new ContextConnector();

    /**
     * The context information.
     */
    private Context context;

    /**
     *
     */
    private Context mainActivityContextForAuthDialog = null;

    /**
     *
     */
    private Context childActivityContextForAuthDialog = null;

    /**
     * <p>
     * Default constructor. It is private to prevent creating this class outside the scope of this class.
     * </p>
     */
    private ContextConnector()
    {
    }

    /**
     * <p>
     * Gets the singleton instance.
     * </p>
     *
     * @return the singleton instance.
     */
    public static ContextConnector getInstance()
    {
        return connector;
    }

    /**
     * <p>
     * Gets the context object.
     * </p>
     *
     * @return the context object.
     */
    public Context getContext()
    {
        return this.context;
    }

    /**
     * <p>
     * Sets the context object.
     * </p>
     *
     * @param context
     *            the context object.
     */
    public void setContext(Context context)
    {
        this.context = context;
    }

    /**
     * <p>
     * Clears the context object.
     * </p>
     */
    public void clearContext()
    {
        this.context = null;
    }

    public void setPreferredContextForAuthDialog(Context context)
    {
        if (!(context instanceof Activity))
        {
//            Trace.i(LOG_TAG, "Current context is not Activity context.");
            return;
        }

        if (this.mainActivityContextForAuthDialog == null)
        {
            //intialization time assignement
            this.mainActivityContextForAuthDialog = context;
            return;
        }

        if (mainActivityContextForAuthDialog.toString().equals(context.toString()))
        {
            return;
        }

        if(childActivityContextForAuthDialog == null)
        {
            this.childActivityContextForAuthDialog = context;
            return;
        }

        if (childActivityContextForAuthDialog.toString().equals(context.toString()))
        {
            return;
        }

        if(!isValidActivity(context))
        {
//            Trace.e(LOG_TAG, "current child activity is not valid" + context.toString());
            return;
        }

//        Trace.d(LOG_TAG, "updated with context" + context.toString());
        this.childActivityContextForAuthDialog = context;
    }

    private boolean isValidActivity(Context previousActivityContext)
    {
        if (previousActivityContext == null)
        {
//            Trace.i(LOG_TAG, "previousActivityContext is null ");
            return false;
        }
        return !(((Activity)previousActivityContext).isFinishing() || ((Activity)previousActivityContext).isDestroyed());
    }

    public Context getPreferredContextForAuthDialog()
    {
        if (isValidActivity(childActivityContextForAuthDialog))
        {
//            Trace.i(LOG_TAG, "childActivityContextForAuthDialog is valid");
            return childActivityContextForAuthDialog;
        }
        childActivityContextForAuthDialog = null;
        return this.mainActivityContextForAuthDialog;
    }
}
