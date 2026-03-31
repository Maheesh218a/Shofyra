package com.shofyra.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NetworkManager — HTTP utility for REST API calls & connectivity checks.
 * Covers: Network Connection → HTTP Connection
 * Uses thread pool for background execution (replaces deprecated AsyncTask pattern).
 */
public class NetworkManager {

    private static final String TAG     = "NetworkManager";
    private static final int TIMEOUT_MS = 15_000;

    private static NetworkManager instance;
    private final Context context;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private NetworkManager(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    public static NetworkManager getInstance(Context ctx) {
        if (instance == null) instance = new NetworkManager(ctx);
        return instance;
    }

    // ── Connectivity ──────────────────────────────

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    public boolean isWifi() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    // ── Callbacks ─────────────────────────────────

    public interface ResponseCallback {
        void onSuccess(String response, int statusCode);
        void onError(String error, int statusCode);
    }

    // ── GET ───────────────────────────────────────

    public void get(String urlString, ResponseCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Accept", "application/json");
                conn.connect();

                int code = conn.getResponseCode();
                String body = readStream(code < 400 ? conn.getInputStream()
                        : conn.getErrorStream());
                if (code >= 200 && code < 300) {
                    callback.onSuccess(body, code);
                } else {
                    callback.onError(body, code);
                }
            } catch (IOException e) {
                Log.e(TAG, "GET failed: " + urlString, e);
                callback.onError(e.getMessage(), -1);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    // ── POST ──────────────────────────────────────

    public void post(String urlString, JSONObject body, ResponseCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(payload.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                }

                int code = conn.getResponseCode();
                String resp = readStream(code < 400 ? conn.getInputStream()
                        : conn.getErrorStream());
                if (code >= 200 && code < 300) callback.onSuccess(resp, code);
                else callback.onError(resp, code);

            } catch (IOException e) {
                Log.e(TAG, "POST failed: " + urlString, e);
                callback.onError(e.getMessage(), -1);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    // ── Currency Conversion (example API use) ─────

    /**
     * Example: Fetch live USD→LKR rate from a public API.
     * Endpoint: https://api.exchangerate-api.com/v4/latest/USD
     */
    public void fetchExchangeRate(String baseCurrency, ResponseCallback callback) {
        get("https://api.exchangerate-api.com/v4/latest/" + baseCurrency, callback);
    }

    // ── Internal ──────────────────────────────────

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append('\n');
        return sb.toString().trim();
    }

    public void shutdown() { executor.shutdown(); }
}