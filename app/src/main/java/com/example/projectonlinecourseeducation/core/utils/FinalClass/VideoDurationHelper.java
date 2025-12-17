package com.example.projectonlinecourseeducation.core.utils.FinalClass;

import android.app.Activity;
import android.app.Dialog;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VideoDurationHelper
 *
 * INTERNAL USE ONLY (dev / fake API).
 * - Dùng để mock việc backend tính thời lượng video khi chạy local dev.
 * - Không khuyến khích Activity hoặc production code trực tiếp gọi helper này.
 *
 * NOTE: giữ public để LessonFakeApiService (khác package) có thể gọi.
 */
public final class VideoDurationHelper {

    private VideoDurationHelper() { /* util */ }

    public interface Callback {
        void onSuccess(@NonNull String durationText /* mm:ss */, int durationSeconds);
        void onError(@NonNull String reason);
    }

    @Nullable
    public static String extractVideoId(@Nullable String urlOrId) {
        if (urlOrId == null) return null;
        String s = urlOrId.trim();
        if (s.isEmpty()) return null;

        // quick id check
        if (s.matches("^[a-zA-Z0-9_-]{11}$")) return s;

        Matcher m = Pattern.compile("(?:v=|/v/|youtu\\.be/|embed/)([a-zA-Z0-9_-]{11})").matcher(s);
        if (m.find()) return m.group(1);

        Matcher m2 = Pattern.compile("v=([a-zA-Z0-9_-]{11})").matcher(s);
        if (m2.find()) return m2.group(1);

        return null;
    }

    /**
     * Fetch duration using android-youtube-player.
     * Must be called with a foreground Activity.
     * Timeout default = 10 seconds.
     *
     * NOTE: This helper creates a tiny Dialog with a YouTubePlayerView to cue the video
     * and receive duration metadata. It's intended for dev/mock usage only.
     */
    public static void fetchDuration(@NonNull Activity activity,
                                     @NonNull String urlOrId,
                                     @NonNull Callback cb) {

        final String videoId = extractVideoId(urlOrId);
        if (videoId == null) {
            cb.onError("Không lấy được videoId từ: " + urlOrId);
            return;
        }

        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);

        FrameLayout container = new FrameLayout(activity);
        YouTubePlayerView youTubePlayerView = new YouTubePlayerView(activity);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(youTubePlayerView, lp);
        dialog.setContentView(container);

        // Register lifecycle observer when possible (improves stability)
        try {
            if (activity instanceof androidx.fragment.app.FragmentActivity) {
                ((androidx.fragment.app.FragmentActivity) activity).getLifecycle().addObserver(youTubePlayerView);
            }
        } catch (Throwable ignored) {}

        try {
            dialog.show();
        } catch (Throwable ignored) {}

        final long timeoutMs = 10_000L;
        final boolean[] done = new boolean[]{false};

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                try {
                    // cue video (not autoplay) - enough to get metadata
                    youTubePlayer.cueVideo(videoId, 0f);
                } catch (Exception e) {
                    if (!done[0]) {
                        done[0] = true;
                        cleanup();
                        cb.onError("Load video failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                if (done[0]) return;
                if (duration <= 0f) return;
                // success
                int secs = Math.round(duration);
                String mmss = formatSeconds(secs);
                done[0] = true;
                cleanup();
                cb.onSuccess(mmss, secs);
            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError error) {
                if (done[0]) return;
                done[0] = true;
                cleanup();
                cb.onError("YouTubePlayer error: " + error.name());
            }

            private void cleanup() {
                try { youTubePlayerView.release(); } catch (Exception ignored) {}
                try { if (dialog.isShowing()) dialog.dismiss(); } catch (Exception ignored) {}
            }
        });

        // timeout fallback
        activity.getWindow().getDecorView().postDelayed(() -> {
            if (!done[0]) {
                try {
                    if (dialog.isShowing()) {
                        try { youTubePlayerView.release(); } catch (Exception ignored) {}
                        try { dialog.dismiss(); } catch (Exception ignored) {}
                        cb.onError("Timeout khi lấy duration (10s).");
                    }
                } catch (Throwable ignored) {}
            }
        }, timeoutMs);
    }

    private static String formatSeconds(int secs) {
        int m = secs / 60;
        int s = secs % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }
}
