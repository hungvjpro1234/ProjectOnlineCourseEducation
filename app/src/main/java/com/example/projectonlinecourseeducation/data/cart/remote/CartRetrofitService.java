package com.example.projectonlinecourseeducation.data.cart.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit interface for Cart API endpoints
 * Maps to backend Cart routes in server.js
 */
public interface CartRetrofitService {

    /**
     * GET /cart/:userId
     * Lấy danh sách cart items cho user
     * Backend response: { success, data: [CartItemDto] }
     *
     * NOTE: Backend hiện tại chỉ trả về payment_status records.
     * Backend cần JOIN với course table để trả về full Course data.
     */
    @GET("cart/{userId}")
    Call<CartApiResponse<List<CartItemDto>>> getCartItems(@Path("userId") int userId);

    /**
     * POST /cart/add
     * Thêm course vào giỏ hàng
     * Request body: { userId, courseId, price_snapshot?, course_name? }
     * Response: { success, message }
     */
    @POST("cart/add")
    Call<CartApiResponse<Void>> addToCart(@Body AddToCartRequest request);

    /**
     * POST /cart/remove
     * Xóa course khỏi giỏ hàng
     * Request body: { userId, courseId }
     * Response: { success, message }
     */
    @POST("cart/remove")
    Call<CartApiResponse<Void>> removeFromCart(@Body RemoveFromCartRequest request);

    /**
     * POST /cart/checkout
     * Thanh toán toàn bộ giỏ hàng
     * Request body: { userId, courseIds: [1, 2, 3] }
     * Response: { success, message, data: { purchased: [...] } }
     */
    @POST("cart/checkout")
    Call<CartApiResponse<Void>> checkout(@Body CheckoutRequest request);

    /**
     * GET /course/:userId/:courseId/status
     * Kiểm tra trạng thái của course cho user
     * Response: { success, data: { status: "NOT_PURCHASED" | "IN_CART" | "PURCHASED" } }
     */
    @GET("course/{userId}/{courseId}/status")
    Call<CartApiResponse<CourseStatusDto>> getCourseStatus(
            @Path("userId") int userId,
            @Path("courseId") int courseId
    );

    // NOTE: Backend chưa có endpoint POST /cart/clear
    // Sẽ cần implement trong backend hoặc dùng workaround (remove từng item)
}
