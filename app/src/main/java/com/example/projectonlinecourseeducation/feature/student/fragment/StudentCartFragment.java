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
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.core.utils.DialogConfirmHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentHomeActivity;
import com.example.projectonlinecourseeducation.feature.student.adapter.CartAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentCartFragment extends Fragment {

    private CartApi cartApi;
    private MyCourseApi myCourseApi;
    private CourseApi courseApi; // <-- NEW: để gọi recordPurchase
    private List<Course> cartList;
    private CartAdapter cartAdapter;
    private TextView tvSummary, tvTotalPrice;
    private Button btnCheckout;

    // Listener để đăng ký với CartApi
    private final CartApi.CartUpdateListener cartUpdateListener = () -> {
        if (!isAdded()) return;
        loadCartAsync();
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        cartApi = ApiProvider.getCartApi();
        myCourseApi = ApiProvider.getMyCourseApi();
        courseApi = ApiProvider.getCourseApi();

        return inflater.inflate(R.layout.fragment_student_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvSummary = view.findViewById(R.id.tvSummary);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        RecyclerView recycler = view.findViewById(R.id.rvCartCourses);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartList, cartActionListener);
        recycler.setAdapter(cartAdapter);

        loadCartAsync();

        btnCheckout.setOnClickListener(v -> {
            showPaymentConfirmDialog(
                    "Bạn có chắc muốn thanh toán toàn bộ giỏ hàng?",
                    () -> {
                        AsyncApiHelper.execute(
                                () -> cartApi.checkout(),
                                new AsyncApiHelper.ApiCallback<List<Course>>() {
                                    @Override
                                    public void onSuccess(List<Course> purchasedCourses) {
                                        // ✅ Update MyCourse cache sau khi checkout thành công
                                        if (myCourseApi != null && purchasedCourses != null && !purchasedCourses.isEmpty()) {
                                            myCourseApi.addPurchasedCourses(purchasedCourses);
                                        }

                                        loadCartAsync();

                                        Intent intent = new Intent(requireContext(), StudentHomeActivity.class);
                                        intent.putExtra("open_my_course", true);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(requireContext(),
                                                "Lỗi thanh toán",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    }
            );
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Đăng ký listener để đồng bộ UI khi cart thay đổi
        if (cartApi != null) {
            cartApi.addCartUpdateListener(cartUpdateListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Hủy đăng ký để tránh leak
        if (cartApi != null) {
            cartApi.removeCartUpdateListener(cartUpdateListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // đảm bảo remove listener nếu view bị destroy giữa chừng
        if (cartApi != null) {
            cartApi.removeCartUpdateListener(cartUpdateListener);
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

    private void loadCartAsync() {
        AsyncApiHelper.execute(
                () -> cartApi.getCartCourses(),
                new AsyncApiHelper.ApiCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> result) {
                        if (!isAdded()) return;

                        cartList.clear();
                        if (result != null) {
                            cartList.addAll(result);
                        }
                        cartAdapter.notifyDataSetChanged();
                        updateSummary();
                    }

                    @Override
                    public void onError(Exception e) {
                        cartList.clear();
                        cartAdapter.notifyDataSetChanged();
                        updateSummary();
                    }
                }
        );
    }

    private final CartAdapter.CartActionListener cartActionListener =
            new CartAdapter.CartActionListener() {

                @Override
                public void onRemoveClicked(Course course, int position) {
                    if (course == null) return;

                    String msg = "Bạn có chắc muốn xóa khóa học \"" + course.getTitle() + "\" khỏi giỏ hàng?";
                    showRemoveConfirmDialog(msg, () -> {
                        AsyncApiHelper.execute(
                                () -> cartApi.removeFromCart(course.getId()),
                                new AsyncApiHelper.ApiCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean removed) {
                                        loadCartAsync();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(requireContext(),
                                                "Lỗi xóa giỏ hàng",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    });
                }

                @Override
                public void onPayItemClicked(Course course) {
                    // ❌ REMOVED: Individual pay functionality (was checking out entire cart)
                    // This method is no longer called as the button is hidden in CartAdapter
                    // Users should use the "Checkout All" button at the bottom instead
                }
            };
}
