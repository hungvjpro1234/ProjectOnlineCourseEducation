package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class ProductLessonInfoAdapter extends RecyclerView.Adapter<ProductLessonInfoAdapter.VH> {

    private final List<Lesson> data = new ArrayList<>();

    public void submitList(List<Lesson> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_product_course_lesson, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Lesson l = data.get(position);
        holder.tvLessonIndex.setText(String.valueOf(position + 1));
        holder.tvLessonTitle.setText(l.getTitle());
        holder.tvLessonDuration.setText(l.getDuration());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLessonIndex, tvLessonTitle, tvLessonDuration;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLessonIndex = itemView.findViewById(R.id.tvLessonIndex);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvLessonDuration = itemView.findViewById(R.id.tvLessonDuration);
        }
    }
}
