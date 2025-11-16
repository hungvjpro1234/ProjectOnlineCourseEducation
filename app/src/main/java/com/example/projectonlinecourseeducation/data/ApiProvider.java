package com.example.projectonlinecourseeducation.data;

import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.auth.AuthFakeApiService;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.course.CourseFakeApiService;
import com.example.projectonlinecourseeducation.data.cart.CartFakeApiService;

public class ApiProvider {

    // Mặc định đang dùng FakeApi cho Course + Auth + Cart
    private static CourseApi courseApi = CourseFakeApiService.getInstance();
    private static AuthApi authApi = AuthFakeApiService.getInstance();
    private static CartApi cartApi = CartFakeApiService.getInstance();

    // -------- Course --------
    public static CourseApi getCourseApi() {
        return courseApi;
    }

    public static void setCourseApi(CourseApi api) {
        courseApi = api;
    }

    // -------- Auth --------
    public static AuthApi getAuthApi() {
        return authApi;
    }

    public static void setAuthApi(AuthApi api) {
        authApi = api;
    }

    // -------- Cart --------
    public static CartApi getCartApi() {
        return cartApi;
    }

    // Sau này ở Application hoặc chỗ init Retrofit:
    // ApiProvider.setCartApi(new CartRemoteApiService(retrofit));
    public static void setCartApi(CartApi api) {
        cartApi = api;
    }
}
