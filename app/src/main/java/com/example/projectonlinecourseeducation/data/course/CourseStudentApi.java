package com.example.projectonlinecourseeducation.data.course;

import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;

import java.util.List;

/**
 * API cho danh sách học viên của 1 course.
 * Implementer remote sẽ trả danh sách học viên thực sự (theo student id).
 */
public interface CourseStudentApi {

    /**
     * Lấy danh sách học viên đã mua/enroll cho 1 course.
     * @param courseId id của khóa học
     * @return danh sách CourseStudent (copy)
     */
    List<CourseStudent> getStudentsForCourse(String courseId);

    /**
     * Listener để UI nhận notify khi có thay đổi danh sách students (ví dụ người mua mới).
     */
    interface StudentUpdateListener {
        void onStudentsChanged(String courseId);
    }

    void addStudentUpdateListener(StudentUpdateListener l);
    void removeStudentUpdateListener(StudentUpdateListener l);
}
