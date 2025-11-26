package com.example.projectonlinecourseeducation.core.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.projectonlinecourseeducation.R;

public class DialogHelper {

    // Callback xác nhận chung
    public interface ConfirmCallback {
        void onConfirmed();
    }

    /**
     * Dialog confirm dạng custom layout.
     *
     * @param context      ngữ cảnh (Activity/Fragment)
     * @param title        tiêu đề dialog
     * @param message      nội dung
     * @param iconResId    icon hiển thị (có thể là confirm / question / remove_cart)
     * @param positiveText text nút xác nhận (vd: "Xác nhận", "Xóa")
     * @param negativeText text nút hủy (vd: "Hủy")
     * @param callback     callback khi ấn nút xác nhận
     */
    public static void showConfirmDialog(
            Context context,
            String title,
            String message,
            int iconResId,
            String positiveText,
            String negativeText,
            int positiveColorResId, // 👉 thêm màu custom
            ConfirmCallback callback
    ) {
        if (context == null) return;

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_confirm_generic, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        ImageView imgIcon = view.findViewById(R.id.imgIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnNegative = view.findViewById(R.id.btnNegative);
        Button btnPositive = view.findViewById(R.id.btnPositive);

        if (iconResId != 0) imgIcon.setImageResource(iconResId);

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnNegative.setText(negativeText);
        btnPositive.setText(positiveText);

        // ⭐ Set màu riêng cho từng loại confirm dialog
        if (positiveColorResId != 0)
            btnPositive.setBackgroundTintList(
                    context.getColorStateList(positiveColorResId)
            );

        btnNegative.setOnClickListener(v -> dialog.dismiss());
        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onConfirmed();
        });

        dialog.show();
    }

    /**
     * Dialog thông báo thành công dạng custom layout.
     *
     * @param context    ngữ cảnh
     * @param title      tiêu đề
     * @param message    nội dung
     * @param iconResId  icon hiển thị
     * @param buttonText text nút đóng
     * @param onDismiss  callback sau khi đóng (có thể null)
     */
    public static void showSuccessDialog(
            Context context,
            String title,
            String message,
            int iconResId,
            String buttonText,
            Runnable onDismiss
    ) {
        if (context == null) return;

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_success_generic, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        ImageView imgIcon = view.findViewById(R.id.imgIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnOk = view.findViewById(R.id.btnOk);

        if (iconResId != 0) {
            imgIcon.setImageResource(iconResId);
        }
        tvTitle.setText(title != null ? title : "");
        tvMessage.setText(message != null ? message : "");
        btnOk.setText(buttonText != null ? buttonText : "Đóng");

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (onDismiss != null) {
                onDismiss.run();
            }
        });

        dialog.show();
    }
}
