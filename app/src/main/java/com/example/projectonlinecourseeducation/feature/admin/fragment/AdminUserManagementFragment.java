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
import com.example.projectonlinecourseeducation.feature.admin.adapter.TabsUserManagementAdapter;

/**
 * Fragment quản lý user với 2 tab: Học viên, Giảng viên
 */
public class AdminUserManagementFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabsUserManagementAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage_user, container, false);

        initViews(view);
        setupAdapter();
        setupTabLayout();

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayoutUserManagement);
        viewPager = view.findViewById(R.id.viewPagerUserManagement);
    }

    private void setupAdapter() {
        adapter = new TabsUserManagementAdapter(this);
        viewPager.setAdapter(adapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Học viên");
                    break;
                case 1:
                    tab.setText("Giảng viên");
                    break;
            }
        }).attach();
    }
}
