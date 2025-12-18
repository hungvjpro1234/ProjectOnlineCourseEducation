package com.example.projectonlinecourseeducation.core.utils.datahelper;

import java.util.Map;

public final class TeacherIdHelper {

    private TeacherIdHelper() {
        // prevent instantiation
    }

    public static final Map<String, String> TEACHER_NAME_TO_ID = Map.of(
            "Nguyễn A", "u2",
            "Teacher Assistant", "u5",
            "Teacher Three", "u10",
            "Teacher Four", "u11",
            "Teacher Five", "u12"
    );
}
