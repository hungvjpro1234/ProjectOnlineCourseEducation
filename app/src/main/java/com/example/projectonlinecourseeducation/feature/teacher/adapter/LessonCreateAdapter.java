package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonCreateAdapter extends RecyclerView.Adapter<LessonCreateAdapter.VH> {

    public interface OnLessonActionListener {
        void onEditLesson(Lesson lesson, int position);
        void onDeleteLesson(Lesson lesson, int position);
        void onEditQuiz(Lesson lesson, int position); // when creating course, this will be a draft edit
    }

    private OnLessonActionListener actionListener;
    private final List<Lesson> data = new ArrayList<>();

    public void setOnLessonActionListener(OnLessonActionListener l) {
        this.actionListener = l;
    }

    public void submitList(List<Lesson> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public List<Lesson> getLessons() {
        return new ArrayList<>(data);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_lesson_create, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Lesson lesson = data.get(pos);
        if (lesson == null) return;

        h.tvOrder.setText(String.valueOf(pos + 1));
        h.tvLessonTitle.setText(lesson.getTitle());
        h.tvDuration.setText(lesson.getDuration());

        h.btnEdit.setOnClickListener(view -> {
            if (actionListener != null) actionListener.onEditLesson(lesson, pos);
        });

        h.btnDelete.setOnClickListener(view -> {
            if (actionListener != null) actionListener.onDeleteLesson(lesson, pos);
        });

        h.btnEditQuiz.setOnClickListener(view -> {
            if (actionListener != null) actionListener.onEditQuiz(lesson, pos);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvOrder, tvLessonTitle, tvDuration;
        ImageButton btnEdit, btnDelete, btnEditQuiz;

        VH(@NonNull View v) {
            super(v);
            tvOrder = v.findViewById(R.id.tvOrder);
            tvLessonTitle = v.findViewById(R.id.tvLessonTitle);
            tvDuration = v.findViewById(R.id.tvDuration);
            btnEdit = v.findViewById(R.id.btnEditLesson);
            btnDelete = v.findViewById(R.id.btnDeleteLesson);
            btnEditQuiz = v.findViewById(R.id.btnEditQuiz);
        }
    }
}
