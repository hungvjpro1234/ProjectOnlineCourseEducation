package com.example.projectonlinecourseeducation.feature.student.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentCourseLessonActivity;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentHomeActivity;
import com.example.projectonlinecourseeducation.feature.student.adapter.CartAdapter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StudentCartFragment extends Fragment {

    private CartApi cartApi;
    private MyCourseApi myCourseApi;
    private List<Course> cartList;
    private CartAdapter cartAdapter;
    private TextView tvSummary, tvTotalPrice;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        cartApi = ApiProvider.getCartApi();
        myCourseApi = ApiProvider.getMyCourseApi();
        cartList = cartApi.getCartCourses();

        if (cartList == null || cartList.isEmpty()) {
            // Giỏ hàng trống
            return inflater.inflate(R.layout.fragment_student_cart_empty, container, false);
        } else {
            // Có dữ liệu
            View view = inflater.inflate(R.layout.fragment_student_cart, container, false);

            tvSummary = view.findViewById(R.id.tvSummary);
            tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
            btnCheckout = view.findViewById(R.id.btnCheckout);

            RecyclerView recycler = view.findViewById(R.id.rvCartCourses);
            cartAdapter = new CartAdapter(cartList, new CartAdapter.CartActionListener() {
                @Override
                public void onRemoveClicked(Course course, int position) {
                    if (course == null) return;

                    // 🛑 Hỏi confirm trước khi xóa
                    String msg = "Bạn có chắc muốn xóa khóa học \"" + course.getTitle() + "\" khỏi giỏ hàng?";
                    showRemoveConfirmDialog(msg, () -> {
                        // Xóa khỏi cart qua CartApi
                        boolean removed = cartApi.removeFromCart(course.getId());
                        if (removed) {
                            // Cập nhật list hiện tại (lấy lại từ API cho chắc)
                            cartList.clear();
                            cartList.addAll(cartApi.getCartCourses());

                            cartAdapter.notifyItemRemoved(position);
                            cartAdapter.notifyItemRangeChanged(position, cartList.size() - position);

                            updateSummary();
                            Toast.makeText(requireContext(), "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onPayItemClicked(Course course) {
                    if (course == null) return;

                    // 👉 Format giá giống bên Course Detail
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                    String priceText = nf.format(course.getPrice());

                    // Thanh toán 1 khóa học trong giỏ: dialog confirm -> dialog thành công
                    String msg = "Bạn có chắc muốn thanh toán khóa học \"" + course.getTitle() + "\"?\n"
                            + "Giá: " + priceText;

                    showPaymentConfirmDialog(msg, () ->
                            showPaymentSuccessDialog(
                                    "Thanh toán khóa \"" + course.getTitle() + "\" thành công",
                                    true,
                                    () -> {
                                        // Sau khi thanh toán khóa riêng lẻ:
                                        // 1. Thêm vào My Course
                                        if (myCourseApi != null) {
                                            myCourseApi.addPurchasedCourse(course);
                                        }
                                        // 2. Xóa khỏi giỏ
                                        cartApi.removeFromCart(course.getId());
                                        // 3. Cập nhật lại list giỏ hàng
                                        cartList.clear();
                                        cartList.addAll(cartApi.getCartCourses());
                                        cartAdapter.notifyDataSetChanged();
                                        updateSummary();
                                        // 4. Điều hướng sang màn Lesson của chính khóa vừa mua
                                        Intent intent = new Intent(requireContext(), StudentCourseLessonActivity.class);
                                        intent.putExtra("course_id", course.getId());
                                        intent.putExtra("course_title", course.getTitle());
                                        startActivity(intent);
                                        // 5. Đóng Activity chứa fragment (thường là StudentHomeActivity)
                                        requireActivity().finish();
                                    }
                            )
                    );
                }
            });

            recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            recycler.setAdapter(cartAdapter);

            updateSummary();

            btnCheckout.setOnClickListener(v -> {
                int count = cartApi.getTotalItems();
                double totalPrice = cartApi.getTotalPrice();

                if (count == 0) {
                    Toast.makeText(requireContext(),
                            "Giỏ hàng trống, không thể thanh toán",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                String msg = "Bạn có chắc muốn thanh toán " + count + " khóa học\n" +
                        "Tổng tiền: " + nf.format(totalPrice) + " ?";

                // Thanh toán toàn bộ giỏ hàng (fake): dialog confirm -> dialog thành công
                showPaymentConfirmDialog(msg, () ->
                        showPaymentSuccessDialog(
                                "Thanh toán toàn bộ giỏ hàng thành công",
                                true,
                                () -> {
                                    // 1. Lấy danh sách hiện tại trong giỏ (trước khi clear)
                                    List<Course> current = cartApi.getCartCourses();
                                    // 2. Thêm tất cả vào My Course
                                    if (myCourseApi != null) {
                                        myCourseApi.addPurchasedCourses(current);
                                    }
                                    // 3. Clear giỏ
                                    cartApi.clearCart();
                                    cartList.clear();
                                    cartAdapter.notifyDataSetChanged();
                                    updateSummary();

                                    // 4. Chọn 1 khóa để mở màn Lesson (ví dụ: khóa đầu tiên)
                                    if (current != null && !current.isEmpty()) {
                                        Course first = current.get(0);
                                        Intent intent = new Intent(requireContext(), StudentCourseLessonActivity.class);
                                        intent.putExtra("course_id", first.getId());
                                        intent.putExtra("course_title", first.getTitle());
                                        startActivity(intent);
                                    } else {
                                        // Nếu vì lý do gì đó không có course, fallback về Home
                                        Intent intent = new Intent(requireContext(), StudentHomeActivity.class);
                                        startActivity(intent);
                                    }

                                    // 5. Đóng Activity chứa fragment
                                    requireActivity().finish();
                                }
                        )
                );
            });

            return view;
        }
    }

    private void updateSummary() {
        if (tvSummary == null || tvTotalPrice == null || cartApi == null) return;

        int count = cartApi.getTotalItems();
        double totalPrice = cartApi.getTotalPrice();

        tvSummary.setText("Tổng cộng: " + count + " khóa học");

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(nf.format(totalPrice));
    }

    /**
     * Hiển thị dialog xác nhận thanh toán trong Fragment.
     *
     * @param message    Nội dung confirm
     * @param onConfirmed callback chạy khi user bấm "Xác nhận"
     */
    private void showPaymentConfirmDialog(String message, Runnable onConfirmed) {
        DialogConfirmHelper.showConfirmDialog(
                requireContext(),
                "Xác nhận thanh toán",
                message,
                R.drawable.question,
                "Xác nhận",
                "Hủy",
                R.color.blue_600, // 💜 màu gốc cho nút xác nhận
                () -> { if (onConfirmed != null) onConfirmed.run(); }
        );
    }

    /**
     * Dialog thông báo thanh toán thành công trong Fragment.
     *
     * @param message   Nội dung hiển thị
     * @param showToast Có hiển thị thêm Toast nữa không
     */
    private void showPaymentSuccessDialog(String message, boolean showToast) {
        showPaymentSuccessDialog(message, showToast, null);
    }

    /**
     * Dialog thông báo thanh toán thành công trong Fragment + callback sau khi đóng.
     *
     * @param message        Nội dung hiển thị
     * @param showToast      Có hiển thị thêm Toast nữa không
     * @param afterDismissed Callback chạy sau khi user bấm "Đóng"
     */
    private void showPaymentSuccessDialog(String message, boolean showToast, @Nullable Runnable afterDismissed) {
        DialogConfirmHelper.showSuccessDialog(
                requireContext(),
                "Thanh toán thành công",
                message,
                R.drawable.confirm,
                "Đóng",
                () -> {
                    if (showToast) {
                        Toast.makeText(requireContext(),
                                "Thanh toán thành công",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (afterDismissed != null) {
                        afterDismissed.run();
                    }
                }
        );
    }

    /**
     * Hiển thị dialog xác nhận xóa sản phẩm khỏi giỏ hàng.
     *
     * @param message     Nội dung confirm
     * @param onConfirmed Callback khi user bấm "Xóa"
     */
    private void showRemoveConfirmDialog(String message, Runnable onConfirmed) {
        DialogConfirmHelper.showConfirmDialog(
                requireContext(),
                "Xóa sản phẩm",
                message,
                R.drawable.remove_cart,
                "Xóa",
                "Hủy",
                R.color.colorError, // 🔥 màu đỏ cho nút XÓA
                () -> { if (onConfirmed != null) onConfirmed.run(); }
        );
    }
}
