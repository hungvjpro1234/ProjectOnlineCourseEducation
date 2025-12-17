package com.example.projectonlinecourseeducation.data;

import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.auth.AuthService;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.cart.CartService;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.course.CourseService;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonService;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressService;
import com.example.projectonlinecourseeducation.data.coursereview.ReviewApi;
import com.example.projectonlinecourseeducation.data.coursereview.ReviewService;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseService;
import com.example.projectonlinecourseeducation.data.lessoncomment.LessonCommentApi;
import com.example.projectonlinecourseeducation.data.lessoncomment.LessonCommentService;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizService;

// NEW imports for CourseStudentApi
import com.example.projectonlinecourseeducation.data.course.CourseStudentApi;
import com.example.projectonlinecourseeducation.data.course.CourseStudentService;

// NEW imports for NotificationApi
import com.example.projectonlinecourseeducation.data.notification.NotificationApi;
import com.example.projectonlinecourseeducation.data.notification.NotificationService;

public class ApiProvider {

    // Mặc định đang dùng FakeApi cho Course + Auth + Cart + Lesson + Review + LessonProgress
    private static AuthApi authApi = AuthService.getInstance();
    private static CourseApi courseApi = CourseService.getInstance();
    private static LessonApi lessonApi = LessonService.getInstance();
    private static LessonProgressApi lessonProgressApi = LessonProgressService.getInstance();
    private static ReviewApi reviewApi = ReviewService.getInstance();
    private static CartApi cartApi = CartService.getInstance();
    private static LessonQuizApi lessonQuizApi = LessonQuizService.getInstance();

    // MyCourseApi: quản lý các khóa học student đã mua
    private static MyCourseApi myCourseApi = MyCourseService.getInstance();

    // LessonCommentApi: quản lý bình luận bài học
    private static LessonCommentApi lessonCommentApi = LessonCommentService.getInstance();

    // -------- CourseStudentApi (NEW) --------
    private static CourseStudentApi courseStudentApi = CourseStudentService.getInstance();

    // NotificationApi: quản lý thông báo cho 3 role (Student, Teacher, Admin)
    private static NotificationApi notificationApi = NotificationService.getInstance();

    // -------- Getters / Setters --------
    // -------- Auth --------
    public static AuthApi getAuthApi() {
        return authApi;
    }

    public static void setAuthApi(AuthApi api) {
        authApi = api;
    }

    // -------- Course --------
    public static CourseApi getCourseApi() {
        return courseApi;
    }

    public static void setCourseApi(CourseApi api) {
        courseApi = api;
    }

    // -------- Lesson --------
    public static LessonApi getLessonApi() {
        return lessonApi;
    }

    public static void setLessonApi(LessonApi api) {
        lessonApi = api;
    }

    // -------- LessonProgress --------
    public static LessonProgressApi getLessonProgressApi() {
        return lessonProgressApi;
    }

    /**
     * Sau này ở Application hoặc chỗ init Retrofit:
     * ApiProvider.setLessonProgressApi(new LessonProgressRemoteApiService(retrofit));
     */
    public static void setLessonProgressApi(LessonProgressApi api) {
        lessonProgressApi = api;
    }

    // -------- Review --------
    public static ReviewApi getReviewApi() {
        return reviewApi;
    }

    public static void setReviewApi(ReviewApi api) {
        reviewApi = api;
    }

    // -------- Cart --------
    public static CartApi getCartApi() {
        return cartApi;
    }

    /**
     * Sau này ở Application hoặc chỗ init Retrofit:
     * ApiProvider.setCartApi(new CartRemoteApiService(retrofit));
     */
    public static void setCartApi(CartApi api) {
        cartApi = api;
    }

    // -------- MyCourse --------
    public static MyCourseApi getMyCourseApi() {
        return myCourseApi;
    }

    /**
     * Sau này ở Application hoặc chỗ init Retrofit:
     * ApiProvider.setMyCourseApi(MyCourseRemoteApiService)
     */
    public static void setMyCourseApi(MyCourseApi api) {
        myCourseApi = api;
    }

    // -------- LessonComment --------
    public static LessonCommentApi getLessonCommentApi() {
        return lessonCommentApi;
    }

    /**
     * Sau này ở Application hoặc chỗ init Retrofit:
     * ApiProvider.setLessonCommentApi(new LessonCommentRemoteApiService(retrofit));
     */
    public static void setLessonCommentApi(LessonCommentApi api) {
        lessonCommentApi = api;
    }

    // -------- CourseStudentApi (NEW) --------
    public static CourseStudentApi getCourseStudentApi() {
        return courseStudentApi;
    }

    /**
     * Khi có remote implementation, set ở Application.onCreate()
     */
    public static void setCourseStudentApi(CourseStudentApi api) {
        courseStudentApi = api;
    }

    // -------- LessonQuiz --------
    public static LessonQuizApi getLessonQuizApi() {
        return lessonQuizApi;
    }

    /**
     * Khi có remote implementation, set ở Application.onCreate()
     * ApiProvider.setQuizApi(new QuizRemoteApiService(retrofit));
     */
    public static void setQuizApi(LessonQuizApi api) {
        lessonQuizApi = api;
    }

    // -------- Notification --------
    public static NotificationApi getNotificationApi() {
        return notificationApi;
    }

    /**
     * Khi có remote implementation, set ở Application.onCreate()
     * ApiProvider.setNotificationApi(new NotificationRemoteApiService(retrofit));
     */
    public static void setNotificationApi(NotificationApi api) {
        notificationApi = api;
    }
}
