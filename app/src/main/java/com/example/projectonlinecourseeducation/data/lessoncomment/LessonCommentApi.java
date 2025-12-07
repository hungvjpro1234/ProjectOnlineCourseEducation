package com.example.projectonlinecourseeducation.data.lessoncomment;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;
import java.util.List;

/**
 * Interface cho API quản lý bình luận bài học
 * Học sinh có thể bình luận để thắc mắc về bài giảng
 * Giáo viên có thể trả lời hoặc xóa bình luận
 *
 * UPDATED: Thêm hỗ trợ teacher reply và delete
 */
public interface LessonCommentApi {

    /**
     * Lấy danh sách bình luận của một bài học
     * @param lessonId ID của bài học
     * @return Danh sách bình luận, sắp xếp theo thời gian (mới nhất trước)
     */
    List<LessonComment> getCommentsForLesson(String lessonId);

    /**
     * Thêm bình luận mới vào bài học
     * @param lessonId ID của bài học
     * @param userId ID của người bình luận
     * @param userName Tên người bình luận
     * @param content Nội dung bình luận
     * @return LessonComment vừa tạo (có id và timestamp)
     */
    LessonComment addComment(String lessonId, String userId, String userName, String content);

    /**
     * Xóa bình luận (chỉ người tạo hoặc giáo viên mới được xóa)
     * @param commentId ID của bình luận
     * @param userId ID của người yêu cầu xóa
     * @return true nếu xóa thành công, false nếu không có quyền hoặc không tìm thấy
     */
    boolean deleteComment(String commentId, String userId);

    /**
     * Đánh dấu comment đã bị xóa (soft delete)
     * Giữ lại comment nhưng đánh dấu isDeleted = true
     * @param commentId ID của comment
     * @return LessonComment đã được update, null nếu không tìm thấy
     */
    LessonComment markCommentAsDeleted(String commentId);

    /**
     * Teacher trả lời bình luận
     * @param commentId ID của comment cần trả lời
     * @param teacherName Tên teacher
     * @param replyContent Nội dung trả lời
     * @return LessonComment đã được update với reply, null nếu không tìm thấy
     */
    LessonComment addReply(String commentId, String teacherName, String replyContent);

    /**
     * Xóa reply của teacher
     * @param commentId ID của comment có reply
     * @return LessonComment đã xóa reply, null nếu không tìm thấy
     */
    LessonComment deleteReply(String commentId);

    /**
     * Đếm số lượng bình luận của một bài học
     * @param lessonId ID của bài học
     * @return Số lượng bình luận
     */
    int getCommentCount(String lessonId);

    // ------------------ LISTENER / NOTIFY ------------------

    /**
     * Listener để UI hoặc các component khác đăng ký nhận thông báo khi bộ comment thay đổi.
     * Khi onCommentsChanged() được gọi, component nên gọi lại getCommentsForLesson()/getCommentCount()
     * để lấy trạng thái mới.
     */
    interface LessonCommentUpdateListener {
        void onCommentsChanged();
    }

    /**
     * Đăng ký listener.
     */
    void addLessonCommentUpdateListener(LessonCommentUpdateListener listener);

    /**
     * Hủy đăng ký listener.
     */
    void removeLessonCommentUpdateListener(LessonCommentUpdateListener listener);
}