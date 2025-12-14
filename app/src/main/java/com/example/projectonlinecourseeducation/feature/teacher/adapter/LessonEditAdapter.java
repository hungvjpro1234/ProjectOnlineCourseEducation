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

public class LessonEditAdapter extends RecyclerView.Adapter<LessonEditAdapter.VH> {

    public interface OnLessonActionListener {
        void onEditLesson(Lesson lesson, int position);
        void onDeleteLesson(Lesson lesson, int position);
        void onEditQuiz(Lesson lesson, int position); // NEW
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
                .inflate(R.layout.item_teacher_lesson_edit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Lesson lesson = data.get(pos);
        if (lesson == null) return;

        // Hiển thị order dựa trên vị trí trong list => luôn liên tục 1..n
        h.tvOrder.setText(String.valueOf(pos + 1));
        h.tvLessonTitle.setText(lesson.getTitle());
        h.tvDuration.setText(lesson.getDuration());

        // Hiển thị approval status tag nếu lesson đang chờ duyệt
        String statusText = lesson.getApprovalStatusText();
        if (statusText != null && !statusText.isEmpty()) {
            h.tvApprovalStatus.setText(statusText);
            h.tvApprovalStatus.setVisibility(View.VISIBLE);
        } else {
            h.tvApprovalStatus.setVisibility(View.GONE);
        }

        h.btnEdit.setOnClickListener(view -> {
            if (actionListener != null) actionListener.onEditLesson(lesson, pos);
        });

        h.btnDelete.setOnClickListener(view -> {
            if (actionListener != null) actionListener.onDeleteLesson(lesson, pos);
        });

        // NEW: open quiz editor
        h.btnEditQuiz.setOnClickListener(view -> {
            if (actionListener != null) actionListener.onEditQuiz(lesson, pos);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvOrder, tvLessonTitle, tvDuration, tvApprovalStatus;
        ImageButton btnEdit, btnDelete, btnEditQuiz; // added btnEditQuiz

        VH(@NonNull View v) {
            super(v);
            tvOrder = v.findViewById(R.id.tvOrder);
            tvLessonTitle = v.findViewById(R.id.tvLessonTitle);
            tvDuration = v.findViewById(R.id.tvDuration);
            tvApprovalStatus = v.findViewById(R.id.tvApprovalStatus);
            btnEdit = v.findViewById(R.id.btnEditLesson);
            btnDelete = v.findViewById(R.id.btnDeleteLesson);
            // IMPORTANT: item_teacher_lesson_edit.xml must include this id
            btnEditQuiz = v.findViewById(R.id.btnEditQuiz);
        }
    }
}
