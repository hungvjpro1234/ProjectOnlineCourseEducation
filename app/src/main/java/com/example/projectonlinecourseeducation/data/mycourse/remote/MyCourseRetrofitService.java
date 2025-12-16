package com.example.projectonlinecourseeducation.data.mycourse.remote;

import com.example.projectonlinecourseeducation.data.cart.remote.CheckoutRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyCourseRetrofitService {

    /**
     * GET /my-courses
     * Lấy danh sách khóa học đã mua của user hiện tại
     */
    @GET("my-courses")
    Call<MyCourseApiResponse> getMyCourses();

    /**
     * GET /my-courses/{courseId}/status
     * Kiểm tra user hiện tại đã mua course chưa
     */
    @GET("my-courses/{courseId}/status")
    Call<MyCourseStatusResponse> isPurchased(
            @Path("courseId") int courseId
    );

    /**
     * POST /cart/checkout
     * Thanh toán – gián tiếp thêm vào my-courses
     */
    @POST("cart/checkout")
    Call<MyCourseApiResponse> checkout(
            @Body CheckoutRequest request
    );

    /**
     * ADMIN – GET /admin/my-courses/{userId}
     */
    @GET("admin/my-courses/{userId}")
    Call<MyCourseApiResponse> getMyCoursesForUser(
            @Path("userId") String userId
    );
}
