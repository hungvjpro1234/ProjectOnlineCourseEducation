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

    // ------------------ LISTENER / NOTIFY ------------------

    /**
     * Listener để UI hoặc các component khác đăng ký nhận thông báo khi giỏ hàng thay đổi.
     * Khi onCartChanged() được gọi, component nên gọi lại getCartCourses()/getTotalItems()/getTotalPrice()
     * để lấy trạng thái mới.
     */
    interface CartUpdateListener {
        void onCartChanged();
    }

    /**
     * Đăng ký listener.
     */
    void addCartUpdateListener(CartUpdateListener listener);

    /**
     * Hủy đăng ký listener.
     */
    void removeCartUpdateListener(CartUpdateListener listener);

    // ------------------ ADMIN: Get data for specific user ------------------

    /**
     * (ADMIN) Lấy giỏ hàng của user cụ thể
     * @param userId ID của user cần xem
     * @return Danh sách courses trong giỏ hàng của user đó
     */
    List<Course> getCartCoursesForUser(String userId);

    /**
     * (ADMIN) Lấy tổng giá trị giỏ hàng của user cụ thể
     * @param userId ID của user cần xem
     * @return Tổng giá trị giỏ hàng
     */
    double getTotalPriceForUser(String userId);

    // ------------------ CHECKOUT ------------------

    /**
     * Checkout toàn bộ giỏ hàng (quy trình thanh toán).
     * Thực hiện các bước:
     * 1. Lấy danh sách courses trong giỏ
     * 2. Gọi courseApi.recordPurchase() cho từng course
     * 3. Thêm vào myCourseApi
     * 4. Xóa giỏ hàng
     *
     * @return Danh sách courses đã thanh toán
     */
    List<Course> checkout();
}
