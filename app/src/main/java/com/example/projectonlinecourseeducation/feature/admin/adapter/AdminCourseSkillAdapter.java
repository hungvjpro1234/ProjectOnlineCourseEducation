package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Skills Adapter - UI với icon check và background đẹp
 */
public class AdminCourseSkillAdapter extends RecyclerView.Adapter<AdminCourseSkillAdapter.SkillViewHolder> {

    private final List<String> skills = new ArrayList<>();

    public void setSkills(List<String> list) {
        skills.clear();
        if (list != null) skills.addAll(list);
        notifyDataSetChanged();
    }

    public List<String> getSkills() {
        return new ArrayList<>(skills);
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_course_skill, parent, false);
        return new SkillViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        String skill = skills.get(position);
        holder.bind(skill);
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    static class SkillViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgCheck;
        private final TextView tvSkillText;

        SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            tvSkillText = itemView.findViewById(R.id.tvSkillText);
        }

        void bind(String skill) {
            tvSkillText.setText(skill != null ? skill : "");
        }
    }
}