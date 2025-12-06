package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter để hiển thị danh sách bình luận bài học
 */
public class LessonCommentAdapter extends RecyclerView.Adapter<LessonCommentAdapter.CommentViewHolder> {

    /**
     * Interface để xử lý sự kiện xóa bình luận
     */
    public interface OnCommentActionListener {
        void onDeleteComment(LessonComment comment);
    }

    private final List<LessonComment> comments = new ArrayList<>();
    private OnCommentActionListener actionListener;
    private String currentUserId; // ID người dùng hiện tại để hiển thị nút xóa

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.actionListener = listener;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    /**
     * Cập nhật danh sách bình luận
     */
    public void submitList(List<LessonComment> newComments) {
        comments.clear();
        if (newComments != null) {
            comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    /**
     * Thêm bình luận mới vào đầu danh sách
     */
    public void addComment(LessonComment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    /**
     * Xóa bình luận khỏi danh sách
     */
    public void removeComment(String commentId) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                comments.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        LessonComment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * ViewHolder cho mỗi bình luận
     */
    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvCommentTime;
        private final TextView tvCommentContent;
        private final ImageButton btnDeleteComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);
        }

        public void bind(LessonComment comment) {
            // Hiển thị tên người dùng
            tvUserName.setText(comment.getUserName());

            // Hiển thị nội dung bình luận
            tvCommentContent.setText(comment.getContent());

            // Hiển thị thời gian (relative time: "2 giờ trước", "1 ngày trước")
            tvCommentTime.setText(getRelativeTime(comment.getCreatedAt()));

            // Hiển thị nút xóa chỉ khi là bình luận của chính người dùng
            if (currentUserId != null && currentUserId.equals(comment.getUserId())) {
                btnDeleteComment.setVisibility(View.VISIBLE);
                btnDeleteComment.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeleteComment(comment);
                    }
                });
            } else {
                btnDeleteComment.setVisibility(View.GONE);
            }
        }

        /**
         * Chuyển timestamp thành chuỗi thời gian tương đối
         * Ví dụ: "Vừa xong", "5 phút trước", "2 giờ trước", "1 ngày trước"
         */
        private String getRelativeTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (seconds < 60) {
                return "Vừa xong";
            } else if (minutes < 60) {
                return minutes + " phút trước";
            } else if (hours < 24) {
                return hours + " giờ trước";
            } else if (days < 7) {
                return days + " ngày trước";
            } else {
                // Nếu quá 7 ngày thì hiển thị ngày cụ thể
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
