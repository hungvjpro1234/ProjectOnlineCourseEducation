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

public class ManagementCourseRequirementAdapter extends RecyclerView.Adapter<ManagementCourseRequirementAdapter.ReqVH> {

    private final List<String> requirements = new ArrayList<>();

    public void setRequirements(List<String> list) {
        requirements.clear();
        if (list != null) requirements.addAll(list);
        notifyDataSetChanged();
    }

    public List<String> getRequirements() {
        return new ArrayList<>(requirements);
    }

    public void addRequirement(String r) {
        requirements.add(r);
        notifyItemInserted(requirements.size() - 1);
    }

    public void removeRequirement(int pos) {
        if (pos >= 0 && pos < requirements.size()) {
            requirements.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    @NonNull
    @Override
    public ReqVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_management_course_requirement, parent, false);
        return new ReqVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReqVH holder, int position) {
        String r = requirements.get(position);
        holder.tvRequirementText.setText("• " + r);
        // no delete UI shown by default
    }

    @Override
    public int getItemCount() {
        return requirements.size();
    }

    static class ReqVH extends RecyclerView.ViewHolder {
        TextView tvRequirementText;
        ReqVH(@NonNull View itemView) {
            super(itemView);
            tvRequirementText = itemView.findViewById(R.id.tvRequirementText);
        }
    }
}
