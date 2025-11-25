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
import com.example.projectonlinecourseeducation.core.model.CourseReview;
import com.example.projectonlinecourseeducation.core.model.Lesson;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.review.ReviewApi;
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

    // Dùng interface
    private CourseApi courseApi;
    private LessonApi lessonApi;
    private ReviewApi reviewApi;
    private CartApi cartApi;

    private ProductLessonInfoAdapter lessonAdapter;
    private HomeCourseAdapter relatedAdapter;
    private ProductCourseReviewDetailedAdapter reviewAdapter;

    // state cho khối khóa học liên quan
    private final List<Course> relatedAll = new ArrayList<>();
    private int relatedVisibleCount = 0;

    // id khóa học hiện tại (dùng cho logic giỏ hàng)
    private String courseId;
    private Course currentCourse; // cache course hiện tại

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_course_detail);

        bindViews();
        setupRecyclerViews();

        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
        reviewApi = ApiProvider.getReviewApi();
        cartApi = ApiProvider.getCartApi();

        courseId = getIntent().getStringExtra("course_id");
        if (courseId == null) courseId = "c1";

        loadCourseDetail(courseId);
        setupActions();

        updateAddToCartButtonState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại từ màn Giỏ hàng
        updateAddToCartButtonState();
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

    private void loadCourseDetail(String id) {
        currentCourse = courseApi.getCourseDetail(id);
        if (currentCourse == null) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<Lesson> lessons = lessonApi.getLessonsForCourse(id);
        List<Course> related = courseApi.getRelatedCourses(id);
        List<CourseReview> reviews = reviewApi.getReviewsForCourse(id);

        // --- Bind dữ liệu khóa học ---
        ImageLoader.getInstance().display(
                currentCourse.getImageUrl(),
                imgBanner,
                R.drawable.ic_image_placeholder
        );

        tvTitle.setText(currentCourse.getTitle());
        tvDescription.setText(currentCourse.getDescription());

        float rating = (float) currentCourse.getRating();
        ratingBar.setRating(rating);
        tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
        tvRatingCount.setText("(" + currentCourse.getRatingCount() + " đánh giá)");
        tvStudents.setText(currentCourse.getStudents() + " học viên");
        tvTeacher.setText("GV: " + currentCourse.getTeacher());
        tvCreatedAt.setText("Cập nhật: " + currentCourse.getCreatedAt());

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvPrice.setText(nf.format(currentCourse.getPrice()));

        // Summary bài giảng + thời lượng
        String time;
        if (currentCourse.getTotalDurationMinutes() >= 60) {
            int h = currentCourse.getTotalDurationMinutes() / 60;
            int m = currentCourse.getTotalDurationMinutes() % 60;
            time = h + " giờ " + (m > 0 ? m + " phút" : "");
        } else {
            time = currentCourse.getTotalDurationMinutes() + " phút";
        }
        tvLectureSummary.setText(currentCourse.getLectures() + " bài • " + time);

        tvRatingSummary.setText(
                String.format(Locale.US,
                        "%.1f / 5.0 • %d lượt đánh giá",
                        rating, currentCourse.getRatingCount())
        );

        // --- Skill / insight ---
        inflateChecklist(layoutSkills, currentCourse.getSkills());

        // --- Requirements ---
        inflateChecklist(layoutRequirements, currentCourse.getRequirements());

        // --- Nội dung khóa học ---
        lessonAdapter.submitList(lessons);

        // --- Khóa học liên quan ---
        relatedAll.clear();
        if (related != null) relatedAll.addAll(related);
        relatedVisibleCount = Math.min(RELATED_PAGE_SIZE, relatedAll.size());
        updateRelatedSection();

        relatedAdapter.setOnCourseClickListener(c -> {
            Intent i = new Intent(this, StudentCourseDetailActivity.class);
            i.putExtra("course_id", c.getId());
            i.putExtra("course_title", c.getTitle());
            startActivity(i);
        });

        // --- Đánh giá ---
        reviewAdapter.submitList(reviews);
    }

    private void updateRelatedSection() {
        if (relatedAll.isEmpty()) {
            btnMoreRelated.setVisibility(View.GONE);
            relatedAdapter.submitList(new ArrayList<Course>());
            return;
        }

        int total = relatedAll.size();

        if (total <= RELATED_PAGE_SIZE) {
            relatedVisibleCount = total;
            btnMoreRelated.setVisibility(View.GONE);
        } else {
            btnMoreRelated.setVisibility(View.VISIBLE);
            if (relatedVisibleCount >= total) {
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

    // ========= GIỎ HÀNG =========

    private boolean isInCart(String cid) {
        return cartApi.isInCart(cid);
    }

    private void updateAddToCartButtonState() {
        boolean inCart = isInCart(courseId);
        if (inCart) {
            btnAddToCart.setText("Đi tới giỏ hàng");
            btnAddToCart.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.blue_900)
            );
        } else {
            btnAddToCart.setText("Thêm vào giỏ hàng");
            btnAddToCart.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.purple_600)
            );
        }
    }

    private void setupActions() {
        btnAddToCart.setOnClickListener(v -> {
            boolean inCart = isInCart(courseId);
            if (!inCart) {
                // Thêm vào giỏ hàng qua CartApi
                if (currentCourse != null) {
                    cartApi.addToCart(currentCourse);
                    updateAddToCartButtonState();
                }
            } else {
                // Đã ở trong giỏ -> chuyển sang màn Home + mở tab Giỏ hàng
                Intent intent = new Intent(this, StudentHomeActivity.class);
                intent.putExtra("open_cart", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnBuyNow.setOnClickListener(v ->
                Toast.makeText(this,
                        "Mua ngay (fake) – sau này chuyển sang màn thanh toán",
                        Toast.LENGTH_SHORT).show()
        );

        btnMoreRelated.setOnClickListener(v -> {
            int total = relatedAll.size();
            if (total <= RELATED_PAGE_SIZE) return;

            if (relatedVisibleCount >= total) {
                relatedVisibleCount = RELATED_PAGE_SIZE;
            } else {
                relatedVisibleCount += RELATED_PAGE_SIZE;
                if (relatedVisibleCount > total) {
                    relatedVisibleCount = total;
                }
            }
            updateRelatedSection();
        });
    }
}
