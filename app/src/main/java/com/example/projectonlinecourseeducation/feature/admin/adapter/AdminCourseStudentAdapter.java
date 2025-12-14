package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Student Adapter - UI card đẹp hơn với gradient progress và icon
 *
 * Sửa các lỗi import (dấu ":" -> ".") và đảm bảo kiểu dữ liệu/khởi tạo adapter hợp lệ.
 */
public class AdminCourseStudentAdapter extends RecyclerView.Adapter<AdminCourseStudentAdapter.StudentViewHolder> {

    private List<StudentProgressItem> students = new ArrayList<>();
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(StudentProgressItem student);
    }

    public AdminCourseStudentAdapter(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public void setStudents(List<StudentProgressItem> students) {
        this.students = students != null ? students : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_course_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        if (position < 0 || position >= students.size()) return;
        StudentProgressItem student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardStudent;
        private final LinearLayout layoutHeader;
        private final LinearLayout layoutDetail;
        private final ImageView imgExpand;
        private final ImageView imgAvatar;
        private final TextView tvName;
        private final TextView tvEnrolledTime;
        private final TextView tvProgress;
        private final ProgressBar pbProgress;
        private final TextView tvCompleted;
        private final RecyclerView rvLessonProgress;
        private final AdminLessonProgressDetailAdapter detailAdapter;
        private boolean isExpanded = false;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            cardStudent = itemView.findViewById(R.id.cardStudent);
            layoutHeader = itemView.findViewById(R.id.layoutHeader);
            layoutDetail = itemView.findViewById(R.id.layoutDetail);
            imgExpand = itemView.findViewById(R.id.imgExpand);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEnrolledTime = itemView.findViewById(R.id.tvEnrolledTime);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            pbProgress = itemView.findViewById(R.id.pbProgress);
            tvCompleted = itemView.findViewById(R.id.tvCompleted);
            rvLessonProgress = itemView.findViewById(R.id.rvLessonProgress);

            if (rvLessonProgress.getLayoutManager() == null) {
                rvLessonProgress.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            detailAdapter = new AdminLessonProgressDetailAdapter(new ArrayList<>());
            rvLessonProgress.setAdapter(detailAdapter);

            try { pbProgress.setMax(100); } catch (Exception ignored) {}

            layoutHeader.setOnClickListener(v -> toggleExpand());

            // optional: forward click to listener
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onStudentClick(students.get(pos));
                }
            });
        }

        public void bind(StudentProgressItem student) {
            if (student == null) {
                tvName.setText("Người dùng ẩn danh");
                tvEnrolledTime.setText("");
                tvProgress.setText("0%");
                try { pbProgress.setProgress(0); } catch (Exception ignored) {}
                tvCompleted.setText("0/0 bài");
                layoutDetail.setVisibility(View.GONE);
                imgAvatar.setImageResource(R.drawable.ava_student);
                detailAdapter.updateItems(new ArrayList<>());
                return;
            }

            CourseStudent s = student.getStudent();
            if (s != null) {
                tvName.setText(safe(s.getName(), "Người dùng"));

                long enrolled = s.getEnrolledAt();
                if (enrolled > 0) {
                    CharSequence rel = DateUtils.getRelativeTimeSpanString(
                            enrolled,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                    );
                    tvEnrolledTime.setText("Tham gia " + rel);
                } else {
                    tvEnrolledTime.setText("");
                }

                String avatar = s.getAvatarUrl();
                if (avatar != null && !avatar.trim().isEmpty()) {
                    // Assuming ImageLoader.display has a callback functional interface
                    ImageLoader.getInstance().display(avatar, imgAvatar, R.drawable.ava_student, success -> { /* no-op */ });
                } else {
                    imgAvatar.setImageResource(R.drawable.ava_student);
                }
            } else {
                tvName.setText("Người dùng ẩn danh");
                tvEnrolledTime.setText("");
                imgAvatar.setImageResource(R.drawable.ava_student);
            }

            int progress = clamp(student.getProgressPercentage());
            tvProgress.setText(progress + "%");
            try { pbProgress.setProgress(progress); } catch (Exception ignored) {}

            int completed = student.getLessonCompleted();
            int total = student.getTotalLessons();
            tvCompleted.setText(completed + "/" + total + " bài hoàn thành");

            List<LessonProgressDetail> details = student.getLessonProgresses();
            if (details == null) details = new ArrayList<>();
            detailAdapter.updateItems(details);

            if (isExpanded) {
                layoutDetail.setVisibility(View.VISIBLE);
                imgExpand.setRotation(180);
            } else {
                layoutDetail.setVisibility(View.GONE);
                imgExpand.setRotation(0);
            }
        }

        private void toggleExpand() {
            isExpanded = !isExpanded;
            if (isExpanded) {
                layoutDetail.setVisibility(View.VISIBLE);
                imgExpand.animate().rotation(180).setDuration(200).start();
            } else {
                layoutDetail.setVisibility(View.GONE);
                imgExpand.animate().rotation(0).setDuration(200).start();
            }
        }

        private String safe(String s, String fallback) {
            return s == null || s.isEmpty() ? fallback : s;
        }

        private int clamp(int p) {
            return Math.max(0, Math.min(100, p));
        }
    }

    // DTO Classes
    public static class StudentProgressItem {
        private CourseStudent student;
        private int progressPercentage;
        private int lessonCompleted;
        private int totalLessons;
        private List<LessonProgressDetail> lessonProgresses;

        public StudentProgressItem(CourseStudent student, int progressPercentage,
                                   int lessonCompleted, int totalLessons,
                                   List<LessonProgressDetail> lessonProgresses) {
            this.student = student;
            this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
            this.lessonCompleted = lessonCompleted;
            this.totalLessons = totalLessons;
            this.lessonProgresses = lessonProgresses != null ? lessonProgresses : new ArrayList<>();
        }

        public CourseStudent getStudent() { return student; }
        public int getProgressPercentage() { return progressPercentage; }
        public int getLessonCompleted() { return lessonCompleted; }
        public int getTotalLessons() { return totalLessons; }
        public List<LessonProgressDetail> getLessonProgresses() { return lessonProgresses; }
    }

    public static class LessonProgressDetail {
        private int order;
        private String lessonTitle;
        private int progressPercentage;
        private boolean isCompleted;

        public LessonProgressDetail(int order, String lessonTitle,
                                    int progressPercentage, boolean isCompleted) {
            this.order = order;
            this.lessonTitle = lessonTitle;
            this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
            this.isCompleted = isCompleted || this.progressPercentage >= 90;
        }

        public int getOrder() { return order; }
        public String getLessonTitle() { return lessonTitle; }
        public int getProgressPercentage() { return progressPercentage; }
        public boolean isCompleted() { return isCompleted; }
    }
}