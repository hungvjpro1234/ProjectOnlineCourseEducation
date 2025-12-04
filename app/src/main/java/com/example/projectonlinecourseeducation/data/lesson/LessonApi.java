package com.example.projectonlinecourseeducation.data.lesson;

import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

import java.util.List;

public interface LessonApi {

    /**
     * Lấy danh sách bài học của một khóa học
     * @param courseId: ID khóa học
     * @return danh sách Lesson
     */
    List<Lesson> getLessonsForCourse(String courseId);

    /**
     * Lấy chi tiết bài học (bao gồm videoUrl, description, order)
     * @param lessonId: ID bài học
     * @return Lesson object
     */
    Lesson getLessonDetail(String lessonId);

    /**
     * Tạo bài học mới
     * @param newLesson: Lesson object cần tạo
     * @return Lesson object đã được tạo (có ID)
     */
    Lesson createLesson(Lesson newLesson);

    /**
     * Cập nhật bài học
     * @param lessonId: ID bài học cần cập nhật
     * @param updatedLesson: Thông tin cập nhật
     * @return Lesson object sau khi cập nhật
     */
    Lesson updateLesson(String lessonId, Lesson updatedLesson);

    /**
     * Xóa bài học
     * @param lessonId: ID bài học cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy
     */
    boolean deleteLesson(String lessonId);

    // ------------------------------------------------------------
    // Listener / observer support
    //
    // LÝ DO THÊM:
    // - Để giao diện (Activity/Fragment) không phải biết provider là fake hay remote nhưng
    //   vẫn có thể nhận các cập nhật bất đồng bộ (ví dụ: backend tính xong duration rồi push).
    // - Implementations có thể gọi listener khi dữ liệu một lesson thay đổi (ví dụ: duration được cập nhật).
    //
    // LƯU Ý:
    // - Nếu implementer không có cơ chế push/realtime, có thể triển khai add/remove listener như no-op.
    // - Các implementer nên đảm bảo notify listener trên thread phù hợp (thường là main/UI thread),
    //   hoặc document rõ hành vi để caller biết phải post lên UI thread nếu cần.
    // ------------------------------------------------------------

    /**
     * Listener để nhận các cập nhật của một Lesson (ví dụ: duration được backend/fake cập nhật).
     * Giao diện có thể đăng ký listener này để cập nhật UI khi dữ liệu thay đổi.
     */
    interface LessonUpdateListener {
        /**
         * Called when a lesson is updated by the provider.
         *
         * @param lessonId ID của lesson được cập nhật
         * @param updatedLesson đối tượng Lesson mới nhất (có thể là same instance hoặc copy)
         */
        void onLessonUpdated(String lessonId, Lesson updatedLesson);
    }

    /**
     * Đăng ký một listener để nhận cập nhật về lesson.
     * Implementations nên giữ reference tới listener cho tới khi removeLessonUpdateListener được gọi.
     *
     * @param l listener cần đăng ký (null sẽ bị bỏ qua)
     */
    void addLessonUpdateListener(LessonUpdateListener l);

    /**
     * Hủy đăng ký một listener đã đăng ký trước đó.
     *
     * @param l listener cần hủy (nếu chưa đăng ký thì bỏ qua)
     */
    void removeLessonUpdateListener(LessonUpdateListener l);
}
