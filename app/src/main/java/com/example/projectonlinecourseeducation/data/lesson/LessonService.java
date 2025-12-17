package com.example.projectonlinecourseeducation.data.lesson;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.course.Course; // ✅ THÊM IMPORT NÀY
import com.example.projectonlinecourseeducation.core.model.user.User;
import static com.example.projectonlinecourseeducation.core.utils.finalclass.LessonSeedData.LESSONS_JSON;
import com.example.projectonlinecourseeducation.core.utils.finalclass.ActivityProvider;
import com.example.projectonlinecourseeducation.core.utils.finalclass.VideoDurationHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LessonService implements LessonApi {

    private static LessonService instance;
    public static LessonService getInstance() {
        if (instance == null) instance = new LessonService();
        return instance;
    }

    private java.util.Map<String, Lesson> lessonMap = new java.util.HashMap<>();
    private java.util.Map<String, Lesson> pendingLessonEdits = new java.util.HashMap<>();

    // 🆕 THÊM: Map để lưu lessons mới được thêm vào (chờ approve)
    // Key: courseId, Value: List của lesson IDs được thêm mới
    private java.util.Map<String, List<String>> pendingAddedLessons = new java.util.HashMap<>();

    // 🆕 THÊM: Map để lưu lessons được đánh dấu xóa (chờ approve)
    // Key: courseId, Value: List của lesson IDs được đánh dấu xóa
    private java.util.Map<String, List<String>> pendingDeletedLessons = new java.util.HashMap<>();

    private int nextLessonId = 1000;

    private String generateNewLessonId(String courseId) {
        return courseId + "_l" + (nextLessonId++);
    }

    private final List<LessonApi.LessonUpdateListener> updateListeners = new ArrayList<>();

    public LessonService() {
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
                // Seed data luôn approved
                lesson.setInitialApproved(true);
                lesson.setEditApproved(true);
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

        User currentUser = null;
        try {
            currentUser = ApiProvider.getAuthApi() != null
                    ? ApiProvider.getAuthApi().getCurrentUser()
                    : null;
        } catch (Exception ignored) {}

        boolean isStudent = (currentUser != null && currentUser.getRole() == User.Role.STUDENT);

        for (Lesson lesson : lessonMap.values()) {
            if (courseId.equals(lesson.getCourseId())) {
                if (isStudent) {
                    // Student chỉ thấy lessons đã được approve INITIAL và không bị đánh dấu xóa
                    if (lesson.isInitialApproved() && !lesson.isDeleteRequested()) {
                        result.add(lesson);
                    }
                } else {
                    // Teacher/Admin thấy tất cả, kể cả pending
                    result.add(lesson);
                }
            }
        }

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

        // 🔄 SỬA: Kiểm tra xem course đã được approve initial chưa
        boolean courseInitialApproved = false;
        try {
            if (ApiProvider.getCourseApi() != null) {
                // ✅ SỬA: Dùng getCourseDetail thay vì getCourseById
                Course course = ApiProvider.getCourseApi().getCourseDetail(newLesson.getCourseId());
                if (course != null) {
                    courseInitialApproved = course.isInitialApproved();
                }
            }
        } catch (Exception ignored) {}

        if (courseInitialApproved) {
            // 🆕 Course đã approved → Lesson mới này là PENDING ADD (chờ approve edit của course)
            newLesson.setInitialApproved(false);
            newLesson.setEditApproved(false);

            // Track lesson này vào pending added
            String courseId = newLesson.getCourseId();
            if (!pendingAddedLessons.containsKey(courseId)) {
                pendingAddedLessons.put(courseId, new ArrayList<>());
            }
            pendingAddedLessons.get(courseId).add(newLesson.getId());
        } else {
            // Course chưa approved → Lesson này cùng chờ approve với course
            newLesson.setInitialApproved(false);
            newLesson.setEditApproved(false);
        }

        lessonMap.put(newLesson.getId(), newLesson);

        // Update course summary (KHÔNG update nếu lesson pending)
        if (!courseInitialApproved) {
            try {
                if (ApiProvider.getCourseApi() instanceof CourseService) {
                    CourseService cs = (CourseService) ApiProvider.getCourseApi();
                    cs.addLessonToCourse(newLesson);
                }
            } catch (Exception ignored) {}
        }

        // Fetch duration
        Activity current = ActivityProvider.getTopActivity();
        if (current != null && newLesson.getVideoUrl() != null && !newLesson.getVideoUrl().trim().isEmpty()) {
            final String assignedId = newLesson.getId();
            VideoDurationHelper.fetchDuration(current, newLesson.getVideoUrl(), new VideoDurationHelper.Callback() {
                @Override
                public void onSuccess(@NonNull String durationText, int durationSeconds) {
                    Lesson exist = lessonMap.get(assignedId);
                    if (exist != null) {
                        int newMinutes = secondsToRoundedMinutes(durationSeconds);
                        int prevMinutes = parseDurationToMinutesSafe(exist.getDuration());
                        exist.setDuration(durationText);
                        notifyLessonUpdated(assignedId, exist);

                        // KHÔNG update course duration nếu lesson chưa approved
                        if (exist.isInitialApproved()) {
                            int delta = newMinutes - prevMinutes;
                            try {
                                if (ApiProvider.getCourseApi() instanceof CourseService) {
                                    CourseService cs = (CourseService) ApiProvider.getCourseApi();
                                    cs.adjustCourseDuration(exist.getCourseId(), delta);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }

                @Override
                public void onError(@NonNull String reason) {}
            });
        }

        return newLesson;
    }

    @Override
    public Lesson updateLesson(String lessonId, Lesson updatedLesson) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null || updatedLesson == null) return null;

        // Clone lesson để lưu các thay đổi
        Lesson pendingVersion = cloneLesson(updatedLesson);
        pendingVersion.setId(lessonId);
        pendingVersion.setCourseId(existing.getCourseId());

        // Đánh dấu là có thay đổi chờ duyệt
        existing.setEditApproved(false);

        // Lưu pending version
        pendingLessonEdits.put(lessonId, pendingVersion);

        // Fetch duration for pending version
        Activity current = ActivityProvider.getTopActivity();
        if (current != null && updatedLesson.getVideoUrl() != null && !updatedLesson.getVideoUrl().trim().isEmpty()) {
            final String id = lessonId;
            VideoDurationHelper.fetchDuration(current, updatedLesson.getVideoUrl(), new VideoDurationHelper.Callback() {
                @Override
                public void onSuccess(@NonNull String durationText, int durationSeconds) {
                    Lesson pending = pendingLessonEdits.get(id);
                    if (pending != null) {
                        pending.setDuration(durationText);
                    }
                }

                @Override
                public void onError(@NonNull String reason) {}
            });
        }

        notifyLessonUpdated(lessonId, existing);
        return existing;
    }

    @Override
    public boolean deleteLesson(String lessonId) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null) return false;

        // 🔄 SỬA: Track lesson này vào pending deleted
        String courseId = existing.getCourseId();
        if (!pendingDeletedLessons.containsKey(courseId)) {
            pendingDeletedLessons.put(courseId, new ArrayList<>());
        }
        if (!pendingDeletedLessons.get(courseId).contains(lessonId)) {
            pendingDeletedLessons.get(courseId).add(lessonId);
        }

        // Soft delete
        existing.setDeleteRequested(true);
        existing.setEditApproved(false);

        notifyLessonUpdated(lessonId, existing);
        return true;
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

    // ============ APPROVAL WORKFLOW METHODS ============

    @Override
    public List<Lesson> getPendingLessons() {
        List<Lesson> pending = new ArrayList<>();
        for (Lesson lesson : lessonMap.values()) {
            if (lesson.isPendingApproval()) {
                pending.add(lesson);
            }
        }
        return pending;
    }

    @Override
    public List<Lesson> getPendingLessonsForCourse(String courseId) {
        List<Lesson> pending = new ArrayList<>();
        if (courseId == null) return pending;

        for (Lesson lesson : lessonMap.values()) {
            if (courseId.equals(lesson.getCourseId()) && lesson.isPendingApproval()) {
                pending.add(lesson);
            }
        }

        pending.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        return pending;
    }

    @Override
    public boolean approveInitialCreation(String lessonId) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null) return false;

        existing.setInitialApproved(true);
        existing.setEditApproved(true);

        notifyLessonUpdated(lessonId, existing);
        return true;
    }

    @Override
    public boolean rejectInitialCreation(String lessonId) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null) return false;

        if (!existing.isInitialApproved()) {
            lessonMap.remove(lessonId);

            try {
                if (ApiProvider.getCourseApi() instanceof CourseService) {
                    CourseService cs = (CourseService) ApiProvider.getCourseApi();
                    cs.removeLessonFromCourse(existing);
                }
            } catch (Exception ignored) {}

            notifyLessonUpdated(lessonId, null);
            return true;
        }

        return false;
    }

    @Override
    public boolean approveLessonEdit(String lessonId) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null) return false;

        Lesson pendingVersion = pendingLessonEdits.get(lessonId);
        if (pendingVersion == null) {
            existing.setEditApproved(true);
            notifyLessonUpdated(lessonId, existing);
            return true;
        }

        int prevMinutes = parseDurationToMinutesSafe(existing.getDuration());
        int newMinutes = parseDurationToMinutesSafe(pendingVersion.getDuration());
        int delta = newMinutes - prevMinutes;

        existing.setTitle(pendingVersion.getTitle());
        existing.setDescription(pendingVersion.getDescription());
        existing.setVideoUrl(pendingVersion.getVideoUrl());
        existing.setDuration(pendingVersion.getDuration());
        existing.setOrder(pendingVersion.getOrder());

        existing.setEditApproved(true);

        pendingLessonEdits.remove(lessonId);

        if (delta != 0) {
            try {
                if (ApiProvider.getCourseApi() instanceof CourseService) {
                    CourseService cs = (CourseService) ApiProvider.getCourseApi();
                    cs.adjustCourseDuration(existing.getCourseId(), delta);
                }
            } catch (Exception ignored) {}
        }

        notifyLessonUpdated(lessonId, existing);
        return true;
    }

    @Override
    public boolean rejectLessonEdit(String lessonId) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null) return false;

        pendingLessonEdits.remove(lessonId);
        existing.setEditApproved(true);

        notifyLessonUpdated(lessonId, existing);
        return true;
    }

    @Override
    public Lesson getPendingEdit(String lessonId) {
        return pendingLessonEdits.get(lessonId);
    }

    @Override
    public boolean hasPendingEdit(String lessonId) {
        return pendingLessonEdits.containsKey(lessonId);
    }

    @Override
    public boolean permanentlyDeleteLesson(String lessonId) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null) return false;

        lessonMap.remove(lessonId);

        try {
            if (ApiProvider.getCourseApi() instanceof CourseService) {
                CourseService cs = (CourseService) ApiProvider.getCourseApi();
                cs.removeLessonFromCourse(existing);
            }
        } catch (Exception ignored) {}

        notifyLessonUpdated(lessonId, null);
        return true;
    }

    @Override
    public boolean cancelDeleteRequest(String lessonId) {
        Lesson existing = lessonMap.get(lessonId);
        if (existing == null) return false;

        existing.setDeleteRequested(false);
        existing.setEditApproved(true);

        notifyLessonUpdated(lessonId, existing);
        return true;
    }

    // 🆕 THÊM: Method để approve tất cả pending lessons của một course (gọi khi approve EDIT)
    @Override
    public void approveAllPendingLessonsForCourse(String courseId) {
        if (courseId == null) return;

        // 1. Approve các lessons mới được thêm
        List<String> addedIds = pendingAddedLessons.get(courseId);
        if (addedIds != null) {
            for (String lessonId : new ArrayList<>(addedIds)) {
                Lesson lesson = lessonMap.get(lessonId);
                if (lesson != null) {
                    lesson.setInitialApproved(true);
                    lesson.setEditApproved(true);

                    // Update course statistics
                    try {
                        if (ApiProvider.getCourseApi() instanceof CourseService) {
                            CourseService cs = (CourseService) ApiProvider.getCourseApi();
                            cs.addLessonToCourse(lesson);
                        }
                    } catch (Exception ignored) {}

                    notifyLessonUpdated(lessonId, lesson);
                }
            }
            pendingAddedLessons.remove(courseId);
        }

        // 2. Xóa các lessons bị đánh dấu xóa
        List<String> deletedIds = pendingDeletedLessons.get(courseId);
        if (deletedIds != null) {
            for (String lessonId : new ArrayList<>(deletedIds)) {
                permanentlyDeleteLesson(lessonId);
            }
            pendingDeletedLessons.remove(courseId);
        }

        // 3. Approve các lessons bị chỉnh sửa
        for (Lesson lesson : new ArrayList<>(lessonMap.values())) {
            if (courseId.equals(lesson.getCourseId()) && !lesson.isEditApproved() && !lesson.isDeleteRequested()) {
                approveLessonEdit(lesson.getId());
            }
        }
    }

    // 🆕 THÊM: Method để reject tất cả pending lessons của một course (gọi khi reject EDIT)
    @Override
    public void rejectAllPendingLessonsForCourse(String courseId) {
        if (courseId == null) return;

        // 1. Xóa các lessons mới được thêm (chưa approve)
        List<String> addedIds = pendingAddedLessons.get(courseId);
        if (addedIds != null) {
            for (String lessonId : new ArrayList<>(addedIds)) {
                lessonMap.remove(lessonId);
                notifyLessonUpdated(lessonId, null);
            }
            pendingAddedLessons.remove(courseId);
        }

        // 2. Hủy yêu cầu xóa các lessons
        List<String> deletedIds = pendingDeletedLessons.get(courseId);
        if (deletedIds != null) {
            for (String lessonId : new ArrayList<>(deletedIds)) {
                cancelDeleteRequest(lessonId);
            }
            pendingDeletedLessons.remove(courseId);
        }

        // 3. Reject các lessons bị chỉnh sửa
        for (Lesson lesson : new ArrayList<>(lessonMap.values())) {
            if (courseId.equals(lesson.getCourseId()) && !lesson.isEditApproved() && !lesson.isDeleteRequested()) {
                rejectLessonEdit(lesson.getId());
            }
        }
    }

    private Lesson cloneLesson(Lesson original) {
        if (original == null) return null;

        Lesson clone = new Lesson(
                original.getId(),
                original.getCourseId(),
                original.getTitle(),
                original.getDescription(),
                original.getVideoUrl(),
                original.getDuration(),
                original.getOrder()
        );

        clone.setInitialApproved(original.isInitialApproved());
        clone.setEditApproved(original.isEditApproved());
        clone.setDeleteRequested(original.isDeleteRequested());

        return clone;
    }
}