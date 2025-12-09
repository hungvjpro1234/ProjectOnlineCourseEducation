package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.feature.admin.adapter.TabsStatisticsAdapter;

/**
 * Fragment thống kê với 3 tab: Khóa học, Học viên, Giảng viên
 */
public class AdminStatisticFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabsStatisticsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_statistics, container, false);

        initViews(view);
        setupAdapter();
        setupTabLayout();

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayoutStatistics);
        viewPager = view.findViewById(R.id.viewPagerStatistics);
    }

    private void setupAdapter() {
        adapter = new TabsStatisticsAdapter(this);
        viewPager.setAdapter(adapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Khóa học");
                    break;
                case 1:
                    tab.setText("Học viên");
                    break;
                case 2:
                    tab.setText("Giảng viên");
                    break;
            }
        }).attach();
    }
}
