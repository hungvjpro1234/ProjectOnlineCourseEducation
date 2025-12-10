package com.example.projectonlinecourseeducation.feature.admin.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Lesson Comment Adapter
 * - Hiển thị comments của students
 * - Admin CHỈ có thể XÓA (không reply - chỉ teacher mới reply)
 */
public class AdminLessonCommentAdapter extends RecyclerView.Adapter<AdminLessonCommentAdapter.CommentViewHolder> {

    private List<LessonComment> comments = new ArrayList<>();
    private final OnCommentDeleteListener listener;

    public interface OnCommentDeleteListener {
        void onDeleteClick(LessonComment comment);
        void onDeleteReplyClick(LessonComment comment);
    }

    public AdminLessonCommentAdapter(OnCommentDeleteListener listener) {
        this.listener = listener;
    }

    public void setComments(List<LessonComment> newComments) {
        this.comments.clear();
        if (newComments != null) {
            this.comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_lesson_comment, parent, false);
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

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardComment;
        private final ImageView imgAvatar;
        private final TextView tvUserName;
        private final TextView tvTimestamp;
        private final TextView tvContent;
        private final ImageButton btnDelete;

        // Reply section (READ-ONLY - teacher đã reply)
        private final View layoutReply;
        private final TextView tvReplyBy;
        private final TextView tvReplyTimestamp;
        private final TextView tvReplyContent;
        private final ImageButton btnDeleteReply;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            cardComment = itemView.findViewById(R.id.cardComment);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            layoutReply = itemView.findViewById(R.id.layoutReply);
            tvReplyBy = itemView.findViewById(R.id.tvReplyBy);
            tvReplyTimestamp = itemView.findViewById(R.id.tvReplyTimestamp);
            tvReplyContent = itemView.findViewById(R.id.tvReplyContent);
            btnDeleteReply = itemView.findViewById(R.id.btnDeleteReply);

            // ẨN reply button - Admin không reply
            ImageButton btnReply = itemView.findViewById(R.id.btnReply);
            if (btnReply != null) {
                btnReply.setVisibility(View.GONE);
            }
        }

        public void bind(LessonComment comment) {
            // User info
            tvUserName.setText(safe(comment.getUserName()));
            tvContent.setText(safe(comment.getContent()));

            // Timestamp (relative time)
            long timestamp = comment.getCreatedAt();
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            tvTimestamp.setText(relativeTime);

            // Reply section (READ-ONLY - chỉ hiển thị, không edit)
            if (comment.getTeacherReplyContent() != null && !comment.getTeacherReplyContent().isEmpty()) {
                layoutReply.setVisibility(View.VISIBLE);
                tvReplyBy.setText(safe(comment.getTeacherReplyBy()));
                tvReplyContent.setText(safe(comment.getTeacherReplyContent()));

                if (comment.getTeacherReplyAt() != null) {
                    CharSequence replyTime = DateUtils.getRelativeTimeSpanString(
                            comment.getTeacherReplyAt(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                    );
                    tvReplyTimestamp.setText(replyTime);
                } else {
                    tvReplyTimestamp.setText("");
                }

                // Delete reply button
                btnDeleteReply.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteReplyClick(comment);
                    }
                });
            } else {
                layoutReply.setVisibility(View.GONE);
            }

            // Delete comment button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(comment);
                }
            });
        }

        private String safe(String s) {
            return s == null ? "" : s;
        }
    }
}