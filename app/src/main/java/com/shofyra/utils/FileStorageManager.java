package com.shofyra.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * FileStorageManager — Internal & External storage utilities.
 * Covers: Data Storage → Internal Storage
 * Uses: invoice PDFs, cached product JSON, receipt images
 */
public class FileStorageManager {

    private static final String DIR_INVOICES = "invoices";
    private static final String DIR_CACHE    = "product_cache";
    private static final String DIR_IMAGES   = "images";

    private final Context context;

    public FileStorageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // ── Text / JSON ───────────────────────────────

    public boolean writeText(String filename, String content) {
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String readText(String filename) {
        try (FileInputStream fis = context.openFileInput(filename)) {
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public boolean deleteFile(String filename) {
        return context.deleteFile(filename);
    }

    // ── Invoice files ─────────────────────────────

    public File getInvoiceDir() {
        File dir = new File(context.getFilesDir(), DIR_INVOICES);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public boolean writeInvoice(String orderId, String htmlContent) {
        File file = new File(getInvoiceDir(), "invoice_" + orderId + ".html");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public File getInvoiceFile(String orderId) {
        return new File(getInvoiceDir(), "invoice_" + orderId + ".html");
    }

    // ── Image caching ─────────────────────────────

    public boolean saveBitmap(String filename, Bitmap bitmap) {
        File dir = new File(context.getCacheDir(), DIR_IMAGES);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, filename + ".png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fos);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Bitmap loadBitmap(String filename) {
        File file = new File(new File(context.getCacheDir(), DIR_IMAGES), filename + ".png");
        if (!file.exists()) return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    // ── Cache management ──────────────────────────

    public long getCacheSize() {
        return getDirSize(context.getCacheDir());
    }

    public void clearCache() {
        deleteDir(context.getCacheDir());
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir == null || !dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            size += f.isDirectory() ? getDirSize(f) : f.length();
        }
        return size;
    }

    private void deleteDir(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) deleteDir(f);
            else f.delete();
        }
    }

    public String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}