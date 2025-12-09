package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.ApiProvider;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// Import activity để compiler có thể resolve class
import com.example.projectonlinecourseeducation.feature.admin.activity.AdminManageCourseDetailActivity;

public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.VH> {

    public interface OnCourseDeletedListener {
        void onCourseDeleted(String courseId, int position);
    }

    private final Context ctx;
    private final List<Course> data;
    private final OnCourseDeletedListener deletedListener;
    private final NumberFormat nf = NumberFormat.getInstance(Locale.US);

    public AdminCourseAdapter(Context ctx, List<Course> data, OnCourseDeletedListener deletedListener) {
        this.ctx = ctx;
        this.data = data;
        this.deletedListener = deletedListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_admin_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Course c = data.get(position);
        holder.tvTitle.setText(c.getTitle() == null ? "—" : c.getTitle());
        holder.tvTeacher.setText(!TextUtils.isEmpty(c.getTeacher()) ? "By " + c.getTeacher() : "By —");
        holder.tvStudents.setText(String.format("%d học viên", c.getStudents()));
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", c.getRating()));
        holder.tvRatingBig.setText(String.format(Locale.getDefault(), "%.1f", c.getRating()));

        holder.tvPrice.setText(formatCurrency(c.getPrice()));

        long total = (long) c.getStudents() * Math.round(c.getPrice());
        holder.tvTotalValue.setText("Doanh thu: " + formatCurrency(total));

        // Load image bằng ImageLoader
        String url = c.getImageUrl() == null ? "" : c.getImageUrl();
        ImageLoader.getInstance().display(
                url,
                holder.imgCourse,
                R.drawable.course_placeholder,
                success -> {
                    // callback không cần xử lý ở đây
                }
        );

        holder.itemView.setOnClickListener(v -> {
            try {
                Context context = ctx;
                Intent it = new Intent(context, AdminManageCourseDetailActivity.class);
                it.putExtra("courseId", c.getId());
                // nếu context không phải Activity, cần thêm flag để tránh crash
                if (!(context instanceof android.app.Activity)) {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(it);
            } catch (Exception ignored) {}
        });

        holder.btnDelete.setOnClickListener(v -> {
            // optional: thêm confirm dialog nếu muốn (mình xóa trực tiếp)
            try {
                boolean ok = ApiProvider.getCourseApi().deleteCourse(c.getId());
                if (ok) {
                    Toast.makeText(ctx, "Đã xóa khóa học", Toast.LENGTH_SHORT).show();
                    if (deletedListener != null) deletedListener.onCourseDeleted(c.getId(), position);
                } else {
                    Toast.makeText(ctx, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(ctx, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // small accessibility
        holder.btnDelete.setContentDescription("Xóa " + (c.getTitle() == null ? "" : c.getTitle()));
    }

    private String formatCurrency(double value) {
        try {
            long v = Math.round(value);
            return "₫" + nf.format(v);
        } catch (Exception e) {
            return "₫0";
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView imgCourse;
        TextView tvTitle, tvTeacher, tvStudents, tvRating, tvPrice, tvTotalValue, tvRatingBig;
        ImageButton btnDelete;

        public VH(@NonNull View itemView) {
            super(itemView);
            imgCourse = itemView.findViewById(R.id.img_course);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTeacher = itemView.findViewById(R.id.tv_teacher);
            tvStudents = itemView.findViewById(R.id.tv_students);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvTotalValue = itemView.findViewById(R.id.tv_total_value);
            tvRatingBig = itemView.findViewById(R.id.tv_rating_big);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
