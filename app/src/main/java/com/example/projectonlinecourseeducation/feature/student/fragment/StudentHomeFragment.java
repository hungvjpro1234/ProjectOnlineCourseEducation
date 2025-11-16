package com.example.projectonlinecourseeducation.feature.student.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView.ScaleType;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.Course;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.data.CourseApi;
import com.example.projectonlinecourseeducation.data.CourseApi.Sort;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentCourseDetailActivity;
import com.example.projectonlinecourseeducation.feature.student.adapter.HomeCourseAdapter;

import java.util.Arrays;
import java.util.List;

public class StudentHomeFragment extends Fragment {

    private static final List<String> SLIDES = Arrays.asList(
            "https://csc.edu.vn/data/images/slider/lap-trinh/LTV-T3H.png",
            "https://letdiv.com/wp-content/uploads/2024/04/khoa-hoc-javascript.jpg",
            "https://s3-sgn09.fptcloud.com/codelearnstorage/files/thumbnails/csharp-co-ban_96ca03bee27f454eb1f1c86e1fc5ef74.png",
            "https://static-assets.codecademy.com/assets/course-landing-page/meta/16x9/learn-python-3.jpg",
            "https://media2.dev.to/dynamic/image/width=1600,height=900,fit=cover,gravity=auto,format=auto/https%3A%2F%2Fdev-to-uploads.s3.amazonaws.com%2Fuploads%2Farticles%2F7y634ebvmt66d75tdenh.png"
    );

    private ViewFlipper flipper;
    private Button btnCategory, btnFilter, btnLoadMore;
    private EditText edtSearch;
    private RecyclerView rv;
    private HomeCourseAdapter adapter;

    // DÙNG interface CourseApi, không phụ thuộc fake hay real
    private CourseApi api;

    private String currentCategory = "All";
    private Sort currentSort = Sort.AZ;
    private String currentQuery = "";
    private int currentLimit = 4; // hiển thị một số khóa học, bấm "Xem thêm" để tăng

