package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị courses đang chờ phê duyệt
 * Hỗ trợ 3 loại: INITIAL, EDIT, DELETE
 */
public class AdminPendingCourseAdapter extends RecyclerView.Adapter<AdminPendingCourseAdapter.PendingCourseViewHolder> {

    private List<Course> courses = new ArrayList<>();
    private ApprovalType currentType = ApprovalType.INITIAL;

    private final OnApproveListener onApprove;
    private final OnRejectListener onReject;
    private final OnViewChangesListener onViewChanges;

    public enum ApprovalType {
        INITIAL,  // Khóa học mới
        EDIT,     // Chỉnh sửa
        DELETE    // Xóa
    }

    public interface OnApproveListener {
        void onApprove(Course course);
    }

    public interface OnRejectListener {
        void onReject(Course course);
    }

    public interface OnViewChangesListener {
        void onViewChanges(Course course);
    }

    public AdminPendingCourseAdapter(OnApproveListener onApprove,
                                     OnRejectListener onReject,
                                     OnViewChangesListener onViewChanges) {
        this.onApprove = onApprove;
        this.onReject = onReject;
        this.onViewChanges = onViewChanges;
    }

    public void setCourses(List<Course> courses) {
        this.courses.clear();
        if (courses != null) {
            this.courses.addAll(courses);
        }
        notifyDataSetChanged();
    }

    public void setType(ApprovalType type) {
        this.currentType = type;
    }

    @NonNull
    @Override
    public PendingCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pending_course, parent, false);
        return new PendingCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingCourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, currentType);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public class PendingCourseViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardCourse;
        private final TextView tvStatusBadge;
        private final ImageView imgCourse;
        private final TextView tvTitle;
        private final TextView tvTeacher;
        private final TextView tvCategory;
        private final TextView tvPrice;
        private final TextView tvLectures;
        private final TextView tvCreatedAt;
        private final Button btnApprove;
        private final Button btnReject;
        private final Button btnViewChanges;

        public PendingCourseViewHolder(@NonNull View itemView) {
            super(itemView);

            cardCourse = itemView.findViewById(R.id.cardCourse);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            imgCourse = itemView.findViewById(R.id.imgCourse);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLectures = itemView.findViewById(R.id.tvLectures);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnViewChanges = itemView.findViewById(R.id.btnViewChanges);
        }

        public void bind(Course course, ApprovalType type) {
            if (course == null) return;

            // Load image
            ImageLoader.getInstance().display(
                    course.getImageUrl(),
                    imgCourse,
                    R.drawable.ic_image_placeholder
            );

            // Basic info
            tvTitle.setText(safe(course.getTitle()));
            tvTeacher.setText("👨‍🏫 " + safe(course.getTeacher()));
            tvCategory.setText(safe(course.getCategory()));

            // Price
            try {
                DecimalFormat df = new DecimalFormat("#,###");
                tvPrice.setText(df.format((long) course.getPrice()) + " VNĐ");
            } catch (Exception e) {
                tvPrice.setText(String.format("%.0f VNĐ", course.getPrice()));
            }

            // Lectures
            tvLectures.setText("📚 " + course.getLectures() + " bài học");

            // Created date
            tvCreatedAt.setText("📅 " + safe(course.getCreatedAt()));

            // Status badge & UI based on type
            switch (type) {
                case INITIAL:
                    tvStatusBadge.setText("🆕 KHÓA HỌC MỚI");
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_new);
                    btnViewChanges.setVisibility(View.GONE);
                    btnApprove.setText("✓ Phê duyệt");
                    btnReject.setText("✗ Từ chối");
                    break;

                case EDIT:
                    tvStatusBadge.setText("✏️ CHỈNH SỬA");
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_edit);
                    btnViewChanges.setVisibility(View.VISIBLE);
                    btnApprove.setText("✓ Duyệt sửa");
                    btnReject.setText("✗ Từ chối");
                    break;

                case DELETE:
                    tvStatusBadge.setText("🗑️ YÊU CẦU XÓA");
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_delete);
                    btnViewChanges.setVisibility(View.GONE);
                    btnApprove.setText("✓ Xóa vĩnh viễn");
                    btnReject.setText("✗ Giữ lại");
                    break;
            }

            // Button listeners
            btnApprove.setOnClickListener(v -> {
                if (onApprove != null) {
                    onApprove.onApprove(course);
                }
            });

            btnReject.setOnClickListener(v -> {
                if (onReject != null) {
                    onReject.onReject(course);
                }
            });

            btnViewChanges.setOnClickListener(v -> {
                if (onViewChanges != null) {
                    onViewChanges.onViewChanges(course);
                }
            });
        }

        private String safe(String s) {
            return s == null || s.isEmpty() ? "-" : s;
        }
    }
}