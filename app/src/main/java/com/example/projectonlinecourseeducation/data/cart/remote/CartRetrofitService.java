package com.example.projectonlinecourseeducation.data.cart.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CartRetrofitService {

    // Student APIs
    @GET("cart")
    Call<CartResponse> getCart();

    @POST("cart")
    Call<Void> addToCart(@Body AddToCartRequest request);

    @DELETE("cart/{courseId}")
    Call<Void> removeFromCart(@Path("courseId") String courseId);

    @DELETE("cart")
    Call<Void> clearCart();

    // Admin APIs
    @GET("admin/cart/{userId}")
    Call<CartResponse> getCartForUser(@Path("userId") String userId);
}
