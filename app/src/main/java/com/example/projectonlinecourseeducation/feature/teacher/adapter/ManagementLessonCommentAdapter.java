package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách comment bài giảng cho TEACHER
 * Teacher có thể xem, trả lời hoặc xóa comment
 * Sử dụng layout: item_teacher_lesson_comment.xml
 */
public class ManagementLessonCommentAdapter extends RecyclerView.Adapter<ManagementLessonCommentAdapter.CommentViewHolder> {

    private List<LessonComment> comments = new ArrayList<>();
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onReplyClick(LessonComment comment);
        void onDeleteClick(LessonComment comment);
        void onDeleteReplyClick(LessonComment comment);
    }

    public ManagementLessonCommentAdapter(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public void setComments(List<LessonComment> commentList) {
        this.comments = commentList != null ? commentList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addComment(LessonComment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    public void removeComment(int position) {
        if (position >= 0 && position < comments.size()) {
            comments.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_management_lesson_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        LessonComment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * ViewHolder cho mỗi item comment của teacher
     */
    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgUserAvatar;
        private TextView tvUserName;
        private TextView tvUserRole;
        private TextView tvCommentTime;
        private ImageButton btnMore;
        private TextView tvCommentContent;
        private LinearLayout layoutTeacherReply;
        private ImageView imgTeacherAvatar;
        private TextView tvTeacherName;
        private TextView tvReplyDate;
        private TextView tvTeacherReplyContent;
        private ImageButton btnDeleteReply;
        private Button btnReply;
        private Button btnDelete;

        public CommentViewHolder(View itemView) {
            super(itemView);

            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            btnMore = itemView.findViewById(R.id.btnMore);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            layoutTeacherReply = itemView.findViewById(R.id.layoutTeacherReply);
            imgTeacherAvatar = itemView.findViewById(R.id.imgTeacherAvatar);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvReplyDate = itemView.findViewById(R.id.tvReplyDate);
            tvTeacherReplyContent = itemView.findViewById(R.id.tvTeacherReplyContent);
            btnDeleteReply = itemView.findViewById(R.id.btnDeleteReply);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Setup click listeners
            btnReply.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onReplyClick(comments.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(comments.get(getAdapterPosition()));
                }
            });

            btnDeleteReply.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteReplyClick(comments.get(getAdapterPosition()));
                }
            });
        }

        public void bind(LessonComment comment) {
            // Hide btnMore (không cần thiết)
            btnMore.setVisibility(View.GONE);

            // Hiển thị thông tin user
            tvUserName.setText(comment.getUserName());
            tvCommentTime.setText(formatDate(comment.getCreatedAt()));
            tvUserRole.setText("Học viên");

            // Load avatar - sử dụng ava_student.png
            imgUserAvatar.setImageResource(R.drawable.ava_student);

            // Hiển thị nội dung hoặc "[Bình luận đã bị xóa]"
            if (comment.isDeleted()) {
                tvCommentContent.setText("[Bình luận đã bị xóa]");
                tvCommentContent.setTextColor(0xFF999999); // Gray color
            } else {
                tvCommentContent.setText(comment.getContent());
                tvCommentContent.setTextColor(0xFF000000); // Black color
            }

            // Hiển thị teacher reply nếu có
            if (comment.hasTeacherReply()) {
                layoutTeacherReply.setVisibility(View.VISIBLE);
                tvTeacherName.setText(comment.getTeacherReplyBy() != null ? comment.getTeacherReplyBy() : "Giảng viên");
                tvTeacherReplyContent.setText(comment.getTeacherReplyContent());
                tvReplyDate.setText(formatDate(comment.getTeacherReplyAt()));

                // Load teacher avatar
                imgTeacherAvatar.setImageResource(R.drawable.ava_teacher);
            } else {
                layoutTeacherReply.setVisibility(View.GONE);
            }

            // Ẩn nút Reply nếu đã có reply hoặc đã xóa
            if (comment.hasTeacherReply() || comment.isDeleted()) {
                btnReply.setVisibility(View.GONE);
            } else {
                btnReply.setVisibility(View.VISIBLE);
            }

            // Nút Delete luôn hiển thị (teacher có thể xóa bất kỳ comment nào)
            btnDelete.setVisibility(View.VISIBLE);
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
