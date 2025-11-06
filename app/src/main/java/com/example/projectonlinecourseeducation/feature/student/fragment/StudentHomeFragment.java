// app/src/main/java/com/example/projectonlinecourseeducation/feature/student/fragment/StudentHomeFragment.java
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView.ScaleType;
import com.example.projectonlinecourseeducation.core.utils.ImageLoader;
import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.feature.student.activity.StudentCourseDetailActivity;
import com.example.projectonlinecourseeducation.feature.student.adapter.CourseAdapter;
import com.example.projectonlinecourseeducation.data.CourseFakeApiService;
import com.example.projectonlinecourseeducation.data.CourseFakeApiService.Sort;
import com.example.projectonlinecourseeducation.core.model.Course;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private CourseAdapter adapter;
    private CourseFakeApiService api;

    private String currentCategory = "All";
    private Sort currentSort = Sort.AZ;
    private String currentQuery = "";
    private int currentLimit = 4; // hiển thị một số khóa học, bấm "Xem thêm" để tăng

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

        api = CourseFakeApiService.getInstance();

        // setup flipper
        for (String url : SLIDES) {
            ImageView iv = new ImageView(requireContext());
            iv.setScaleType(ScaleType.CENTER_CROP);
            // dùng ImageLoader để nạp ảnh từ URL
            ImageLoader.getInstance().display(url, iv, R.drawable.ic_image_placeholder);
            flipper.addView(iv);
        }
        handler.postDelayed(flipRunnable, 4000);

        // ✅ THÊM ĐOẠN NÀY NGAY SAU PHẦN SETUP FLIPPER
        int span = getResources().getDisplayMetrics().widthPixels > 700 ? 2 : 1;
        rv.setLayoutManager(new GridLayoutManager(requireContext(), span));
        adapter = new CourseAdapter();
        rv.setAdapter(adapter);

        // Khi click 1 course -> mở trang chi tiết
        adapter.setOnCourseClickListener(course -> {
            Intent i = new Intent(requireContext(), StudentCourseDetailActivity.class);
            i.putExtra("course_id", course.getId());
            i.putExtra("course_title", course.getTitle());
            startActivity(i);
        });

        // Gọi dữ liệu ban đầu
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
            pm.getMenu().add("Java");
            pm.getMenu().add("C");
            pm.getMenu().add("C++");
            pm.getMenu().add("Python");
            pm.setOnMenuItemClickListener(item -> {
                currentCategory = item.getTitle().toString();
                btnCategory.setText("Category: " + currentCategory);
                currentLimit = 4;
                applyQuery();
                return true;
            });
            pm.show();
        });

        // Popup Filter (SỬA)
        btnFilter.setOnClickListener(v12 -> {
            PopupMenu pm = new PopupMenu(requireContext(), btnFilter);
            pm.getMenu().add("A-Z");
            pm.getMenu().add("Z-A");
            pm.getMenu().add("Rating ↓"); // giảm dần
            pm.getMenu().add("Rating ↑"); // tăng dần
            pm.setOnMenuItemClickListener(item -> {
                String t = item.getTitle().toString();
                if (t.equals("A-Z")) {
                    currentSort = Sort.AZ;
                } else if (t.equals("Z-A")) {
                    currentSort = Sort.ZA;
                } else if (t.contains("↑")) {
                    currentSort = Sort.RATING_UP;      // ✅ tăng dần
                } else if (t.contains("↓")) {
                    currentSort = Sort.RATING_DOWN;    // ✅ giảm dần
                } else {
                    currentSort = Sort.AZ; // fallback
                }
                btnFilter.setText("Filter: " + t);
                currentLimit = 4;
                applyQuery();
                return true;
            });
            pm.show();
        });


        // Load more
        btnLoadMore.setOnClickListener(v13 -> {
            currentLimit += 4;
            applyQuery();
        });
    }

    private void applyQuery() {
        List<Course> out = api.filterSearchSort(currentCategory, currentQuery, currentSort, currentLimit);
        adapter.submitList(out);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(flipRunnable);
        flipper = null;
    }
}
