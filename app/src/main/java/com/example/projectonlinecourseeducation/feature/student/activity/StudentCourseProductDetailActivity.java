package com.example.projectonlinecourseeducation.feature.student.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.course.CourseReview;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;
import com.example.projectonlinecourseeducation.core.model.course.CourseStatus;
import com.example.projectonlinecourseeducation.core.utils.CourseStatusResolver;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.lesson.LessonApi;
import com.example.projectonlinecourseeducation.data.review.ReviewApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.feature.student.adapter.HomeCourseAdapter;
import com.example.projectonlinecourseeducation.feature.student.adapter.ProductCourseReviewDetailedAdapter;
import com.example.projectonlinecourseeducation.feature.student.adapter.ProductCourseLessonInfoAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentCourseProductDetailActivity extends AppCompatActivity {

    // số khóa học liên quan hiển thị mỗi lần
    private static final int RELATED_PAGE_SIZE = 4;

    private NestedScrollView scrollView;

    private ImageView imgBanner;
    private ImageButton btnBack; // nút quay lại trên banner
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
    private MyCourseApi myCourseApi; // My Course API

    private ProductCourseLessonInfoAdapter lessonAdapter;
    private HomeCourseAdapter relatedAdapter;
    private ProductCourseReviewDetailedAdapter reviewAdapter;

    // state cho khối khóa học liên quan
    private final List<Course> relatedAll = new ArrayList<>();
    private int relatedVisibleCount = 0;

    // id khóa học hiện tại (dùng cho logic giỏ hàng + MyCourse)
    private String courseId;
    private Course currentCourse; // cache course hiện tại

    // trạng thái hiện tại của khóa học đối với student
    private CourseStatus currentStatus = CourseStatus.NOT_PURCHASED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_course_product_detail);

        bindViews();
        setupRecyclerViews();

        courseApi = ApiProvider.getCourseApi();
        lessonApi = ApiProvider.getLessonApi();
        reviewApi = ApiProvider.getReviewApi();
        cartApi = ApiProvider.getCartApi();
        myCourseApi = ApiProvider.getMyCourseApi();

        courseId = getIntent().getStringExtra("course_id");
        if (courseId == null) courseId = "c1";

        // Register course update listener so this detail page updates automatically
        try {
            courseApi.addCourseUpdateListener(courseUpdateListener);
        } catch (Throwable ignored) {}

        loadCourseDetail(courseId);
        setupActions();

        // cập nhật state ban đầu cho nút giỏ hàng / mua ngay / học ngay
        updatePurchaseUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại từ màn Giỏ hàng hoặc My Course
        updatePurchaseUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (courseApi != null) courseApi.removeCourseUpdateListener(courseUpdateListener);
        } catch (Throwable ignored) {}
    }

    private void bindViews() {
        scrollView = findViewById(R.id.scrollView);

        imgBanner = findViewById(R.id.imgBanner);
        btnBack = findViewById(R.id.btnBack);
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
        lessonAdapter = new ProductCourseLessonInfoAdapter();
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
            Intent i = new Intent(this, StudentCourseProductDetailActivity.class);
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

    // ========= GIỎ HÀNG + TRẠNG THÁI MUA HÀNG =========

    private boolean isInCart(String cid) {
        return cartApi != null && cartApi.isInCart(cid);
    }

    /**
     * Cập nhật UI của nút "Thêm vào giỏ hàng" dựa trên tình trạng giỏ.
     * (Chỉ gọi khi khóa học chưa ở trạng thái PURCHASED)
     */
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
                    ContextCompat.getColorStateList(this, R.color.purple_200)
            );
        }
    }

    /**
     * Cập nhật UI dựa trên trạng thái khóa học:
     * - NOT_PURCHASED: hiện đủ "Thêm vào giỏ" + "Mua ngay" + giá
     * - IN_CART      : nút "Thêm vào giỏ" -> "Đi tới giỏ hàng" + giá
     * - PURCHASED    : ẩn "Thêm vào giỏ", "Mua ngay" -> "Học ngay" + ẩn giá
     */
    private void updatePurchaseUi() {
        currentStatus = CourseStatusResolver.getStatus(courseId);

        if (currentStatus == CourseStatus.PURCHASED) {
            // Ẩn nút giỏ hàng, chỉ còn "Học ngay" + ẩn giá
            btnAddToCart.setVisibility(View.GONE);
            btnBuyNow.setText("Học ngay");
            btnBuyNow.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.purple_600)
            );
            tvPrice.setVisibility(View.GONE);
        } else {
            // Chưa mua: hiện đầy đủ 2 nút + giá
            btnAddToCart.setVisibility(View.VISIBLE);
            btnBuyNow.setText("Mua ngay");
            btnBuyNow.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.colorAccent)
            );
            tvPrice.setVisibility(View.VISIBLE);
            updateAddToCartButtonState();
        }
    }

    private void setupActions() {
        // Nút quay lại trên banner
        btnBack.setOnClickListener(v -> finish());

        btnAddToCart.setOnClickListener(v -> {
            // Nếu đã mua thì không cho thao tác giỏ nữa
            if (currentStatus == CourseStatus.PURCHASED) {
                Toast.makeText(this,
                        "Bạn đã sở hữu khóa học này",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inCart = isInCart(courseId);
            if (!inCart) {
                // Thêm vào giỏ hàng qua CartApi
                if (currentCourse != null) {
                    cartApi.addToCart(currentCourse);
                    updatePurchaseUi();

                    // 👉 Toast thông báo đã thêm vào giỏ hàng
                    Toast.makeText(
                            this,
                            "Đã thêm khóa học vào giỏ hàng",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            this,
                            "Không thể thêm vào giỏ hàng, dữ liệu khóa học bị lỗi",
                            Toast.LENGTH_SHORT
                    ).show();
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

        // Nút "Mua ngay" / "Học ngay"
        btnBuyNow.setOnClickListener(v -> {
            if (currentCourse == null) {
                Toast.makeText(this,
                        "Không tìm thấy dữ liệu khóa học để thanh toán",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentStatus == CourseStatus.PURCHASED) {
                // ✅ ĐÃ MUA -> chuyển sang màn lesson
                Intent i = new Intent(this, StudentCoursePurchasedActivity.class);
                i.putExtra("course_id", currentCourse.getId());
                i.putExtra("course_title", currentCourse.getTitle());
                startActivity(i);
                return;
            }

            // 👉 Thêm hiển thị giá vào nội dung confirm
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String priceText = nf.format(currentCourse.getPrice());
            String message = "Bạn có chắc muốn thanh toán khóa học \"" + currentCourse.getTitle() + "\"?\n"
                    + "Giá: " + priceText;

            showPaymentConfirmDialog(
                    message,
                    () -> showPaymentSuccessDialog(
                            "Thanh toán thành công",
                            true,
                            () -> {
                                // SAFE ORDER: thêm vào MyCourse trước, sau đó gọi recordPurchase để backend/fake tăng students
                                if (myCourseApi != null) {
                                    myCourseApi.addPurchasedCourse(currentCourse);
                                }
                                if (cartApi != null) {
                                    cartApi.removeFromCart(courseId);
                                }
                                // call backend/fake to record purchase (this will notify listeners)
                                if (courseApi != null) {
                                    courseApi.recordPurchase(courseId);
                                }
                                // update UI and navigate to MyCourse
                                updatePurchaseUi();
                                Intent intent = new Intent(this, StudentHomeActivity.class);
                                intent.putExtra("open_my_course", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                    )
            );
        });


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

    /**
     * Hiển thị dialog xác nhận thanh toán.
     *
     * @param message    Nội dung confirm
     * @param onConfirmed callback chạy khi user bấm "Xác nhận"
     */
    private void showPaymentConfirmDialog(String message, Runnable onConfirmed) {
        DialogConfirmHelper.showConfirmDialog(
                this,
                "Xác nhận thanh toán",
                message,
                R.drawable.question,
                "Xác nhận",
                "Hủy",
                R.color.blue_600, // 💜 màu gốc cho nút xác nhận
                () -> { if (onConfirmed != null) onConfirmed.run(); }
        );
    }

    /**
     * Dialog thông báo thanh toán thành công.
     *
     * @param message  Nội dung hiển thị
     * @param showToast Có hiển thị thêm Toast nữa không
     */
    private void showPaymentSuccessDialog(String message, boolean showToast) {
        showPaymentSuccessDialog(message, showToast, null);
    }

    /**
     * Dialog thông báo thanh toán thành công + callback sau khi đóng dialog.
     *
     * @param message        Nội dung hiển thị
     * @param showToast      Có hiển thị thêm Toast nữa không
     * @param afterDismissed Callback chạy sau khi user bấm "Đóng"
     */
    private void showPaymentSuccessDialog(String message, boolean showToast, @Nullable Runnable afterDismissed) {
        DialogConfirmHelper.showSuccessDialog(
                this,
                "Thanh toán thành công",
                message,
                R.drawable.confirm,
                "Đóng",
                () -> {
                    if (showToast) {
                        Toast.makeText(this,
                                "Thanh toán thành công",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (afterDismissed != null) {
                        afterDismissed.run();
                    }
                }
        );
    }

    // CourseUpdateListener: update UI when course changes
    private final CourseApi.CourseUpdateListener courseUpdateListener = new CourseApi.CourseUpdateListener() {
        @Override
        public void onCourseUpdated(String id, Course updatedCourse) {
            if (id == null || !id.equals(courseId)) return;
            if (updatedCourse == null) return; // deleted case could finish activity
            runOnUiThread(() -> {
                currentCourse = updatedCourse;
                // update visible fields only (students, lectures, duration, rating, price if changed)
                tvStudents.setText(currentCourse.getStudents() + " học viên");

                // update lecture summary
                String time;
                if (currentCourse.getTotalDurationMinutes() >= 60) {
                    int h = currentCourse.getTotalDurationMinutes() / 60;
                    int m = currentCourse.getTotalDurationMinutes() % 60;
                    time = h + " giờ " + (m > 0 ? m + " phút" : "");
                } else {
                    time = currentCourse.getTotalDurationMinutes() + " phút";
                }
                tvLectureSummary.setText(currentCourse.getLectures() + " bài • " + time);

                // rating
                float rating = (float) currentCourse.getRating();
                ratingBar.setRating(rating);
                tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
                tvRatingCount.setText("(" + currentCourse.getRatingCount() + " đánh giá)");

                // price / purchase state might change
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvPrice.setText(nf.format(currentCourse.getPrice()));
                updatePurchaseUi();
            });
        }
    };
}
