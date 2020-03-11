package com.methce.keepon;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.Runnable;

import com.methce.keepon.AppRTC.AppRTCProximitySensor;

public class KeepOnManager {
    private static final String TAG = "KeepOnManager";

    private WakeLock mProximityLock = null;
    private Method mPowerManagerRelease;
    private boolean proximitySupported = false;
    private AppRTCProximitySensor proximitySensor = null;

    /** Construction */
    static KeepOnManager create(Context context, final KeepOn keepOn) {
        return new KeepOnManager(context, keepOn);
    }

    private KeepOnManager(Context context, final KeepOn keepOn) {
        Log.d(TAG, "KeepOnManager");
        checkProximitySupport(context);
        if (proximitySupported) {
            proximitySensor = AppRTCProximitySensor.create(context,
                new Runnable() {
                    @Override
                    public void run() {
                        keepOn.onProximitySensorChangedState(proximitySensor.sensorReportsNearState());               
                    }
                }
            );
        }
    }

    private void checkProximitySupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null) {
            proximitySupported = false;
            return;
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        proximitySupported = true;

        // --- Check if PROXIMITY_SCREEN_OFF_WAKE_LOCK is implemented.
        try {
            boolean _proximitySupported = false;
            Field field = PowerManager.class.getDeclaredField("PROXIMITY_SCREEN_OFF_WAKE_LOCK");
            int proximityScreenOffWakeLock = (Integer) field.get(null);

            if (android.os.Build.VERSION.SDK_INT < 17) {
                Method method = powerManager.getClass().getDeclaredMethod("getSupportedWakeLockFlags");
                int powerManagerSupportedFlags = (Integer) method.invoke(powerManager);
                _proximitySupported = ((powerManagerSupportedFlags & proximityScreenOffWakeLock) != 0x0);
            } else {
                // --- android 4.2+
                Method method = powerManager.getClass().getDeclaredMethod("isWakeLockLevelSupported", int.class);
                _proximitySupported = (Boolean) method.invoke(powerManager, proximityScreenOffWakeLock);
            }

            if (_proximitySupported) {
                mProximityLock = powerManager.newWakeLock(proximityScreenOffWakeLock, TAG);
                mProximityLock.setReferenceCounted(false);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to get proximity screen locker. exception: ", e);
        }

        if (mProximityLock != null) {
            Log.d(TAG, "use native screen locker...");
            try {
                mPowerManagerRelease = mProximityLock.getClass().getDeclaredMethod("release", int.class);
            } catch (Exception e) {
                Log.d(TAG, "failed to get proximity screen locker: `release()`. exception: ", e);
            }
        } else {
            Log.d(TAG, "fallback to old school screen locker...");
        }
    }

    public boolean start() {
        if (!proximitySupported) {
            return false;
        }
        return proximitySensor.start();
    }

    public void stop() {
        proximitySensor.stop();
    }

    public boolean isProximitySupported() {
        return proximitySupported;
    }

    public boolean isProximityWakeLockSupported() {
        return mProximityLock != null;
    }

    public boolean getProximityIsNear() {
        return (proximitySupported) ? proximitySensor.sensorReportsNearState() : false;
    }

    public void acquireProximityWakeLock() {
        if (!isProximityWakeLockSupported()) {
            return;
        }
        synchronized (mProximityLock) {
            if (!mProximityLock.isHeld()) {
                Log.d(TAG, "acquireProximityWakeLock()");
                mProximityLock.acquire();
            }
        }
    }

    public void releaseProximityWakeLock(final boolean waitForNoProximity) {
        if (!isProximityWakeLockSupported()) {
            return;
        }
        synchronized (mProximityLock) {
            if (mProximityLock.isHeld()) {
                try {
                    int flags = waitForNoProximity ? PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY : 0;
                    mPowerManagerRelease.invoke(mProximityLock, flags);
                    Log.d(TAG, "releaseProximityWakeLock()");
                } catch (Exception e) {
                    Log.e(TAG, "failed to release proximity lock. e: ", e);
                }
            }
        }
    }
}