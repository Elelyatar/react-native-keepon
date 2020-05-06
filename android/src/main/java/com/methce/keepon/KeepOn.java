package com.methce.keepon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.Manifest.permission;

import android.net.Uri;
import android.os.PowerManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.Runnable;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class KeepOn extends ReactContextBaseJavaModule implements LifecycleEventListener{

    private static final String REACT_NATIVE_MODULE_NAME = "KeepOn";
    private static final String TAG = REACT_NATIVE_MODULE_NAME;
    private static SparseArray<Promise> mRequestPermissionCodePromises;
    private static SparseArray<String> mRequestPermissionCodeTargetPermission;
    private String mPackageName = "com.methce.keepon";
    private boolean isProximityRegistered = false;
    private boolean proximityIsNear = false;
    // --- Screen Manager
    private PowerManager mPowerManager;
    private WindowManager.LayoutParams lastLayoutParams;
    private WindowManager mWindowManager;
    // -- Proximity Manager
    private final KeepOnManager proximityManager;
    private final KeepOnWakeLockUtils wakeLockUtils;

    @Override
    public String getName() {
        return REACT_NATIVE_MODULE_NAME;
    }

    public KeepOn(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
        mWindowManager = (WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE);
        mPowerManager = (PowerManager) reactContext.getSystemService(Context.POWER_SERVICE);
        mRequestPermissionCodePromises = new SparseArray<Promise>();
        mRequestPermissionCodeTargetPermission = new SparseArray<String>();
        proximityManager = KeepOnManager.create(reactContext, this);
        wakeLockUtils = new KeepOnWakeLockUtils(reactContext);
    }

    private void sendEvent(final String eventName, @Nullable WritableMap params) {
        try {
            ReactContext reactContext = getReactApplicationContext();
            if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(eventName, params);
            } else {
                Log.e(TAG, "sendEvent(): reactContext is null or not having CatalystInstance yet.");
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "sendEvent(): java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!");
        }
    }
    
    @ReactMethod
    public void activate() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().addFlags(
                        lastLayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | lastLayoutParams.FLAG_DISMISS_KEYGUARD
                                | lastLayoutParams.FLAG_KEEP_SCREEN_ON
                                | lastLayoutParams.FLAG_TURN_SCREEN_ON);
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

    private void manualTurnScreenOff() {
        Log.d(TAG, "manualTurnScreenOff()");
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                Activity mCurrentActivity = getCurrentActivity();
                if (mCurrentActivity == null) {
                    Log.d(TAG, "ReactContext doesn't hava any Activity attached.");
                    return;
                }
                Window window = mCurrentActivity.getWindow();
                WindowManager.LayoutParams params = window.getAttributes();
                lastLayoutParams = params; // --- store last param
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF; // --- Dim as dark as possible. see BRIGHTNESS_OVERRIDE_OFF
                window.setAttributes(params);
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    private void manualTurnScreenOn() {
        Log.d(TAG, "manualTurnScreenOn()");
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                Activity mCurrentActivity = getCurrentActivity();
                if (mCurrentActivity == null) {
                    Log.d(TAG, "ReactContext doesn't hava any Activity attached.");
                    return;
                }
                Window window = mCurrentActivity.getWindow();
                if (lastLayoutParams != null) {
                    window.setAttributes(lastLayoutParams);
                } else {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.screenBrightness = -1; // --- Dim to preferable one
                    window.setAttributes(params);
                }
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    public void onProximitySensorChangedState(boolean isNear) {
            if (isNear) {
                turnScreenOff();
            } else {
                turnScreenOn();
            }
        WritableMap data = Arguments.createMap();
        data.putBoolean("isNear", isNear);
        sendEvent("Proximity", data);
    }

    @ReactMethod
    private void startProximitySensor() {
        if (!proximityManager.isProximitySupported()) {
            Log.d(TAG, "Proximity Sensor is not supported.");
            return;
        }
        if (isProximityRegistered) {
            Log.d(TAG, "Proximity Sensor is already registered.");
            return;
        }
        // --- SENSOR_DELAY_FASTEST(0 milisecs), SENSOR_DELAY_GAME(20 milisecs), SENSOR_DELAY_UI(60 milisecs), SENSOR_DELAY_NORMAL(200 milisecs)
        if (!proximityManager.start()) {
            Log.d(TAG, "proximityManager.start() failed. return false");
            return;
        }
        Log.d(TAG, "startProximitySensor()");
        isProximityRegistered = true;
    }

    @ReactMethod
    private void stopProximitySensor() {
        if (!proximityManager.isProximitySupported()) {
            Log.d(TAG, "Proximity Sensor is not supported.");
            return;
        }
        if (!isProximityRegistered) {
            Log.d(TAG, "Proximity Sensor is not registered.");
            return;
        }
        Log.d(TAG, "stopProximitySensor()");
        proximityManager.stop();
        isProximityRegistered = false;
    }

    @ReactMethod
    public void turnScreenOn() {
        if (proximityManager.isProximityWakeLockSupported()) {
            Log.d(TAG, "turnScreenOn(): use proximity lock.");
            proximityManager.releaseProximityWakeLock(true);
        } else {
            Log.d(TAG, "turnScreenOn(): proximity lock is not supported. try manually.");
            manualTurnScreenOn();
        }
    }

    @ReactMethod
    public void turnScreenOff() {
        if (proximityManager.isProximityWakeLockSupported()) {
            Log.d(TAG, "turnScreenOff(): use proximity lock.");
            proximityManager.acquireProximityWakeLock();
        } else {
            Log.d(TAG, "turnScreenOff(): proximity lock is not supported. try manually.");
            manualTurnScreenOff();
        }
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "onResume()");
        //resume();
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onPause()");
        //pause();
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onDestroy()");
        //destroy();
    }
}