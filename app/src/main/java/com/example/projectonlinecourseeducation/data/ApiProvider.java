package com.example.projectonlinecourseeducation.data;

import com.example.projectonlinecourseeducation.data.auth.AuthApi;
import com.example.projectonlinecourseeducation.data.auth.AuthFakeApiService;

public class ApiProvider {

    // Mặc định đang dùng FakeApi cho Course + Auth
    private static CourseApi courseApi = CourseFakeApiService.getInstance();
    private static AuthApi authApi = AuthFakeApiService.getInstance();

    public static CourseApi getCourseApi() {
        return courseApi;
    }

    public static void setCourseApi(CourseApi api) {
        courseApi = api;
    }

    public static AuthApi getAuthApi() {
        return authApi;
    }

    // Sau này ở Application hoặc chỗ init Retrofit:
    // ApiProvider.setAuthApi(new AuthRemoteApiService(retrofit));
    public static void setAuthApi(AuthApi api) {
        authApi = api;
    }
}
