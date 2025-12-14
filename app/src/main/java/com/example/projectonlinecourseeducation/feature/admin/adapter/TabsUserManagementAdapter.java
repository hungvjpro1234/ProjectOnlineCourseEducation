package com.example.projectonlinecourseeducation.feature.admin.adapter;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminUserManagementStudentFragment;
import com.example.projectonlinecourseeducation.feature.admin.fragment.AdminUserManagementTeacherFragment;

/**
 * Adapter cho ViewPager2 trong User Management Fragment
 * Quản lý 2 tab: Học viên, Giảng viên
 */
public class TabsUserManagementAdapter extends FragmentStateAdapter {

    public TabsUserManagementAdapter(Fragment fragment) {
        super(fragment);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminUserManagementStudentFragment();
            case 1:
                return new AdminUserManagementTeacherFragment();
            default:
                return new AdminUserManagementStudentFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
