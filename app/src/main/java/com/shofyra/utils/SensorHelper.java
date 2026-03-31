package com.shofyra.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * SensorHelper — registers & reads device sensors.
 * Covers: Sensors Controlling
 *
 * Uses in Shofyra:
 *  - Accelerometer → shake-to-refresh product list
 *  - Light sensor  → auto-dim UI in low light (complement DayNight theme)
 *  - Proximity     → dim screen when phone is near face (call support)
 */
public class SensorHelper implements SensorEventListener {

    private static final String TAG            = "SensorHelper";
    private static final float  SHAKE_THRESHOLD = 12.0f;
    private static final long   SHAKE_SLOP_MS  = 500;

    private final SensorManager sensorManager;

    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    private long lastShakeTime = 0;

    // Callbacks
    private ShakeListener  shakeListener;
    private LightListener  lightListener;
    private ProxListener   proxListener;

    // ── Interfaces ────────────────────────────────

    public interface ShakeListener  { void onShake(); }
    public interface LightListener  { void onLightLevel(float lux); }
    public interface ProxListener   { void onProximityChange(boolean isNear); }

    // ── Constructor ───────────────────────────────

    public SensorHelper(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            lightSensor    = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximitySensor= sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    // ── Registration ──────────────────────────────

    public void registerShake(ShakeListener listener) {
        this.shakeListener = listener;
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void registerLight(LightListener listener) {
        this.lightListener = listener;
        if (lightSensor != null)
            sensorManager.registerListener(this, lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void registerProximity(ProxListener listener) {
        this.proxListener = listener;
        if (proximitySensor != null)
            sensorManager.registerListener(this, proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterAll() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    // ── SensorEventListener ───────────────────────

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                handleAccelerometer(event.values);
                break;

            case Sensor.TYPE_LIGHT:
                if (lightListener != null) lightListener.onLightLevel(event.values[0]);
                break;

            case Sensor.TYPE_PROXIMITY:
                if (proxListener != null) {
                    boolean near = event.values[0] < proximitySensor.getMaximumRange();
                    proxListener.onProximityChange(near);
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accuracy changed: " + sensor.getName() + " → " + accuracy);
    }

    // ── Shake Detection ───────────────────────────

    private void handleAccelerometer(float[] values) {
        float x = values[0], y = values[1], z = values[2];

        float gForce = (float) Math.sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH;

        if (gForce > SHAKE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > SHAKE_SLOP_MS) {
                lastShakeTime = now;
                Log.d(TAG, "Shake detected! gForce=" + gForce);
                if (shakeListener != null) shakeListener.onShake();
            }
        }
    }

    // ── Sensor Availability ───────────────────────

    public boolean hasAccelerometer()  { return accelerometer   != null; }
    public boolean hasLightSensor()    { return lightSensor      != null; }
    public boolean hasProximitySensor(){ return proximitySensor  != null; }
}