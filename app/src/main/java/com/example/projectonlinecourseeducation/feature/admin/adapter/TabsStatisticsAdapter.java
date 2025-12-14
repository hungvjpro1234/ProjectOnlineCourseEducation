package com.example.projectonlinecourseeducation.feature.admin.adapter;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminStatisticsCourseFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminStatisticsStudentFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminStatisticsTeacherFragment;

/**
 * Adapter cho ViewPager2 trong Statistics Fragment
 * Quản lý 3 tab: Khóa học, Học viên, Giảng viên
 */
public class TabsStatisticsAdapter extends FragmentStateAdapter {

    public TabsStatisticsAdapter(Fragment fragment) {
        super(fragment);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminStatisticsCourseFragment();
            case 1:
                return new AdminStatisticsStudentFragment();
            case 2:
                return new AdminStatisticsTeacherFragment();
            default:
                return new AdminStatisticsCourseFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
