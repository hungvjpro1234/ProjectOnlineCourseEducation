package com.example.projectonlinecourseeducation.data.cart;

import android.util.Log;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.cart.remote.AddToCartRequest;
import com.example.projectonlinecourseeducation.data.cart.remote.CartApiResponse;
import com.example.projectonlinecourseeducation.data.cart.remote.CartDtoMapper;
import com.example.projectonlinecourseeducation.data.cart.remote.CartRetrofitService;
import com.example.projectonlinecourseeducation.data.cart.remote.CheckoutRequest;
import com.example.projectonlinecourseeducation.data.cart.remote.RemoveFromCartRequest;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;
import com.example.projectonlinecourseeducation.data.network.SessionManager;
import com.example.projectonlinecourseeducation.data.cart.remote.CartCourseDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * CartRemoteApiService - Implementation of CartApi using Retrofit for real backend calls
 *
 * Usage:
 * 1. Initialize RetrofitClient first: RetrofitClient.initialize(context);
 * 2. Swap in ApiProvider: ApiProvider.setCartApi(new CartRemoteApiService());
 * 3. Use normally: ApiProvider.getCartApi().getCartCourses();
 *
 * IMPORTANT: All methods perform network calls and MUST be wrapped with AsyncApiHelper
 * to avoid ANR crashes.
 *
 * BACKEND REQUIREMENTS:
 * - GET /cart/:userId MUST JOIN with course table to return full Course data (not just payment_status)
 * - Backend needs POST /cart/clear endpoint (currently missing - using workaround)
 */
public class CartRemoteApiService implements CartApi {

    private static final String TAG = "CartRemoteApiService";

    private final CartRetrofitService retrofitService;
    private final SessionManager sessionManager;

    // Observers for cart changes
    private final List<CartUpdateListener> listeners = new ArrayList<>();

    // ===== LOCAL CART CACHE (for UI state) =====
    private final List<String> localCartIds = new ArrayList<>();


    public CartRemoteApiService() {
        this.retrofitService = RetrofitClient.getCartService();
        this.sessionManager = RetrofitClient.getSessionManager();
    }

    // ============ HELPER METHODS ============

