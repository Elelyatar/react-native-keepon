package com.methce.keepon;

import android.app.Activity;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class KCKeepOn extends ReactContextBaseJavaModule {

    public KCKeepOn(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "KCKeepOn";
    }

    @ReactMethod
    public void activate() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                }
            });
        }
    }

    @ReactMethod
    public void deactivate() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                    activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                    activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                    activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    }

    @ReactMethod
    public void addLightOut() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_OFF);
                }
            });
        }
    }

    @ReactMethod
    public void RemoveLightOut() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_OFF);
                }
            });
        }
    }
}