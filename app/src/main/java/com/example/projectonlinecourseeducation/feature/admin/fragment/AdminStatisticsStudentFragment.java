package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.utils.AsyncApiHelper;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.course.CourseApi;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Fragment thống kê học viên - phân tích lượt mua và doanh thu
 */
public class AdminStatisticsStudentFragment extends Fragment {

    private TextView tvTotalPurchases, tvAvgRevenuePerPurchase, tvMostPopularCount, tvTotalCourses, tvHighestRevenue;
    private HorizontalBarChart horizontalBarChart;
    private PieChart pieChartRevenue;

    private CourseApi courseApi;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_statistics_student, container, false);

        initViews(view);
        initApis();
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvTotalPurchases = view.findViewById(R.id.tvTotalPurchases);
        tvAvgRevenuePerPurchase = view.findViewById(R.id.tvAvgRevenuePerPurchase);
        tvMostPopularCount = view.findViewById(R.id.tvMostPopularCount);
        tvTotalCourses = view.findViewById(R.id.tvTotalCourses);
        tvHighestRevenue = view.findViewById(R.id.tvHighestRevenue);
        horizontalBarChart = view.findViewById(R.id.horizontalBarChart);
        pieChartRevenue = view.findViewById(R.id.pieChartRevenue);
    }

    private void initApis() {
        courseApi = ApiProvider.getCourseApi();
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

                    int totalPurchases = 0;
                    double totalRevenue = 0;
                    int maxStudents = 0;
                    double highestRevenue = 0;

                    for (Course course : approvedCourses) {
                        int students = course.getStudents();
                        double revenue = course.getPrice() * students;

                        totalPurchases += students;
                        totalRevenue += revenue;

                        if (students > maxStudents) {
                            maxStudents = students;
                        }

                        if (revenue > highestRevenue) {
                            highestRevenue = revenue;
                        }
                    }

                    double avgRevenuePerPurchase =
                            totalPurchases > 0 ? totalRevenue / totalPurchases : 0;

                    // Sort by students DESC
                    Collections.sort(approvedCourses,
                            (a, b) -> Integer.compare(b.getStudents(), a.getStudents()));

                    List<Course> top10 = approvedCourses.size() > 10
                            ? new ArrayList<>(approvedCourses.subList(0, 10))
                            : new ArrayList<>(approvedCourses);

                    List<Course> top5 = approvedCourses.size() > 5
                            ? new ArrayList<>(approvedCourses.subList(0, 5))
                            : new ArrayList<>(approvedCourses);

                    return new StudentStatResult(
                            totalPurchases,
                            avgRevenuePerPurchase,
                            maxStudents,
                            approvedCourses.size(),
                            highestRevenue,
                            top10,
                            top5
                    );
                },
                new AsyncApiHelper.ApiCallback<StudentStatResult>() {
                    @Override
                    public void onSuccess(StudentStatResult result) {
                        // ===== MAIN THREAD =====

                        displayStatistics(
                                result.totalPurchases,
                                result.avgRevenuePerPurchase,
                                result.mostPopularCount,
                                result.totalCourses,
                                result.highestRevenue
                        );

                        setupHorizontalBarChart(result.top10);
                        setupPieChart(result.top5);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }


    static class StudentStatResult {
        int totalPurchases;
        double avgRevenuePerPurchase;
        int mostPopularCount;
        int totalCourses;
        double highestRevenue;
        List<Course> top10;
        List<Course> top5;

        StudentStatResult(int totalPurchases,
                          double avgRevenuePerPurchase,
                          int mostPopularCount,
                          int totalCourses,
                          double highestRevenue,
                          List<Course> top10,
                          List<Course> top5) {
            this.totalPurchases = totalPurchases;
            this.avgRevenuePerPurchase = avgRevenuePerPurchase;
            this.mostPopularCount = mostPopularCount;
            this.totalCourses = totalCourses;
            this.highestRevenue = highestRevenue;
            this.top10 = top10;
            this.top5 = top5;
        }
    }


    private void displayStatistics(int totalPurchases, double avgRevenue, int mostPopular,
                                    int totalCourses, double highestRevenue) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvTotalPurchases.setText(String.valueOf(totalPurchases));
        tvAvgRevenuePerPurchase.setText(formatter.format(avgRevenue / 1000) + "K");
        tvMostPopularCount.setText(String.valueOf(mostPopular));
        tvTotalCourses.setText(String.valueOf(totalCourses));
        tvHighestRevenue.setText(formatter.format(highestRevenue / 1000000) + "M");
    }

    private void setupHorizontalBarChart(List<Course> topCourses) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < topCourses.size(); i++) {
            Course course = topCourses.get(i);
            entries.add(new BarEntry(i, course.getStudents()));
            String shortName = course.getTitle().length() > 15
                    ? course.getTitle().substring(0, 15) + "..."
                    : course.getTitle();
            labels.add(shortName);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        int[] colors = new int[]{
                Color.rgb(64, 89, 255), Color.rgb(255, 87, 34), Color.rgb(76, 175, 80),
                Color.rgb(233, 30, 99), Color.rgb(255, 193, 7), Color.rgb(33, 150, 243),
                Color.rgb(156, 39, 176), Color.rgb(0, 150, 136), Color.rgb(255, 152, 0),
                Color.rgb(121, 85, 72)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        horizontalBarChart.setData(data);
        horizontalBarChart.setFitBars(true);

        // Y-axis (course names)
        YAxis leftAxis = horizontalBarChart.getAxisLeft();
        leftAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(9f);
        leftAxis.setDrawGridLines(false);

        horizontalBarChart.getAxisRight().setEnabled(false);

        // X-axis (values)
        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Description desc = new Description();
        desc.setText("");
        horizontalBarChart.setDescription(desc);

        Legend legend = horizontalBarChart.getLegend();
        legend.setEnabled(false);

        horizontalBarChart.animateX(1000);
        horizontalBarChart.invalidate();
    }

    private void setupPieChart(List<Course> topCourses) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        int[] materialColors = new int[]{
                Color.rgb(255, 87, 34),   // Orange
                Color.rgb(76, 175, 80),   // Green
                Color.rgb(233, 30, 99),   // Pink
                Color.rgb(255, 193, 7),   // Amber
                Color.rgb(64, 89, 255)    // Blue
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
        pieChartRevenue.setHoleRadius(35f);
        pieChartRevenue.setTransparentCircleRadius(40f);
        pieChartRevenue.setEntryLabelTextSize(9f);

        Description desc = new Description();
        desc.setText("");
        pieChartRevenue.setDescription(desc);

        Legend legend = pieChartRevenue.getLegend();
        legend.setEnabled(false);

        pieChartRevenue.animateY(1000);
        pieChartRevenue.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
