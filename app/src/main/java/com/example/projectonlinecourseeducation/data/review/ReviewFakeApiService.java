package com.example.projectonlinecourseeducation.data.review;

import com.example.projectonlinecourseeducation.core.model.CourseReview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
            "   \"comment\":\"Mình mới học Java nên khoá này rất hợp, giảng dễ hiểu.\"},\n" +
            "  {\"id\":\"r2\",\"courseId\":\"c1\",\"userName\":\"Trần Thị C\",\"rating\":4.0,\n" +
            "   \"comment\":\"Nội dung ổn, nếu có thêm bài tập thực hành nữa thì tuyệt.\"},\n" +
            "  {\"id\":\"r3\",\"courseId\":\"c1\",\"userName\":\"Lê Văn D\",\"rating\":5.0,\n" +
            "   \"comment\":\"Sau khoá này mình đã nắm được OOP và làm project console đơn giản.\"},\n" +
            "\n" +
            "  {\"id\":\"r4\",\"courseId\":\"c2\",\"userName\":\"Phạm Minh K\",\"rating\":5.0,\n" +
            "   \"comment\":\"Giải thích Spring Boot rõ ràng, phần REST API rất chi tiết.\"},\n" +
            "  {\"id\":\"r5\",\"courseId\":\"c2\",\"userName\":\"Hoàng Thu H\",\"rating\":4.5,\n" +
            "   \"comment\":\"Hướng dẫn JPA, entity khá thực tế, áp dụng được ngay vào dự án.\"},\n" +
            "  {\"id\":\"r6\",\"courseId\":\"c2\",\"userName\":\"Đỗ Quốc L\",\"rating\":4.0,\n" +
            "   \"comment\":\"Phần Security cơ bản, hi vọng có thêm phần JWT nâng cao.\"},\n" +
            "\n" +
            "  {\"id\":\"r7\",\"courseId\":\"c3\",\"userName\":\"Nguyễn Thị M\",\"rating\":4.6,\n" +
            "   \"comment\":\"Mình chuyển từ HTML/CSS sang nên học rất trôi chảy.\"},\n" +
            "  {\"id\":\"r8\",\"courseId\":\"c3\",\"userName\":\"Vũ Anh T\",\"rating\":4.8,\n" +
            "   \"comment\":\"Phần DOM + mini project giúp mình hiểu JS hơn nhiều.\"},\n" +
            "  {\"id\":\"r9\",\"courseId\":\"c3\",\"userName\":\"Lương Gia P\",\"rating\":4.2,\n" +
            "   \"comment\":\"Khoá học tốt, nếu có thêm phần ES6 nâng cao sẽ tuyệt hơn.\"}\n" +
            "]";

    private ReviewFakeApiService() {
    }

    @Override
    public List<CourseReview> getReviewsForCourse(String courseId) {
        List<CourseReview> result = new ArrayList<>();
        if (courseId == null) return result;

        try {
            JSONArray arr = new JSONArray(REVIEWS_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!courseId.equals(o.optString("courseId"))) continue;

                float rating = (float) o.optDouble("rating", 0.0);
                result.add(new CourseReview(
                        o.optString("userName", "Ẩn danh"),
                        rating,
                        o.optString("comment", "")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
