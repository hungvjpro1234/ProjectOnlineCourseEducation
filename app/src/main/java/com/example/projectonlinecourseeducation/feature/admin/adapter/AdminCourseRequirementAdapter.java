package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Requirements Adapter - UI với số thứ tự trong hình tròn
 */
public class AdminCourseRequirementAdapter extends RecyclerView.Adapter<AdminCourseRequirementAdapter.RequirementViewHolder> {

    private final List<String> requirements = new ArrayList<>();

    public void setRequirements(List<String> list) {
        requirements.clear();
        if (list != null) requirements.addAll(list);
        notifyDataSetChanged();
    }

    public List<String> getRequirements() {
        return new ArrayList<>(requirements);
    }

    @NonNull
    @Override
    public RequirementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_course_requirement, parent, false);
        return new RequirementViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequirementViewHolder holder, int position) {
        String requirement = requirements.get(position);
        holder.bind(requirement, position + 1);
    }

    @Override
    public int getItemCount() {
        return requirements.size();
    }

    static class RequirementViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNumber;
        private final TextView tvRequirementText;

        RequirementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvRequirementText = itemView.findViewById(R.id.tvRequirementText);
        }

        void bind(String requirement, int number) {
            tvNumber.setText(String.valueOf(number));
            tvRequirementText.setText(requirement != null ? requirement : "");
        }
    }
}