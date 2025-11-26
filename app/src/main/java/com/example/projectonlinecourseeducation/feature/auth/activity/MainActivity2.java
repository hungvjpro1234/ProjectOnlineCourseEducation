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

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnForgot = findViewById(R.id.btnForgot);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // 🚀 Back Press Callback mới theo chuẩn AndroidX
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!doubleBackToExitPressedOnce) {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(MainActivity2.this,
                            "Bấm lần nữa để thoát ứng dụng",
                            Toast.LENGTH_SHORT).show();

                    handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 1000);
                } else {
                    finishAffinity(); // 🔥 Thoát toàn bộ ứng dụng
                    System.exit(0);  // Đảm bảo app đóng hẳn
                }
            }
        });
    }
}
