package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.core.model.CourseLesson;
import com.example.projectonlinecourseeducation.core.model.CourseReview;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.CourseApi;
import com.example.projectonlinecourseeducation.feature.student.adapter.HomeCourseAdapter;
import com.example.projectonlinecourseeducation.feature.student.adapter.ProductCourseReviewDetailedAdapter;
import com.example.projectonlinecourseeducation.feature.student.adapter.ProductLessonInfoAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentCourseDetailActivity extends AppCompatActivity {

    // số khóa học liên quan hiển thị mỗi lần
    private static final int RELATED_PAGE_SIZE = 4;

    private ImageView imgBanner;
    private TextView tvTitle, tvDescription, tvRatingValue, tvRatingCount,
            tvStudents, tvTeacher, tvCreatedAt, tvPrice, tvLectureSummary, tvRatingSummary;
    private RatingBar ratingBar;
    private Button btnAddToCart, btnBuyNow, btnMoreRelated;
    private LinearLayout layoutSkills, layoutRequirements;
    private RecyclerView rvLessons, rvRelatedCourses, rvReviews;

    // Dùng interface CourseApi, không phụ thuộc fake/real
    private CourseApi api;

    private ProductLessonInfoAdapter lessonAdapter;
    private HomeCourseAdapter relatedAdapter;
    private ProductCourseReviewDetailedAdapter reviewAdapter;

    // state cho khối khóa học liên quan
    private final List<Course> relatedAll = new ArrayList<>();
    private int relatedVisibleCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_course_detail);

        bindViews();
        setupRecyclerViews();

        // lấy implementation hiện tại từ ApiProvider (Fake hoặc Remote)
        api = ApiProvider.getCourseApi();

        String courseId = getIntent().getStringExtra("course_id");
        if (courseId == null) courseId = "c1";

        loadCourseDetail(courseId);
        setupActions();
    }

    private void bindViews() {
        imgBanner = findViewById(R.id.imgBanner);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvStudents = findViewById(R.id.tvStudents);
        tvTeacher = findViewById(R.id.tvTeacher);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvPrice = findViewById(R.id.tvPrice);
        ratingBar = findViewById(R.id.ratingBar);
        tvLectureSummary = findViewById(R.id.tvLectureSummary);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);

        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnMoreRelated = findViewById(R.id.btnMoreRelated);

        layoutSkills = findViewById(R.id.layoutSkills);
        layoutRequirements = findViewById(R.id.layoutRequirements);

        rvLessons = findViewById(R.id.rvLessons);
        rvRelatedCourses = findViewById(R.id.rvRelatedCourses);
        rvReviews = findViewById(R.id.rvReviews);
    }

    private void setupRecyclerViews() {
        // Nội dung khóa học
        lessonAdapter = new ProductLessonInfoAdapter();
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
        rvLessons.setNestedScrollingEnabled(false);

        // Khóa học liên quan (dọc)
        relatedAdapter = new HomeCourseAdapter();
        rvRelatedCourses.setLayoutManager(new LinearLayoutManager(this));
        rvRelatedCourses.setAdapter(relatedAdapter);
        rvRelatedCourses.setNestedScrollingEnabled(false);

        // Đánh giá học viên
        reviewAdapter = new ProductCourseReviewDetailedAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setNestedScrollingEnabled(false);
    }

    private void loadCourseDetail(String courseId) {
        // Fake local hoặc gọi API thật, tùy implementation trong ApiProvider
        Course course = api.getCourseDetail(courseId);
        if (course == null) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<CourseLesson> lessons = api.getLessonsForCourse(courseId);
        List<Course> related = api.getRelatedCourses(courseId);
        List<CourseReview> reviews = api.getReviewsForCourse(courseId);

        // --- Bind dữ liệu khóa học ---
        ImageLoader.getInstance().display(
                course.getImageUrl(),
                imgBanner,
                R.drawable.ic_image_placeholder
        );

        tvTitle.setText(course.getTitle());
        tvDescription.setText(course.getDescription());

        float rating = (float) course.getRating();
        ratingBar.setRating(rating);
        tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
        tvRatingCount.setText("(" + course.getRatingCount() + " đánh giá)");
        tvStudents.setText(course.getStudents() + " học viên");
        tvTeacher.setText("GV: " + course.getTeacher());
        tvCreatedAt.setText("Cập nhật: " + course.getCreatedAt());

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvPrice.setText(nf.format(course.getPrice()));

        // Summary bài giảng + thời lượng
        String time;
        if (course.getTotalDurationMinutes() >= 60) {
            int h = course.getTotalDurationMinutes() / 60;
            int m = course.getTotalDurationMinutes() % 60;
            time = h + " giờ " + (m > 0 ? m + " phút" : "");
        } else {
            time = course.getTotalDurationMinutes() + " phút";
        }
        tvLectureSummary.setText(course.getLectures() + " bài • " + time);

        tvRatingSummary.setText(
                String.format(Locale.US,
                        "%.1f / 5.0 • %d lượt đánh giá",
                        rating, course.getRatingCount())
        );

        // --- Skill / insight ---
        inflateChecklist(layoutSkills, course.getSkills());

        // --- Requirements ---
        inflateChecklist(layoutRequirements, course.getRequirements());

        // --- Nội dung khóa học ---
        lessonAdapter.submitList(lessons);

        // --- Khóa học liên quan ---
        relatedAll.clear();
        if (related != null) relatedAll.addAll(related);
        // mỗi lần load khóa học mới: reset số lượng hiển thị
        relatedVisibleCount = Math.min(RELATED_PAGE_SIZE, relatedAll.size());
        updateRelatedSection();

        // Click vào khóa học liên quan -> mở lại activity với id mới
        relatedAdapter.setOnCourseClickListener(c -> {
            Intent i = new Intent(this, StudentCourseDetailActivity.class);
            i.putExtra("course_id", c.getId());
            i.putExtra("course_title", c.getTitle());
            startActivity(i);
        });

        // --- Đánh giá ---
        reviewAdapter.submitList(reviews);
    }

    /**
     * Cập nhật list + text nút Xem thêm / Rút gọn cho khối khóa học liên quan
     * Hiển thị theo kiểu phân trang: 4 khóa / lần.
     */
    private void updateRelatedSection() {
        if (relatedAll.isEmpty()) {
            btnMoreRelated.setVisibility(View.GONE);
            relatedAdapter.submitList(new ArrayList<Course>());
            return;
        }

        int total = relatedAll.size();

        if (total <= RELATED_PAGE_SIZE) {
            // Ít hơn hoặc bằng page size: show hết, ẩn nút
            relatedVisibleCount = total;
            btnMoreRelated.setVisibility(View.GONE);
        } else {
            btnMoreRelated.setVisibility(View.VISIBLE);
            if (relatedVisibleCount >= total) {
                // đang hiển thị hết -> cho phép Rút gọn
                btnMoreRelated.setText("Rút gọn");
                btnMoreRelated.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.purple_400));
            } else {
                btnMoreRelated.setText("Xem thêm");
                btnMoreRelated.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.colorSecondary)
                );
            }
        }

        int end = Math.min(relatedVisibleCount, total);
        List<Course> display = new ArrayList<>(relatedAll.subList(0, end));
        relatedAdapter.submitList(display);
    }

    private void inflateChecklist(LinearLayout container, List<String> items) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (items == null) return;
        for (String s : items) {
            if (s == null || s.trim().length() == 0) continue;
            View v = inflater.inflate(
                    R.layout.item_student_product_course_checklist_row,
                    container,
                    false
            );
            TextView tv = v.findViewById(R.id.tvChecklistText);
            tv.setText(s);
            container.addView(v);
        }
    }

    private void setupActions() {
        btnAddToCart.setOnClickListener(v -> {
            Course currentCourse = api.getCourseDetail(getIntent().getStringExtra("course_id"));
            if (currentCourse != null) { // currentCourse là Course đang hiển thị
                StudentCartActivity.getInstance().addCourse(currentCourse);
                Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBuyNow.setOnClickListener(v -> {
            Toast.makeText(this, "Mua ngay (fake) – sau này chuyển sang màn thanh toán", Toast.LENGTH_SHORT).show();
        });

        // Toggle Xem thêm (+4) / Rút gọn (về 4)
        btnMoreRelated.setOnClickListener(v -> {
            int total = relatedAll.size();
            if (total <= RELATED_PAGE_SIZE) return;

            if (relatedVisibleCount >= total) {
                // đang show hết -> rút gọn về 4
                relatedVisibleCount = RELATED_PAGE_SIZE;
            } else {
                // show thêm 4
                relatedVisibleCount += RELATED_PAGE_SIZE;
                if (relatedVisibleCount > total) {
                    relatedVisibleCount = total;
                }
            }
            updateRelatedSection();
        });
    }
}
