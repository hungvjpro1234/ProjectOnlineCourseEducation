package com.example.projectonlinecourseeducation.core.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper to run API calls on background thread without MVVM refactoring
 * Prevents ANR when using RemoteApiService
 *
 * Usage:
 * AsyncApiHelper.execute(
 *     () -> ApiProvider.getAuthApi().login(username, password),
 *     new AsyncApiHelper.ApiCallback<ApiResult<User>>() {
 *         @Override
 *         public void onSuccess(ApiResult<User> result) {
 *             // Handle result on main thread
 *         }
 *
 *         @Override
 *         public void onError(Exception e) {
 *             // Handle error on main thread
 *         }
 *     }
 * );
 */
public class AsyncApiHelper {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Execute API call on background thread, callback on main thread
     *
     * @param apiCall The API call to execute (runs on background thread)
     * @param callback Callback for success/error (runs on main thread)
     */
    public static <T> void execute(ApiCall<T> apiCall, ApiCallback<T> callback) {
        executor.execute(() -> {
            try {
                T result = apiCall.call();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Functional interface for API call
     */
    public interface ApiCall<T> {
        T call();
    }

    /**
     * Callback interface for handling result
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
