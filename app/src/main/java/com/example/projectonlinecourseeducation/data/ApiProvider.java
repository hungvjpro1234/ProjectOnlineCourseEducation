package com.example.projectonlinecourseeducation.data;

import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.auth.AuthFakeApiService;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.cart.CartFakeApiService;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.course.CourseFakeApiService;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonFakeApiService;
import com.example.projectonlinecourseeducation.data.review.ReviewApi;
import com.example.projectonlinecourseeducation.data.review.ReviewFakeApiService;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseFakeApiService;

public class ApiProvider {

    // Mặc định đang dùng FakeApi cho Course + Auth + Cart + Lesson + Review
    private static AuthApi authApi = AuthFakeApiService.getInstance();
    private static CourseApi courseApi = CourseFakeApiService.getInstance();
    private static LessonApi lessonApi = LessonFakeApiService.getInstance();
    private static ReviewApi reviewApi = ReviewFakeApiService.getInstance();
    private static CartApi cartApi = CartFakeApiService.getInstance();

    // MyCourseApi: quản lý các khóa học student đã mua
    private static MyCourseApi myCourseApi = MyCourseFakeApiService.getInstance();

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
     * ApiProvider.setMyCourseApi(new MyCourseRemoteApiService(retrofit));
     */
    public static void setMyCourseApi(MyCourseApi api) {
        myCourseApi = api;
    }
}
