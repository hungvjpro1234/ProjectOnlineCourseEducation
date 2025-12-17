package com.example.projectonlinecourseeducation.data.mycourse;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.mycourse.remote.MyCourseRetrofitService;
import com.example.projectonlinecourseeducation.data.mycourse.remote.MyCourseDto;
import com.example.projectonlinecourseeducation.data.mycourse.remote.AddMyCourseRequest;
import com.example.projectonlinecourseeducation.data.mycourse.remote.MyCoursesResponse;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Remote implementation of MyCourseApi
 *
 * - Call backend bằng Retrofit (sync)
 * - UI phải wrap bằng AsyncApiHelper
 * - Không có realtime/listener
 */
public class MyCourseRemoteApiService implements MyCourseApi {

    private final MyCourseRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(MyCourseRetrofitService.class);

    // ------------------ Mapping helpers ------------------

    private Course map(MyCourseDto d) {
        if (d == null) return null;

        Course c = new Course();
        c.setId(d.id);
        c.setTitle(d.title);
        c.setDescription(d.description);
        c.setImageUrl(d.imageUrl); // ✅ ĐÚNG – KHÔNG ĐỔI
        c.setPrice(d.price);

        // ✅ FIX approval mapping (nhẹ – an toàn)
        c.setInitialApproved(d.isApproved);
        c.setEditApproved(true);
        c.setDeleteRequested(false);

        return c;
    }


    private List<Course> mapList(List<MyCourseDto> list) {
        List<Course> result = new ArrayList<>();
        if (list == null) return result;

        for (MyCourseDto d : list) {
            Course c = map(d);
            if (c != null) result.add(c);
        }
        return result;
    }

    // ------------------ API: Student ------------------

    @Override
    public List<Course> getMyCourses() {
        try {
            Response<MyCoursesResponse> res = api.getMyCourses().execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return mapList(res.body().data);
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @Override
    public boolean isPurchased(String courseId) {
        if (courseId == null) return false;

        List<Course> myCourses = getMyCourses();
        for (Course c : myCourses) {
            if (c != null && courseId.equals(c.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addPurchasedCourse(Course course) {
        if (course == null || course.getId() == null) return;

        try {
            AddMyCourseRequest req = new AddMyCourseRequest();
            req.courseId = course.getId();
            api.addPurchasedCourse(req).execute();
        } catch (Exception ignored) {}
    }

    @Override
    public void addPurchasedCourses(List<Course> courses) {
        if (courses == null) return;
        for (Course c : courses) {
            addPurchasedCourse(c);
        }
    }

    @Override
    public void clearMyCourses() {
        // Backend thường KHÔNG hỗ trợ clear toàn bộ
        // Method này chỉ có ý nghĩa trong FakeApi
        // → Remote: NO-OP
    }

    // ------------------ API: ADMIN ------------------

    @Override
    public List<Course> getMyCoursesForUser(String userId) {
        if (userId == null) return new ArrayList<>();

        try {
            Response<MyCoursesResponse> res =
                    api.getMyCoursesForUser(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return mapList(res.body().data);
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @Override
    public boolean isPurchasedForUser(String courseId, String userId) {
        if (courseId == null || userId == null) return false;

        List<Course> courses = getMyCoursesForUser(userId);
        for (Course c : courses) {
            if (c != null && courseId.equals(c.getId())) {
                return true;
            }
        }
        return false;
    }
}
