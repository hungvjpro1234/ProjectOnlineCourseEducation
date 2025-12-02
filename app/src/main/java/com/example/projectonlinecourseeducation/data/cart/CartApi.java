package com.example.projectonlinecourseeducation.data.cart;

import com.example.projectonlinecourseeducation.core.model.course.Course;

import java.util.List;

public interface CartApi {

    /**
     * Lấy toàn bộ khóa học trong giỏ.
     * Tưởng tượng tương đương GET /cart
     */
    List<Course> getCartCourses();

    /**
     * Thêm 1 khóa học vào giỏ.
     * Trả về true nếu thêm mới, false nếu đã tồn tại trước đó.
     * Tưởng tượng tương đương POST /cart
     */
    boolean addToCart(Course course);

    /**
     * Xóa 1 khóa học khỏi giỏ.
     * Tưởng tượng tương đương DELETE /cart/{courseId}
     */
    boolean removeFromCart(String courseId);

    /**
     * Xóa toàn bộ giỏ hàng.
     * Tưởng tượng tương đương DELETE /cart
     */
    void clearCart();

    /**
     * Khóa học đã có trong giỏ chưa.
     * Tưởng tượng tương đương GET /cart/contains?courseId=...
     */
    boolean isInCart(String courseId);

    /**
     * Số item trong giỏ.
     */
    int getTotalItems();

    /**
     * Tổng số tiền cần thanh toán.
     */
    double getTotalPrice();
}
