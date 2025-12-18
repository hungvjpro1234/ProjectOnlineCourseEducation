package com.example.projectonlinecourseeducation.core.utils.datahelper.apihelper;

import com.example.projectonlinecourseeducation.core.model.course.CourseReview;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.coursereview.ReviewApi;
import com.example.projectonlinecourseeducation.data.coursereview.remote.AddReviewRequest;
import com.example.projectonlinecourseeducation.data.coursereview.remote.CourseReviewDto;
import com.example.projectonlinecourseeducation.data.coursereview.remote.CourseReviewsResponse;
import com.example.projectonlinecourseeducation.data.coursereview.remote.ReviewRetrofitService;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class ReviewService implements ReviewApi {

    private final ReviewRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(ReviewRetrofitService.class);

    // ================= Mapping =================

    private CourseReview map(CourseReviewDto d) {
        if (d == null) return null;
        return new CourseReview(
                d.id,
                d.courseId,
                d.userName,
                d.rating,
                d.comment,
                d.createdAt
        );
    }

    // ================= API =================

    @Override
    public List<CourseReview> getReviewsForCourse(String courseId) {
        if (courseId == null) return new ArrayList<>();
        try {
            Response<CourseReviewsResponse> res =
                    api.getReviewsForCourse(courseId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                List<CourseReview> result = new ArrayList<>();
                for (CourseReviewDto d : res.body().data) {
                    CourseReview r = map(d);
                    if (r != null) result.add(r);
                }
                return result;
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @Override
    public CourseReview addReviewToCourse(String courseId,
                                          String studentName,
                                          float rating,
                                          String comment) {
        if (courseId == null) return null;

        try {
            AddReviewRequest req = new AddReviewRequest();
            req.studentName = studentName;
            req.rating = rating;
            req.comment = comment;

            Response<CourseReviewDto> res =
                    api.addReview(courseId, req).execute();

            if (res.isSuccessful() && res.body() != null) {
                CourseReview review = map(res.body());

                // giống Fake: cập nhật lại rating course
                try {
                    if (ApiProvider.getCourseApi() != null) {
                        ApiProvider.getCourseApi()
                                .recalculateCourseRating(courseId);
                    }
                } catch (Exception ignored) {}

                return review;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ================= LISTENER (NO-OP) =================

    @Override
    public void addReviewUpdateListener(ReviewUpdateListener listener) {
        // Remote không realtime → NO-OP
    }

    @Override
    public void removeReviewUpdateListener(ReviewUpdateListener listener) {
        // NO-OP
    }

    // ================= OPTIONAL =================

    public boolean removeReview(String courseId, String reviewId) {
        if (courseId == null || reviewId == null) return false;
        try {
            api.deleteReview(courseId, reviewId).execute();
            return true;
        } catch (Exception ignored) {}
        return false;
    }
}
