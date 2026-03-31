package com.shofyra.ui;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shofyra.R;

/**
 * SensorActivity — Demonstrates Accelerometer, Light, and Proximity sensors.
 * Used in Shofyra for: shake-to-wishlist, auto-brightness adaptation,
 * pocket-detection (pause banner slider when in pocket).
 */
public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, lightSensor, proximitySensor;
    private TextView tvAccelerometer, tvLight, tvProximity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        tvAccelerometer = findViewById(R.id.tv_accelerometer);
        tvLight         = findViewById(R.id.tv_light);
        tvProximity     = findViewById(R.id.tv_proximity);

        sensorManager   = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer   = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor     = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,   SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor,     SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                tvAccelerometer.setText(String.format("Accelerometer\nX: %.2f  Y: %.2f  Z: %.2f",
                        event.values[0], event.values[1], event.values[2]));
                break;
            case Sensor.TYPE_LIGHT:
                tvLight.setText(String.format("Light: %.1f lx", event.values[0]));
                break;
            case Sensor.TYPE_PROXIMITY:
                tvProximity.setText(String.format("Proximity: %.1f cm", event.values[0]));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}