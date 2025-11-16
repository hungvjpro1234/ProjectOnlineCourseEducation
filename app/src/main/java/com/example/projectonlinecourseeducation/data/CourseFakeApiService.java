package com.example.projectonlinecourseeducation.data;

import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.core.model.CourseLesson;
import com.example.projectonlinecourseeducation.core.model.CourseReview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// DÙNG Sort từ CourseApi
import com.example.projectonlinecourseeducation.data.CourseApi.Sort;

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

    // dùng cho create khóa học mới (vì đã có c1..c10)
    private int nextId = 100;

    private String generateNewId() {
        return "c" + (nextId++);
    }

    // --------------------------------------------------------------------
    // 1) JSON SEED CHO TẤT CẢ KHÓA HỌC
    //    (giống như response /courses từ backend)
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
            "    \"title\":\"Java Nâng Cao\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/j2/640/360\",\n" +
            "    \"category\":\"Java, OOP, Backend\",\n" +
            "    \"lectures\":60,\n" +
            "    \"students\":950,\n" +
            "    \"rating\":4.7,\n" +
            "    \"price\":249000,\n" +
            "    \"description\":\"Đào sâu vào Generics, Stream API, đa luồng và best practice trong Java.\",\n" +
            "    \"createdAt\":\"04/2024\",\n" +
            "    \"ratingCount\":180,\n" +
            "    \"totalDurationMinutes\":840,\n" +
            "    \"skills\":[\"Generics\", \"Stream API\", \"Đa luồng\", \"Clean code với Java\"],\n" +
            "    \"requirements\":[\"Nắm Java cơ bản\", \"Hiểu OOP\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c3\",\n" +
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
            "    \"id\":\"c4\",\n" +
            "    \"title\":\"Lập Trình Java Cho Người Đi Làm\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/j4/640/360\",\n" +
            "    \"category\":\"Java, Backend\",\n" +
            "    \"lectures\":40,\n" +
            "    \"students\":800,\n" +
            "    \"rating\":4.5,\n" +
            "    \"price\":189000,\n" +
            "    \"description\":\"Tổng hợp các kiến thức Java, exception, file, JDBC để áp dụng thực tế.\",\n" +
            "    \"createdAt\":\"02/2024\",\n" +
            "    \"ratingCount\":90,\n" +
            "    \"totalDurationMinutes\":600,\n" +
            "    \"skills\":[\"Exception handling\", \"Làm việc với file\", \"JDBC cơ bản\", \"Debug ứng dụng Java\"],\n" +
            "    \"requirements\":[\"Nắm Java cơ bản\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c5\",\n" +
            "    \"title\":\"Luyện Thi Chứng Chỉ Java OCP\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/j5/640/360\",\n" +
            "    \"category\":\"Java, OOP\",\n" +
            "    \"lectures\":70,\n" +
            "    \"students\":430,\n" +
            "    \"rating\":4.9,\n" +
            "    \"price\":349000,\n" +
            "    \"description\":\"Ôn tập toàn diện Java Core, câu hỏi trắc nghiệm và tips vượt qua kỳ thi OCP.\",\n" +
            "    \"createdAt\":\"06/2024\",\n" +
            "    \"ratingCount\":300,\n" +
            "    \"totalDurationMinutes\":1020,\n" +
            "    \"skills\":[\"Ôn tập Java Core\", \"Làm đề OCP\", \"Phân tích câu hỏi khó\"],\n" +
            "    \"requirements\":[\"Đã học Java nâng cao\", \"Đọc hiểu tiếng Anh cơ bản\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c6\",\n" +
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
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c7\",\n" +
            "    \"title\":\"React + TypeScript Từ A-Z\",\n" +
            "    \"teacher\":\"Trần B\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/js2/640/360\",\n" +
            "    \"category\":\"JavaScript, TypeScript, Frontend\",\n" +
            "    \"lectures\":65,\n" +
            "    \"students\":1300,\n" +
            "    \"rating\":4.7,\n" +
            "    \"price\":279000,\n" +
            "    \"description\":\"Xây dựng SPA với React, hook, router và quản lý state với TypeScript.\",\n" +
            "    \"createdAt\":\"03/2024\",\n" +
            "    \"ratingCount\":160,\n" +
            "    \"totalDurationMinutes\":840,\n" +
            "    \"skills\":[\"React hook\", \"React Router\", \"TypeScript cơ bản\", \"Tổ chức project React\"],\n" +
            "    \"requirements\":[\"Nắm JavaScript ES6\", \"Hiểu HTML/CSS\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c8\",\n" +
            "    \"title\":\"Python Cho Phân Tích Dữ Liệu\",\n" +
            "    \"teacher\":\"Lê C\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/py1/640/360\",\n" +
            "    \"category\":\"Python, Data, SQL\",\n" +
            "    \"lectures\":48,\n" +
            "    \"students\":1750,\n" +
            "    \"rating\":4.8,\n" +
            "    \"price\":259000,\n" +
            "    \"description\":\"Dùng Python, Pandas, Matplotlib để phân tích dữ liệu thực tế.\",\n" +
            "    \"createdAt\":\"02/2024\",\n" +
            "    \"ratingCount\":190,\n" +
            "    \"totalDurationMinutes\":720,\n" +
            "    \"skills\":[\"Python cơ bản\", \"Pandas\", \"Data cleaning\", \"Visualization với Matplotlib\"],\n" +
            "    \"requirements\":[\"Biết Excel cơ bản\", \"Tư duy logic tốt\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c9\",\n" +
            "    \"title\":\"Python Web Với Django\",\n" +
            "    \"teacher\":\"Lê C\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/py2/640/360\",\n" +
            "    \"category\":\"Python, Django, Backend\",\n" +
            "    \"lectures\":52,\n" +
            "    \"students\":980,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":229000,\n" +
            "    \"description\":\"Xây dựng website hoàn chỉnh với Django, ORM, template và auth.\",\n" +
            "    \"createdAt\":\"04/2024\",\n" +
            "    \"ratingCount\":140,\n" +
            "    \"totalDurationMinutes\":780,\n" +
            "    \"skills\":[\"Django cơ bản\", \"Django ORM\", \"Template & form\", \"Auth & middleware\"],\n" +
            "    \"requirements\":[\"Nắm Python cơ bản\", \"Biết HTML cơ bản\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c10\",\n" +
            "    \"title\":\"Fullstack Web: HTML/CSS/JavaScript\",\n" +
            "    \"teacher\":\"Trần B\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/fs1/640/360\",\n" +
            "    \"category\":\"HTML, CSS, JavaScript, Frontend\",\n" +
            "    \"lectures\":58,\n" +
            "    \"students\":2200,\n" +
            "    \"rating\":4.7,\n" +
            "    \"price\":239000,\n" +
            "    \"description\":\"Xây dựng từ landing page đến web nhiều trang với HTML, CSS và JS thuần.\",\n" +
            "    \"createdAt\":\"05/2024\",\n" +
            "    \"ratingCount\":260,\n" +
            "    \"totalDurationMinutes\":840,\n" +
            "    \"skills\":[\"Layout với Flexbox/Grid\", \"Responsive design\", \"Vanilla JS\", \"Deploy project đơn giản\"],\n" +
            "    \"requirements\":[\"Không yêu cầu kiến thức trước\", \"Máy tính kết nối Internet\"]\n" +
            "  }\n" +
            "]";

    // JSON lesson dùng chung (demo) – sau này có thể tách theo courseId
    private static final String LESSONS_JSON = "{ \"lessons\":[" +
            "{\"id\":\"l1\",\"title\":\"Giới thiệu khoá học & cài đặt môi trường\",\"duration\":\"10:30\"}," +
            "{\"id\":\"l2\",\"title\":\"Biến, kiểu dữ liệu, toán tử\",\"duration\":\"18:20\"}," +
            "{\"id\":\"l3\",\"title\":\"Cấu trúc điều khiển\",\"duration\":\"22:15\"}," +
            "{\"id\":\"l4\",\"title\":\"Array / Collection cơ bản\",\"duration\":\"25:00\"}," +
            "{\"id\":\"l5\",\"title\":\"Lập trình hướng đối tượng\",\"duration\":\"30:45\"}" +
            "]}";

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

    @Override
    public List<CourseLesson> getLessonsForCourse(String courseId) {
        List<CourseLesson> result = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(LESSONS_JSON);
            JSONArray arr = root.getJSONArray("lessons");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject l = arr.getJSONObject(i);
                result.add(new CourseLesson(
                        courseId + "_" + l.getString("id"), // gắn courseId cho unique
                        l.getString("title"),
                        l.getString("duration")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

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

    @Override
    public List<CourseReview> getReviewsForCourse(String courseId) {
        // Tạm dùng chung một bộ review – sau này có thể đổi sang JSON riêng từng course
        return Arrays.asList(
                new CourseReview("Nguyễn Văn B", 4.5f,
                        "Nội dung dễ hiểu, giảng viên giải thích cặn kẽ. Hợp cho người mới."),
                new CourseReview("Trần Thị C", 5.0f,
                        "Khóa học rất chất lượng, có nhiều ví dụ thực tế."),
                new CourseReview("Lê Văn D", 4.0f,
                        "Ổn, mình chỉ mong có thêm phần bài tập nâng cao.")
        );
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
