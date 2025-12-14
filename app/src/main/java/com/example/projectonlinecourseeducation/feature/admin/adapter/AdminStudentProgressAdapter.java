package com.example.projectonlinecourseeducation.feature.admin.adapter;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho Admin: hiển thị danh sách students + progress summary,
 * expand để load và show lesson-level progress (on-demand).
 */
public class AdminStudentProgressAdapter extends RecyclerView.Adapter<AdminStudentProgressAdapter.StudentViewHolder> {

    public interface OnStudentActionListener {
        void onViewDetails(String userId, String userName);

        /**
         * Called when adapter wants to load detail lesson progress for given user.
         * Callback must be invoked on UI thread.
         */
        void onLoadDetailRequested(String userId, DetailLoadCallback callback);
    }

    public interface DetailLoadCallback {
        void onLoaded(List<LessonProgressRow> rows);
    }

    public static class StudentRow {
        public final String userId;
        public final String name;
        public final String email;
        public final double totalSpent;
        public final int coursesPurchased;
        public final int cartItems;

        public StudentRow(String userId, String name, String email, double totalSpent, int coursesPurchased, int cartItems) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.totalSpent = totalSpent;
            this.coursesPurchased = coursesPurchased;
            this.cartItems = cartItems;
        }
    }

    public static class LessonProgressRow {
        public final int order;
        public final String title;
        public final int percent;
        public final boolean completed;
        public final String courseTitle;

        public LessonProgressRow(int order, String title, int percent, boolean completed, String courseTitle) {
            this.order = order;
            this.title = title;
            this.percent = percent;
            this.completed = completed;
            this.courseTitle = courseTitle;
        }
    }

    private final List<StudentRow> students = new ArrayList<>();
    private final OnStudentActionListener listener;

    public AdminStudentProgressAdapter(OnStudentActionListener listener) {
        this.listener = listener;
    }

    public void setStudents(List<StudentRow> rows) {
        students.clear();
        if (rows != null) students.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_student_progress, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentRow r = students.get(position);
        holder.bind(r);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvEmail, tvCourses, tvSpent, tvProgressText;
        private final ProgressBar pbSummary;
        private final ImageView imgExpand;
        private final LinearLayout layoutDetails;
        private final RecyclerView rvLessons;
        private final LessonProgressAdapter lessonAdapter;
        private boolean isExpanded = false;
        private String currentUserId;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminStudentName);
            tvEmail = itemView.findViewById(R.id.tvAdminStudentEmail);
            tvCourses = itemView.findViewById(R.id.tvAdminCourses);
            tvSpent = itemView.findViewById(R.id.tvAdminSpent);
            pbSummary = itemView.findViewById(R.id.pbAdminSummary);
            tvProgressText = itemView.findViewById(R.id.tvAdminProgressText);
            imgExpand = itemView.findViewById(R.id.imgAdminExpand);
            layoutDetails = itemView.findViewById(R.id.layoutAdminDetails);
            rvLessons = itemView.findViewById(R.id.rvAdminLessonProgress);

            if (rvLessons.getLayoutManager() == null) {
                rvLessons.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }
            lessonAdapter = new LessonProgressAdapter(new ArrayList<>());
            rvLessons.setAdapter(lessonAdapter);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    StudentRow row = students.get(pos);
                    if (listener != null) listener.onViewDetails(row.userId, row.name);
                }
            });

            imgExpand.setOnClickListener(v -> toggleExpand());
        }

        public void bind(StudentRow row) {
            currentUserId = row.userId;
            tvName.setText(row.name != null ? row.name : "Người dùng");
            tvEmail.setText(row.email != null ? row.email : "");
            tvCourses.setText(String.format("%d khóa", row.coursesPurchased));
            tvSpent.setText(String.format("%.0f VNĐ", row.totalSpent));

            // initial summary progress display: we don't have precomputed percent in StudentRow,
            // so show placeholder 0% until detail loads (or you compute aggregate in fragment)
            pbSummary.setProgress(0);
            tvProgressText.setText("0%");

            // collapse by default
            layoutDetails.setVisibility(View.GONE);
            imgExpand.setRotation(0f);
            isExpanded = false;
        }

        private void toggleExpand() {
            isExpanded = !isExpanded;
            if (isExpanded) {
                layoutDetails.setVisibility(View.VISIBLE);
                imgExpand.animate().rotation(180f).start();

                // start loading lesson details (if any)
                if (listener != null && currentUserId != null) {
                    // show loading placeholder
                    lessonAdapter.updateItems(new ArrayList<>());
                    listener.onLoadDetailRequested(currentUserId, rows -> {
                        // update nested adapter
                        lessonAdapter.updateItems(rows);

                        // compute aggregate percent
                        int sum = 0;
                        int total = 0;
                        int completed = 0;
                        for (LessonProgressRow r : rows) {
                            sum += r.percent;
                            total++;
                            if (r.completed) completed++;
                        }
                        int agg = total > 0 ? sum / total : 0;
                        pbSummary.setProgress(agg);
                        tvProgressText.setText(agg + "% (" + completed + "/" + total + ")");
                    });
                }
            } else {
                layoutDetails.setVisibility(View.GONE);
                imgExpand.animate().rotation(0f).start();
            }
        }
    }

    // simple adapter for nested lesson list
    private static class LessonProgressAdapter extends RecyclerView.Adapter<LessonProgressAdapter.LessonVH> {
        private final List<LessonProgressRow> items;

        LessonProgressAdapter(List<LessonProgressRow> items) { this.items = items != null ? items : new ArrayList<>(); }

        void updateItems(List<LessonProgressRow> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LessonVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_lesson_progress, parent, false);
            return new LessonVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull LessonVH holder, int position) {
            LessonProgressRow r = items.get(position);
            holder.bind(r);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class LessonVH extends RecyclerView.ViewHolder {
            private final TextView tvTitle, tvPercent, tvCourse;
            private final ProgressBar pb;
            LessonVH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvLessonTitle);
                tvPercent = itemView.findViewById(R.id.tvLessonPercent);
                tvCourse = itemView.findViewById(R.id.tvLessonCourse);
                pb = itemView.findViewById(R.id.pbLesson);
            }
            void bind(LessonProgressRow r) {
                tvTitle.setText((r.order > 0 ? r.order + ". " : "") + (r.title != null ? r.title : "Bài"));
                tvPercent.setText(r.percent + "%");
                tvCourse.setText(r.courseTitle != null ? r.courseTitle : "");
                try { pb.setMax(100); pb.setProgress(r.percent); } catch (Exception ignored) {}
            }
        }
    }
}
