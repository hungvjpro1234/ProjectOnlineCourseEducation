package com.example.projectonlinecourseeducation.data.network;

import android.content.Context;

import com.example.projectonlinecourseeducation.data.auth.remote.AuthRetrofitService;
import com.example.projectonlinecourseeducation.data.cart.remote.CartRetrofitService;
import com.example.projectonlinecourseeducation.data.course.remote.CourseRetrofitService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit client for all API services
 *
 * Usage:
 * RetrofitClient.initialize(context);
 * AuthRetrofitService authService = RetrofitClient.getAuthService();
 */
public class RetrofitClient {

    // TODO: Change to your backend server URL
    // For emulator: use 10.0.2.2:3000
    // For physical device: use your computer's IP (e.g., 192.168.1.100:3000)
    private static final String BASE_URL = "https://projectonlinecourseeducation.onrender.com/";

    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final AuthRetrofitService authService;
    private final CartRetrofitService cartService;
    private final CourseRetrofitService courseService;
    private final SessionManager sessionManager;

    private RetrofitClient(Context context) {
        this.sessionManager = SessionManager.getInstance(context.getApplicationContext());

        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // OkHttp client with interceptors
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    // Add JWT token to all requests if available
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();

                    String token = sessionManager.getToken();
                    if (token != null && !token.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }

                    requestBuilder.method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create services
        authService = retrofit.create(AuthRetrofitService.class);
        cartService = retrofit.create(CartRetrofitService.class);
        courseService = retrofit.create(CourseRetrofitService.class);
    }

    /**
     * Initialize RetrofitClient (call this in Application or MainActivity)
     */
    public static void initialize(Context context) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    instance = new RetrofitClient(context);
                }
            }
        }
    }

    /**
     * Get RetrofitClient instance
     */
    public static RetrofitClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RetrofitClient not initialized. Call initialize(context) first.");
        }
        return instance;
    }

    /**
     * Get Auth API service
     */
    public static AuthRetrofitService getAuthService() {
        return getInstance().authService;
    }

    /**
     * Get Cart API service
     */
    public static CartRetrofitService getCartService() {
        return getInstance().cartService;
    }

    /**
     * Get Course API service
     */
    public static CourseRetrofitService getCourseService() {
        return getInstance().courseService;
    }

    /**
     * Get SessionManager
     */
    public static SessionManager getSessionManager() {
        return getInstance().sessionManager;
    }

    /**
     * Get base Retrofit instance for creating other services
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }
}