// app/src/main/java/com/example/projectonlinecourseeducation/core/utils/ImageLoader.java
package com.example.projectonlinecourseeducation.core.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Trình tải ảnh tối giản, không cần thư viện ngoài.
 * - Có cache RAM bằng LruCache
 * - Tải nền bằng threadpool
 * - Tránh gán nhầm ảnh nhờ setTag(url)
 */
public class ImageLoader {

    private static ImageLoader instance;

    public static ImageLoader getInstance() {
        if (instance == null) instance = new ImageLoader();
        return instance;
    }

    private final LruCache<String, Bitmap> memoryCache;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Handler main = new Handler(Looper.getMainLooper());

    private ImageLoader() {
        final int maxMem = (int) (Runtime.getRuntime().maxMemory() / 1024); // KB
        final int cacheSize = maxMem / 8; // 1/8 bộ nhớ cho cache
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024; // KB
            }
        };
    }

    public void display(String url, ImageView target, int placeholderResId) {
        target.setTag(url);
        if (placeholderResId != 0) target.setImageResource(placeholderResId);

        Bitmap cached = memoryCache.get(url);
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }

        executor.execute(() -> {
            Bitmap bmp = downloadBitmap(url);
            if (bmp != null) {
                memoryCache.put(url, bmp);
                main.post(() -> {
                    // Chỉ gán nếu ImageView vẫn đang cần url này
                    Object tag = target.getTag();
                    if (tag != null && tag.equals(url)) {
                        target.setImageBitmap(bmp);
                    }
                });
            }
        });
    }

    private Bitmap downloadBitmap(String urlStr) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            is = conn.getInputStream();
            return BitmapFactory.decodeStream(is);
        } catch (Exception ignored) {
            return null;
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }
}
