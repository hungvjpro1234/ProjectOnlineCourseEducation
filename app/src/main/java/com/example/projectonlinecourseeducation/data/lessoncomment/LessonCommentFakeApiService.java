package com.example.projectonlinecourseeducation.data.lessoncomment;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;
import static com.example.projectonlinecourseeducation.core.utils.OnlyApiService.LessonCommentSeedData.COMMENTS_JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fake implementation của LessonCommentApi
 * - Hỗ trợ seed từ JSON
 * - Hỗ trợ teacher reply và soft delete
 * - Thêm cơ chế listener để UI có thể đăng ký và nhận notify khi có thay đổi
 *
 * UPDATED: Implement addReply, deleteReply, markAsDeleted
 */
public class LessonCommentFakeApiService implements LessonCommentApi {

    private static LessonCommentFakeApiService instance;

    // Bộ đếm để tạo ID tự động
    private final AtomicInteger commentIdCounter = new AtomicInteger(1);

    // Danh sách lưu trữ tất cả bình luận
    private final List<LessonComment> allComments = new ArrayList<>();

    // Registered listeners
    private final List<LessonCommentApi.LessonCommentUpdateListener> listeners = new ArrayList<>();

    private LessonCommentFakeApiService() {
        seedFromJson();
    }

    public static synchronized LessonCommentFakeApiService getInstance() {
        if (instance == null) {
            instance = new LessonCommentFakeApiService();
        }
        return instance;
    }

