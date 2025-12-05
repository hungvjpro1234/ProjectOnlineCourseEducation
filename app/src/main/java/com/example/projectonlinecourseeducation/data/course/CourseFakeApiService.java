package com.example.projectonlinecourseeducation.data.course;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fake Course API Service (in-memory)
 *
 * - createCourse now sets sensible defaults (id, createdAt...)
 * - helper methods to add/remove lessons so LessonFakeApiService can update course summary fields.
 */
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
    // JSON SEED CHO TẤT CẢ KHÓA HỌC
    // --------------------------------------------------------------------
    private static final String COURSES_JSON = "[\n" +
            "  {\n" +
            "    \"id\":\"c1\",\n" +
            "    \"title\":\"Java Cơ Bản\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/j1/640/360\",\n" +
            "    \"category\":\"Java, OOP, Backend\",\n" +
            "    \"lectures\":5,\n" +
            "    \"students\":1200,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":199000,\n" +
            "    \"description\":\"Khóa học Java cơ bản cho người mới bắt đầu, đi từ cú pháp đến OOP và thực hành.\",\n" +
            "    \"createdAt\":\"03/2024\",\n" +
            "    \"ratingCount\":3,\n" +
            "    \"totalDurationMinutes\":106,\n" +
            "    \"skills\":[\"Nắm cú pháp\", \"Hiểu OOP\", \"Collection\", \"Build console app đơn giản\"],\n" +
            "    \"requirements\":[\"Biết máy tính cơ bản\", \"Dành 6-8 giờ/tuần\", \"Cài JDK\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c2\",\n" +
            "    \"title\":\"Java Web với Spring Boot\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/j3/640/360\",\n" +
            "    \"category\":\"Java, Spring Boot, Backend\",\n" +
            "    \"lectures\":5,\n" +
            "    \"students\":1500,\n" +
            "    \"rating\":4.8,\n" +
            "    \"price\":299000,\n" +
            "    \"description\":\"Xây dựng REST API và ứng dụng web backend với Spring Boot, JPA và Security.\",\n" +
            "    \"createdAt\":\"05/2024\",\n" +
            "    \"ratingCount\":3,\n" +
            "    \"totalDurationMinutes\":112,\n" +
            "    \"skills\":[\"Spring Boot cơ bản\", \"REST API\", \"JPA/Hibernate\", \"Spring Security cơ bản\"],\n" +
            "    \"requirements\":[\"Nắm Java cơ bản\", \"Kiến thức OOP\", \"Biết SQL cơ bản\"]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":\"c3\",\n" +
            "    \"title\":\"JavaScript Cơ Bản Đến Nâng Cao\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://picsum.photos/seed/js1/640/360\",\n" +
            "    \"category\":\"JavaScript, HTML, CSS, Frontend\",\n" +
            "    \"lectures\":5,\n" +
            "    \"students\":2000,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":199000,\n" +
            "    \"description\":\"Học JavaScript từ cơ bản đến nâng cao, thao tác DOM và làm mini project.\",\n" +
            "    \"createdAt\":\"01/2024\",\n" +
            "    \"ratingCount\":3,\n" +
            "    \"totalDurationMinutes\":109,\n" +
            "    \"skills\":[\"Cú pháp JS cơ bản\", \"DOM manipulation\", \"Async/await\", \"Build web mini project\"],\n" +
            "    \"requirements\":[\"Biết HTML/CSS cơ bản\", \"Biết sử dụng trình duyệt\"]\n" +
            "  }\n" +
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
                        o.optString("teacher", ""),
                        o.optString("imageUrl", ""),
                        o.optString("category", ""),
                        o.optInt("lectures", 0),
                        o.optInt("students", 0),
                        o.optDouble("rating", 0.0),
                        o.optDouble("price", 0.0),
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

    @Override
    public List<Course> filterSearchSort(String categoryOrAll, String query, Sort sort, int limit) {
        String cat = categoryOrAll == null ? "All" : categoryOrAll;
        String q = query == null ? "" : query.trim().toLowerCase(Locale.US);

        List<Course> res = new ArrayList<>();
        for (Course c : allCourses) {
            boolean catOk = cat.equals("All") || hasCategory(c.getCategory(), cat);
            boolean matches = q.isEmpty()
                    || (c.getTitle() != null && c.getTitle().toLowerCase(Locale.US).contains(q))
                    || (c.getTeacher() != null && c.getTeacher().toLowerCase(Locale.US).contains(q));
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

    @Override
    public List<Course> getCoursesByTeacher(String teacherName) {
        List<Course> courses = new ArrayList<>();
        if (teacherName == null || teacherName.trim().isEmpty()) {
            return courses;
        }

        for (Course c : allCourses) {
            if (teacherName.equalsIgnoreCase(c.getTeacher())) {
                courses.add(c);
            }
        }
        return courses;
    }

    @Override
    public Course getCourseDetail(String courseId) {
        Course c = findById(courseId);
        if (c != null) return c;
        return allCourses.isEmpty() ? null : allCourses.get(0);
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

    // ------------------ CRUD (fake) ------------------

    @Override
    public Course createCourse(Course newCourse) {
        if (newCourse == null) return null;

        // assign id if missing
        if (newCourse.getId() == null || newCourse.getId().trim().isEmpty()) {
            newCourse.setId(generateNewId());
        } else {
            Course old = findById(newCourse.getId());
            if (old != null) {
                allCourses.remove(old);
            }
        }

        // defaults
        if (newCourse.getCreatedAt() == null || newCourse.getCreatedAt().trim().isEmpty()) {
            SimpleDateFormat fmt = new SimpleDateFormat("MM/yyyy", Locale.US);
            newCourse.setCreatedAt(fmt.format(new Date()));
        }
        if (newCourse.getSkills() == null) newCourse.setSkills(new ArrayList<>());
        if (newCourse.getRequirements() == null) newCourse.setRequirements(new ArrayList<>());
        if (newCourse.getPrice() < 0) newCourse.setPrice(0);

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

    @Override
    public Course recalculateCourseRating(String courseId) {
        Course course = findById(courseId);
        if (course == null) return null;

        // TODO: implement using ReviewApi if available
        return course;
    }

    // ----------------- Helpers for Lesson <-> Course sync -----------------

    /**
     * Called by LessonFakeApiService when a new lesson is created for this course.
     * Updates lectures count and totalDurationMinutes (if duration provided).
     */
    public void addLessonToCourse(Lesson lesson) {
        if (lesson == null) return;
        if (lesson.getCourseId() == null) return;
        Course c = findById(lesson.getCourseId());
        if (c == null) return;

        c.setLectures(c.getLectures() + 1);
        int addMinutes = parseDurationToMinutes(lesson.getDuration());
        c.setTotalDurationMinutes(c.getTotalDurationMinutes() + addMinutes);
    }

    /**
     * Called when a lesson is removed.
     */
    public void removeLessonFromCourse(Lesson lesson) {
        if (lesson == null) return;
        if (lesson.getCourseId() == null) return;
        Course c = findById(lesson.getCourseId());
        if (c == null) return;

        c.setLectures(Math.max(0, c.getLectures() - 1));
        int subMinutes = parseDurationToMinutes(lesson.getDuration());
        c.setTotalDurationMinutes(Math.max(0, c.getTotalDurationMinutes() - subMinutes));
    }

    /**
     * Adjust totalDurationMinutes of a course by delta (can be negative).
     */
    public void adjustCourseDuration(String courseId, int deltaMinutes) {
        if (courseId == null) return;
        Course c = findById(courseId);
        if (c == null) return;
        int newVal = c.getTotalDurationMinutes() + deltaMinutes;
        c.setTotalDurationMinutes(Math.max(0, newVal));
    }

    private int parseDurationToMinutes(String durationText) {
        if (durationText == null) return 0;
        try {
            String[] parts = durationText.split(":");
            int seconds = 0;
            if (parts.length == 2) {
                int mm = Integer.parseInt(parts[0].trim());
                int ss = Integer.parseInt(parts[1].trim());
                seconds = mm * 60 + ss;
            } else if (parts.length == 3) {
                int hh = Integer.parseInt(parts[0].trim());
                int mm = Integer.parseInt(parts[1].trim());
                int ss = Integer.parseInt(parts[2].trim());
                seconds = hh * 3600 + mm * 60 + ss;
            } else {
                int val = Integer.parseInt(durationText.trim());
                seconds = val;
            }
            return (seconds + 30) / 60;
        } catch (Exception e) {
            return 0;
        }
    }
}
