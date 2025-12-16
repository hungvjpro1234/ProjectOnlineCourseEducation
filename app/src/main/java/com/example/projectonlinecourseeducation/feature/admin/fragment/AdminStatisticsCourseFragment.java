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
import com.example.projectonlinecourseeducation.feature.admin.adapter.AdminTopCourseStatAdapter;
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
import java.util.List;
import java.util.Locale;

/**
 * Fragment thống kê khóa học - ưu tiên doanh thu
 */
public class AdminStatisticsCourseFragment extends Fragment {

    // UI Components
    private TextView tvTotalRevenue, tvTotalTransactions, tvTotalCourses, tvTotalEnrollments, tvAveragePrice;
    private PieChart pieChartRevenue;
    private BarChart barChartEnrollments;
    private RecyclerView rvTopCourses;

    // Data
    private CourseApi courseApi;
    private AdminTopCourseStatAdapter topCoursesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_statistics_course, container, false);

        initViews(view);
        initApis();
        setupRecyclerView();
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalTransactions = view.findViewById(R.id.tvTotalTransactions);
        tvTotalCourses = view.findViewById(R.id.tvTotalCourses);
        tvTotalEnrollments = view.findViewById(R.id.tvTotalEnrollments);
        tvAveragePrice = view.findViewById(R.id.tvAveragePrice);
        pieChartRevenue = view.findViewById(R.id.pieChartRevenue);
        barChartEnrollments = view.findViewById(R.id.barChartEnrollments);
        rvTopCourses = view.findViewById(R.id.rvTopCourses);
    }

    private void initApis() {
        courseApi = ApiProvider.getCourseApi();
    }

    private void setupRecyclerView() {
        topCoursesAdapter = new AdminTopCourseStatAdapter();
        rvTopCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTopCourses.setAdapter(topCoursesAdapter);
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

                    double totalRevenue = 0;
                    int totalEnrollments = 0;
                    double totalPrice = 0;

                    for (Course course : approvedCourses) {
                        totalRevenue += course.getPrice() * course.getStudents();
                        totalEnrollments += course.getStudents();
                        totalPrice += course.getPrice();
                    }

                    double avgPrice = approvedCourses.isEmpty()
                            ? 0
                            : totalPrice / approvedCourses.size();

                    // Sort by revenue DESC
                    Collections.sort(approvedCourses, (a, b) -> {
                        double ra = a.getPrice() * a.getStudents();
                        double rb = b.getPrice() * b.getStudents();
                        return Double.compare(rb, ra);
                    });

                    List<Course> top5 = approvedCourses.size() > 5
                            ? new ArrayList<>(approvedCourses.subList(0, 5))
                            : new ArrayList<>(approvedCourses);

                    List<Course> top10 = approvedCourses.size() > 10
                            ? new ArrayList<>(approvedCourses.subList(0, 10))
                            : new ArrayList<>(approvedCourses);

                    return new CourseStatResult(
                            approvedCourses.size(),
                            totalRevenue,
                            totalEnrollments,
                            avgPrice,
                            top5,
                            top10
                    );
                },
                new AsyncApiHelper.ApiCallback<CourseStatResult>() {
                    @Override
                    public void onSuccess(CourseStatResult result) {
                        // ===== MAIN THREAD =====

                        displayStatistics(
                                result.totalCourses,
                                result.totalRevenue,
                                result.totalEnrollments,
                                result.avgPrice
                        );

                        setupPieChart(result.top5);
                        setupBarChart(result.top10);
                        topCoursesAdapter.setCourses(result.top5);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    static class CourseStatResult {
        int totalCourses;
        double totalRevenue;
        int totalEnrollments;
        double avgPrice;
        List<Course> top5;
        List<Course> top10;

        CourseStatResult(int totalCourses,
                         double totalRevenue,
                         int totalEnrollments,
                         double avgPrice,
                         List<Course> top5,
                         List<Course> top10) {
            this.totalCourses = totalCourses;
            this.totalRevenue = totalRevenue;
            this.totalEnrollments = totalEnrollments;
            this.avgPrice = avgPrice;
            this.top5 = top5;
            this.top10 = top10;
        }
    }


    private void displayStatistics(int totalCourses, double totalRevenue, int totalEnrollments, double avgPrice) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvTotalCourses.setText(String.valueOf(totalCourses));
        tvTotalEnrollments.setText(String.valueOf(totalEnrollments));
        tvTotalRevenue.setText(formatter.format(totalRevenue) + " VNĐ");
        tvTotalTransactions.setText("Từ " + totalEnrollments + " giao dịch");
        tvAveragePrice.setText(formatter.format(avgPrice / 1000) + "K");
    }

    private void setupPieChart(List<Course> topCourses) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        int[] materialColors = new int[]{
                Color.rgb(64, 89, 255),   // Blue
                Color.rgb(255, 87, 34),   // Orange
                Color.rgb(76, 175, 80),   // Green
                Color.rgb(233, 30, 99),   // Pink
                Color.rgb(255, 193, 7)    // Amber
        };

        for (int i = 0; i < topCourses.size(); i++) {
            Course course = topCourses.get(i);
            float revenue = (float) (course.getPrice() * course.getStudents());
            entries.add(new PieEntry(revenue, course.getTitle()));
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

        pieChartRevenue.setData(data);
        pieChartRevenue.setDrawHoleEnabled(true);
        pieChartRevenue.setHoleRadius(40f);
        pieChartRevenue.setTransparentCircleRadius(45f);
        pieChartRevenue.setEntryLabelColor(Color.BLACK);
        pieChartRevenue.setEntryLabelTextSize(10f);

        Description desc = new Description();
        desc.setText("");
        pieChartRevenue.setDescription(desc);

        Legend legend = pieChartRevenue.getLegend();
        legend.setEnabled(false);

        pieChartRevenue.animateY(1000);
        pieChartRevenue.invalidate();
    }

    private void setupBarChart(List<Course> topCourses) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < topCourses.size(); i++) {
            Course course = topCourses.get(i);
            entries.add(new BarEntry(i, course.getStudents()));
            // Shorten course name for X-axis
            String shortName = course.getTitle().length() > 10
                    ? course.getTitle().substring(0, 10) + "..."
                    : course.getTitle();
            labels.add(shortName);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Số học viên");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        barChartEnrollments.setData(data);
        barChartEnrollments.setFitBars(true);

        // X-axis
        XAxis xAxis = barChartEnrollments.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(9f);

        // Y-axis
        barChartEnrollments.getAxisLeft().setGranularity(1f);
        barChartEnrollments.getAxisRight().setEnabled(false);

        Description desc = new Description();
        desc.setText("");
        barChartEnrollments.setDescription(desc);

        Legend legend = barChartEnrollments.getLegend();
        legend.setEnabled(false);

        barChartEnrollments.animateY(1000);
        barChartEnrollments.invalidate();
    }
}
