package com.example.projectonlinecourseeducation.core.utils;

import com.example.projectonlinecourseeducation.core.model.course.CourseStatus;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;

/**
 * Async helper để xác định trạng thái của 1 khóa học
 * (PURCHASED / IN_CART / NOT_PURCHASED)
 *
 * IMPORTANT:
 * - BẮT BUỘC dùng async vì RemoteApiService gọi network
 * - KHÔNG trả về giá trị trực tiếp
 */
public class CourseStatusResolver {

    public interface StatusCallback {
        void onResult(CourseStatus status);
    }

    public interface BooleanCallback {
        void onResult(boolean value);
    }

    /**
     * Resolve course status ASYNC
     */
    public static void resolveStatus(
            String courseId,
            StatusCallback callback
    ) {
        if (courseId == null) {
            callback.onResult(CourseStatus.NOT_PURCHASED);
            return;
        }

        MyCourseApi myCourseApi = ApiProvider.getMyCourseApi();
        CartApi cartApi = ApiProvider.getCartApi();

        // Chạy async để tránh ANR
        AsyncApiHelper.execute(
                () -> {
                    // Ưu tiên PURCHASED
                    if (myCourseApi != null && myCourseApi.isPurchased(courseId)) {
                        return CourseStatus.PURCHASED;
                    }

                    if (cartApi != null && cartApi.isInCart(courseId)) {
                        return CourseStatus.IN_CART;
                    }

                    return CourseStatus.NOT_PURCHASED;
                },
                new AsyncApiHelper.ApiCallback<CourseStatus>() {
                    @Override
                    public void onSuccess(CourseStatus status) {
                        callback.onResult(status);
                    }

                    @Override
                    public void onError(Exception e) {
                        // fallback an toàn
                        callback.onResult(CourseStatus.NOT_PURCHASED);
                    }
                }
        );
    }

    // ===== Convenience methods =====

    public static void isPurchased(
            String courseId,
            BooleanCallback callback
    ) {
        resolveStatus(courseId, status ->
                callback.onResult(status == CourseStatus.PURCHASED)
        );
    }

    public static void isInCart(
            String courseId,
            BooleanCallback callback
    ) {
        resolveStatus(courseId, status ->
                callback.onResult(status == CourseStatus.IN_CART)
        );
    }
}