    /**
     * Get current user ID from session
     */
    private Integer getCurrentUserId() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No current user in session");
            return null;
        }
        try {
            return Integer.parseInt(currentUser.getId());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid user ID format: " + currentUser.getId(), e);
            return null;
        }
    }

    /**
     * Notify all listeners that cart changed
     */
    private void notifyListeners() {
        android.os.Handler mainHandler =
                new android.os.Handler(android.os.Looper.getMainLooper());

        mainHandler.post(() -> {
            for (CartUpdateListener listener : listeners) {
                try {
                    listener.onCartChanged();
                } catch (Exception e) {
                    Log.e(TAG, "CartUpdateListener error", e);
                }
            }
        });
    }

    // ============ CART OPERATIONS (CURRENT USER) ============

    @Override
    public List<Course> getCartCourses() {
        Integer userId = getCurrentUserId();
        if (userId == null) return new ArrayList<>();

        try {
            Response<CartApiResponse<List<CartCourseDto>>> response =
                    retrofitService.getCartItems(userId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CartApiResponse<List<CartCourseDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    List<Course> courses = new ArrayList<>();

                    // ⭐ SYNC LOCAL CACHE
                    localCartIds.clear();

                    for (CartCourseDto dto : apiResponse.getData()) {
                        Course c = CartDtoMapper.toCourse(dto);
                        courses.add(c);
                        localCartIds.add(c.getId());
                    }
                    return courses;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getCartCourses error", e);
        }

        return new ArrayList<>();
    }

    @Override
    public boolean addToCart(Course course) {
        Integer userId = getCurrentUserId();
        if (userId == null || course == null) return false;

        Integer courseId = CartDtoMapper.parseCourseId(course.getId());
        if (courseId == null) return false;

        try {
            AddToCartRequest request = new AddToCartRequest(
                    userId,
                    courseId,
                    course.getPrice(),
                    course.getTitle()
            );

            Response<CartApiResponse<Void>> response =
                    retrofitService.addToCart(request).execute();

            if (!response.isSuccessful() || response.body() == null) {
                return false;
            }

            CartApiResponse<Void> body = response.body();

            if (body.isSuccess() && body.isAdded()) {
                // ✅ update local cache FIRST
                localCartIds.add(course.getId());

                // ✅ notify UI
                notifyListeners();
                return true;
            }

            // added=false → đã trong cart hoặc đã mua
            return false;

        } catch (Exception e) {
            Log.e(TAG, "addToCart error", e);
            return false;
        }
    }

    @Override
    public boolean removeFromCart(String courseId) {
        boolean removed = removeFromCartInternal(courseId);
        if (removed) {
            notifyListeners();
        }
        return removed;
    }


    @Override
    public void clearCart() {
        List<Course> cartCourses = getCartCourses();
        if (cartCourses.isEmpty()) return;

        boolean changed = false;

        for (Course c : cartCourses) {
            if (removeFromCartInternal(c.getId())) {
                changed = true;
            }
        }

        if (changed) {
            notifyListeners();
        }
    }

    /**
     * ✅ FIX: CHECK LOCAL CACHE ONLY
     *
     * Cache được sync khi:
     * - getCartCourses() được gọi (StudentCartFragment loads data)
     * - addToCart() thành công
     * - removeFromCart() thành công
     *
     * KHÔNG GỌI backend để tránh Binder transaction overflow
     */
    @Override
    public boolean isInCart(String courseId) {
        if (courseId == null) return false;

        // ✅ CHECK CACHE ONLY
        boolean inCache = localCartIds.contains(courseId);
        Log.d(TAG, "isInCart(" + courseId + "): cache result = " + inCache);
        return inCache;
    }

    @Override
    public int getTotalItems() {
        return localCartIds.size();
    }

    @Override
    public double getTotalPrice() {
        // UI badge only — backend sẽ tính khi checkout
        return 0;
    }

    // ============ OBSERVERS ============

    @Override
    public void addCartUpdateListener(CartUpdateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeCartUpdateListener(CartUpdateListener listener) {
        listeners.remove(listener);
    }

    // ============ ADMIN FEATURES ============

    @Override
    public List<Course> getCartCoursesForUser(String userId) {
        try {
            Integer uid = Integer.parseInt(userId);

            Response<CartApiResponse<List<CartCourseDto>>> response =
                    retrofitService.getCartItems(uid).execute();

            if (response.isSuccessful() && response.body() != null) {
                CartApiResponse<List<CartCourseDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    List<Course> courses = new ArrayList<>();

                    // ⭐ SYNC LOCAL CACHE
                    localCartIds.clear();

                    for (CartCourseDto dto : apiResponse.getData()) {
                        Course c = CartDtoMapper.toCourse(dto);
                        courses.add(c);
                        localCartIds.add(c.getId());
                    }
                    return courses;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getCartCoursesForUser error", e);
        }

        return new ArrayList<>();
    }

    @Override
    public double getTotalPriceForUser(String userId) {
        double total = 0;
        for (Course c : getCartCoursesForUser(userId)) {
            total += c.getPrice();
        }
        return total;
    }

    // ============ CHECKOUT ============

    @Override
    public List<Course> checkout() {
        Integer userId = getCurrentUserId();
        if (userId == null) return new ArrayList<>();

        List<Course> cartCourses = getCartCourses();
        if (cartCourses.isEmpty()) return new ArrayList<>();

        try {
            CheckoutRequest request = new CheckoutRequest(userId);

            Response<CartApiResponse<List<CartCourseDto>>> response =
                    retrofitService.checkout(request).execute();

            if (response.isSuccessful()
                    && response.body() != null
                    && response.body().isSuccess()
                    && response.body().getData() != null) {

                List<Course> purchased = new ArrayList<>();
                for (CartCourseDto dto : response.body().getData()) {
                    purchased.add(CartDtoMapper.toCourse(dto));
                }

                localCartIds.clear(); // ⭐ CLEAR CART CACHE

                // ⭐⭐⭐ CRITICAL FIX: UPDATE MYCOURSE CACHE ⭐⭐⭐
                // Giống logic CartFakeApiService.checkout()
                // Cập nhật MyCourse cache ngay sau checkout thành công
                // để isPurchased() trả về true và button state đúng
                com.example.projectonlinecourseeducation.data.mycourse.MyCourseApi myCourseApi =
                        com.example.projectonlinecourseeducation.data.ApiProvider.getMyCourseApi();
                if (myCourseApi != null) {
                    myCourseApi.addPurchasedCourses(purchased); // ✅ Sync MyCourse cache
                    Log.d(TAG, "✅ Synced " + purchased.size() + " courses to MyCourse cache after checkout");
                } else {
                    Log.w(TAG, "⚠️ MyCourseApi is null, cannot sync cache");
                }

                notifyListeners();
                return purchased;
            }

        } catch (Exception e) {
            Log.e(TAG, "checkout error", e);
        }

        return new ArrayList<>();
    }

    /**
     * Internal remove method - NO notify
     * Used for batch operations (clearCart)
     */
    private boolean removeFromCartInternal(String courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null || courseId == null) return false;

        Integer parsedCourseId = CartDtoMapper.parseCourseId(courseId);
        if (parsedCourseId == null) return false;

        try {
            RemoveFromCartRequest request =
                    new RemoveFromCartRequest(userId, parsedCourseId);

            Response<CartApiResponse<Void>> response =
                    retrofitService.removeFromCart(request).execute();

            if (!response.isSuccessful() || response.body() == null) {
                return false;
            }

            CartApiResponse<Void> body = response.body();
            if (body.isSuccess() && body.isRemoved()) {
                localCartIds.remove(courseId); // ✅ sync local
                return true;
            }
            return false;

        } catch (Exception e) {
            Log.e(TAG, "removeFromCartInternal error", e);
            return false;
        }
    }
}