    // tổng số khóa học sau khi filter + search
    private int totalMatched = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable flipRunnable = new Runnable() {
        @Override public void run() {
            if (flipper != null && flipper.getChildCount() > 0) {
                flipper.showNext();
                handler.postDelayed(this, 2000); // 2s
            }
        }
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_home, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        flipper = v.findViewById(R.id.viewFlipper);
        btnCategory = v.findViewById(R.id.btnCategory);
        btnFilter = v.findViewById(R.id.btnFilter);
        btnLoadMore = v.findViewById(R.id.btnLoadMore);
        edtSearch = v.findViewById(R.id.edtSearch);
        rv = v.findViewById(R.id.rvCourses);

        // Lấy implementation từ ApiProvider (hiện tại là FakeApi, sau này là RemoteApi)
        api = ApiProvider.getCourseApi();

        // setup flipper
        for (String url : SLIDES) {
            ImageView iv = new ImageView(requireContext());
            iv.setScaleType(ScaleType.CENTER_CROP);
            ImageLoader.getInstance().display(url, iv, R.drawable.ic_image_placeholder);
            flipper.addView(iv);
        }
        handler.postDelayed(flipRunnable, 4000);

        int span = getResources().getDisplayMetrics().widthPixels > 700 ? 2 : 1;
        rv.setLayoutManager(new GridLayoutManager(requireContext(), span));
        adapter = new HomeCourseAdapter();
        rv.setAdapter(adapter);

        // Khi click 1 course -> mở trang chi tiết đúng ID
        adapter.setOnCourseClickListener(course -> {
            Intent i = new Intent(requireContext(), StudentCourseDetailActivity.class);
            i.putExtra("course_id", course.getId());
            i.putExtra("course_title", course.getTitle());
            startActivity(i);
        });

        // Dữ liệu ban đầu
        applyQuery();

        // Search realtime
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                currentQuery = s.toString();
                currentLimit = 4;
                applyQuery();
            }
        });

        // Popup Category
        btnCategory.setOnClickListener(v1 -> {
            PopupMenu pm = new PopupMenu(requireContext(), btnCategory);
            pm.getMenu().add("All");
            // danh sách ngôn ngữ / nhãn bạn đã liệt kê
            pm.getMenu().add("Java");
            pm.getMenu().add("JavaScript");
            pm.getMenu().add("Python");
            pm.getMenu().add("C");
            pm.getMenu().add("C++");
            pm.getMenu().add("C#");
            pm.getMenu().add("PHP");
            pm.getMenu().add("SQL");
            pm.getMenu().add("HTML");
            pm.getMenu().add("CSS");
            pm.getMenu().add("TypeScript");
            pm.getMenu().add("Go");
            pm.getMenu().add("Kotlin");
            pm.getMenu().add("Backend");
            pm.getMenu().add("Frontend");
            pm.getMenu().add("Data / AI");
            pm.getMenu().add("Mobile");
            pm.getMenu().add("System");
            pm.getMenu().add("DevOps");
            pm.getMenu().add("Swift");
            pm.getMenu().add("Dart");
            pm.getMenu().add("Rust");
            pm.getMenu().add("Ruby");
            pm.getMenu().add("R");
            pm.getMenu().add("Lua");
            pm.getMenu().add("MATLAB");
            pm.getMenu().add("Scala");
            pm.getMenu().add("Shell / Bash");
            pm.getMenu().add("Haskell");
            pm.getMenu().add("Elixir");
            pm.getMenu().add("Perl");
            pm.setOnMenuItemClickListener(item -> {
                currentCategory = item.getTitle().toString();
                btnCategory.setText("Category: " + currentCategory);
                currentLimit = 4;
                applyQuery();
                return true;
            });
            pm.show();
        });

        // Popup Filter
        btnFilter.setOnClickListener(v12 -> {
            PopupMenu pm = new PopupMenu(requireContext(), btnFilter);
            pm.getMenu().add("A-Z");
            pm.getMenu().add("Z-A");
            pm.getMenu().add("Rating ↓");
            pm.getMenu().add("Rating ↑");
            pm.setOnMenuItemClickListener(item -> {
                String t = item.getTitle().toString();
                if (t.equals("A-Z")) {
                    currentSort = Sort.AZ;
                } else if (t.equals("Z-A")) {
                    currentSort = Sort.ZA;
                } else if (t.contains("↑")) {
                    currentSort = Sort.RATING_UP;
                } else if (t.contains("↓")) {
                    currentSort = Sort.RATING_DOWN;
                } else {
                    currentSort = Sort.AZ;
                }
                btnFilter.setText("Filter: " + t);
                currentLimit = 4;
                applyQuery();
                return true;
            });
            pm.show();
        });

        // Load more: Xem thêm / Rút gọn
        btnLoadMore.setOnClickListener(v13 -> {
            if (totalMatched <= 4) return;

            if (currentLimit >= totalMatched) {
                // đang xem hết -> rút gọn về 4
                currentLimit = 4;
            } else {
                // xem thêm 4
                currentLimit += 4;
                if (currentLimit > totalMatched) currentLimit = totalMatched;
            }
            applyQuery();
        });
    }

    private void applyQuery() {
        // Lấy full list theo filter + search, không giới hạn
        List<Course> all = api.filterSearchSort(currentCategory, currentQuery, currentSort, 0);
        totalMatched = all.size();

        if (currentLimit <= 0) currentLimit = 4;
        if (totalMatched == 0) {
            adapter.submitList(all);
            updateLoadMoreButton();
            return;
        }

        if (currentLimit > totalMatched) currentLimit = totalMatched;

        List<Course> out;
        if (currentLimit < totalMatched) {
            out = all.subList(0, currentLimit);
        } else {
            out = all;
        }

        adapter.submitList(out);
        updateLoadMoreButton();
    }

    private void updateLoadMoreButton() {
        if (btnLoadMore == null) return;

        if (totalMatched <= 4) {
            // ít hoặc bằng 4 khóa -> ẩn nút
            btnLoadMore.setVisibility(View.GONE);
            return;
        }

        btnLoadMore.setVisibility(View.VISIBLE);

        if (currentLimit >= totalMatched) {
            // đang xem hết -> cho phép Rút gọn
            btnLoadMore.setText("Rút gọn");
            btnLoadMore.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.purple_400)
            );
        } else {
            // còn có thể xem thêm
            btnLoadMore.setText("Xem thêm");
            btnLoadMore.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSecondary)
            );
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(flipRunnable);
        flipper = null;
    }
}
