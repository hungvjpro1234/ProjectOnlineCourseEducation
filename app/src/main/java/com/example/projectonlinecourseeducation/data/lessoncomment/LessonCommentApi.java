package com.example.projectonlinecourseeducation.data.lessoncomment;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;
import java.util.List;

/**
 * Interface cho API quản lý bình luận bài học
 * Học sinh có thể bình luận để thắc mắc về bài giảng
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
     * @param userAvatar Avatar của người bình luận (có thể null)
     * @param content Nội dung bình luận
     * @return LessonComment vừa tạo (có id và timestamp)
     */
    LessonComment addComment(String lessonId, String userId, String userName,
                            String userAvatar, String content);

    /**
     * Xóa bình luận (chỉ người tạo hoặc giáo viên mới được xóa)
     * @param commentId ID của bình luận
     * @param userId ID của người yêu cầu xóa
     * @return true nếu xóa thành công, false nếu không có quyền hoặc không tìm thấy
     */
    boolean deleteComment(String commentId, String userId);

    /**
     * Đếm số lượng bình luận của một bài học
     * @param lessonId ID của bài học
     * @return Số lượng bình luận
     */
    int getCommentCount(String lessonId);
}
