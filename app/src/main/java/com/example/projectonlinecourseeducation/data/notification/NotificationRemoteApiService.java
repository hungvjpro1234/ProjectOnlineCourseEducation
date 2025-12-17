package com.example.projectonlinecourseeducation.data.notification;

import com.example.projectonlinecourseeducation.core.model.notification.Notification;
import com.example.projectonlinecourseeducation.data.notification.remote.*;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationType;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Remote implementation of NotificationApi
 *
 * - Gọi backend bằng Retrofit (sync)
 * - KHÔNG có realtime listener (UI tự refresh)
 * - Mapping DTO → Model bằng Notification.Builder (AN TOÀN, CHUẨN)
 */
public class NotificationRemoteApiService implements NotificationApi {

    private final NotificationRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(NotificationRetrofitService.class);

    // ================== MAPPING ==================

    private Notification map(NotificationDto d) {
        if (d == null) return null;

        return new Notification.Builder()
                .id(d.id)
                .userId(d.userId)
                .type(d.type)
                .status(d.status)
                .createdAt(d.createdAt)
                .title(d.title)
                .message(d.message)
                .avatarUrl(null)                 // backend chưa có → để null
                .targetCourseId(d.courseId)
                .targetLessonId(d.lessonId)
                .targetCommentId(d.commentId)
                .targetReviewId(d.reviewId)
                .senderName(null)                // backend chưa có → để null
                .courseTitle(d.courseTitle)
                .lessonTitle(d.lessonTitle)
                .build();
    }

    private List<Notification> mapList(List<NotificationDto> list) {
        List<Notification> result = new ArrayList<>();
        if (list == null) return result;

        for (NotificationDto d : list) {
            Notification n = map(d);
            if (n != null) result.add(n);
        }
        return result;
    }

    // ================== QUERY ==================

    @Override
    public List<Notification> getNotificationsForUser(String userId) {
        try {
            Response<NotificationsResponse> res =
                    api.getNotificationsForUser(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return mapList(res.body().data);
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @Override
    public List<Notification> getUnreadNotifications(String userId) {
        try {
            Response<NotificationsResponse> res =
                    api.getUnreadNotifications(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return mapList(res.body().data);
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @Override
    public int getUnreadCount(String userId) {
        try {
            Response<CountResponse> res =
                    api.getUnreadCount(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return res.body().count;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public Notification getNotificationById(String notificationId) {
        try {
            Response<NotificationResponse> res =
                    api.getNotificationById(notificationId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return map(res.body().data);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ================== STATUS UPDATE ==================

    @Override
    public Notification markAsViewed(String notificationId) {
        try {
            Response<NotificationResponse> res =
                    api.markAsViewed(notificationId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return map(res.body().data);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public Notification markAsRead(String notificationId) {
        try {
            Response<NotificationResponse> res =
                    api.markAsRead(notificationId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return map(res.body().data);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public int markAllAsViewed(String userId) {
        try {
            Response<CountResponse> res =
                    api.markAllAsViewed(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return res.body().count;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public int markAllAsRead(String userId) {
        try {
            Response<CountResponse> res =
                    api.markAllAsRead(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return res.body().count;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // ================== CREATE ==================

    @Override
    public Notification createTeacherReplyNotification(String studentId, String teacherName,
                                                       String lessonId, String lessonTitle,
                                                       String courseId, String courseTitle,
                                                       String commentId) {
        try {
            TeacherReplyNotificationRequest req = new TeacherReplyNotificationRequest();
            req.studentId = studentId;
            req.teacherName = teacherName;
            req.lessonId = lessonId;
            req.lessonTitle = lessonTitle;
            req.courseId = courseId;
            req.courseTitle = courseTitle;
            req.commentId = commentId;

            Response<NotificationResponse> res =
                    api.createTeacherReplyNotification(req).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return map(res.body().data);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public Notification createStudentCourseReviewNotification(String teacherId, String studentName,
                                                              String courseId, String courseTitle,
                                                              String reviewId, float rating) {
        try {
            StudentCourseReviewNotificationRequest req =
                    new StudentCourseReviewNotificationRequest();
            req.teacherId = teacherId;
            req.studentName = studentName;
            req.courseId = courseId;
            req.courseTitle = courseTitle;
            req.reviewId = reviewId;
            req.rating = rating;

            Response<NotificationResponse> res =
                    api.createStudentCourseReviewNotification(req).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return map(res.body().data);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public Notification createStudentLessonCommentNotification(String teacherId, String studentName,
                                                               String lessonId, String lessonTitle,
                                                               String courseId, String courseTitle,
                                                               String commentId) {
        try {
            StudentLessonCommentNotificationRequest req =
                    new StudentLessonCommentNotificationRequest();
            req.teacherId = teacherId;
            req.studentName = studentName;
            req.lessonId = lessonId;
            req.lessonTitle = lessonTitle;
            req.courseId = courseId;
            req.courseTitle = courseTitle;
            req.commentId = commentId;

            Response<NotificationResponse> res =
                    api.createStudentLessonCommentNotification(req).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return map(res.body().data);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ================== DELETE ==================

    @Override
    public boolean deleteNotification(String notificationId) {
        try {
            Response<CountResponse> res =
                    api.deleteNotification(notificationId).execute();
            return res.isSuccessful() && res.body() != null && res.body().success;
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public int deleteReadNotifications(String userId) {
        try {
            Response<CountResponse> res =
                    api.deleteReadNotifications(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return res.body().count;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public int deleteAllNotifications(String userId) {
        try {
            Response<CountResponse> res =
                    api.deleteAllNotifications(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return res.body().count;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public List<Notification> getNotificationsByType(String userId,
                                                     NotificationType type) {
        if (userId == null || type == null) return new ArrayList<>();

        List<Notification> all = getNotificationsForUser(userId);
        List<Notification> result = new ArrayList<>();

        for (Notification n : all) {
            if (n != null && n.getType() == type) {
                result.add(n);
            }
        }
        return result;
    }



    // ================== LISTENER (NO-OP) ==================

    @Override
    public void addNotificationUpdateListener(NotificationUpdateListener listener) {
        // Remote API không hỗ trợ realtime
    }

    @Override
    public void removeNotificationUpdateListener(NotificationUpdateListener listener) {
        // NO-OP
    }
}
