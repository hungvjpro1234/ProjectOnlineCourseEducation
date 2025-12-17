package com.example.projectonlinecourseeducation.data.lessoncomment.remote;

public class LessonCommentDto {

    public String id;
    public String lessonId;
    public String userId;
    public String userName;
    public String content;
    public long createdAt;

    public boolean isDeleted;

    public String teacherReplyContent;
    public String teacherReplyBy;
    public Long teacherReplyAt;
}
