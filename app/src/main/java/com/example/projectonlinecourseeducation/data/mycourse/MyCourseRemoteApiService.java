package com.example.projectonlinecourseeducation.data.mycourse;

import android.util.Log;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Remote implementation cho MyCourseApi
 * Gọi backend thật qua Retrofit
 *
 * LƯU Ý:
 * - Tất cả method là synchronous
 * - PHẢI được gọi bên trong AsyncApiHelper
 *
 * CACHE STRATEGY:
 * - Có local cache purchasedCourseIds để tăng performance
 * - Cache được sync khi gọi getMyCourses()
 * - isPurchased() check cache trước, fallback backend nếu cache chưa init
 */
public class MyCourseRemoteApiService implements MyCourseApi {

    private static final String TAG = "MyCourseRemoteApi";

    private final MyCourseRetrofitService retrofitService;

    // ===== LOCAL CACHE (for performance) =====
    private final Set<String> purchasedCourseIds = new HashSet<>();
    private boolean cacheInitialized = false;

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

            // ✅ SYNC CACHE
            purchasedCourseIds.clear();

            for (CourseDto dto : body.getData()) {
                Course course = CourseMapper.toCourse(dto);
                if (course != null && course.getId() != null) {
                    result.add(course);
                    purchasedCourseIds.add(course.getId()); // Update cache
                }
            }

            cacheInitialized = true; // Mark cache as ready
            Log.d(TAG, "getMyCourses: synced " + purchasedCourseIds.size() + " courses to cache");

            return result;

        } catch (Exception e) {
            Log.e(TAG, "getMyCourses error", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isPurchased(String courseId) {
        if (courseId == null) return false;

        // ✅ CHECK CACHE ONLY
        // Cache được preload trong StudentHomeActivity.onCreate()
        // KHÔNG GỌI backend fallback để tránh Binder transaction overflow
        if (!cacheInitialized) {
            Log.d(TAG, "isPurchased(" + courseId + "): cache not ready yet, returning false (will be updated after preload)");
            return false;
        }

        boolean inCache = purchasedCourseIds.contains(courseId);
        Log.d(TAG, "isPurchased(" + courseId + "): cache result = " + inCache);
        return inCache;
    }

    /**
     * ✅ FIX: Chỉ update local cache
     * Backend đã tự động thêm vào MyCourse qua /cart/checkout
     * Method này CHỈ để sync cache sau khi purchase thành công
     */
    @Override
    public void addPurchasedCourse(Course course) {
        if (course == null || course.getId() == null) return;

        // ✅ UPDATE CACHE ONLY
        purchasedCourseIds.add(course.getId());
        cacheInitialized = true; // Ensure cache is marked as ready

        Log.d(TAG, "addPurchasedCourse: added " + course.getId() + " to cache");
    }

    /**
     * ✅ FIX: Chỉ update local cache cho multiple courses
     */
    @Override
    public void addPurchasedCourses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return;

        // ✅ UPDATE CACHE ONLY
        for (Course course : courses) {
            if (course != null && course.getId() != null) {
                purchasedCourseIds.add(course.getId());
            }
        }
        cacheInitialized = true;

        Log.d(TAG, "addPurchasedCourses: added " + courses.size() + " courses to cache");
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
