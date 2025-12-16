package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminCourseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment quản lý khóa học (Admin)
 *
 * - Lấy dữ liệu từ ApiProvider.getCourseApi()
 * - Hỗ trợ lọc theo category, tìm kiếm tên/giảng viên, sort
 * - Lắng nghe CourseUpdateListener để tự refresh khi có thay đổi
 *
 * Giữ comment, thêm comment nơi cần thiết.
 */
public class AdminCourseManagementFragment extends Fragment {

    private RecyclerView rvCourses;
    private EditText etSearch;
    private Spinner spinnerCategory, spinnerSort;
    private Button btnClear;
    private TextView tvEmpty;
    private AdminCourseAdapter adapter;
    private List<Course> courseList = new ArrayList<>();

    // Categories (theo yêu cầu)
    private static final List<String> ALL_CATS = Arrays.asList(
            "All",
            "Java","JavaScript","Python","C","C++","C#","PHP","SQL","HTML","CSS","TypeScript","Go","Kotlin",
            "Backend","Frontend","Data / AI","Mobile","System","DevOps","Swift","Dart","Rust","Ruby","R",
            "Lua","MATLAB","Scala","Shell / Bash","Haskell","Elixir","Perl"
    );

    // Sort labels and mapping to CourseApi.Sort
    private static final String[] SORT_LABELS = new String[] { "AZ", "ZA", "Rating ↑", "Rating ↓" };
    private static final CourseApi.Sort[] SORT_VALUES = new CourseApi.Sort[] {
            CourseApi.Sort.AZ, CourseApi.Sort.ZA, CourseApi.Sort.RATING_UP, CourseApi.Sort.RATING_DOWN
    };

    private CourseApi courseApi;
    private final CourseApi.CourseUpdateListener updateListener = new CourseApi.CourseUpdateListener() {
        @Override
        public void onCourseUpdated(String courseId, Course updatedCourse) {
            // refresh list using current filters when any course changed/removed/added
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> applyFilters());
        }
    };

    public AdminCourseManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_admin_course_management, container, false);

        rvCourses = root.findViewById(R.id.rv_courses);
        etSearch = root.findViewById(R.id.et_search);
        spinnerCategory = root.findViewById(R.id.spinner_category);
        spinnerSort = root.findViewById(R.id.spinner_sort);
        btnClear = root.findViewById(R.id.btn_clear_filters);
        tvEmpty = root.findViewById(R.id.tv_empty);

        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));

        courseApi = ApiProvider.getCourseApi();

        adapter = new AdminCourseAdapter(getContext(), courseList, (courseId, position) -> {
            // remove locally (adapter expects external update)
            // reload list applying current filters
            applyFilters();
        });

        rvCourses.setAdapter(adapter);

        setupSpinners();
        setupSearch();
        btnClear.setOnClickListener(v -> {
            spinnerCategory.setSelection(0);
            spinnerSort.setSelection(0);
            etSearch.setText("");
            applyFilters();
        });

        // register listener for live updates
        if (courseApi != null) {
            courseApi.addCourseUpdateListener(updateListener);
        }

        // initial load
        applyFilters();

        return root;
    }

    private void setupSpinners() {
        // category spinner
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ALL_CATS);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // sort spinner
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, SORT_LABELS);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(0);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // live search
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        if (courseApi == null) {
            courseList.clear();
            refreshList();
            return;
        }

        final String selectedCat = (String) spinnerCategory.getSelectedItem();
        final String query = etSearch.getText() == null
                ? ""
                : etSearch.getText().toString().toLowerCase().trim();
        final CourseApi.Sort sort =
                SORT_VALUES[Math.max(0, spinnerSort.getSelectedItemPosition())];

        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====

                    List<Course> allCourses = courseApi.listAll();
                    List<Course> filtered = new ArrayList<>();

                    for (Course c : allCourses) {
                        // Category filter
                        if (!"All".equalsIgnoreCase(selectedCat)) {
                            if (c.getCategory() == null || !c.getCategory().contains(selectedCat)) {
                                continue;
                            }
                        }

                        // Search filter
                        if (!query.isEmpty()) {
                            String title = c.getTitle() == null ? "" : c.getTitle().toLowerCase();
                            String teacher = c.getTeacher() == null ? "" : c.getTeacher().toLowerCase();
                            if (!title.contains(query) && !teacher.contains(query)) {
                                continue;
                            }
                        }

                        filtered.add(c);
                    }

                    // Sort
                    if (sort == CourseApi.Sort.AZ) {
                        filtered.sort((a, b) -> {
                            String ta = a.getTitle() == null ? "" : a.getTitle();
                            String tb = b.getTitle() == null ? "" : b.getTitle();
                            return ta.compareToIgnoreCase(tb);
                        });
                    } else if (sort == CourseApi.Sort.ZA) {
                        filtered.sort((a, b) -> {
                            String ta = a.getTitle() == null ? "" : a.getTitle();
                            String tb = b.getTitle() == null ? "" : b.getTitle();
                            return tb.compareToIgnoreCase(ta);
                        });
                    } else if (sort == CourseApi.Sort.RATING_UP) {
                        filtered.sort((a, b) -> Double.compare(a.getRating(), b.getRating()));
                    } else if (sort == CourseApi.Sort.RATING_DOWN) {
                        filtered.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
                    }

                    return filtered;
                },
                new AsyncApiHelper.ApiCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> filtered) {
                        courseList.clear();
                        courseList.addAll(filtered);
                        refreshList();
                    }

                    @Override
                    public void onError(Exception e) {
                        courseList.clear();
                        refreshList();
                    }
                }
        );
    }


    private void refreshList() {
        if (courseList.isEmpty()) {
            rvCourses.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvCourses.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (courseApi != null) {
            courseApi.removeCourseUpdateListener(updateListener);
        }
    }
}
