package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartActionListener {
        void onRemoveClicked(Course course, int position);
        void onPayItemClicked(Course course);
    }

    private final List<Course> courseList;
    private final CartActionListener listener;

    public CartAdapter(List<Course> courses, CartActionListener listener) {
        this.courseList = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_cart_course_card, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.tvTitle.setText(course.getTitle());
        holder.tvTeacher.setText(course.getTeacher());
        holder.tvPrice.setText(String.valueOf(course.getPrice()));
        holder.tvInfo.setText(course.getRating() + " ★  •  " + course.getLectures() + " bài học");

        ImageLoader.getInstance().display(
                course.getImageUrl(),
                holder.imgCourse,
                R.drawable.ic_image_placeholder
        );

        // Sự kiện nút xóa
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onRemoveClicked(courseList.get(pos), pos);
                }
            }
        });

        // Sự kiện thanh toán từng item
        holder.btnPayItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPayItemClicked(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList != null ? courseList.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTeacher, tvPrice, tvInfo;
        ImageView imgCourse, btnRemove;
        Button btnPayItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            imgCourse = itemView.findViewById(R.id.imgCourse);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnPayItem = itemView.findViewById(R.id.btnPayItem);
        }
    }
}
