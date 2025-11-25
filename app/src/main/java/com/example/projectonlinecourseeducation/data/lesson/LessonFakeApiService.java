package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.Lesson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LessonFakeApiService implements LessonApi {

    // Singleton
    private static LessonFakeApiService instance;
    public static LessonFakeApiService getInstance() {
        if (instance == null) instance = new LessonFakeApiService();
        return instance;
    }

    // JSON SEED CHO NỘI DUNG KHÓA HỌC (LESSON)
    private static final String LESSONS_JSON = "[\n" +
            "  {\"id\":\"c1_l1\",\"courseId\":\"c1\",\"title\":\"Giới thiệu Java & cài đặt môi trường\",\"duration\":\"09:30\"},\n" +
            "  {\"id\":\"c1_l2\",\"courseId\":\"c1\",\"title\":\"Biến, kiểu dữ liệu & toán tử\",\"duration\":\"18:20\"},\n" +
            "  {\"id\":\"c1_l3\",\"courseId\":\"c1\",\"title\":\"Cấu trúc điều khiển (if, switch, loop)\",\"duration\":\"22:15\"},\n" +
            "  {\"id\":\"c1_l4\",\"courseId\":\"c1\",\"title\":\"Mảng & Collection cơ bản\",\"duration\":\"25:00\"},\n" +
            "  {\"id\":\"c1_l5\",\"courseId\":\"c1\",\"title\":\"Giới thiệu lập trình hướng đối tượng\",\"duration\":\"30:45\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l1\",\"courseId\":\"c2\",\"title\":\"Giới thiệu Spring Boot & tạo project\",\"duration\":\"12:10\"},\n" +
            "  {\"id\":\"c2_l2\",\"courseId\":\"c2\",\"title\":\"Cấu hình REST Controller\",\"duration\":\"20:05\"},\n" +
            "  {\"id\":\"c2_l3\",\"courseId\":\"c2\",\"title\":\"Làm việc với JPA & Entity\",\"duration\":\"24:40\"},\n" +
            "  {\"id\":\"c2_l4\",\"courseId\":\"c2\",\"title\":\"Repository & Service Layer\",\"duration\":\"26:15\"},\n" +
            "  {\"id\":\"c2_l5\",\"courseId\":\"c2\",\"title\":\"Authentication cơ bản với Spring Security\",\"duration\":\"28:30\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l1\",\"courseId\":\"c3\",\"title\":\"Giới thiệu JavaScript & môi trường chạy\",\"duration\":\"08:45\"},\n" +
            "  {\"id\":\"c3_l2\",\"courseId\":\"c3\",\"title\":\"Biến, kiểu dữ liệu & toán tử trong JS\",\"duration\":\"17:30\"},\n" +
            "  {\"id\":\"c3_l3\",\"courseId\":\"c3\",\"title\":\"DOM cơ bản & thao tác thực tế\",\"duration\":\"23:10\"},\n" +
            "  {\"id\":\"c3_l4\",\"courseId\":\"c3\",\"title\":\"Async, callback, promise, async/await\",\"duration\":\"27:20\"},\n" +
            "  {\"id\":\"c3_l5\",\"courseId\":\"c3\",\"title\":\"Mini project: To-do list\",\"duration\":\"32:00\"}\n" +
            "]";

    private LessonFakeApiService() {
        // Không cần seed sẵn, parse trực tiếp từ JSON mỗi lần cho đơn giản
    }

    @Override
    public List<Lesson> getLessonsForCourse(String courseId) {
        List<Lesson> result = new ArrayList<>();
        if (courseId == null) return result;

        try {
            JSONArray arr = new JSONArray(LESSONS_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!courseId.equals(o.optString("courseId"))) continue;

                result.add(new Lesson(
                        o.getString("id"),
                        o.getString("title"),
                        o.optString("duration", "")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
