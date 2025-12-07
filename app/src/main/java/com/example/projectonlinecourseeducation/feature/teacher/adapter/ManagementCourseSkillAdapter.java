package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.util.ArrayList;
import java.util.List;

public class ManagementCourseSkillAdapter extends RecyclerView.Adapter<ManagementCourseSkillAdapter.SkillVH> {

    private final List<String> skills = new ArrayList<>();

    public void setSkills(List<String> list) {
        skills.clear();
        if (list != null) skills.addAll(list);
        notifyDataSetChanged();
    }

    public List<String> getSkills() {
        return new ArrayList<>(skills);
    }

    /**
     * Optional: public API to modify list programmatically from Activity.
     */
    public void addSkill(String skill) {
        skills.add(skill);
        notifyItemInserted(skills.size() - 1);
    }

    public void removeSkill(int pos) {
        if (pos >= 0 && pos < skills.size()) {
            skills.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    @NonNull
    @Override
    public SkillVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_management_course_skill, parent, false);
        return new SkillVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillVH holder, int position) {
        String s = skills.get(position);
        holder.tvSkillText.setText("• " + s);
        // không có button "add" theo yêu cầu
        // nếu muốn add long-press handling, có thể đăng listener tại đây
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    static class SkillVH extends RecyclerView.ViewHolder {
        TextView tvSkillText;
        SkillVH(@NonNull View itemView) {
            super(itemView);
            tvSkillText = itemView.findViewById(R.id.tvSkillText);
        }
    }
}
