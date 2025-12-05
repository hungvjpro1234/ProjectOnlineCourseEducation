package com.example.projectonlinecourseeducation.data.lesson;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.utils.OnlyFakeApiService.ActivityProvider;
import com.example.projectonlinecourseeducation.core.utils.OnlyFakeApiService.VideoDurationHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseFakeApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation of LessonApi - in-memory store.
 *
 * - This class contains DEV-only helpers to compute duration using VideoDurationHelper.
 * - It implements LessonApi including listener registration so UI can subscribe without knowing impl.
 *
 * IMPORTANT:
 * - To allow auto compute of duration in dev, initialize ActivityProvider in Application.onCreate():
 *     ActivityProvider.init(getApplication());
 */
public class LessonFakeApiService implements LessonApi {

    // Singleton
    private static LessonFakeApiService instance;
    public static LessonFakeApiService getInstance() {
        if (instance == null) instance = new LessonFakeApiService();
        return instance;
    }

    // JSON SEED CHO NỘI DUNG KHÓA HỌC (LESSON + VIDEO)
    private static final String LESSONS_JSON = "[\n" +
            "  {\"id\":\"c1_l1\",\"courseId\":\"c1\",\"order\":1,\"title\":\"Giới thiệu Java & cài đặt môi trường\",\n" +
            "   \"description\":\"Bài học này giới thiệu những kiến thức cơ bản về Java, lịch sử phát triển, đặc điểm, và hướng dẫn chi tiết cách cài đặt JDK, IDE để bắt đầu lập trình.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"09:30\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l2\",\"courseId\":\"c1\",\"order\":2,\"title\":\"Biến, kiểu dữ liệu & toán tử\",\n" +
            "   \"description\":\"Tìm hiểu về các kiểu dữ liệu nguyên thủy (primitive types), cách khai báo biến, toán tử số học, so sánh, logic trong Java.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"18:20\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l3\",\"courseId\":\"c1\",\"order\":3,\"title\":\"Cấu trúc điều khiển (if, switch, loop)\",\n" +
            "   \"description\":\"Học cách sử dụng if-else, switch-case, vòng lặp for, while để điều khiển luồng chương trình.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"22:15\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l4\",\"courseId\":\"c1\",\"order\":4,\"title\":\"Mảng & Collection cơ bản\",\n" +
            "   \"description\":\"Làm quen với mảng, ArrayList, HashMap - những cấu trúc dữ liệu quan trọng trong Java.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"25:00\"},\n" +
            "\n" +
            "  {\"id\":\"c1_l5\",\"courseId\":\"c1\",\"order\":5,\"title\":\"Giới thiệu lập trình hướng đối tượng\",\n" +
            "   \"description\":\"Bước đầu tiếp cận OOP: class, object, inheritance, polymorphism, encapsulation - nền tảng cho Java phía sau.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"30:45\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l1\",\"courseId\":\"c2\",\"order\":1,\"title\":\"Giới thiệu Spring Boot & tạo project\",\n" +
            "   \"description\":\"Khám phá Spring Boot, tại sao nó được yêu thích, cách tạo project, cấu trúc thư mục chuẩn.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"12:10\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l2\",\"courseId\":\"c2\",\"order\":2,\"title\":\"Cấu hình REST Controller\",\n" +
            "   \"description\":\"Xây dựng REST API với Spring Boot: @RestController, @RequestMapping, HTTP methods.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"20:05\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l3\",\"courseId\":\"c2\",\"order\":3,\"title\":\"Làm việc với JPA & Entity\",\n" +
            "   \"description\":\"Kết nối database với Spring Boot thông qua JPA, định nghĩa Entity, relationships.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"24:40\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l4\",\"courseId\":\"c2\",\"order\":4,\"title\":\"Repository & Service Layer\",\n" +
            "   \"description\":\"Xây dựng architecture bằng Repository pattern, Service layer để tách biệt business logic.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"26:15\"},\n" +
            "\n" +
            "  {\"id\":\"c2_l5\",\"courseId\":\"c2\",\"order\":5,\"title\":\"Authentication cơ bản với Spring Security\",\n" +
            "   \"description\":\"Bảo mật API với Spring Security: authentication, authorization, JWT basics.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"28:30\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l1\",\"courseId\":\"c3\",\"order\":1,\"title\":\"Giới thiệu JavaScript & môi trường chạy\",\n" +
            "   \"description\":\"JavaScript là gì, tại sao nó quan trọng, setup môi trường (Browser, Node.js), console debugging.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"08:45\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l2\",\"courseId\":\"c3\",\"order\":2,\"title\":\"Biến, kiểu dữ liệu & toán tử trong JS\",\n" +
            "   \"description\":\"var, let, const; string, number, boolean, object; các toán tử, type coercion trong JavaScript.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"17:30\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l3\",\"courseId\":\"c3\",\"order\":3,\"title\":\"DOM cơ bản & thao tác thực tế\",\n" +
            "   \"description\":\"Làm việc với DOM: select elements, thêm/xóa/sửa content, event listeners.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"23:10\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l4\",\"courseId\":\"c3\",\"order\":4,\"title\":\"Async, callback, promise, async/await\",\n" +
            "   \"description\":\"Xử lý bất đồng bộ: callback hell, Promise, async/await, fetch API.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"27:20\"},\n" +
            "\n" +
            "  {\"id\":\"c3_l5\",\"courseId\":\"c3\",\"order\":5,\"title\":\"Mini project: To-do list\",\n" +
            "   \"description\":\"Áp dụng tất cả kiến thức để tạo một ứng dụng To-do list hoàn chỉnh với localStorage.\",\n" +
            "   \"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"32:00\"}\n" +
            "]";

