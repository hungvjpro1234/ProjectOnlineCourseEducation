package com.example.projectonlinecourseeducation.data.coursereview;

import com.example.projectonlinecourseeducation.core.model.course.CourseReview;
import static com.example.projectonlinecourseeducation.core.utils.OnlyApiService.ReviewSeedData.REVIEWS_JSON;
import com.example.projectonlinecourseeducation.data.ApiProvider; // NEW

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReviewFakeApiService implements ReviewApi {

    // Singleton
    private static ReviewFakeApiService instance;
    public static ReviewFakeApiService getInstance() {
        if (instance == null) instance = new ReviewFakeApiService();
        return instance;
    }

    // Cache lưu review theo courseId
    private final Map<String, List<CourseReview>> reviewCache = new HashMap<>();

    // Registered listeners
    private final List<ReviewApi.ReviewUpdateListener> listeners = new ArrayList<>();

    private ReviewFakeApiService() {
        initializeFromJson();
    }

    private void initializeFromJson() {
        try {
            JSONArray arr = new JSONArray(REVIEWS_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String courseId = o.optString("courseId");

                CourseReview review = new CourseReview(
                        o.optString("id"),
                        courseId,
                        o.optString("userName", "Ẩn danh"),
                        (float) o.optDouble("rating", 0.0),
                        o.optString("comment", ""),
                        o.optLong("createdAt", System.currentTimeMillis())
                );

                reviewCache.computeIfAbsent(courseId, k -> new ArrayList<>()).add(review);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized List<CourseReview> getReviewsForCourse(String courseId) {
        if (courseId == null) return new ArrayList<>();
        // Trả về bản copy để UI không sửa trực tiếp list bên trong cache
        return new ArrayList<>(reviewCache.getOrDefault(courseId, new ArrayList<>()));
    }

    @Override
    public synchronized CourseReview addReviewToCourse(String courseId, String studentName, float rating, String comment) {
        if (courseId == null) return null;

        // Tạo ID mới cho review
        String reviewId = "review_" + UUID.randomUUID().toString().substring(0, 8);
        long createdAt = System.currentTimeMillis();

        CourseReview review = new CourseReview(
                reviewId,
                courseId,
                studentName != null ? studentName : "Ẩn danh",
                rating,
                comment != null ? comment : "",
                createdAt
        );

        // Lưu vào cache
        reviewCache.computeIfAbsent(courseId, k -> new ArrayList<>()).add(review);

        // Notify listeners rằng review của courseId đã thay đổi
        notifyReviewsChanged(courseId);

        // --- NEW: cập nhật lại rating của course tương ứng ---
        try {
            if (ApiProvider.getCourseApi() != null) {
                ApiProvider.getCourseApi().recalculateCourseRating(courseId);
            }
        } catch (Exception e) {
            // Không để việc cập nhật rating làm crash app
            e.printStackTrace();
        }

        // TODO: Sau này thay thế bằng POST /api/courses/{courseId}/reviews

        return review;
    }

    // ----------------- Listener registration -----------------

    @Override
    public synchronized void addReviewUpdateListener(ReviewApi.ReviewUpdateListener listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    @Override
    public synchronized void removeReviewUpdateListener(ReviewApi.ReviewUpdateListener listener) {
        listeners.remove(listener);
    }

    private synchronized void notifyReviewsChanged(String courseId) {
        // copy to avoid concurrent modification while notifying
        List<ReviewApi.ReviewUpdateListener> copy = new ArrayList<>(listeners);
        for (ReviewApi.ReviewUpdateListener l : copy) {
            try {
                l.onReviewsChanged(courseId);
            } catch (Exception ignored) {
                // ignore listener exceptions to avoid breaking others
            }
        }
    }

    // (Tùy chọn) helper để xóa review — nếu cần có thể thêm notify tương tự
    public synchronized boolean removeReview(String courseId, String reviewId) {
        if (courseId == null || reviewId == null) return false;
        List<CourseReview> list = reviewCache.get(courseId);
        if (list == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (reviewId.equals(list.get(i).getId())) {
                list.remove(i);
                notifyReviewsChanged(courseId);
                return true;
            }
        }
        return false;
    }

    // (Tùy chọn) clear reviews of a course
    public synchronized void clearReviewsForCourse(String courseId) {
        if (courseId == null) return;
        List<CourseReview> list = reviewCache.get(courseId);
        if (list == null || list.isEmpty()) return;
        list.clear();
        notifyReviewsChanged(courseId);
    }
}
