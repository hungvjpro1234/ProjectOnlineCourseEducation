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

    // ✅ GET cart → List<Course>
    @GET("cart/{userId}")
    Call<CartApiResponse<List<CartCourseDto>>> getCartItems(
            @Path("userId") int userId
    );

    // ✅ add to cart
    @POST("cart/add")
    Call<CartApiResponse<Void>> addToCart(
            @Body AddToCartRequest request
    );

    // ✅ remove from cart
    @POST("cart/remove")
    Call<CartApiResponse<Void>> removeFromCart(
            @Body RemoveFromCartRequest request
    );

    // ✅ checkout (backend IGNORE courseIds)
    @POST("cart/checkout")
    Call<CartApiResponse<List<CartCourseDto>>> checkout(
            @Body CheckoutRequest request
    );

    // ✅ get course status
    @GET("course/{userId}/{courseId}/status")
    Call<CourseStatusResponse> getCourseStatus(
            @Path("userId") int userId,
            @Path("courseId") int courseId
    );
}
