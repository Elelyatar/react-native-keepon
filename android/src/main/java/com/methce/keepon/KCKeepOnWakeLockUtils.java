package com.methce.keepon;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class InCallWakeLockUtils {
    private static final String TAG = "InCallWakeLockUtils";

    private PowerManager mPowerManager;

    private WakeLock mFullLock = null;
    private WakeLock mPokeFullLock = null;
    private WakeLock mPartialLock = null;


    public KeepOnWakeLockUtils(Context context) {
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        mFullLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
        mFullLock.setReferenceCounted(false);

        mPartialLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mPartialLock.setReferenceCounted(false);

        mPokeFullLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
        mPokeFullLock.setReferenceCounted(false);
    }

    private boolean _acquireWakeLock(WakeLock lock) {
        return _acquireWakeLock(lock, 0);
    }

    private boolean _acquireWakeLock(WakeLock lock, long timeout) {
        synchronized (lock) {
            if (!lock.isHeld()) {
                if (timeout > 0) {
                    lock.acquire(timeout);
                } else {
                    lock.acquire();
                }
                return true;
            }
        }
        return false;
    }

    private boolean _releaseWakeLock(WakeLock lock) {
        synchronized (lock) {
            if (lock.isHeld()) {
                lock.release();
                return true;
            }
        }
        return false;
    }

    public boolean acquireFullWakeLock() {
        boolean sta = _acquireWakeLock(mFullLock);
        Log.d(TAG, "acquireFullWakeLock(). sta=" + sta);
        return sta;
    }

    public boolean releaseFullWakeLock() {
        boolean sta = _releaseWakeLock(mFullLock);
        Log.d(TAG, "releaseFullWakeLock(). sta=" + sta);
        return sta;
    }

    public boolean acquirePokeFullWakeLock() {
        boolean sta = _acquireWakeLock(mPokeFullLock);
        Log.d(TAG, "acquirePokeFullWakeLock(). sta=" + sta);
        return sta;
    }

    public boolean releasePokeFullWakeLock() {
        boolean sta = _releaseWakeLock(mPokeFullLock);
        Log.d(TAG, "releasePokeFullWakeLock(). sta=" + sta);
        return sta;
    }

    public boolean acquirePartialWakeLock() {
        boolean sta = _acquireWakeLock(mPartialLock);
        Log.d(TAG, "acquirePartialWakeLock(). sta=" + sta);
        return sta;
    }

    public boolean releasePartialWakeLock() {
        boolean sta = _releaseWakeLock(mPartialLock);
        Log.d(TAG, "releasePartialWakeLock(). sta=" + sta);
        return sta;
    }

    public boolean acquirePokeFullWakeLockReleaseAfter(long timeout) {
        boolean sta = _acquireWakeLock(mPokeFullLock, timeout);
        Log.d(TAG, String.format("acquirePokeFullWakeLockReleaseAfter() timeout=%s, sta=%s", timeout, sta));
        return sta;
    }
}