package com.shofyra.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.shofyra.R;

/**
 * MapsActivity — Google Maps integration for Shofyra Store Locator.
 * Covers: Google Maps
 *
 * Features:
 *  - Show all Shofyra physical store locations
 *  - Current user location
 *  - Custom markers with store info
 *  - Nearest store highlight
 *  - Dark map style in dark mode
 *
 * Required in Manifest (inside <application>):
 *   <meta-data
 *       android:name="com.google.android.geo.API_KEY"
 *       android:value="YOUR_MAPS_API_KEY" />
 *
 * Required permission:
 *   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 *   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 *
 * Add to build.gradle:
 *   implementation 'com.google.android.gms:play-services-maps:18.2.0'
 *   implementation 'com.google.android.gms:play-services-location:21.2.0'
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST = 501;
    private static final float DEFAULT_ZOOM = 13f;

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;

    // Shofyra store locations (Sri Lanka)
    private static final StoreLocation[] STORES = {
            new StoreLocation("Shofyra Matale",        7.4675, 80.6234, "No. 112, Main Street, Matale"),
            new StoreLocation("Shofyra Colombo",       6.9271, 79.8612, "No. 45, Galle Road, Colombo 03"),
            new StoreLocation("Shofyra Kandy",         7.2906, 80.6337, "No. 12, Dalada Veediya, Kandy"),
            new StoreLocation("Shofyra Negombo",       7.2089, 79.8357, "No. 78, Colombo Road, Negombo"),
            new StoreLocation("Shofyra Galle",         6.0535, 80.2210, "No. 23, Main Street, Galle"),
            new StoreLocation("Shofyra Kurunegala",    7.4818, 80.3609, "No. 5, Colombo Road, Kurunegala"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Back button
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Apply dark style if in dark mode
        boolean isDarkMode = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));
        }

        gMap.setOnMarkerClickListener(this);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setCompassEnabled(true);
        gMap.getUiSettings().setMapToolbarEnabled(true);

        // Add store markers
        addStoreMarkers();

        // Request location permission
        requestLocationPermission();
    }

    private void addStoreMarkers() {
        LatLng firstStore = null;

        for (StoreLocation store : STORES) {
            LatLng position = new LatLng(store.lat, store.lng);
            if (firstStore == null) firstStore = position;

            Marker marker = gMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(store.name)
                    .snippet(store.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            if (marker != null) marker.setTag(store);

            // Coverage radius circle
            gMap.addCircle(new CircleOptions()
                    .center(position)
                    .radius(1500) // 1.5km delivery radius
                    .strokeWidth(2f)
                    .strokeColor(0x220057FF)
                    .fillColor(0x110057FF));
        }

        // Camera to first store
        if (firstStore != null) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstStore, DEFAULT_ZOOM));
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        gMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM));
                highlightNearestStore(location);
            }
        });
    }

    private void highlightNearestStore(Location userLocation) {
        StoreLocation nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (StoreLocation store : STORES) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                    store.lat, store.lng, results);
            if (results[0] < minDistance) {
                minDistance = results[0];
                nearest = store;
            }
        }

        if (nearest != null) {
            float distKm = minDistance / 1000f;
            Toast.makeText(this,
                    String.format("Nearest store: %s (%.1f km)", nearest.name, distKm),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        marker.showInfoWindow();
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15f));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    // ── Inner Model ───────────────────────────────

    static class StoreLocation {
        final String name, address;
        final double lat, lng;

        StoreLocation(String name, double lat, double lng, String address) {
            this.name = name; this.lat = lat; this.lng = lng; this.address = address;
        }
    }
}