package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminTopTeacherStatAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment thống kê giảng viên - ưu tiên doanh thu theo teacher
 */
public class AdminStatisticsTeacherFragment extends Fragment {

    private TextView tvTotalTeacherRevenue, tvTotalTeachers, tvTotalCourses, tvAvgCoursesPerTeacher, tvAvgRevenuePerTeacher;
    private PieChart pieChartTeacherRevenue;
    private BarChart barChartCoursesByTeacher;
    private RecyclerView rvTopTeachers;

    private CourseApi courseApi;
    private AdminTopTeacherStatAdapter topTeachersAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_statistics_teacher, container, false);

        initViews(view);
        initApis();
        setupRecyclerView();
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvTotalTeacherRevenue = view.findViewById(R.id.tvTotalTeacherRevenue);
        tvTotalTeachers = view.findViewById(R.id.tvTotalTeachers);
        tvTotalCourses = view.findViewById(R.id.tvTotalCourses);
        tvAvgCoursesPerTeacher = view.findViewById(R.id.tvAvgCoursesPerTeacher);
        tvAvgRevenuePerTeacher = view.findViewById(R.id.tvAvgRevenuePerTeacher);
        pieChartTeacherRevenue = view.findViewById(R.id.pieChartTeacherRevenue);
        barChartCoursesByTeacher = view.findViewById(R.id.barChartCoursesByTeacher);
        rvTopTeachers = view.findViewById(R.id.rvTopTeachers);
    }

    private void initApis() {
        courseApi = ApiProvider.getCourseApi();
    }

    private void setupRecyclerView() {
        topTeachersAdapter = new AdminTopTeacherStatAdapter();
        rvTopTeachers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTopTeachers.setAdapter(topTeachersAdapter);
    }

    private void loadStatistics() {
        AsyncApiHelper.execute(
                () -> {
                    // ===== BACKGROUND THREAD =====

                    List<Course> allCourses = courseApi.listAll();
                    List<Course> approvedCourses = new ArrayList<>();

                    for (Course course : allCourses) {
                        if (course.isInitialApproved() && !course.isDeleteRequested()) {
                            approvedCourses.add(course);
                        }
                    }

                    Map<String, AdminTopTeacherStatAdapter.TeacherStats> teacherStatsMap = new HashMap<>();

                    for (Course course : approvedCourses) {
                        String teacherName = course.getTeacher() != null
                                ? course.getTeacher()
                                : "Unknown";

                        AdminTopTeacherStatAdapter.TeacherStats stats =
                                teacherStatsMap.get(teacherName);

                        if (stats == null) {
                            stats = new AdminTopTeacherStatAdapter.TeacherStats(teacherName);
                            teacherStatsMap.put(teacherName, stats);
                        }

                        stats.courseCount++;
                        stats.totalRevenue += course.getPrice() * course.getStudents();
                        stats.totalStudents += course.getStudents();
                    }

                    List<AdminTopTeacherStatAdapter.TeacherStats> teacherStatsList =
                            new ArrayList<>(teacherStatsMap.values());

                    Collections.sort(teacherStatsList,
                            (a, b) -> Double.compare(b.totalRevenue, a.totalRevenue));

                    double totalRevenue = 0;
                    int totalCourses = approvedCourses.size();
                    int totalTeachers = teacherStatsList.size();

                    for (AdminTopTeacherStatAdapter.TeacherStats stats : teacherStatsList) {
                        totalRevenue += stats.totalRevenue;
                    }

                    double avgCoursesPerTeacher =
                            totalTeachers > 0 ? (double) totalCourses / totalTeachers : 0;

                    double avgRevenuePerTeacher =
                            totalTeachers > 0 ? totalRevenue / totalTeachers : 0;

                    List<AdminTopTeacherStatAdapter.TeacherStats> top5 =
                            teacherStatsList.size() > 5
                                    ? new ArrayList<>(teacherStatsList.subList(0, 5))
                                    : new ArrayList<>(teacherStatsList);

                    return new TeacherStatResult(
                            totalRevenue,
                            totalTeachers,
                            totalCourses,
                            avgCoursesPerTeacher,
                            avgRevenuePerTeacher,
                            top5
                    );
                },
                new AsyncApiHelper.ApiCallback<TeacherStatResult>() {
                    @Override
                    public void onSuccess(TeacherStatResult result) {
                        // ===== MAIN THREAD =====

                        displayStatistics(
                                result.totalRevenue,
                                result.totalTeachers,
                                result.totalCourses,
                                result.avgCoursesPerTeacher,
                                result.avgRevenuePerTeacher
                        );

                        setupPieChart(result.topTeachers);
                        setupBarChart(result.topTeachers);
                        topTeachersAdapter.setTeachers(result.topTeachers);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    static class TeacherStatResult {
        double totalRevenue;
        int totalTeachers;
        int totalCourses;
        double avgCoursesPerTeacher;
        double avgRevenuePerTeacher;
        List<AdminTopTeacherStatAdapter.TeacherStats> topTeachers;

        TeacherStatResult(double totalRevenue,
                          int totalTeachers,
                          int totalCourses,
                          double avgCoursesPerTeacher,
                          double avgRevenuePerTeacher,
                          List<AdminTopTeacherStatAdapter.TeacherStats> topTeachers) {
            this.totalRevenue = totalRevenue;
            this.totalTeachers = totalTeachers;
            this.totalCourses = totalCourses;
            this.avgCoursesPerTeacher = avgCoursesPerTeacher;
            this.avgRevenuePerTeacher = avgRevenuePerTeacher;
            this.topTeachers = topTeachers;
        }
    }


    private void displayStatistics(double totalRevenue, int totalTeachers, int totalCourses,
                                    double avgCourses, double avgRevenue) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvTotalTeacherRevenue.setText(formatter.format(totalRevenue) + " VNĐ");
        tvTotalTeachers.setText("Từ " + totalTeachers + " giảng viên");
        tvTotalCourses.setText(String.valueOf(totalCourses));
        tvAvgCoursesPerTeacher.setText(String.format(Locale.getDefault(), "%.1f", avgCourses));
        tvAvgRevenuePerTeacher.setText(formatter.format(avgRevenue / 1000000) + "M");
    }

    private void setupPieChart(List<AdminTopTeacherStatAdapter.TeacherStats> topTeachers) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        int[] materialColors = new int[]{
                Color.rgb(156, 39, 176),  // Purple
                Color.rgb(233, 30, 99),   // Pink
                Color.rgb(255, 87, 34),   // Orange
                Color.rgb(76, 175, 80),   // Green
                Color.rgb(64, 89, 255)    // Blue
        };

        for (int i = 0; i < topTeachers.size(); i++) {
            AdminTopTeacherStatAdapter.TeacherStats stats = topTeachers.get(i);
            entries.add(new PieEntry((float) stats.totalRevenue, stats.teacherName));
            colors.add(materialColors[i % materialColors.length]);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return NumberFormat.getInstance(new Locale("vi", "VN")).format(value / 1000000) + "M";
            }
        });

        PieData data = new PieData(dataSet);

        pieChartTeacherRevenue.setData(data);
        pieChartTeacherRevenue.setDrawHoleEnabled(true);
        pieChartTeacherRevenue.setHoleRadius(40f);
        pieChartTeacherRevenue.setTransparentCircleRadius(45f);
        pieChartTeacherRevenue.setEntryLabelTextSize(10f);

        Description desc = new Description();
        desc.setText("");
        pieChartTeacherRevenue.setDescription(desc);

        Legend legend = pieChartTeacherRevenue.getLegend();
        legend.setEnabled(false);

        pieChartTeacherRevenue.animateY(1000);
        pieChartTeacherRevenue.invalidate();
    }

    private void setupBarChart(List<AdminTopTeacherStatAdapter.TeacherStats> topTeachers) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < topTeachers.size(); i++) {
            AdminTopTeacherStatAdapter.TeacherStats stats = topTeachers.get(i);
            entries.add(new BarEntry(i, stats.courseCount));
            String shortName = stats.teacherName.length() > 12
                    ? stats.teacherName.substring(0, 12) + "..."
                    : stats.teacherName;
            labels.add(shortName);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Số khóa học");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        barChartCoursesByTeacher.setData(data);
        barChartCoursesByTeacher.setFitBars(true);

        // X-axis
        XAxis xAxis = barChartCoursesByTeacher.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(9f);

        // Y-axis
        barChartCoursesByTeacher.getAxisLeft().setGranularity(1f);
        barChartCoursesByTeacher.getAxisRight().setEnabled(false);

        Description desc = new Description();
        desc.setText("");
        barChartCoursesByTeacher.setDescription(desc);

        Legend legend = barChartCoursesByTeacher.getLegend();
        legend.setEnabled(false);

        barChartCoursesByTeacher.animateY(1000);
        barChartCoursesByTeacher.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
