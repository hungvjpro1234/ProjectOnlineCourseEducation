package com.example.projectonlinecourseeducation.data.review;

import com.example.projectonlinecourseeducation.core.model.course.CourseReview;

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

    // JSON SEED CHO REVIEW KHÓA HỌC
    private static final String REVIEWS_JSON = "[\n" +
            "  {\"id\":\"r1\",\"courseId\":\"c1\",\"userName\":\"Nguyễn Văn B\",\"rating\":4.5,\n" +
            "   \"comment\":\"Mình mới học Java nên khoá này rất hợp, giảng dễ hiểu.\",\"createdAt\":1699000000000},\n" +
            "  {\"id\":\"r2\",\"courseId\":\"c1\",\"userName\":\"Trần Thị C\",\"rating\":4.0,\n" +
            "   \"comment\":\"Nội dung ổn, nếu có thêm bài tập thực hành nữa thì tuyệt.\",\"createdAt\":1699000100000},\n" +
            "  {\"id\":\"r3\",\"courseId\":\"c1\",\"userName\":\"Lê Văn D\",\"rating\":5.0,\n" +
            "   \"comment\":\"Sau khoá này mình đã nắm được OOP và làm project console đơn giản.\",\"createdAt\":1699000200000},\n" +
            "\n" +
            "  {\"id\":\"r4\",\"courseId\":\"c2\",\"userName\":\"Phạm Minh K\",\"rating\":5.0,\n" +
            "   \"comment\":\"Giải thích Spring Boot rõ ràng, phần REST API rất chi tiết.\",\"createdAt\":1699000300000},\n" +
            "  {\"id\":\"r5\",\"courseId\":\"c2\",\"userName\":\"Hoàng Thu H\",\"rating\":4.5,\n" +
            "   \"comment\":\"Hướng dẫn JPA, entity khá thực tế, áp dụng được ngay vào dự án.\",\"createdAt\":1699000400000},\n" +
            "  {\"id\":\"r6\",\"courseId\":\"c2\",\"userName\":\"Đỗ Quốc L\",\"rating\":4.0,\n" +
            "   \"comment\":\"Phần Security cơ bản, hi vọng có thêm phần JWT nâng cao.\",\"createdAt\":1699000500000},\n" +
            "\n" +
            "  {\"id\":\"r7\",\"courseId\":\"c3\",\"userName\":\"Nguyễn Thị M\",\"rating\":4.6,\n" +
            "   \"comment\":\"Mình chuyển từ HTML/CSS sang nên học rất trôi chảy.\",\"createdAt\":1699000600000},\n" +
            "  {\"id\":\"r8\",\"courseId\":\"c3\",\"userName\":\"Vũ Anh T\",\"rating\":4.8,\n" +
            "   \"comment\":\"Phần DOM + mini project giúp mình hiểu JS hơn nhiều.\",\"createdAt\":1699000700000},\n" +
            "  {\"id\":\"r9\",\"courseId\":\"c3\",\"userName\":\"Lương Gia P\",\"rating\":4.2,\n" +
            "   \"comment\":\"Khoá học tốt, nếu có thêm phần ES6 nâng cao sẽ tuyệt hơn.\",\"createdAt\":1699000800000}\n" +
            "]";

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
