package com.example.projectonlinecourseeducation.data.course;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;
import com.example.projectonlinecourseeducation.core.model.course.CourseReview; // NEW
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.coursereview.ReviewApi; // NEW

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fake Course API Service (in-memory)
 *
 * - createCourse now sets sensible defaults (id, createdAt...)
 * - helper methods to add/remove lessons so LessonFakeApiService can update course summary fields.
 *
 * Important changes:
 * - Seed data students reset to 0 (chỉ hiện thị, chưa ai mua thật).
 * - Implement recordPurchase(courseId) để tăng students khi có giao dịch thật.
 * - Thêm cơ chế CourseUpdateListener để UI có thể subscribe và tự cập nhật khi course thay đổi.
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

    // PENDING CHANGES: Lưu các thay đổi course chưa được admin duyệt
    // Key = courseId, Value = bản course đã sửa (chờ duyệt)
    private final Map<String, Course> pendingCourseEdits = new HashMap<>();

    // dùng cho create khóa học mới (vì đã có c1..c3)
    private int nextId = 100;

    private String generateNewId() {
        return "c" + (nextId++);
    }

    // Listeners for course updates
    private final List<CourseApi.CourseUpdateListener> courseUpdateListeners = new ArrayList<>();

    // --------------------------------------------------------------------
    // JSON SEED CHO TẤT CẢ KHÓA HỌC
    // LƯU Ý: giữ nguyên JSON nhưng sẽ override students -> 0 sau khi parse
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
            "    \"requirements\":[\"Biết máy tính cơ bản\", \"Dành 6-8 giờ/tuần\", \"Cài JDK\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
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
            "    \"requirements\":[\"Nắm Java cơ bản\", \"Kiến thức OOP\", \"Biết SQL cơ bản\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
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
            "    \"requirements\":[\"Biết HTML/CSS cơ bản\", \"Biết sử dụng trình duyệt\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
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

                // Note: even nếu JSON có trường "students", ta reset = 0 vì "chưa ai mua thật"
                Course c = new Course(
                        o.getString("id"),
                        o.getString("title"),
                        o.optString("teacher", ""),
                        o.optString("imageUrl", ""),
                        o.optString("category", ""),
                        o.optInt("lectures", 0),
                        0, // students reset to 0
                        o.optDouble("rating", 0.0),
                        o.optDouble("price", 0.0),
                        o.optString("description", ""),
                        o.optString("createdAt", ""),
                        o.optInt("ratingCount", 0),
                        o.optInt("totalDurationMinutes", 0),
                        skills,
                        requirements
                );

                // Set approval fields from JSON (default to false if not present)
                c.setInitialApproved(o.optBoolean("isInitialApproved", false));
                c.setEditApproved(o.optBoolean("isEditApproved", false));
                c.setDeleteRequested(o.optBoolean("isDeleteRequested", false));

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

    /**
     * Clone một course object (deep copy)
     */
    private Course cloneCourse(Course original) {
        if (original == null) return null;

        Course clone = new Course(
            original.getId(),
            original.getTitle(),
            original.getTeacher(),
            original.getImageUrl(),
            original.getCategory(),
            original.getLectures(),
            original.getStudents(),
            original.getRating(),
            original.getPrice(),
            original.getDescription(),
            original.getCreatedAt(),
            original.getRatingCount(),
            original.getTotalDurationMinutes(),
            new ArrayList<>(original.getSkills()),
            new ArrayList<>(original.getRequirements())
        );

        // Copy approval fields
        clone.setInitialApproved(original.isInitialApproved());
        clone.setEditApproved(original.isEditApproved());
        clone.setDeleteRequested(original.isDeleteRequested());

        return clone;
    }

    // Notify helpers
    private void notifyCourseUpdated(String courseId, Course course) {
        for (CourseApi.CourseUpdateListener l : new ArrayList<>(courseUpdateListeners)) {
            try {
                l.onCourseUpdated(courseId, course);
            } catch (Exception ignored) {}
        }
    }

    // --------------------------------------------------------------------
    // IMPLEMENT CourseApi
    // --------------------------------------------------------------------

    @Override
    public List<Course> listAll() {
        // trả bản copy để tránh sửa ngoài ý muốn
        return new ArrayList<>(allCourses);
    }

    @Override
    public List<Course> filterSearchSort(String categoryOrAll, String query, Sort sort, int limit) {
        String cat = categoryOrAll == null ? "All" : categoryOrAll;
        String q = query == null ? "" : query.trim().toLowerCase(Locale.US);

        List<Course> res = new ArrayList<>();
        for (Course c : allCourses) {
            // FILTER: Student chỉ thấy course đã được duyệt KHỞI TẠO
            // Course đang chờ duyệt EDIT hoặc DELETE vẫn hiển thị (student thấy version cũ)
            // Chỉ khi admin APPROVE DELETE thì course mới bị xóa vĩnh viễn (permanentlyDeleteCourse)
            if (!c.isInitialApproved()) {
                continue; // Skip courses chưa được duyệt khởi tạo
            }

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

            // FILTER: Student chỉ thấy course đã được duyệt KHỞI TẠO
            // Course đang chờ duyệt EDIT hoặc DELETE vẫn hiển thị
            if (!c.isInitialApproved()) {
                continue;
            }

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

        // Important: when creating course in system (admin/teacher), initial students = 0
        newCourse.setStudents(0);

        // FIX: Auto-set teacher name from current user (phân quyền courses theo teacher)
        if (newCourse.getTeacher() == null || newCourse.getTeacher().trim().isEmpty()) {
            User currentUser = ApiProvider.getAuthApi() != null
                ? ApiProvider.getAuthApi().getCurrentUser()
                : null;
            if (currentUser != null && currentUser.getRole() == User.Role.TEACHER) {
                newCourse.setTeacher(currentUser.getName());
            }
        }

        // APPROVAL LOGIC: Khi tạo mới khóa học, cả 2 trường đều false (chờ admin duyệt)
        newCourse.setInitialApproved(false);
        newCourse.setEditApproved(false);

        allCourses.add(newCourse);

        // Notify listeners about new course
        notifyCourseUpdated(newCourse.getId(), newCourse);

        return newCourse;
    }

    @Override
    public Course updateCourse(String id, Course updatedCourse) {
        Course existing = findById(id);
        if (existing == null) return null;

        // CRITICAL FIX: KHÔNG update course gốc, lưu vào pending thay vì
        // Course gốc giữ nguyên để student vẫn thấy version cũ

        // Clone course hiện tại để lưu các thay đổi
        Course pendingVersion = cloneCourse(updatedCourse);
        pendingVersion.setId(id); // Ensure same ID

        // Đánh dấu là có thay đổi chờ duyệt
        existing.setEditApproved(false);

        // Lưu pending version (bản đã sửa chờ duyệt)
        pendingCourseEdits.put(id, pendingVersion);

        // Notify listeners
        notifyCourseUpdated(id, existing);

        return existing; // Trả về course gốc, KHÔNG phải pending version
    }

    @Override
    public boolean deleteCourse(String id) {
        Course existing = findById(id);
        if (existing == null) return false;

        // SOFT DELETE: Đánh dấu course là đang chờ duyệt xóa thay vì xóa thật
        // Teacher xóa → chờ admin duyệt
        existing.setDeleteRequested(true);
        existing.setEditApproved(false); // Đánh dấu có thay đổi cần duyệt

        // Notify listeners that course status changed
        notifyCourseUpdated(id, existing);

        return true;
    }

    // ============ APPROVAL WORKFLOW METHODS ============

    /**
     * Lấy danh sách tất cả courses đang chờ phê duyệt
     * (khởi tạo, chỉnh sửa, hoặc xóa)
     */
    @Override
    public List<Course> getPendingCourses() {
        List<Course> pending = new ArrayList<>();
        for (Course c : allCourses) {
            if (c.isPendingApproval()) {
                pending.add(c);
            }
        }
        return pending;
    }

    /**
     * Admin phê duyệt khởi tạo course
     * Cho phép course hiển thị với students
     */
    @Override
    public boolean approveInitialCreation(String courseId) {
        Course existing = findById(courseId);
        if (existing == null) return false;

        existing.setInitialApproved(true);
        existing.setEditApproved(true); // Cũng set edit approved luôn

        notifyCourseUpdated(courseId, existing);
        return true;
    }

    /**
     * Admin từ chối khởi tạo course
     * Xóa course khỏi database (chưa được duyệt thì xóa luôn)
     */
    @Override
    public boolean rejectInitialCreation(String courseId) {
        Course existing = findById(courseId);
        if (existing == null) return false;

        // Nếu course chưa được duyệt khởi tạo thì xóa luôn
        if (!existing.isInitialApproved()) {
            boolean removed = allCourses.remove(existing);
            if (removed) {
                notifyCourseUpdated(courseId, null);
            }
            return removed;
        }

        // Nếu đã được duyệt rồi thì không cho xóa bằng method này
        return false;
    }

    /**
     * HARD DELETE: Xóa course vĩnh viễn khỏi hệ thống
     * Chỉ admin mới được gọi method này sau khi duyệt yêu cầu xóa
     */
    @Override
    public boolean permanentlyDeleteCourse(String id) {
        Course existing = findById(id);
        if (existing == null) return false;

        boolean removed = allCourses.remove(existing);
        if (removed) {
            // Notify listeners that course was permanently deleted
            notifyCourseUpdated(id, null);
        }
        return removed;
    }

    /**
     * Hủy yêu cầu xóa course (admin từ chối xóa)
     */
    @Override
    public boolean cancelDeleteRequest(String id) {
        Course existing = findById(id);
        if (existing == null) return false;

        existing.setDeleteRequested(false);
        existing.setEditApproved(true); // Quay lại trạng thái đã duyệt

        notifyCourseUpdated(id, existing);
        return true;
    }

    /**
     * Duyệt chỉnh sửa course (admin approve edit)
     * Apply pending changes vào course gốc
     */
    @Override
    public boolean approveCourseEdit(String id) {
        Course existing = findById(id);
        if (existing == null) return false;

        Course pendingVersion = pendingCourseEdits.get(id);
        if (pendingVersion == null) {
            // Không có pending changes, chỉ cần set approved = true
            existing.setEditApproved(true);
            notifyCourseUpdated(id, existing);
            return true;
        }

        // Apply tất cả thay đổi từ pending version vào course gốc
        existing.setTitle(pendingVersion.getTitle());
        existing.setTeacher(pendingVersion.getTeacher());
        existing.setImageUrl(pendingVersion.getImageUrl());
        existing.setCategory(pendingVersion.getCategory());
        existing.setLectures(pendingVersion.getLectures());
        existing.setPrice(pendingVersion.getPrice());
        existing.setDescription(pendingVersion.getDescription());
        existing.setSkills(pendingVersion.getSkills());
        existing.setRequirements(pendingVersion.getRequirements());

        // Set approved
        existing.setEditApproved(true);

        // Remove pending version
        pendingCourseEdits.remove(id);

        notifyCourseUpdated(id, existing);
        return true;
    }

    /**
     * Từ chối chỉnh sửa course (admin reject edit)
     * Xóa pending changes, giữ nguyên course gốc
     */
    @Override
    public boolean rejectCourseEdit(String id) {
        Course existing = findById(id);
        if (existing == null) return false;

        // Remove pending version
        pendingCourseEdits.remove(id);

        // Set approved (quay lại trạng thái đã duyệt)
        existing.setEditApproved(true);

        notifyCourseUpdated(id, existing);
        return true;
    }

    /**
     * Lấy pending edit của course (cho admin/teacher xem)
     * @return pending version nếu có, null nếu không
     */
    @Override
    public Course getPendingEdit(String id) {
        return pendingCourseEdits.get(id);
    }

    /**
     * Kiểm tra course có pending edit không
     */
    @Override
    public boolean hasPendingEdit(String id) {
        return pendingCourseEdits.containsKey(id);
    }

    @Override
    public Course recalculateCourseRating(String courseId) {
        Course course = findById(courseId);
        if (course == null) return null;

        try {
            // Lấy ReviewApi từ ApiProvider (có thể là ReviewFakeApiService)
            ReviewApi reviewApi = ApiProvider.getReviewApi();
            if (reviewApi == null) {
                // không có review api -> giữ nguyên
                return course;
            }

            List<CourseReview> reviews = reviewApi.getReviewsForCourse(courseId);
            if (reviews == null || reviews.isEmpty()) {
                // Nếu không có review, reset rating về 0 (tùy ý — hoặc giữ giá trị cũ)
                course.setRating(0.0);
                course.setRatingCount(0);
                notifyCourseUpdated(courseId, course);
                return course;
            }

            double sum = 0.0;
            for (CourseReview r : reviews) {
                sum += r.getRating();
            }
            double avg = sum / reviews.size();

            // Cập nhật course
            course.setRating(avg);
            course.setRatingCount(reviews.size());

            // Notify listeners để UI (ví dụ StudentCourseProductDetailActivity / StudentCoursePurchasedActivity)
            notifyCourseUpdated(courseId, course);

        } catch (Exception e) {
            // không để lỗi này làm crash app
            android.util.Log.w("CourseFakeApiService", "Failed to recalculate rating: " + e.getMessage());
        }

        return course;
    }

    @Override
    public Course recordPurchase(String courseId) {
        // When a real purchase happens, backend should increment students.
        // Here in fake service we just increment by 1 and return updated course.
        if (courseId == null) return null;
        Course c = findById(courseId);
        if (c == null) return null;

        // increment students by 1
        int current = c.getStudents();
        c.setStudents(current + 1);

        // NEW: Đồng bộ student vào CourseStudentApi để TeacherCourseManagementActivity có thể track
        try {
            User currentUser = ApiProvider.getAuthApi() != null
                    ? ApiProvider.getAuthApi().getCurrentUser()
                    : null;

            if (currentUser != null) {
                // Tạo CourseStudent object từ current user
                CourseStudent courseStudent = new CourseStudent(
                        currentUser.getId(),
                        currentUser.getName(),
                        currentUser.getAvatar(), // User.getAvatar() not getAvatarUrl()
                        System.currentTimeMillis() // enrolledAt = now
                );

                // Thêm vào CourseStudentApi
                CourseStudentApi csApi = ApiProvider.getCourseStudentApi();
                if (csApi instanceof CourseStudentFakeApiService) {
                    ((CourseStudentFakeApiService) csApi).addStudentToCourse(courseId, courseStudent);
                }
            }
        } catch (Exception e) {
            // Log error nhưng không crash app (purchase vẫn thành công)
            android.util.Log.w("CourseFakeApiService",
                    "Failed to sync student to CourseStudentApi: " + e.getMessage());
        }

        // Notify listeners about students change
        notifyCourseUpdated(courseId, c);

        // Optionally: if you want to keep track of purchase history/logs, do here (not implemented)
        return c;
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

        // Notify listeners about lecture count / duration change
        notifyCourseUpdated(c.getId(), c);
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

        // Notify listeners about lecture count / duration change
        notifyCourseUpdated(c.getId(), c);
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

        // Notify listeners about duration change
        notifyCourseUpdated(courseId, c);
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

    // ------------------ CourseUpdateListener registration ------------------

    @Override
    public void addCourseUpdateListener(CourseApi.CourseUpdateListener l) {
        if (l == null) return;
        if (!courseUpdateListeners.contains(l)) courseUpdateListeners.add(l);
    }

    @Override
    public void removeCourseUpdateListener(CourseApi.CourseUpdateListener l) {
        courseUpdateListeners.remove(l);
    }
}
