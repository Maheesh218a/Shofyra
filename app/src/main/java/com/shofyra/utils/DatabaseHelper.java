package com.shofyra.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper — SQLite for local order history & offline cart cache.
 * Covers: Data Storage → SQLite
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "shofyra.db";
    private static final int    DB_VERSION = 1;

    // ── Orders table ──────────────────────────────
    public static final String TABLE_ORDERS       = "orders";
    public static final String COL_ORDER_ID       = "id";
    public static final String COL_ORDER_DATE     = "order_date";
    public static final String COL_ORDER_TOTAL    = "total";
    public static final String COL_ORDER_STATUS   = "status";
    public static final String COL_ORDER_ITEMS    = "items_json"; // JSON blob

    // ── Search history table ──────────────────────
    public static final String TABLE_SEARCH       = "search_history";
    public static final String COL_SEARCH_ID      = "id";
    public static final String COL_SEARCH_QUERY   = "query";
    public static final String COL_SEARCH_TIME    = "searched_at";

    // ── Wishlist table ────────────────────────────
    public static final String TABLE_WISHLIST     = "wishlist";
    public static final String COL_WISH_ID        = "id";
    public static final String COL_WISH_PROD_ID   = "product_id";
    public static final String COL_WISH_NAME      = "name";
    public static final String COL_WISH_PRICE     = "price";
    public static final String COL_WISH_IMAGE     = "image_url";
    public static final String COL_WISH_CATEGORY  = "category";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context ctx) {
        super(ctx.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ORDER_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_DATE   + " TEXT NOT NULL, " +
                COL_ORDER_TOTAL  + " REAL NOT NULL, " +
                COL_ORDER_STATUS + " TEXT DEFAULT 'Pending', " +
                COL_ORDER_ITEMS  + " TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_SEARCH + " (" +
                COL_SEARCH_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SEARCH_QUERY + " TEXT UNIQUE NOT NULL, " +
                COL_SEARCH_TIME  + " INTEGER NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_WISHLIST + " (" +
                COL_WISH_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WISH_PROD_ID + " TEXT UNIQUE NOT NULL, " +
                COL_WISH_NAME    + " TEXT, " +
                COL_WISH_PRICE   + " REAL, " +
                COL_WISH_IMAGE   + " TEXT, " +
                COL_WISH_CATEGORY+ " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHLIST);
        onCreate(db);
    }

    // ─── Orders ───────────────────────────────────

    public long insertOrder(String date, double total, String itemsJson) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_DATE,  date);
        cv.put(COL_ORDER_TOTAL, total);
        cv.put(COL_ORDER_ITEMS, itemsJson);
        return getWritableDatabase().insert(TABLE_ORDERS, null, cv);
    }

    public List<OrderRecord> getAllOrders() {
        List<OrderRecord> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE_ORDERS, null, null, null, null, null,
                COL_ORDER_DATE + " DESC")) {
            while (c.moveToNext()) {
                OrderRecord r = new OrderRecord();
                r.id        = c.getLong(c.getColumnIndexOrThrow(COL_ORDER_ID));
                r.date      = c.getString(c.getColumnIndexOrThrow(COL_ORDER_DATE));
                r.total     = c.getDouble(c.getColumnIndexOrThrow(COL_ORDER_TOTAL));
                r.status    = c.getString(c.getColumnIndexOrThrow(COL_ORDER_STATUS));
                r.itemsJson = c.getString(c.getColumnIndexOrThrow(COL_ORDER_ITEMS));
                list.add(r);
            }
        }
        return list;
    }

    public void updateOrderStatus(long orderId, String status) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_STATUS, status);
        getWritableDatabase().update(TABLE_ORDERS, cv,
                COL_ORDER_ID + "=?", new String[]{String.valueOf(orderId)});
    }

    // ─── Search History ───────────────────────────

    public void addSearchQuery(String query) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SEARCH_QUERY, query.trim());
        cv.put(COL_SEARCH_TIME, System.currentTimeMillis());
        // REPLACE handles the UNIQUE constraint gracefully
        getWritableDatabase().insertWithOnConflict(TABLE_SEARCH, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<String> getRecentSearches(int limit) {
        List<String> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE_SEARCH, new String[]{COL_SEARCH_QUERY},
                null, null, null, null,
                COL_SEARCH_TIME + " DESC",
                String.valueOf(limit))) {
            while (c.moveToNext()) list.add(c.getString(0));
        }
        return list;
    }

    public void clearSearchHistory() {
        getWritableDatabase().delete(TABLE_SEARCH, null, null);
    }

    // ─── Wishlist ─────────────────────────────────

    public boolean addToWishlist(String productId, String name, double price,
                                 String imageUrl, String category) {
        ContentValues cv = new ContentValues();
        cv.put(COL_WISH_PROD_ID,  productId);
        cv.put(COL_WISH_NAME,     name);
        cv.put(COL_WISH_PRICE,    price);
        cv.put(COL_WISH_IMAGE,    imageUrl);
        cv.put(COL_WISH_CATEGORY, category);
        long result = getWritableDatabase().insertWithOnConflict(
                TABLE_WISHLIST, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public void removeFromWishlist(String productId) {
        getWritableDatabase().delete(TABLE_WISHLIST,
                COL_WISH_PROD_ID + "=?", new String[]{productId});
    }

    public boolean isInWishlist(String productId) {
        try (Cursor c = getReadableDatabase().query(TABLE_WISHLIST,
                new String[]{COL_WISH_ID},
                COL_WISH_PROD_ID + "=?", new String[]{productId},
                null, null, null)) {
            return c.getCount() > 0;
        }
    }

    // ─── Inner record class ───────────────────────

    public static class OrderRecord {
        public long   id;
        public String date;
        public double total;
        public String status;
        public String itemsJson;

        public String getFormattedTotal() {
            return String.format("Rs. %,.0f", total);
        }
    }
}