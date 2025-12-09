package com.example.projectonlinecourseeducation.feature.admin.model;

import com.example.projectonlinecourseeducation.core.model.user.User;

/**
 * Model chứa thống kê về student (dành cho admin)
 */
public class StudentStats {
    private final User user;
    private final double totalSpent;       // Tổng tiền đã chi để mua khóa học
    private final int coursesPurchased;    // Số khóa học đã mua
    private final int cartItems;           // Số khóa học trong giỏ hàng

    public StudentStats(User user, double totalSpent, int coursesPurchased, int cartItems) {
        this.user = user;
        this.totalSpent = totalSpent;
        this.coursesPurchased = coursesPurchased;
        this.cartItems = cartItems;
    }

    public User getUser() {
        return user;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public int getCoursesPurchased() {
        return coursesPurchased;
    }

    public int getCartItems() {
        return cartItems;
    }
}