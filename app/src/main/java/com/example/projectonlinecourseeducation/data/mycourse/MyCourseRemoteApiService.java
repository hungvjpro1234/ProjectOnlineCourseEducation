package com.example.projectonlinecourseeducation.data.mycourse;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.mycourse.remote.CourseDto;
import com.example.projectonlinecourseeducation.data.mycourse.remote.CourseMapper;
import com.example.projectonlinecourseeducation.data.mycourse.remote.MyCourseApiResponse;
import com.example.projectonlinecourseeducation.data.mycourse.remote.MyCourseRetrofitService;
import com.example.projectonlinecourseeducation.data.mycourse.remote.MyCourseStatusResponse;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;
import com.example.projectonlinecourseeducation.data.cart.remote.CheckoutRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Remote implementation cho MyCourseApi
 * Gọi backend thật qua Retrofit
 *
 * LƯU Ý:
 * - Tất cả method là synchronous
 * - PHẢI được gọi bên trong AsyncApiHelper
 */
public class MyCourseRemoteApiService implements MyCourseApi {

    private final MyCourseRetrofitService retrofitService;

    public MyCourseRemoteApiService() {
        this.retrofitService =
                RetrofitClient.getInstance().getMyCourseRetrofitService();
    }

    // =========================
    // USER APIs
    // =========================

    @Override
    public List<Course> getMyCourses() {
        try {
            Call<MyCourseApiResponse> call = retrofitService.getMyCourses();
            Response<MyCourseApiResponse> response = call.execute();

            if (!response.isSuccessful() || response.body() == null) {
                return Collections.emptyList();
            }

            MyCourseApiResponse body = response.body();
            if (!body.isSuccess() || body.getData() == null) {
                return Collections.emptyList();
            }

            List<Course> result = new ArrayList<>();
            for (CourseDto dto : body.getData()) {
                Course course = CourseMapper.toCourse(dto);
                if (course != null) {
                    result.add(course);
                }
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isPurchased(String courseId) {
        if (courseId == null) return false;

        try {
            int cid = Integer.parseInt(courseId);
            Call<MyCourseStatusResponse> call =
                    retrofitService.isPurchased(cid);

            Response<MyCourseStatusResponse> response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                return false;
            }

            MyCourseStatusResponse body = response.body();
            return body.isSuccess() && body.isPurchased();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ⚠️ Không gọi trực tiếp backend
     * Việc add course vào my-courses xảy ra thông qua /cart/checkout
     */
    @Override
    public void addPurchasedCourse(Course course) {
        if (course == null || course.getId() == null) return;
        addPurchasedCourses(Collections.singletonList(course));
    }

    /**
     * Thực hiện checkout (POST /cart/checkout)
     * → backend tự insert course_student + PURCHASED
     */
    @Override
    public void addPurchasedCourses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return;

        User currentUser = ApiProvider.getAuthApi().getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) return;

        try {
            List<Integer> courseIds = new ArrayList<>();
            for (Course c : courses) {
                if (c != null && c.getId() != null) {
                    courseIds.add(Integer.parseInt(c.getId()));
                }
            }

            if (courseIds.isEmpty()) return;

            CheckoutRequest request = new CheckoutRequest(
                    Integer.parseInt(currentUser.getId()),
                    courseIds
            );

            retrofitService.checkout(request).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Backend KHÔNG có endpoint clear
     * Giữ method để tương thích interface
     */
    @Override
    public void clearMyCourses() {
        // NO-OP
    }

    // =========================
    // ADMIN APIs
    // =========================
    @Override
    public List<Course> getMyCoursesForUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Response<MyCourseApiResponse> response =
                    retrofitService.getMyCoursesForUser(userId).execute();

            if (!response.isSuccessful()) {
                return Collections.emptyList();
            }

            MyCourseApiResponse body = response.body();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                return Collections.emptyList();
            }

            List<Course> result = new ArrayList<>(body.getData().size());
            for (CourseDto dto : body.getData()) {
                Course course = CourseMapper.toCourse(dto);
                if (course != null && course.getId() != null) {
                    result.add(course);
                }
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isPurchasedForUser(String courseId, String userId) {
        if (courseId == null || userId == null) return false;

        // Backend chưa có endpoint riêng cho ADMIN check status
        // → dùng cách an toàn: load my-courses của user rồi check local
        List<Course> courses = getMyCoursesForUser(userId);
        for (Course c : courses) {
            if (c != null && courseId.equals(c.getId())) {
                return true;
            }
        }
        return false;
    }
}
