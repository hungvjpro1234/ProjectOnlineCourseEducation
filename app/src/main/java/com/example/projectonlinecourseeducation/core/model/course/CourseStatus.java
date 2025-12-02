package com.example.projectonlinecourseeducation.core.model.course;

/**
 * Trạng thái của 1 khóa học đối với STUDENT hiện tại.
 * NOT_PURCHASED  : chưa mua (có thể đang hoặc không ở trong giỏ hàng)
 * IN_CART        : đang nằm trong giỏ hàng
 * PURCHASED      : đã mua, hiển thị ở My Course, được quyền "Học ngay"
 */
public enum CourseStatus {
    NOT_PURCHASED,
    IN_CART,
    PURCHASED
}
