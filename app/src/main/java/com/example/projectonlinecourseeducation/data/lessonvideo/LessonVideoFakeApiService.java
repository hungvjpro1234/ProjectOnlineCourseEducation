package com.example.projectonlinecourseeducation.data.lessonvideo;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fake API Service cho LessonVideo
 * Mock dữ liệu video của các bài học
 * Sau này có thể thay bằng LessonVideoRemoteApiService kết nối backend
 */
public class LessonVideoFakeApiService implements LessonVideoApi {

    // Singleton
    private static LessonVideoFakeApiService instance;
    public static LessonVideoFakeApiService getInstance() {
        if (instance == null) instance = new LessonVideoFakeApiService();
        return instance;
    }

    // JSON SEED CHO VIDEO CỦA CÁC BÀI HỌC
    // Ghi chú: videoUrl là YouTube Video ID (có thể tự thay đổi nếu cần)
    private static final String LESSONS_VIDEO_JSON = "[\n" +
            "  {\"id\":\"c1_l1\",\"courseId\":\"c1\",\"order\":1,\"title\":\"Giới thiệu Java & cài đặt môi trường\",\n" +
            "   \"description\":\"Bài học này giới thiệu những kiến thức cơ bản về Java, lịch sử phát triển, đặc điểm, và hướng dẫn chi tiết cách cài đặt JDK, IDE để bắt đầu lập trình.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"09:30\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l2\",\"courseId\":\"c1\",\"order\":2,\"title\":\"Biến, kiểu dữ liệu & toán tử\",\n" +
            "   \"description\":\"Tìm hiểu về các kiểu dữ liệu nguyên thủy (primitive types), cách khai báo biến, toán tử số học, so sánh, logic trong Java.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"18:20\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l3\",\"courseId\":\"c1\",\"order\":3,\"title\":\"Cấu trúc điều khiển (if, switch, loop)\",\n" +
            "   \"description\":\"Học cách sử dụng if-else, switch-case, vòng lặp for, while để điều khiển luồng chương trình.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"22:15\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l4\",\"courseId\":\"c1\",\"order\":4,\"title\":\"Mảng & Collection cơ bản\",\n" +
            "   \"description\":\"Làm quen với mảng, ArrayList, HashMap - những cấu trúc dữ liệu quan trọng trong Java.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"25:00\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l5\",\"courseId\":\"c1\",\"order\":5,\"title\":\"Giới thiệu lập trình hướng đối tượng\",\n" +
            "   \"description\":\"Bước đầu tiếp cận OOP: class, object, inheritance, polymorphism, encapsulation - nền tảng cho Java phía sau.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"30:45\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l1\",\"courseId\":\"c2\",\"order\":1,\"title\":\"Giới thiệu Spring Boot & tạo project\",\n" +
            "   \"description\":\"Khám phá Spring Boot, tại sao nó được yêu thích, cách tạo project, cấu trúc thư mục chuẩn.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"12:10\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l2\",\"courseId\":\"c2\",\"order\":2,\"title\":\"Cấu hình REST Controller\",\n" +
            "   \"description\":\"Xây dựng REST API với Spring Boot: @RestController, @RequestMapping, HTTP methods.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"20:05\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l3\",\"courseId\":\"c2\",\"order\":3,\"title\":\"Làm việc với JPA & Entity\",\n" +
            "   \"description\":\"Kết nối database với Spring Boot thông qua JPA, định nghĩa Entity, relationships.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"24:40\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l4\",\"courseId\":\"c2\",\"order\":4,\"title\":\"Repository & Service Layer\",\n" +
            "   \"description\":\"Xây dựng architecture bằng Repository pattern, Service layer để tách biệt business logic.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"26:15\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l5\",\"courseId\":\"c2\",\"order\":5,\"title\":\"Authentication cơ bản với Spring Security\",\n" +
            "   \"description\":\"Bảo mật API với Spring Security: authentication, authorization, JWT basics.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"28:30\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l1\",\"courseId\":\"c3\",\"order\":1,\"title\":\"Giới thiệu JavaScript & môi trường chạy\",\n" +
            "   \"description\":\"JavaScript là gì, tại sao nó quan trọng, setup môi trường (Browser, Node.js), console debugging.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"08:45\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l2\",\"courseId\":\"c3\",\"order\":2,\"title\":\"Biến, kiểu dữ liệu & toán tử trong JS\",\n" +
            "   \"description\":\"var, let, const; string, number, boolean, object; các toán tử, type coercion trong JavaScript.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"17:30\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l3\",\"courseId\":\"c3\",\"order\":3,\"title\":\"DOM cơ bản & thao tác thực tế\",\n" +
            "   \"description\":\"Làm việc với DOM: select elements, thêm/xóa/sửa content, event listeners.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"23:10\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l4\",\"courseId\":\"c3\",\"order\":4,\"title\":\"Async, callback, promise, async/await\",\n" +
            "   \"description\":\"Xử lý bất đồng bộ: callback hell, Promise, async/await, fetch API.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"27:20\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l5\",\"courseId\":\"c3\",\"order\":5,\"title\":\"Mini project: To-do list\",\n" +
            "   \"description\":\"Áp dụng tất cả kiến thức để tạo một ứng dụng To-do list hoàn chỉnh với localStorage.\",\n" +
            "   \"videoUrl\":\"mtL4fOWm3vY\",\"duration\":\"32:00\"}\n" +
            "]";

    private LessonVideoFakeApiService() {
        // Constructor rỗng
    }

    @Override
    public LessonVideo getLessonVideoDetail(String lessonId) {
        if (lessonId == null) return null;

        try {
            JSONArray arr = new JSONArray(LESSONS_VIDEO_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (lessonId.equals(o.optString("id"))) {
                    return new LessonVideo(
                            o.getString("id"),
                            o.getString("title"),
                            o.optString("description", ""),
                            o.getString("videoUrl"),
                            o.optString("duration", ""),
                            o.getInt("order"),
                            o.getString("courseId")
                    );
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}