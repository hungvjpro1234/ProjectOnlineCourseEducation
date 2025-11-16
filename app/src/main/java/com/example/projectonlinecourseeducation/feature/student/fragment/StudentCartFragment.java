package com.example.projectonlinecourseeducation.feature.student.fragment;

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
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.feature.student.adapter.CartAdapter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StudentCartFragment extends Fragment {

    private CartApi cartApi;
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
                }

                @Override
                public void onPayItemClicked(Course course) {
                    Toast.makeText(requireContext(),
                            "Thanh toán khóa: " + course.getTitle(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            recycler.setAdapter(cartAdapter);

            updateSummary();

            btnCheckout.setOnClickListener(v ->
                    Toast.makeText(requireContext(),
                            "Thanh toán toàn bộ giỏ hàng (fake)",
                            Toast.LENGTH_SHORT).show()
            );

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
}
