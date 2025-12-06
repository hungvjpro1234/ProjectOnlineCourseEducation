package com.example.projectonlinecourseeducation.data.lessoncomment;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fake implementation của LessonCommentApi
 * Lưu trữ bình luận trong bộ nhớ để test và phát triển frontend
 */
public class LessonCommentFakeApiService implements LessonCommentApi {

    private static LessonCommentFakeApiService instance;

    // Bộ đếm để tạo ID tự động
    private final AtomicInteger commentIdCounter = new AtomicInteger(1);

    // Danh sách lưu trữ tất cả bình luận
    private final List<LessonComment> allComments = new ArrayList<>();

    private LessonCommentFakeApiService() {
        // Seed một số bình luận mẫu
        seedSampleComments();
    }

    public static synchronized LessonCommentFakeApiService getInstance() {
        if (instance == null) {
            instance = new LessonCommentFakeApiService();
        }
        return instance;
    }

    /**
     * Tạo dữ liệu bình luận mẫu cho một số bài học với thời gian khác nhau
     */
    private void seedSampleComments() {
        long now = System.currentTimeMillis();
        long ONE_MINUTE = 60 * 1000;
        long ONE_HOUR = 60 * ONE_MINUTE;
        long ONE_DAY = 24 * ONE_HOUR;

        // Bình luận cho bài học "c1_l1" với thời gian đa dạng
        addCommentWithTime("c1_l1", "student1", "Nguyễn Văn A",
                  "https://i.pravatar.cc/150?img=1",
                  "Em chưa hiểu rõ phần cài đặt JDK, thầy có thể giải thích thêm không ạ?",
                  now - 3 * ONE_DAY); // 3 ngày trước

        addCommentWithTime("c1_l1", "student2", "Trần Thị B",
                  "https://i.pravatar.cc/150?img=2",
                  "Video rất hay và dễ hiểu. Cảm ơn thầy!",
                  now - 2 * ONE_DAY); // 2 ngày trước

        addCommentWithTime("c1_l1", "student3", "Lê Văn C",
                  "https://i.pravatar.cc/150?img=3",
                  "Cho em hỏi là sau khi cài JDK xong thì phải config biến môi trường như thế nào ạ?",
                  now - 5 * ONE_HOUR); // 5 giờ trước

        addCommentWithTime("c1_l1", "student4", "Phạm Thị D",
                  "https://i.pravatar.cc/150?img=4",
                  "Cảm ơn thầy, em đã hiểu rồi ạ!",
                  now - 30 * ONE_MINUTE); // 30 phút trước

        addCommentWithTime("c1_l1", "student5", "Hoàng Văn E",
                  "https://i.pravatar.cc/150?img=5",
                  "Bài giảng rất chi tiết, em rất thích!",
                  now - 2 * ONE_MINUTE); // 2 phút trước (Vừa xong)

        // Bình luận cho bài học "c1_l2"
        addCommentWithTime("c1_l2", "student1", "Nguyễn Văn A",
                  "https://i.pravatar.cc/150?img=1",
                  "Phần này khó quá, em cần xem lại nhiều lần.",
                  now - 1 * ONE_DAY); // 1 ngày trước

        addCommentWithTime("c1_l2", "student2", "Trần Thị B",
                  "https://i.pravatar.cc/150?img=2",
                  "Thầy ơi, em làm theo hướng dẫn nhưng bị lỗi này thì sửa như thế nào ạ?",
                  now - 8 * ONE_HOUR); // 8 giờ trước
    }

    /**
     * Thêm bình luận với timestamp tùy chỉnh
     */
    private LessonComment addCommentWithTime(String lessonId, String userId, String userName,
                                             String userAvatar, String content, long timestamp) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        String commentId = "comment_" + commentIdCounter.getAndIncrement();

        LessonComment newComment = new LessonComment(
            commentId,
            lessonId,
            userId,
            userName,
            userAvatar,
            content.trim(),
            timestamp // Sử dụng timestamp tùy chỉnh
        );

        allComments.add(newComment);
        return newComment;
    }

    @Override
    public List<LessonComment> getCommentsForLesson(String lessonId) {
        List<LessonComment> result = new ArrayList<>();

        for (LessonComment comment : allComments) {
            if (comment.getLessonId().equals(lessonId)) {
                result.add(comment);
            }
        }

        // Sắp xếp theo thời gian mới nhất trước
        Collections.sort(result, new Comparator<LessonComment>() {
            @Override
            public int compare(LessonComment c1, LessonComment c2) {
                return Long.compare(c2.getCreatedAt(), c1.getCreatedAt());
            }
        });

        return result;
    }

    @Override
    public LessonComment addComment(String lessonId, String userId, String userName,
                                    String userAvatar, String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        String commentId = "comment_" + commentIdCounter.getAndIncrement();
        long timestamp = System.currentTimeMillis();

        LessonComment newComment = new LessonComment(
            commentId,
            lessonId,
            userId,
            userName,
            userAvatar,
            content.trim(),
            timestamp
        );

        allComments.add(newComment);
        return newComment;
    }

    @Override
    public boolean deleteComment(String commentId, String userId) {
        for (int i = 0; i < allComments.size(); i++) {
            LessonComment comment = allComments.get(i);
            if (comment.getId().equals(commentId)) {
                // Chỉ cho phép người tạo bình luận xóa
                if (comment.getUserId().equals(userId)) {
                    allComments.remove(i);
                    return true;
                }
                // Không có quyền xóa
                return false;
            }
        }
        // Không tìm thấy bình luận
        return false;
    }

    @Override
    public int getCommentCount(String lessonId) {
        int count = 0;
        for (LessonComment comment : allComments) {
            if (comment.getLessonId().equals(lessonId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Reset toàn bộ dữ liệu (dùng cho testing)
     */
    public void clearAllComments() {
        allComments.clear();
        commentIdCounter.set(1);
    }
}