    // In-memory storage for runtime modifications
    private java.util.Map<String, Lesson> lessonMap = new java.util.HashMap<>();
    private int nextLessonId = 1000;

    private String generateNewLessonId(String courseId) {
        return courseId + "_l" + (nextLessonId++);
    }

    // Listeners (exposed via LessonApi interface)
    private final List<LessonApi.LessonUpdateListener> updateListeners = new ArrayList<>();

    public LessonFakeApiService() {
        seedLessonsFromJson();
    }

    private void seedLessonsFromJson() {
        try {
            JSONArray arr = new JSONArray(LESSONS_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Lesson lesson = new Lesson(
                        o.getString("id"),
                        o.getString("courseId"),
                        o.getString("title"),
                        o.optString("description", ""),
                        o.getString("videoUrl"),
                        o.optString("duration", ""),
                        o.getInt("order")
                );
                lessonMap.put(lesson.getId(), lesson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Lesson> getLessonsForCourse(String courseId) {
        List<Lesson> result = new ArrayList<>();
        if (courseId == null) return result;

        for (Lesson lesson : lessonMap.values()) {
            if (courseId.equals(lesson.getCourseId())) {
                result.add(lesson);
            }
        }

        // Sort by order
        result.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        return result;
    }

    @Override
    public Lesson getLessonDetail(String lessonId) {
        return lessonId == null ? null : lessonMap.get(lessonId);
    }

    @Override
    public Lesson createLesson(Lesson newLesson) {
        if (newLesson == null) return null;

        if (newLesson.getId() == null || newLesson.getId().trim().isEmpty()) {
            newLesson.setId(generateNewLessonId(newLesson.getCourseId()));
        }

        // Put into map
        lessonMap.put(newLesson.getId(), newLesson);

        // Inform CourseFakeApiService (if available) that a new lesson was added (updates lectures + duration)
        try {
            if (ApiProvider.getCourseApi() instanceof CourseFakeApiService) {
                CourseFakeApiService cs = (CourseFakeApiService) ApiProvider.getCourseApi();
                cs.addLessonToCourse(newLesson); // will add lecture count and add duration minutes (duration may be "Đang tính..." => parsed as 0)
            }
        } catch (Exception ignored) {}

        // DEV-only behavior: if ActivityProvider has a foreground activity, try to compute duration asynchronously.
        Activity current = ActivityProvider.getTopActivity();
        if (current != null && newLesson.getVideoUrl() != null && !newLesson.getVideoUrl().trim().isEmpty()) {
            final String assignedId = newLesson.getId();
            VideoDurationHelper.fetchDuration(current, newLesson.getVideoUrl(), new VideoDurationHelper.Callback() {
                @Override
                public void onSuccess(@NonNull String durationText, int durationSeconds) {
                    Lesson exist = lessonMap.get(assignedId);
                    if (exist != null) {
                        // compute delta minutes relative to previous value (previous likely 0 or placeholder)
                        int newMinutes = secondsToRoundedMinutes(durationSeconds);

                        // Determine previous minutes from exist.getDuration()
                        int prevMinutes = parseDurationToMinutesSafe(exist.getDuration());

                        exist.setDuration(durationText);
                        notifyLessonUpdated(assignedId, exist);

                        // update course total duration by delta
                        int delta = newMinutes - prevMinutes;
                        try {
                            if (ApiProvider.getCourseApi() instanceof CourseFakeApiService) {
                                CourseFakeApiService cs = (CourseFakeApiService) ApiProvider.getCourseApi();
                                cs.adjustCourseDuration(exist.getCourseId(), delta);
                            }
                        } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onError(@NonNull String reason) {
                    // ignore in dev mock - optionally could set placeholder
                }
            });
        }

        return newLesson;
    }

    @Override
    public Lesson updateLesson(String lessonId, Lesson updatedLesson) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null || updatedLesson == null) return null;

        // compute prev minutes for proper delta if duration will change
        int prevMinutes = parseDurationToMinutesSafe(existing.getDuration());

        existing.setTitle(updatedLesson.getTitle());
        existing.setDescription(updatedLesson.getDescription());
        existing.setVideoUrl(updatedLesson.getVideoUrl());
        existing.setDuration(updatedLesson.getDuration());
        existing.setOrder(updatedLesson.getOrder());

        // If we have a foreground Activity, recompute duration (dev-only).
        Activity current = ActivityProvider.getTopActivity();
        if (current != null && updatedLesson.getVideoUrl() != null && !updatedLesson.getVideoUrl().trim().isEmpty()) {
            final String id = lessonId;
            VideoDurationHelper.fetchDuration(current, updatedLesson.getVideoUrl(), new VideoDurationHelper.Callback() {
                @Override
                public void onSuccess(@NonNull String durationText, int durationSeconds) {
                    Lesson exist2 = lessonMap.get(id);
                    if (exist2 != null) {
                        int newMinutes = secondsToRoundedMinutes(durationSeconds);
                        int prev = parseDurationToMinutesSafe(existing.getDuration());
                        exist2.setDuration(durationText);
                        notifyLessonUpdated(id, exist2);
                        int delta = newMinutes - prev;
                        try {
                            if (ApiProvider.getCourseApi() instanceof CourseFakeApiService) {
                                CourseFakeApiService cs = (CourseFakeApiService) ApiProvider.getCourseApi();
                                cs.adjustCourseDuration(exist2.getCourseId(), delta);
                            }
                        } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onError(@NonNull String reason) {
                    // ignore
                }
            });
        } else {
            // notify immediate update (duration likely unchanged)
            notifyLessonUpdated(lessonId, existing);
        }

        return existing;
    }

    @Override
    public boolean deleteLesson(String lessonId) {
        if (lessonId == null) return false;
        Lesson removed = lessonMap.remove(lessonId);
        if (removed != null) {
            // update course summary
            try {
                if (ApiProvider.getCourseApi() instanceof CourseFakeApiService) {
                    CourseFakeApiService cs = (CourseFakeApiService) ApiProvider.getCourseApi();
                    cs.removeLessonFromCourse(removed);
                }
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    @Override
    public void addLessonUpdateListener(LessonApi.LessonUpdateListener l) {
        if (l == null) return;
        if (!updateListeners.contains(l)) updateListeners.add(l);
    }

    @Override
    public void removeLessonUpdateListener(LessonApi.LessonUpdateListener l) {
        updateListeners.remove(l);
    }

    private void notifyLessonUpdated(String lessonId, Lesson lesson) {
        for (LessonApi.LessonUpdateListener l : new ArrayList<>(updateListeners)) {
            try {
                l.onLessonUpdated(lessonId, lesson);
            } catch (Exception ignored) {}
        }
    }

    // Helpers
    private int parseDurationToMinutesSafe(String durationText) {
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

    private int secondsToRoundedMinutes(int seconds) {
        return (seconds + 30) / 60;
    }
}
