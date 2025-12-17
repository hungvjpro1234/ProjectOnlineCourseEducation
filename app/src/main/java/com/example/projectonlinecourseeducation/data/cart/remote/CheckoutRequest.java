package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request body for POST /cart/checkout
 * Backend expects: { userId, courseIds: [1, 2, 3] }
 */
public class CheckoutRequest {

    @SerializedName("userId")
    private Integer userId;

    public CheckoutRequest(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}
