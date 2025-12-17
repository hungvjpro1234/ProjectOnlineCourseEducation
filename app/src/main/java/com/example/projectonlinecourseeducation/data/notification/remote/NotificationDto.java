package com.example.projectonlinecourseeducation.data.notification.remote;

import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationStatus;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationType;

public class NotificationDto {
    public String id;

    public String userId;

    public NotificationType type;
    public NotificationStatus status;

    public String title;
    public String message;

    public String courseId;
    public String courseTitle;

    public String lessonId;
    public String lessonTitle;

    public String commentId;
    public String reviewId;

    public float rating;

    public long createdAt;
}
