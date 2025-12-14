package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.feature.admin.model.StudentStats;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách học viên với real data (expandable)
 */
public class UserStudentAdapter extends RecyclerView.Adapter<UserStudentAdapter.StudentViewHolder> {

    private List<StudentStats> studentList = new ArrayList<>();
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onViewDetailsClick(StudentStats studentStats);
    }

    public UserStudentAdapter(OnStudentClickListener listener) {
        this.listener = listener;
    }

    /**
     * Set danh sách students (đã có stats)
     */
    public void setStudents(List<StudentStats> students) {
        this.studentList = students != null ? students : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_manage_user_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentStats stats = studentList.get(position);
        holder.bind(stats, listener);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout collapsedView;
        private LinearLayout expandedView;
        private ImageView imgExpand;
        private ImageView imgAvatar;
        private boolean isExpanded = false;

        private TextView tvStudentName;
        private TextView tvStudentEmail;
        private TextView tvTotalSpent;
        private TextView tvCoursesPurchased;
        private TextView tvCartItems;
        private Button btnViewDetails;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            collapsedView = itemView.findViewById(R.id.collapsedView);
            expandedView = itemView.findViewById(R.id.expandedView);
            imgExpand = itemView.findViewById(R.id.imgExpandStudent);
            imgAvatar = itemView.findViewById(R.id.imgStudentAvatar);

            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvTotalSpent = itemView.findViewById(R.id.tvTotalSpent);
            tvCoursesPurchased = itemView.findViewById(R.id.tvCoursesPurchased);
            tvCartItems = itemView.findViewById(R.id.tvCartItems);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(StudentStats stats, OnStudentClickListener listener) {
            // Hiển thị avatar
            if (imgAvatar != null) {
                imgAvatar.setImageResource(R.drawable.ava_student);
            }

            // Hiển thị thông tin cơ bản
            tvStudentName.setText(stats.getUser().getName());
            tvStudentEmail.setText(stats.getUser().getEmail());

            // Format số tiền
            NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            tvTotalSpent.setText(currencyFormat.format(stats.getTotalSpent()) + " VNĐ");
            tvCoursesPurchased.setText(stats.getCoursesPurchased() + " khóa");
            tvCartItems.setText(stats.getCartItems() + " khóa");

            // Toggle expand/collapse
            collapsedView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                if (isExpanded) {
                    expandedView.setVisibility(View.VISIBLE);
                    imgExpand.setRotation(180);
                } else {
                    expandedView.setVisibility(View.GONE);
                    imgExpand.setRotation(0);
                }
            });

            // View details button
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(stats);
                }
            });
        }
    }
}