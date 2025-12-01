package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Lesson;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentLessonVideoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách bài học dưới dạng card
 * Mỗi card chứa: thumbnail (placeholder), tiêu đề, duration, play icon
 * Click vào card sẽ navigate tới StudentLessonVideoActivity
 */
public class StudentLessonCardAdapter extends RecyclerView.Adapter<StudentLessonCardAdapter.VH> {

    private final List<Lesson> data = new ArrayList<>();
    private final Context context;

    public StudentLessonCardAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<Lesson> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_lesson_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Lesson lesson = data.get(position);

        // Lesson number
        holder.tvLessonNumber.setText("Bài " + (position + 1));

        // Lesson title
        holder.tvLessonTitle.setText(lesson.getTitle());

        // Lesson duration (nếu có, để trống nếu chưa lấy được từ video)
        String duration = lesson.getDuration();
        holder.tvLessonDuration.setText(duration != null && !duration.isEmpty() ? duration : "00:00");

        // Thumbnail - sinh tự động placeholder dựa trên position
        // Xoay qua các màu: purple, blue, cyan
        int[] placeholderDrawables = {
                R.drawable.ic_lesson_placeholder_purple,
                R.drawable.ic_lesson_placeholder_blue,
                R.drawable.ic_lesson_placeholder_cyan
        };
        int placeholderIndex = position % 3;
        holder.ivLessonThumbnail.setImageResource(placeholderDrawables[placeholderIndex]);

        // Click listener - navigate to StudentLessonVideoActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentLessonVideoActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            intent.putExtra("lesson_title", lesson.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLessonNumber, tvLessonTitle, tvLessonDuration;
        ImageView ivLessonThumbnail;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLessonNumber = itemView.findViewById(R.id.tvLessonNumber);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvLessonDuration = itemView.findViewById(R.id.tvLessonDuration);
            ivLessonThumbnail = itemView.findViewById(R.id.ivLessonThumbnail);
        }
    }
}