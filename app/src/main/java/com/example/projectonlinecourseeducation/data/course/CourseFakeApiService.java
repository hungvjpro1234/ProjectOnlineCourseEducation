package com.example.projectonlinecourseeducation.data.course;

import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.core.model.CourseLesson;
import com.example.projectonlinecourseeducation.core.model.CourseReview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// DÙNG Sort từ CourseApi


// IMPLEMENTS CourseApi
public class CourseFakeApiService implements CourseApi {

    // --------------------------------------------------------------------
    // Singleton
    // --------------------------------------------------------------------
    private static CourseFakeApiService instance;
    public static CourseFakeApiService getInstance() {
        if (instance == null) instance = new CourseFakeApiService();
        return instance;
    }

    private final List<Course> allCourses = new ArrayList<>();

    // dùng cho create khóa học mới (vì đã có c1..c3)
    private int nextId = 100;

    private String generateNewId() {
        return "c" + (nextId++);
    }

    // --------------------------------------------------------------------
    // 1) JSON SEED CHO TẤT CẢ KHÓA HỌC
    //    (giống như response /courses từ backend)
    //    -> ĐÃ RÚT GỌN CÒN 3 KHÓA
    // --------------------------------------------------------------------
    private static final String COURSES_JSON = "[\n" +
            "  {\n" +
            "    \"id\":\"c1\",\n" +
            "    \"title\":\"Java Cơ Bản\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/j1/640/360\",\n" +
            "    \"category\":\"Java, OOP, Backend\",\n" +
            "    \"lectures\":45,\n" +
            "    \"students\":1200,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":199000,\n" +
            "    \"description\":\"Khóa học Java cơ bản cho người mới bắt đầu, đi từ cú pháp đến OOP và thực hành.\",\n" +
            "    \"createdAt\":\"03/2024\",\n" +
            "    \"ratingCount\":120,\n" +
            "    \"totalDurationMinutes\":720,\n" +
            "    \"skills\":[\"Nắm cú pháp\", \"Hiểu OOP\", \"Collection\", \"Build console app đơn giản\"],\n" +
            "    \"requirements\":[\"Biết máy tính cơ bản\", \"Dành 6-8 giờ/tuần\", \"Cài JDK\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c2\",\n" +
            "    \"title\":\"Java Web với Spring Boot\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/j3/640/360\",\n" +
            "    \"category\":\"Java, Spring Boot, Backend\",\n" +
            "    \"lectures\":55,\n" +
            "    \"students\":1500,\n" +
            "    \"rating\":4.8,\n" +
            "    \"price\":299000,\n" +
            "    \"description\":\"Xây dựng REST API và ứng dụng web backend với Spring Boot, JPA và Security.\",\n" +
            "    \"createdAt\":\"05/2024\",\n" +
            "    \"ratingCount\":210,\n" +
            "    \"totalDurationMinutes\":900,\n" +
            "    \"skills\":[\"Spring Boot cơ bản\", \"REST API\", \"JPA/Hibernate\", \"Spring Security cơ bản\"],\n" +
            "    \"requirements\":[\"Nắm Java cơ bản\", \"Kiến thức OOP\", \"Biết SQL cơ bản\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c3\",\n" +
            "    \"title\":\"JavaScript Cơ Bản Đến Nâng Cao\",\n" +
            "    \"teacher\":\"Trần B\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/js1/640/360\",\n" +
            "    \"category\":\"JavaScript, HTML, CSS, Frontend\",\n" +
            "    \"lectures\":50,\n" +
            "    \"students\":2000,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":199000,\n" +
            "    \"description\":\"Học JavaScript từ cơ bản đến nâng cao, thao tác DOM và làm mini project.\",\n" +
            "    \"createdAt\":\"01/2024\",\n" +
            "    \"ratingCount\":220,\n" +
            "    \"totalDurationMinutes\":780,\n" +
            "    \"skills\":[\"Cú pháp JS cơ bản\", \"DOM manipulation\", \"Async/await\", \"Build web mini project\"],\n" +
            "    \"requirements\":[\"Biết HTML/CSS cơ bản\", \"Biết sử dụng trình duyệt\"]\n" +
            "  }\n" +
            "]";

