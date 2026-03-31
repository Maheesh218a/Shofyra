package com.shofyra.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSeeder {

    public interface SeedCallback {
        void onComplete(boolean success);
    }

    public void seedDatabase(Context context, SeedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // 1. Categories
        String[][] categoriesData = {
                { "home_kitchen", "Home & Kitchen", "ic_cat_home", "#E3F2FD", "1",
                        "https://images.unsplash.com/photo-1556911220-bff31c812dba" },
                { "mobile_phones", "Mobile Phones & Tablets", "ic_cat_mobile", "#E8F5E9", "2",
                        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9" },
                { "televisions", "Televisions", "ic_cat_tv", "#FFF3E0", "3",
                        "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1" },
                { "laptops_desktops", "Laptops & Desktops", "ic_cat_laptop", "#F3E5F5", "4",
                        "https://images.unsplash.com/photo-1496181133206-80ce9b88a853" },
                { "bathroom_items", "Bathroom Items", "ic_cat_bathroom", "#E0F7FA", "5",
                        "https://images.unsplash.com/photo-1584622650111-993a426fbf0a" }
        };

        for (String[] cat : categoriesData) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cat[0]);
            map.put("name", cat[1]);
            map.put("icon", cat[2]);
            map.put("color_hex", cat[3]);
            map.put("sort_order", Integer.parseInt(cat[4]));
            map.put("categoryImageUrl", cat[5]);
            batch.set(db.collection("categories").document(cat[0]), map);
        }

        // 2. Banners
        String[][] bannersData = {
                { "banner_001", "Mega Electronics Sale", "Up to 40% off on top brands", "MEGA SALE",
                        "https://images.unsplash.com/photo-1550009158-9effb6ba3573", "Shop Now", null, "1" },
                { "banner_002", "New Arrivals This Season", "Latest smartphones & laptops", "NEW",
                        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9", "Explore", "mobile_phones",
                        "2" },
                { "banner_003", "Smart Home Devices", "Transform your living space", "FEATURED",
                        "https://images.unsplash.com/photo-1582218084533-eb89d9e63c0a", "Discover", "home_kitchen",
                        "3" }
        };

        for (String[] banner : bannersData) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", banner[0]);
            map.put("title", banner[1]);
            map.put("subtitle", banner[2]);
            map.put("label", banner[3]);
            map.put("image_url", banner[4]);
            map.put("cta_text", banner[5]);
            map.put("target_category", banner[6]);
            map.put("sort_order", Integer.parseInt(banner[7]));
            map.put("is_active", true);
            batch.set(db.collection("banners").document(banner[0]), map);
        }

        // 3. Products
        Object[][] productsData = {
                { "prod_001", "Samsung Side-by-Side Refrigerator 700L", 85000.0, 95000.0, "home_kitchen",
                        "https://images.unsplash.com/photo-1584568694244-14fbdf83bd30",
                        "Twin Cooling Plus system, large capacity, energy efficient.", 15, 4.6, 320, true, "SALE" },
                { "prod_002", "Panasonic Microwave Oven 32L", 22000.0, 25000.0, "home_kitchen",
                        "https://images.unsplash.com/photo-1585659722983-39cb8ee84ea1",
                        "Inverter technology, 10 power levels, child lock.", 30, 4.3, 158, false, "NEW" },
                { "prod_003", "Philips Air Fryer XXL 7.3L", 18500.0, 0.0, "home_kitchen",
                        "https://images.unsplash.com/photo-1626200419109-38290ce88950",
                        "Rapid Air technology, digital display, fat removal technology.", 20, 4.7, 412, true, "" },
                { "prod_004", "Whirlpool Washing Machine 9kg Front Load", 75000.0, 82000.0, "home_kitchen",
                        "https://images.unsplash.com/photo-1610557892470-55d9e80c0bce",
                        "6th Sense technology, steam refresh, 1400 RPM.", 10, 4.5, 275, false, "SALE" },
                { "prod_005", "Prestige Induction Cooktop 2000W", 8500.0, 0.0, "home_kitchen",
                        "https://images.unsplash.com/photo-1588698425178-01d019f39e8d",
                        "Auto-shut off, feather touch controls, 8 preset menus.", 50, 4.2, 189, false, "" },

                { "prod_006", "Samsung Galaxy S24 Ultra 256GB", 189000.0, 210000.0, "mobile_phones",
                        "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf",
                        "Snapdragon 8 Gen 3, 200MP camera, built-in S Pen, 5000mAh.", 25, 4.8, 634, true, "HOT" },
                { "prod_007", "Apple iPhone 15 Pro 128GB", 195000.0, 0.0, "mobile_phones",
                        "https://images.unsplash.com/photo-1603798125914-7b5d27789248",
                        "A17 Pro chip, titanium design, ProMotion 120Hz, USB-C.", 20, 4.9, 891, true, "NEW" },
                { "prod_008", "Xiaomi Redmi Note 13 Pro 256GB", 55000.0, 62000.0, "mobile_phones",
                        "https://images.unsplash.com/photo-1598327105666-5b89351cb31b",
                        "200MP OIS camera, 120Hz AMOLED, 67W fast charging.", 40, 4.4, 317, false, "SALE" },
                { "prod_009", "Samsung Galaxy Tab S9 FE 128GB", 82000.0, 90000.0, "mobile_phones",
                        "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0",
                        "10.9-inch LCD, Exynos 1380, S Pen included, IP68.", 15, 4.3, 204, false, "" },
                { "prod_010", "Apple iPad Air 5th Gen 64GB WiFi", 115000.0, 0.0, "mobile_phones",
                        "https://images.unsplash.com/photo-1581452140409-eec528ff2275",
                        "M1 chip, 10.9-inch Liquid Retina, USB-C, Touch ID.", 18, 4.7, 445, true, "" },

                { "prod_011", "Samsung 65\" QLED 4K Smart TV", 175000.0, 195000.0, "televisions",
                        "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1",
                        "Quantum Dot, Neo QLED, 120Hz, Dolby Atmos, Tizen OS.", 12, 4.7, 528, true, "SALE" },
                { "prod_012", "LG 55\" OLED evo C3 4K Smart TV", 155000.0, 0.0, "televisions",
                        "https://images.unsplash.com/photo-1592659762303-90081d37b9eb",
                        "α9 AI Processor 4K Gen6, NVIDIA G-Sync, webOS 23.", 8, 4.9, 712, true, "HOT" },
                { "prod_013", "Sony Bravia 50\" X80L 4K Smart TV", 95000.0, 108000.0, "televisions",
                        "https://images.unsplash.com/photo-1593305841991-05c297ba4575",
                        "X1 4K processor, TRILUMINOS Pro, Google TV, Dolby Vision.", 20, 4.5, 389, false, "SALE" },
                { "prod_014", "TCL 43\" P635 4K Smart TV", 48000.0, 55000.0, "televisions",
                        "https://images.unsplash.com/photo-1558882224-dda166733046",
                        "HDR10+, Dolby Audio, Google TV, Chromecast built-in.", 35, 4.2, 231, false, "" },
                { "prod_015", "Hisense 75\" U7H ULED 4K Smart TV", 138000.0, 155000.0, "televisions",
                        "https://plus.unsplash.com/premium_photo-1681233760431-7e74288cb4db",
                        "Mini-LED, 120Hz VRR, Dolby Vision IQ, VIDAA U6.", 6, 4.6, 165, true, "SALE" },

                { "prod_016", "Apple MacBook Air M2 13\" 8GB 256GB", 185000.0, 0.0, "laptops_desktops",
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
                        "M2 chip, Liquid Retina display, 18hr battery, fanless.", 10, 4.9, 823, true, "NEW" },
                { "prod_017", "Dell XPS 15 Intel i7 32GB 1TB SSD", 225000.0, 245000.0, "laptops_desktops",
                        "https://images.unsplash.com/photo-1593642632823-8f785ba67e45",
                        "15.6\" OLED touch, RTX 4060, Thunderbolt 4, 86Whr.", 7, 4.7, 412, true, "" },
                { "prod_018", "HP Victus 15 Gaming Laptop i5 16GB", 115000.0, 128000.0, "laptops_desktops",
                        "https://images.unsplash.com/photo-1603302576837-37561b2e2302",
                        "144Hz FHD, GTX 1650, 512GB SSD, Omen Tempest cooling.", 15, 4.3, 278, false, "SALE" },
                { "prod_019", "Lenovo IdeaPad 3 Ryzen 5 8GB 512GB", 68000.0, 75000.0, "laptops_desktops",
                        "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2",
                        "15.6\" FHD, Radeon graphics, rapid charge, Windows 11.", 25, 4.1, 196, false, "" },
                { "prod_020", "Acer Predator Helios 300 i7 16GB", 185000.0, 200000.0, "laptops_desktops",
                        "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed",
                        "15.6\" QHD 165Hz, 1TB SSD, Advanced Optimus, MUX switch.", 9, 4.6, 344, false, "HOT" },

                { "prod_021", "Panasonic Hair Dryer EH-NA65 2300W", 12500.0, 15000.0, "bathroom_items",
                        "https://images.unsplash.com/photo-1522337660859-02fbefca4702",
                        "Nanoe technology, 4 settings, ionizing, folding handle.", 40, 4.5, 287, true, "" },
                { "prod_022", "Philips Electric Shaver Series 5000", 28000.0, 32000.0, "bathroom_items",
                        "https://images.unsplash.com/photo-1621215443217-0ccae2ba5108",
                        "Wet & dry, SmartClick beard trimmer, 60min runtime.", 30, 4.4, 198, false, "SALE" },
                { "prod_023", "Oral-B iO Series 7 Electric Toothbrush", 22000.0, 26000.0, "bathroom_items",
                        "https://images.unsplash.com/photo-1502575402098-b8ce7b7899be",
                        "AI-powered, 7 brushing modes, pressure sensor, 2wk battery.", 25, 4.6, 412, true, "NEW" },
                { "prod_024", "Waterpik Aquarius Water Flosser", 18500.0, 0.0, "bathroom_items",
                        "https://images.unsplash.com/photo-1584622650111-993a426fbf0a",
                        "10 pressure settings, 7 tips included, 90s tank capacity.", 20, 4.7, 356, false, "" },
                { "prod_025", "Dyson Supersonic Hair Dryer HD08", 75000.0, 82000.0, "bathroom_items",
                        "https://images.unsplash.com/photo-1522337660859-02fbefca4702",
                        "Intelligent heat control, 5 attachments, 13 settings.", 12, 4.8, 534, true, "HOT" }
        };

        for (Object[] prod : productsData) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", prod[0]);
            map.put("name", prod[1]);
            map.put("price", prod[2]);
            map.put("original_price", prod[3]);
            map.put("category_id", prod[4]);
            map.put("image_url", prod[5]);
            map.put("description", prod[6]);
            map.put("stock", prod[7]);
            map.put("rating", prod[8]);
            map.put("review_count", prod[9]);
            map.put("is_featured", prod[10]);
            map.put("badge", prod[11]);
            map.put("created_at", System.currentTimeMillis());

            batch.set(db.collection("products").document((String) prod[0]), map);
        }

        // Commit all
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseSeeder", "Database seeded successfully.");
                    if (callback != null)
                        callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseSeeder", "Error seeding database", e);
                    if (callback != null)
                        callback.onComplete(false);
                });
    }
}
