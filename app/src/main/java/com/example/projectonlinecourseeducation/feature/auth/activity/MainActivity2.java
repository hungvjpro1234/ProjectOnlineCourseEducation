// app/src/main/java/com/example/projectonlinecourseeducation/MainActivity2.java
package com.example.projectonlinecourseeducation.feature.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthRemoteApiService;
import com.example.projectonlinecourseeducation.data.cart.CartRemoteApiService;
import com.example.projectonlinecourseeducation.data.course.remote.CourseRemoteApiService;
import com.example.projectonlinecourseeducation.data.course.remote.CourseStudentRemoteApiService;
import com.example.projectonlinecourseeducation.data.mycourse.MyCourseRemoteApiService;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

public class MainActivity2 extends AppCompatActivity {

    Button btnLogin, btnRegister, btnForgot;

    private boolean doubleBackToExitPressedOnce = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize RetrofitClient (BẮT BUỘC)
        RetrofitClient.initialize(this);

        // Switch to Remote API implementations
        ApiProvider.setAuthApi(new AuthRemoteApiService());
        ApiProvider.setCartApi(new CartRemoteApiService());
        ApiProvider.setCourseApi(new CourseRemoteApiService());
        ApiProvider.setCourseStudentApi(new CourseStudentRemoteApiService());
        ApiProvider.setMyCourseApi(new MyCourseRemoteApiService());

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnForgot = findViewById(R.id.btnForgot);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!doubleBackToExitPressedOnce) {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(MainActivity2.this,
                            "Bấm lần nữa để thoát ứng dụng",
                            Toast.LENGTH_SHORT).show();

                    handler.postDelayed(() ->
                            doubleBackToExitPressedOnce = false, 1000);
                } else {
                    finishAffinity();
                    System.exit(0);
                }
            }
        });
    }
}
