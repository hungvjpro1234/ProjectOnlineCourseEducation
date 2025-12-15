package com.example.projectonlinecourseeducation.data.cart;

import android.util.Log;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.core.model.user.User;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.cart.remote.AddToCartRequest;
import com.example.projectonlinecourseeducation.data.cart.remote.CartApiResponse;
import com.example.projectonlinecourseeducation.data.cart.remote.CartItemDto;
import com.example.projectonlinecourseeducation.data.cart.remote.CartRetrofitService;
import com.example.projectonlinecourseeducation.data.cart.remote.CheckoutRequest;
import com.example.projectonlinecourseeducation.data.cart.remote.CourseStatusDto;
import com.example.projectonlinecourseeducation.data.cart.remote.RemoveFromCartRequest;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;
import com.example.projectonlinecourseeducation.data.network.SessionManager;

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
     * Convert CartItemDto to Course object
     * NOTE: Backend MUST return full course data (JOIN with course table)
     */
    private Course convertDtoToCourse(CartItemDto dto) {
        Course course = new Course();

        // Course ID
        if (dto.getCourseId() != null) {
            course.setId(String.valueOf(dto.getCourseId()));
        }

        // If backend joined with course table, these fields will be populated
        course.setTitle(dto.getTitle() != null ? dto.getTitle() : dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setImageUrl(dto.getImageUrl());
        course.setCategory(dto.getCategory());
        course.setTeacher(dto.getTeacher());

        // Price - use price_snapshot from cart, fallback to course price
        if (dto.getPriceSnapshot() != null) {
            course.setPrice(dto.getPriceSnapshot());
        } else if (dto.getPrice() != null) {
            course.setPrice(dto.getPrice());
        }

        // Stats
        if (dto.getRating() != null) {
            course.setRating(dto.getRating());
        }
        if (dto.getStudents() != null) {
            course.setStudents(dto.getStudents());
        }
        if (dto.getTotalDurationMinutes() != null) {
            course.setTotalDurationMinutes(dto.getTotalDurationMinutes());
        }

        return course;
    }

    /**
     * Notify all listeners that cart changed
     */
    private void notifyListeners() {
        for (CartUpdateListener listener : listeners) {
            listener.onCartChanged();
        }
    }

    // ============ CART OPERATIONS (CURRENT USER) ============

    @Override
    public List<Course> getCartCourses() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return new ArrayList<>();
        }

        try {
            Response<CartApiResponse<List<CartItemDto>>> response =
                    retrofitService.getCartItems(userId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CartApiResponse<List<CartItemDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    List<Course> courses = new ArrayList<>();
                    for (CartItemDto dto : apiResponse.getData()) {
                        courses.add(convertDtoToCourse(dto));
                    }
                    return courses;
                } else {
                    Log.w(TAG, "getCartCourses failed: " + apiResponse.getMessage());
                    return new ArrayList<>();
                }
            } else {
                Log.e(TAG, "getCartCourses HTTP error: " + response.code());
                return new ArrayList<>();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in getCartCourses", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in getCartCourses", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addToCart(Course course) {
        Integer userId = getCurrentUserId();
        if (userId == null || course == null) {
            return false;
        }

        try {
            Integer courseId = Integer.parseInt(course.getId());
            AddToCartRequest request = new AddToCartRequest(
                    userId,
                    courseId,
                    course.getPrice(),
                    course.getTitle()
            );

            Response<CartApiResponse<Void>> response =
                    retrofitService.addToCart(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                CartApiResponse<Void> apiResponse = response.body();
                boolean success = apiResponse.isSuccess();

                if (success) {
                    notifyListeners();
                } else {
                    Log.w(TAG, "addToCart failed: " + apiResponse.getMessage());
                }

                return success;
            } else {
                Log.e(TAG, "addToCart HTTP error: " + response.code());
                return false;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid course ID: " + course.getId(), e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Network error in addToCart", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in addToCart", e);
            return false;
        }
    }

    @Override
    public boolean removeFromCart(String courseId) {
        boolean success = removeFromCartInternal(courseId);
        if (success) {
            notifyListeners();
        }
        return success;
    }


    @Override
    public void clearCart() {

        List<Course> cartCourses = getCartCourses();
        if (cartCourses.isEmpty()) {
            return;
        }

        boolean anyRemoved = false;

        for (Course course : cartCourses) {
            boolean removed = removeFromCartInternal(course.getId());
            if (removed) {
                anyRemoved = true;
            }
        }

        if (anyRemoved) {
            notifyListeners();
        }
    }

    @Override
    public boolean isInCart(String courseId) {
        List<Course> cartCourses = getCartCourses();
        for (Course c : cartCourses) {
            if (c.getId().equals(courseId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getTotalItems() {
        return getCartCourses().size();
    }

    @Override
    public double getTotalPrice() {
        double total = 0;
        for (Course c : getCartCourses()) {
            total += c.getPrice();
        }
        return total;
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
        if (userId == null) {
            return new ArrayList<>();
        }

        try {
            Integer userIdInt = Integer.parseInt(userId);

            Response<CartApiResponse<List<CartItemDto>>> response =
                    retrofitService.getCartItems(userIdInt).execute();

            if (response.isSuccessful() && response.body() != null) {
                CartApiResponse<List<CartItemDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    List<Course> courses = new ArrayList<>();
                    for (CartItemDto dto : apiResponse.getData()) {
                        courses.add(convertDtoToCourse(dto));
                    }
                    return courses;
                } else {
                    Log.w(TAG, "getCartCoursesForUser failed: " + apiResponse.getMessage());
                    return new ArrayList<>();
                }
            } else {
                Log.e(TAG, "getCartCoursesForUser HTTP error: " + response.code());
                return new ArrayList<>();
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid user ID: " + userId, e);
            return new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG, "Network error in getCartCoursesForUser", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in getCartCoursesForUser", e);
            return new ArrayList<>();
        }
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
        if (userId == null) {
            return new ArrayList<>();
        }

        // 1. Get cart courses
        List<Course> cartCourses = getCartCourses();
        if (cartCourses.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Convert course IDs to List<Integer>
        List<Integer> courseIds = new ArrayList<>();
        for (Course c : cartCourses) {
            try {
                courseIds.add(Integer.parseInt(c.getId()));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid course ID in cart: " + c.getId(), e);
            }
        }

        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. Call backend checkout endpoint
        try {
            CheckoutRequest request = new CheckoutRequest(userId, courseIds);

            Response<CartApiResponse<Void>> response =
                    retrofitService.checkout(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                CartApiResponse<Void> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "Checkout successful: " + apiResponse.getMessage());

                    // 4. Update MyCourseApi (backend already updated database, but sync FakeApi if needed)
                    // NOTE: If using MyCourseFakeApiService, we should update it here
                    // If using MyCourseRemoteApiService, it will fetch from backend automatically

                    // 5. Notify listeners
                    notifyListeners();

                    // 6. Return purchased courses
                    return cartCourses;
                } else {
                    Log.w(TAG, "Checkout failed: " + apiResponse.getMessage());
                    return new ArrayList<>();
                }
            } else {
                Log.e(TAG, "Checkout HTTP error: " + response.code());
                return new ArrayList<>();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in checkout", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in checkout", e);
            return new ArrayList<>();
        }
    }

    /**
     * Internal remove method - NO notify
     * Used for batch operations (clearCart)
     */
    private boolean removeFromCartInternal(String courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null || courseId == null) {
            return false;
        }

        try {
            Integer courseIdInt = Integer.parseInt(courseId);
            RemoveFromCartRequest request =
                    new RemoveFromCartRequest(userId, courseIdInt);

            Response<CartApiResponse<Void>> response =
                    retrofitService.removeFromCart(request).execute();

            return response.isSuccessful()
                    && response.body() != null
                    && response.body().isSuccess();

        } catch (Exception e) {
            Log.e(TAG, "removeFromCartInternal error", e);
            return false;
        }
    }
}
