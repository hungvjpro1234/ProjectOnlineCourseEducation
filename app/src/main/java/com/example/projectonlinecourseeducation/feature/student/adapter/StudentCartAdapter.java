package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;

import java.util.List;

public class StudentCartAdapter extends RecyclerView.Adapter<StudentCartAdapter.CartViewHolder> {
    private List<Course> courseList;

    public StudentCartAdapter(List<Course> courses) {
        this.courseList = courses;
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
        holder.tvInfo.setText(String.valueOf(course.getRating()) +" ★  •  "+ String.valueOf(course.getLectures()) + " bài học");
        // Thêm set hình ảnh:
        //ImageLoader.getInstance().display(course.getImageUrl(), holder.imgBanner, R.drawable.ic_image_placeholder);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTeacher, tvPrice, tvInfo;
        //ImageView imgBanner; // ảnh

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            //imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}
