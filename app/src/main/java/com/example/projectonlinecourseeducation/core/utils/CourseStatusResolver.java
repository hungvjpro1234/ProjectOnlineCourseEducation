package com.example.projectonlinecourseeducation.core.utils;

import com.example.projectonlinecourseeducation.core.model.CourseStatus;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;

/**
 * Helper để xác định trạng thái của 1 khóa học đối với student hiện tại.
 * Trạng thái được suy ra từ MyCourseApi + CartApi,
 * không phải field cứng trong model Course.
 */
public class CourseStatusResolver {

    /**
     * Suy ra trạng thái từ MyCourseApi + CartApi.
     */
    public static CourseStatus getStatus(String courseId) {
        if (courseId == null) return CourseStatus.NOT_PURCHASED;

        MyCourseApi myCourseApi = ApiProvider.getMyCourseApi();
        if (myCourseApi != null && myCourseApi.isPurchased(courseId)) {
            return CourseStatus.PURCHASED;
        }

        CartApi cartApi = ApiProvider.getCartApi();
        if (cartApi != null && cartApi.isInCart(courseId)) {
            return CourseStatus.IN_CART;
        }

        return CourseStatus.NOT_PURCHASED;
    }

    public static boolean isPurchased(String courseId) {
        return getStatus(courseId) == CourseStatus.PURCHASED;
    }

    public static boolean isInCart(String courseId) {
        return getStatus(courseId) == CourseStatus.IN_CART;
    }

    public static boolean isNotPurchased(String courseId) {
        return getStatus(courseId) == CourseStatus.NOT_PURCHASED;
    }
}