package com.example.projectonlinecourseeducation.feature.common.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.notification.Notification;
import com.example.projectonlinecourseeducation.core.model.notification.Notification.NotificationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter chung cho notification của Student, Teacher, Admin
 * Hỗ trợ 3 trạng thái: UNREAD, VIEWED, READ với background khác nhau
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.notifications = new ArrayList<>();
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardNotification;
        private final View layoutBackground;
        private final View viewUnreadBadge;
        private final TextView tvTitle;
        private final TextView tvTime;
        private final TextView tvSenderName;
        private final TextView tvMessage;
        private final TextView tvCourseTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotification = itemView.findViewById(R.id.cardNotification);
            layoutBackground = itemView.findViewById(R.id.layoutNotificationBackground);
            viewUnreadBadge = itemView.findViewById(R.id.viewUnreadBadge);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
        }

        public void bind(Notification notification, OnNotificationClickListener listener) {
            // Title
            tvTitle.setText(notification.getTitle());

            // Sender Name
            tvSenderName.setText(notification.getSenderName());

            // Message
            tvMessage.setText(notification.getMessage());

            // Time (relative time)
            tvTime.setText(getRelativeTime(notification.getCreatedAt()));

            // Course/Lesson Title (optional)
            if (notification.getCourseTitle() != null && !notification.getCourseTitle().isEmpty()) {
                tvCourseTitle.setVisibility(View.VISIBLE);
                String courseText = "Khóa học: " + notification.getCourseTitle();
                if (notification.getLessonTitle() != null && !notification.getLessonTitle().isEmpty()) {
                    courseText += " - Bài: " + notification.getLessonTitle();
                }
                tvCourseTitle.setText(courseText);
            } else {
                tvCourseTitle.setVisibility(View.GONE);
            }

            // ========== STATUS-BASED STYLING ==========

            NotificationStatus status = notification.getStatus();

            // Unread Badge (chỉ hiện khi UNREAD)
            viewUnreadBadge.setVisibility(
                    status == NotificationStatus.UNREAD ? View.VISIBLE : View.GONE
            );

            // Background color
            if (notification.shouldHighlight()) {
                // UNREAD hoặc VIEWED → background màu xám đậm (dễ phân biệt)
                layoutBackground.setBackgroundColor(Color.parseColor("#E0E0E0"));
            } else {
                // READ → background trắng/transparent (màu mặc định)
                layoutBackground.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }

            // Click listener
            cardNotification.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        /**
         * Chuyển timestamp thành relative time (2 giờ trước, 3 ngày trước, ...)
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
            } else if (days < 30) {
                long weeks = days / 7;
                return weeks + " tuần trước";
            } else {
                long months = days / 30;
                return months + " tháng trước";
            }
        }
    }
}
