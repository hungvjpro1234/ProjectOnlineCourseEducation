package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.CourseStudent;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách học viên với khả năng expand/collapse
 * Null-safe: không ném NPE nếu student hoặc lessonProgresses == null
 *
 * NOTE:
 * - Layout item phải chứa các id: layoutStudentHeader, layoutStudentDetail, imgExpand,
 *   imgStudentAvatar, tvStudentName, tvStudentEmail, tvProgress, pbProgress,
 *   tvLessonCompleted, rvLessonProgress
 */
public class ManagementCourseStudentAdapter extends RecyclerView.Adapter<ManagementCourseStudentAdapter.StudentViewHolder> {

    private List<StudentProgressItem> students = new ArrayList<>();
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(StudentProgressItem student);
    }

    public ManagementCourseStudentAdapter(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public void setStudents(List<StudentProgressItem> students) {
        this.students = students != null ? students : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addStudent(StudentProgressItem student) {
        if (student == null) return;
        students.add(0, student);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_management_course_student, parent, false);
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

    /**
     * ViewHolder cho mỗi item học viên
     */
    public class StudentViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutStudentHeader;
        private final LinearLayout layoutStudentDetail;
        private final ImageView imgExpand;
        private final ImageView imgStudentAvatar;
        private final TextView tvStudentName;
        private final TextView tvStudentEmail; // reused to show enrolled time if email absent
        private final TextView tvProgress;
        private final ProgressBar pbProgress;
        private final TextView tvLessonCompleted;
        private final RecyclerView rvLessonProgress;
        private final ManagementLessonProgressDetailAdapter detailAdapter;
        private boolean isExpanded = false;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutStudentHeader = itemView.findViewById(R.id.layoutStudentHeader);
            layoutStudentDetail = itemView.findViewById(R.id.layoutStudentDetail);
            imgExpand = itemView.findViewById(R.id.imgExpand);
            imgStudentAvatar = itemView.findViewById(R.id.imgStudentAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            pbProgress = itemView.findViewById(R.id.pbProgress);
            tvLessonCompleted = itemView.findViewById(R.id.tvLessonCompleted);
            rvLessonProgress = itemView.findViewById(R.id.rvLessonProgress);

            // ensure nested RecyclerView has layout manager (prevent NPE)
            if (rvLessonProgress.getLayoutManager() == null) {
                rvLessonProgress.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            // create a reusable adapter for nested list to avoid recreating on each bind
            detailAdapter = new ManagementLessonProgressDetailAdapter(new ArrayList<>());
            rvLessonProgress.setAdapter(detailAdapter);

            // set progress max (safety)
            try { pbProgress.setMax(100); } catch (Exception ignored) {}

            // Expand/collapse when header clicked
            layoutStudentHeader.setOnClickListener(v -> toggleExpand());

            // item click forwards to listener (optional)
            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onStudentClick(students.get(pos));
                }
            });
        }

        public void bind(StudentProgressItem student) {
            // guard: student may be null (though we avoid adding nulls)
            if (student == null) {
                tvStudentName.setText("Người dùng ẩn danh");
                tvStudentEmail.setText("");
                tvProgress.setText("0%");
                pbProgress.setProgress(0);
                tvLessonCompleted.setText("0/0");
                layoutStudentDetail.setVisibility(View.GONE);
                imgStudentAvatar.setImageResource(R.drawable.ava_student);
                detailAdapter.updateItems(new ArrayList<>());
                return;
            }

            // CourseStudent may be null -> show placeholders
            CourseStudent s = student.getStudent();
            if (s != null) {
                String name = safe(s.getName());
                tvStudentName.setText(!name.isEmpty() ? name : "Người dùng");

                // course student does not have email in model; show enrolledAt if available
                long enrolled = s.getEnrolledAt();
                if (enrolled > 0) {
                    CharSequence rel = DateUtils.getRelativeTimeSpanString(enrolled, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                    tvStudentEmail.setText(rel); // reuse email TextView to show enrollment time
                } else {
                    tvStudentEmail.setText("");
                }

                // load avatar if available
                String avatar = s.getAvatarUrl();
                if (avatar != null && !avatar.trim().isEmpty()) {
                    ImageLoader.getInstance().display(avatar, imgStudentAvatar, R.drawable.ava_student, success -> {
                        // no-op
                    });
                } else {
                    imgStudentAvatar.setImageResource(R.drawable.ava_student);
                }
            } else {
                tvStudentName.setText("Người dùng ẩn danh");
                tvStudentEmail.setText("");
                imgStudentAvatar.setImageResource(R.drawable.ava_student);
            }

            int progress = clampPercent(student.getProgressPercentage());
            tvProgress.setText(progress + "%");
            try { pbProgress.setProgress(progress); } catch (Exception ignored) {}

            int completed = student.getLessonCompleted();
            int total = student.getTotalLessons();
            tvLessonCompleted.setText(completed + "/" + total);

            // setup nested adapter safely — update existing adapter's data
            List<LessonProgressDetail> details = student.getLessonProgresses();
            if (details == null) details = new ArrayList<>();
            detailAdapter.updateItems(details);

            // apply expand state
            if (isExpanded) {
                layoutStudentDetail.setVisibility(View.VISIBLE);
                imgExpand.setRotation(180);
            } else {
                layoutStudentDetail.setVisibility(View.GONE);
                imgExpand.setRotation(0);
            }
        }

        private void toggleExpand() {
            isExpanded = !isExpanded;
            if (isExpanded) {
                layoutStudentDetail.setVisibility(View.VISIBLE);
                imgExpand.animate().rotation(180).start();
            } else {
                layoutStudentDetail.setVisibility(View.GONE);
                imgExpand.animate().rotation(0).start();
            }
        }

        private String safe(String s) {
            return s == null ? "" : s;
        }

        private int clampPercent(int p) {
            if (p < 0) return 0;
            if (p > 100) return 100;
            return p;
        }
    }

    /**
     * Model để hold dữ liệu học viên + tiến độ học
     */
    public static class StudentProgressItem {
        private CourseStudent student;
        private int progressPercentage;  // 0-100
        private int lessonCompleted;
        private int totalLessons;
        private List<LessonProgressDetail> lessonProgresses;

        public StudentProgressItem(CourseStudent student, int progressPercentage, int lessonCompleted,
                                   int totalLessons, List<LessonProgressDetail> lessonProgresses) {
            this.student = student;
            this.progressPercentage = clampPercent(progressPercentage);
            this.lessonCompleted = lessonCompleted;
            this.totalLessons = totalLessons;
            this.lessonProgresses = lessonProgresses != null ? lessonProgresses : new ArrayList<>();
        }

        // Getters
        public CourseStudent getStudent() { return student; }
        public int getProgressPercentage() { return progressPercentage; }
        public int getLessonCompleted() { return lessonCompleted; }
        public int getTotalLessons() { return totalLessons; }
        public List<LessonProgressDetail> getLessonProgresses() { return lessonProgresses; }

        private int clampPercent(int p) {
            if (p < 0) return 0;
            if (p > 100) return 100;
            return p;
        }
    }

    /**
     * Model cho chi tiết tiến độ mỗi bài học
     */
    public static class LessonProgressDetail {
        private int order;
        private String lessonTitle;
        private int progressPercentage;
        private boolean isCompleted;

        public LessonProgressDetail(int order, String lessonTitle, int progressPercentage, boolean isCompleted) {
            this.order = order;
            this.lessonTitle = lessonTitle;
            this.progressPercentage = clampPercent(progressPercentage);
            this.isCompleted = isCompleted || this.progressPercentage >= 90;
        }

        public int getOrder() { return order; }
        public String getLessonTitle() { return lessonTitle; }
        public int getProgressPercentage() { return progressPercentage; }
        public boolean isCompleted() { return isCompleted; }

        private int clampPercent(int p) {
            if (p < 0) return 0;
            if (p > 100) return 100;
            return p;
        }
    }
}
