package com.example.projectonlinecourseeducation.core.utils;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTubeUtils
 *
 * - Chứa các helper nhỏ liên quan YouTube mà cả UI và provider (fake/remote) có thể dùng.
 * - KHÔNG chứa logic hiển thị hay tạo YouTubePlayerView; chỉ xử lý text/ID.
 *
 * Lý do: tách extractVideoId ra khỏi VideoDurationHelper để UI không phụ thuộc helper dev-only.
 */
public final class YouTubeUtils {

    private YouTubeUtils() { /* util */ }

    /**
     * Extract a YouTube video id from an input which may be:
     *  - plain 11-char id
     *  - full youtube url (watch?v=..., youtu.be/..., embed/...)
     *
     * Returns null if cannot parse.
     */
    @Nullable
    public static String extractVideoId(@Nullable String urlOrId) {
        if (urlOrId == null) return null;
        String s = urlOrId.trim();
        if (s.isEmpty()) return null;

        // quick id check (11 chars)
        if (s.matches("^[a-zA-Z0-9_-]{11}$")) return s;

        // common url patterns
        Matcher m = Pattern.compile("(?:v=|/v/|youtu\\.be/|embed/)([a-zA-Z0-9_-]{11})").matcher(s);
        if (m.find()) return m.group(1);

        // fallback query param
        Matcher m2 = Pattern.compile("v=([a-zA-Z0-9_-]{11})").matcher(s);
        if (m2.find()) return m2.group(1);

        return null;
    }
}
