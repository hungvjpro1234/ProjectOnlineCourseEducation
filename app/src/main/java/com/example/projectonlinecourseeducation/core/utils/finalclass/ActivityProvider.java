package com.example.projectonlinecourseeducation.core.utils.finalclass;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Simple activity tracker to allow dev-only components (fake API) to obtain the current foreground Activity.
 *
 * Usage:
 * - Call ActivityProvider.init(application) in Application.onCreate()
 * - Call ActivityProvider.getTopActivity() to obtain current resumed Activity (may be null)
 *
 * NOTE: this helper is lightweight and intended for dev/fake usage only.
 */
public final class ActivityProvider implements Application.ActivityLifecycleCallbacks {

    private static ActivityProvider instance;
    private static Activity current;

    private ActivityProvider() { }

    public static synchronized void init(Application app) {
        if (instance == null) {
            instance = new ActivityProvider();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    /**
     * Returns the current foreground Activity (resumed). May be null if none.
     */
    public static Activity getTopActivity() {
        return current;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { /* no-op */ }

    @Override
    public void onActivityStarted(Activity activity) { /* no-op */ }

    @Override
    public void onActivityResumed(Activity activity) {
        current = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (current == activity) current = null;
    }

    @Override
    public void onActivityStopped(Activity activity) { /* no-op */ }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { /* no-op */ }

    @Override
    public void onActivityDestroyed(Activity activity) { /* no-op */ }
}