    // JSON SEED CHO NỘI DUNG KHÓA HỌC (LESSON)
    // Mỗi object tương đương 1 record trong bảng lessons: id, courseId, title, duration
    // Mô phỏng cho endpoint: GET /courses/{id}/lessons
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

    // JSON SEED CHO REVIEW KHÓA HỌC
    // Mỗi object tương đương 1 record trong bảng reviews: id, courseId, userName, rating, comment
    // Mô phỏng cho endpoint: GET /courses/{id}/reviews
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

    private CourseFakeApiService() {
        seedFromJson();
    }

    private void seedFromJson() {
        allCourses.clear();
        try {
            JSONArray arr = new JSONArray(COURSES_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                List<String> skills = jsonArrayToList(o.optJSONArray("skills"));
                List<String> requirements = jsonArrayToList(o.optJSONArray("requirements"));

                Course c = new Course(
                        o.getString("id"),
                        o.getString("title"),
                        o.getString("teacher"),
                        o.getString("imageUrl"),
                        o.getString("category"),
                        o.getInt("lectures"),
                        o.getInt("students"),
                        o.getDouble("rating"),
                        o.getDouble("price"),
                        o.optString("description", ""),
                        o.optString("createdAt", ""),
                        o.optInt("ratingCount", 0),
                        o.optInt("totalDurationMinutes", 0),
                        skills,
                        requirements
                );
                allCourses.add(c);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<String> jsonArrayToList(JSONArray arr) throws JSONException {
        List<String> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.getString(i));
        }
        return list;
    }

    // --------------------------------------------------------------------
    // Helper cho category multi-tag
    // --------------------------------------------------------------------

    // category kiểu "Java, Python, SQL" -> kiểm tra có chứa wanted hay không
    private boolean hasCategory(String categories, String wanted) {
        if (categories == null || wanted == null) return false;
        String[] parts = categories.split(",");
        for (String p : parts) {
            if (p != null && p.trim().equalsIgnoreCase(wanted.trim())) {
                return true;
            }
        }
        return false;
    }

    // Hai chuỗi category có ít nhất 1 tag trùng nhau hay không
    private boolean shareCategory(String cat1, String cat2) {
        if (cat1 == null || cat2 == null) return false;
        String[] a1 = cat1.split(",");
        String[] a2 = cat2.split(",");
        for (String s1 : a1) {
            if (s1 == null) continue;
            String t1 = s1.trim();
            if (t1.isEmpty()) continue;
            for (String s2 : a2) {
                if (s2 == null) continue;
                if (t1.equalsIgnoreCase(s2.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------
    // Helper
    // --------------------------------------------------------------------
    private Course findById(String id) {
        if (id == null) return null;
        for (Course c : allCourses) {
            if (id.equals(c.getId())) return c;
        }
        return null;
    }

    // --------------------------------------------------------------------
    // IMPLEMENT CourseApi
    // --------------------------------------------------------------------

    @Override
    public List<Course> listAll() {
        return new ArrayList<>(allCourses);
    }

    // API CHO HOME – filter + sort + limit
    @Override
    public List<Course> filterSearchSort(String categoryOrAll, String query, Sort sort, int limit) {
        String cat = categoryOrAll == null ? "All" : categoryOrAll;
        String q = query == null ? "" : query.trim().toLowerCase(Locale.US);

        List<Course> res = new ArrayList<>();
        for (Course c : allCourses) {
            boolean catOk = cat.equals("All") || hasCategory(c.getCategory(), cat);
            boolean matches = q.isEmpty()
                    || c.getTitle().toLowerCase(Locale.US).contains(q)
                    || c.getTeacher().toLowerCase(Locale.US).contains(q);
            if (catOk && matches) res.add(c);
        }

        Comparator<Course> cmp;
        switch (sort) {
            case ZA:
                cmp = (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle());
                break;
            case RATING_UP:
                cmp = (a, b) -> Double.compare(a.getRating(), b.getRating());
                break;
            case RATING_DOWN:
                cmp = (a, b) -> Double.compare(b.getRating(), a.getRating());
                break;
            case AZ:
            default:
                cmp = (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle());
        }
        res.sort(cmp);

        if (limit > 0 && res.size() > limit) {
            return new ArrayList<>(res.subList(0, limit));
        }
        return res;
    }

    // API CHO MÀN CHI TIẾT
    @Override
    public Course getCourseDetail(String courseId) {
        Course c = findById(courseId);
        if (c != null) return c;
        return allCourses.isEmpty() ? null : allCourses.get(0);
    }

    // --------------------------------------------------------------------
    // MỖI KHÓA HỌC CÓ BÀI HỌC RIÊNG (LẤY TỪ JSON, LỌC THEO courseId)
    // --------------------------------------------------------------------
    @Override
    public List<CourseLesson> getLessonsForCourse(String courseId) {
        List<CourseLesson> result = new ArrayList<>();
        if (courseId == null) return result;

        try {
            JSONArray arr = new JSONArray(LESSONS_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!courseId.equals(o.optString("courseId"))) continue;

                result.add(new CourseLesson(
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

    // --------------------------------------------------------------------
    // KHÓA HỌC LIÊN QUAN (vẫn logic cũ)
    // --------------------------------------------------------------------
    @Override
    public List<Course> getRelatedCourses(String courseId) {
        List<Course> related = new ArrayList<>();
        Course base = findById(courseId);
        if (base == null) return related;

        for (Course c : allCourses) {
            if (c.getId().equals(base.getId())) continue;

            boolean sameTeacher = c.getTeacher() != null
                    && base.getTeacher() != null
                    && c.getTeacher().equalsIgnoreCase(base.getTeacher());

            boolean sameCategory = shareCategory(c.getCategory(), base.getCategory());

            if (sameTeacher || sameCategory) {
                related.add(c);
            }
        }
        return related;
    }

    // --------------------------------------------------------------------
    // MỖI KHÓA HỌC CÓ REVIEW RIÊNG (LẤY TỪ JSON, LỌC THEO courseId)
    // --------------------------------------------------------------------
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

    // ------------------ CRUD (fake) ------------------

    @Override
    public Course createCourse(Course newCourse) {
        if (newCourse.getId() == null || newCourse.getId().trim().isEmpty()) {
            newCourse.setId(generateNewId());
        } else {
            Course old = findById(newCourse.getId());
            if (old != null) {
                allCourses.remove(old);
            }
        }
        allCourses.add(newCourse);
        return newCourse;
    }

    @Override
    public Course updateCourse(String id, Course updatedCourse) {
        Course existing = findById(id);
        if (existing == null) return null;

        existing.setTitle(updatedCourse.getTitle());
        existing.setTeacher(updatedCourse.getTeacher());
        existing.setImageUrl(updatedCourse.getImageUrl());
        existing.setCategory(updatedCourse.getCategory());
        existing.setLectures(updatedCourse.getLectures());
        existing.setStudents(updatedCourse.getStudents());
        existing.setRating(updatedCourse.getRating());
        existing.setPrice(updatedCourse.getPrice());
        existing.setDescription(updatedCourse.getDescription());
        existing.setCreatedAt(updatedCourse.getCreatedAt());
        existing.setRatingCount(updatedCourse.getRatingCount());
        existing.setTotalDurationMinutes(updatedCourse.getTotalDurationMinutes());
        existing.setSkills(updatedCourse.getSkills());
        existing.setRequirements(updatedCourse.getRequirements());

        return existing;
    }

    @Override
    public boolean deleteCourse(String id) {
        Course existing = findById(id);
        if (existing == null) return false;
        return allCourses.remove(existing);
    }
}
