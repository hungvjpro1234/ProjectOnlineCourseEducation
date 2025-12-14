package com.example.projectonlinecourseeducation.data.cart;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fake implementation cho CartApi.
 * Lưu giỏ hàng trong RAM, không có backend.
 * Sau này thay bằng CartRemoteApiService (Retrofit) là xong.
 *
 * CHANGES:
 * - Thêm cơ chế CartUpdateListener để UI có thể đăng ký và nhận notify tự động
 *   khi có thay đổi add/remove/clear.
 * - FIX: Phân quyền giỏ hàng theo userId - mỗi user có giỏ hàng riêng
 */
public class CartFakeApiService implements CartApi {

    private static CartFakeApiService instance;

    public static CartFakeApiService getInstance() {
        if (instance == null) {
            instance = new CartFakeApiService();
        }
        return instance;
    }

    // "Bảng" cart_courses trong RAM - PER USER (key = userId)
    private final Map<String, List<Course>> cartCoursesMap = new HashMap<>();

    // Registered listeners
    private final List<CartApi.CartUpdateListener> listeners = new ArrayList<>();

    private CartFakeApiService() {
    }

    /**
     * Helper: Lấy userId của user hiện tại
     */
    private String getCurrentUserId() {
        User currentUser = ApiProvider.getAuthApi() != null
            ? ApiProvider.getAuthApi().getCurrentUser()
            : null;
        if (currentUser == null || currentUser.getId() == null) {
            return "_GUEST_"; // fallback for guest/unauthenticated users
        }
        return currentUser.getId();
    }

    /**
     * Helper: Lấy giỏ hàng của user hiện tại
     */
    private List<Course> getCurrentUserCart() {
        String userId = getCurrentUserId();
        if (!cartCoursesMap.containsKey(userId)) {
            cartCoursesMap.put(userId, new ArrayList<>());
        }
        return cartCoursesMap.get(userId);
    }

    @Override
    public synchronized List<Course> getCartCourses() {
        // trả bản copy của giỏ hàng user hiện tại để UI không chỉnh trực tiếp list bên trong
        return new ArrayList<>(getCurrentUserCart());
    }

    @Override
    public synchronized boolean addToCart(Course course) {
        if (course == null || course.getId() == null) return false;
        List<Course> userCart = getCurrentUserCart();
        // Không cho trùng courseId
        for (Course c : userCart) {
            if (course.getId().equals(c.getId())) {
                return false; // đã có rồi
            }
        }
        userCart.add(course);
        notifyCartChanged();
        return true;
    }

    @Override
    public synchronized boolean removeFromCart(String courseId) {
        if (courseId == null) return false;
        List<Course> userCart = getCurrentUserCart();
        for (int i = 0; i < userCart.size(); i++) {
            if (courseId.equals(userCart.get(i).getId())) {
                userCart.remove(i);
                notifyCartChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void clearCart() {
        List<Course> userCart = getCurrentUserCart();
        if (userCart.isEmpty()) return;
        userCart.clear();
        notifyCartChanged();
    }

    @Override
    public synchronized boolean isInCart(String courseId) {
        if (courseId == null) return false;
        List<Course> userCart = getCurrentUserCart();
        for (Course c : userCart) {
            if (courseId.equals(c.getId())) return true;
        }
        return false;
    }

    @Override
    public synchronized int getTotalItems() {
        return getCurrentUserCart().size();
    }

    @Override
    public synchronized double getTotalPrice() {
        double total = 0;
        List<Course> userCart = getCurrentUserCart();
        for (Course c : userCart) {
            if (c != null) {
                total += c.getPrice();
            }
        }
        return total;
    }

    // ----------------- Listener registration -----------------

    @Override
    public synchronized void addCartUpdateListener(CartApi.CartUpdateListener listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    @Override
    public synchronized void removeCartUpdateListener(CartApi.CartUpdateListener listener) {
        listeners.remove(listener);
    }

    private synchronized void notifyCartChanged() {
        // copy to avoid concurrent modification while notifying
        List<CartApi.CartUpdateListener> copy = new ArrayList<>(listeners);
        for (CartApi.CartUpdateListener l : copy) {
            try {
                l.onCartChanged();
            } catch (Exception ignored) {
                // ignore listener exceptions to avoid breaking others
            }
        }
    }

    // ------------------ ADMIN: Get data for specific user ------------------

    @Override
    public synchronized List<Course> getCartCoursesForUser(String userId) {
        if (userId == null) return new ArrayList<>();
        if (!cartCoursesMap.containsKey(userId)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(cartCoursesMap.get(userId));
    }

    @Override
    public synchronized double getTotalPriceForUser(String userId) {
        if (userId == null) return 0;
        List<Course> userCart = getCartCoursesForUser(userId);
        double total = 0;
        for (Course c : userCart) {
            if (c != null) {
                total += c.getPrice();
            }
        }
        return total;
    }

    // ------------------ CHECKOUT ------------------

    @Override
    public synchronized List<Course> checkout() {
        // 1. Lấy danh sách courses trong giỏ hiện tại
        List<Course> currentCart = getCartCourses();
        if (currentCart.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Record purchase cho từng course
        com.example.projectonlinecourseeducation.data.course.CourseApi courseApi =
            ApiProvider.getCourseApi();
        if (courseApi != null) {
            for (Course c : currentCart) {
                courseApi.recordPurchase(c.getId());
            }
        }

        // 3. Thêm vào My Courses
        com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi myCourseApi =
            ApiProvider.getMyCourseApi();
        if (myCourseApi != null) {
            myCourseApi.addPurchasedCourses(currentCart);
        }

        // 4. Xóa giỏ hàng
        clearCart();

        // 5. Trả về danh sách courses đã thanh toán
        return currentCart;
    }
}