    /**
     * Seed từ JSON
     */
    private void seedFromJson() {
        synchronized (this) {
            allComments.clear();
            commentIdCounter.set(1);

            long now = System.currentTimeMillis();

            try {
                JSONArray arr = new JSONArray(COMMENTS_JSON);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);

                    String lessonId = o.optString("lessonId", "");
                    String userId = o.optString("userId", "");
                    String userName = o.optString("userName", "");
                    String content = o.optString("content", "");
                    long offset = o.optLong("createdAtOffsetMs", 0);
                    long createdAt = now + offset;

                    addCommentInternal(lessonId, userId, userName, content, createdAt);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // ----------------- IMPLEMENT LessonCommentApi -----------------

    @Override
    public synchronized List<LessonComment> getCommentsForLesson(String lessonId) {
        List<LessonComment> result = new ArrayList<>();

        for (LessonComment comment : allComments) {
            if (comment.getLessonId() != null && comment.getLessonId().equals(lessonId)) {
                // FIX: Không hiển thị comment đã bị xóa (kể cả có reply)
                // Khi teacher soft delete, comment sẽ bị ẩn hoàn toàn khỏi UI
                if (!comment.isDeleted()) {
                    result.add(comment);
                }
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
    public synchronized LessonComment addComment(String lessonId, String userId, String userName, String content) {
        if (content == null || content.trim().isEmpty()) return null;

        long ts = System.currentTimeMillis();

        LessonComment added = addCommentInternal(lessonId, userId, userName, content.trim(), ts);

        if (added != null) {
            notifyCommentsChanged();
        }

        return added;
    }

    @Override
    public synchronized boolean deleteComment(String commentId, String userId) {
        for (int i = 0; i < allComments.size(); i++) {
            LessonComment comment = allComments.get(i);
            if (comment.getId().equals(commentId)) {
                if (comment.getUserId().equals(userId)) {
                    allComments.remove(i);
                    notifyCommentsChanged();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public synchronized LessonComment markCommentAsDeleted(String commentId) {
        LessonComment found = findById(commentId);
        if (found == null) return null;

        // Tạo comment mới với isDeleted = true
        LessonComment updated = new LessonComment(
                found.getId(),
                found.getLessonId(),
                found.getUserId(),
                found.getUserName(),
                found.getContent(),
                found.getCreatedAt(),
                true, // isDeleted = true
                found.getTeacherReplyContent(),
                found.getTeacherReplyBy(),
                found.getTeacherReplyAt()
        );

        // Replace trong list
        replaceComment(found, updated);
        notifyCommentsChanged();
        return updated;
    }

    @Override
    public synchronized LessonComment addReply(String commentId, String teacherName, String replyContent) {
        LessonComment found = findById(commentId);
        if (found == null) return null;
        if (replyContent == null || replyContent.trim().isEmpty()) return null;

        // FIX: Đơn giản hóa - chỉ 1 teacher (course owner) có thể reply
        // Nếu đã có reply → update/replace (không append vì chỉ có 1 teacher)
        LessonComment updated = new LessonComment(
                found.getId(),
                found.getLessonId(),
                found.getUserId(),
                found.getUserName(),
                found.getContent(),
                found.getCreatedAt(),
                found.isDeleted(),
                replyContent.trim(), // Replace reply content
                teacherName,
                System.currentTimeMillis() // Update reply timestamp
        );

        // Replace trong list
        replaceComment(found, updated);
        notifyCommentsChanged();
        return updated;
    }

    @Override
    public synchronized LessonComment deleteReply(String commentId) {
        LessonComment found = findById(commentId);
        if (found == null) return null;

        // Tạo comment mới với reply = null
        LessonComment updated = new LessonComment(
                found.getId(),
                found.getLessonId(),
                found.getUserId(),
                found.getUserName(),
                found.getContent(),
                found.getCreatedAt(),
                found.isDeleted(),
                null, // xóa reply
                null,
                null
        );

        // Replace trong list
        replaceComment(found, updated);
        notifyCommentsChanged();
        return updated;
    }

    @Override
    public synchronized int getCommentCount(String lessonId) {
        int count = 0;
        for (LessonComment comment : allComments) {
            if (lessonId == null) continue;
            if (lessonId.equals(comment.getLessonId())) count++;
        }
        return count;
    }

    // ----------------- Listener registration -----------------

    @Override
    public synchronized void addLessonCommentUpdateListener(LessonCommentApi.LessonCommentUpdateListener listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    @Override
    public synchronized void removeLessonCommentUpdateListener(LessonCommentApi.LessonCommentUpdateListener listener) {
        listeners.remove(listener);
    }

    private synchronized void notifyCommentsChanged() {
        List<LessonCommentApi.LessonCommentUpdateListener> copy = new ArrayList<>(listeners);
        for (LessonCommentApi.LessonCommentUpdateListener l : copy) {
            try {
                l.onCommentsChanged();
            } catch (Exception ignored) {
            }
        }
    }

    // ----------------- Helpers -----------------

    /**
     * Internal helper để add comment với timestamp tùy chỉnh
     */
    private LessonComment addCommentInternal(String lessonId, String userId, String userName,
                                             String content, long timestamp) {
        if (content == null || content.trim().isEmpty()) return null;

        String commentId = "comment_" + commentIdCounter.getAndIncrement();

        LessonComment newComment = new LessonComment(
                commentId,
                lessonId,
                userId,
                userName,
                content,
                timestamp
        );

        allComments.add(newComment);
        return newComment;
    }

    /**
     * Tìm comment theo id
     */
    private LessonComment findById(String id) {
        if (id == null) return null;
        for (LessonComment c : allComments) {
            if (id.equals(c.getId())) return c;
        }
        return null;
    }

    /**
     * Replace comment trong list (vì LessonComment immutable)
     */
    private void replaceComment(LessonComment oldComment, LessonComment newComment) {
        for (int i = 0; i < allComments.size(); i++) {
            if (allComments.get(i).getId().equals(oldComment.getId())) {
                allComments.set(i, newComment);
                return;
            }
        }
    }

    /**
     * Dùng cho testing: reset toàn bộ dữ liệu
     */
    public synchronized void clearAllComments() {
        if (allComments.isEmpty()) return;
        allComments.clear();
        commentIdCounter.set(1);
        notifyCommentsChanged();
    }

    /**
     * Trả danh sách (copy) toàn bộ comments - tiện cho debug
     */
    public synchronized List<LessonComment> listAllComments() {
        return new ArrayList<>(allComments);
    }
}