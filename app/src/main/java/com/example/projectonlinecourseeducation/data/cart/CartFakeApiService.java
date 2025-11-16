package com.example.projectonlinecourseeducation.data.cart;

import com.example.projectonlinecourseeducation.core.model.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fake implementation cho CartApi.
 * Lưu giỏ hàng trong RAM, không có backend.
 * Sau này thay bằng CartRemoteApiService (Retrofit) là xong.
 */
public class CartFakeApiService implements CartApi {

    private static CartFakeApiService instance;

    public static CartFakeApiService getInstance() {
        if (instance == null) {
            instance = new CartFakeApiService();
        }
        return instance;
    }

    // "Bảng" cart_courses trong RAM
    private final List<Course> cartCourses = new ArrayList<>();

    private CartFakeApiService() {
    }

    @Override
    public synchronized List<Course> getCartCourses() {
        // trả bản copy để UI không chỉnh trực tiếp list bên trong
        return new ArrayList<>(cartCourses);
    }

    @Override
    public synchronized boolean addToCart(Course course) {
        if (course == null || course.getId() == null) return false;
        // Không cho trùng courseId
        for (Course c : cartCourses) {
            if (course.getId().equals(c.getId())) {
                return false; // đã có rồi
            }
        }
        cartCourses.add(course);
        return true;
    }

    @Override
    public synchronized boolean removeFromCart(String courseId) {
        if (courseId == null) return false;
        for (int i = 0; i < cartCourses.size(); i++) {
            if (courseId.equals(cartCourses.get(i).getId())) {
                cartCourses.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void clearCart() {
        cartCourses.clear();
    }

    @Override
    public synchronized boolean isInCart(String courseId) {
        if (courseId == null) return false;
        for (Course c : cartCourses) {
            if (courseId.equals(c.getId())) return true;
        }
        return false;
    }

    @Override
    public synchronized int getTotalItems() {
        return cartCourses.size();
    }

    @Override
    public synchronized double getTotalPrice() {
        double total = 0;
        for (Course c : cartCourses) {
            if (c != null) {
                total += c.getPrice();
            }
        }
        return total;
    }
}
