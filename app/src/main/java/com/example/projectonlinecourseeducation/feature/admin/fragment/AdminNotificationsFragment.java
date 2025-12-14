package com.example.projectonlinecourseeducation.feature.admin.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.projectonlinecourseeducation.R;

/**
 * Fragment thông báo
 */
public class AdminNotificationsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Admin không sử dụng tính năng thông báo
        return inflater.inflate(R.layout.fragment_admin_notifications, container, false);
    }

}
