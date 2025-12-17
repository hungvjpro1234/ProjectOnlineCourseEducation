package com.example.projectonlinecourseeducation.data.notification.remote;

import retrofit2.Call;
import retrofit2.http.*;

public interface NotificationRetrofitService {

    // -------- QUERY --------

    @GET("notifications")
    Call<NotificationsResponse> getNotificationsForUser(
            @Query("userId") String userId
    );

    @GET("notifications/unread")
    Call<NotificationsResponse> getUnreadNotifications(
            @Query("userId") String userId
    );

    @GET("notifications/unread/count")
    Call<CountResponse> getUnreadCount(
            @Query("userId") String userId
    );

    @GET("notifications/{id}")
    Call<NotificationResponse> getNotificationById(
            @Path("id") String notificationId
    );

    // -------- STATUS UPDATE --------

    @POST("notifications/{id}/viewed")
    Call<NotificationResponse> markAsViewed(
            @Path("id") String notificationId
    );

    @POST("notifications/{id}/read")
    Call<NotificationResponse> markAsRead(
            @Path("id") String notificationId
    );

    @POST("notifications/viewed/all")
    Call<CountResponse> markAllAsViewed(
            @Query("userId") String userId
    );

    @POST("notifications/read/all")
    Call<CountResponse> markAllAsRead(
            @Query("userId") String userId
    );

    // -------- CREATE --------

    @POST("notifications/teacher-reply")
    Call<NotificationResponse> createTeacherReplyNotification(
            @Body TeacherReplyNotificationRequest req
    );

    @POST("notifications/student-course-review")
    Call<NotificationResponse> createStudentCourseReviewNotification(
            @Body StudentCourseReviewNotificationRequest req
    );

    @POST("notifications/student-lesson-comment")
    Call<NotificationResponse> createStudentLessonCommentNotification(
            @Body StudentLessonCommentNotificationRequest req
    );

    // -------- DELETE --------

    @DELETE("notifications/{id}")
    Call<CountResponse> deleteNotification(
            @Path("id") String notificationId
    );

    @DELETE("notifications/read")
    Call<CountResponse> deleteReadNotifications(
            @Query("userId") String userId
    );

    @DELETE("notifications")
    Call<CountResponse> deleteAllNotifications(
            @Query("userId") String userId
    );
}
