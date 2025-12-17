package com.example.projectonlinecourseeducation.core.apiservice;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.cart.CartApi;
import com.example.projectonlinecourseeducation.data.cart.remote.AddToCartRequest;
import com.example.projectonlinecourseeducation.data.cart.remote.CartItemDto;
import com.example.projectonlinecourseeducation.data.cart.remote.CartResponse;
import com.example.projectonlinecourseeducation.data.cart.remote.CartRetrofitService;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class CartService implements CartApi {

    private final CartRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(CartRetrofitService.class);

    // ================= Mapping =================

    private Course map(CartItemDto d) {
        if (d == null) return null;

        Course c = new Course();
        c.setId(d.courseId);
        c.setTitle(d.title);
        c.setImageUrl(d.imageUrl);
        c.setPrice(d.price);
        return c;
    }

    private List<Course> mapList(List<CartItemDto> list) {
        List<Course> result = new ArrayList<>();
        if (list == null) return result;
        for (CartItemDto d : list) {
            Course c = map(d);
            if (c != null) result.add(c);
        }
        return result;
    }

    // ================= API =================

    @Override
    public List<Course> getCartCourses() {
        try {
            Response<CartResponse> res = api.getCart().execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return mapList(res.body().data);
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @Override
    public boolean addToCart(Course course) {
        if (course == null || course.getId() == null) return false;
        try {
            AddToCartRequest req = new AddToCartRequest();
            req.courseId = course.getId();
            api.addToCart(req).execute();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public boolean removeFromCart(String courseId) {
        if (courseId == null) return false;
        try {
            api.removeFromCart(courseId).execute();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public void clearCart() {
        try {
            api.clearCart().execute();
        } catch (Exception ignored) {}
    }

    @Override
    public boolean isInCart(String courseId) {
        if (courseId == null) return false;
        for (Course c : getCartCourses()) {
            if (c != null && courseId.equals(c.getId())) return true;
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
            if (c != null) total += c.getPrice();
        }
        return total;
    }

    // ================= LISTENER (NO-OP) =================
    // Remote API không realtime → implement rỗng là ĐÚNG

    @Override
    public void addCartUpdateListener(CartUpdateListener listener) {
        // NO-OP
    }

    @Override
    public void removeCartUpdateListener(CartUpdateListener listener) {
        // NO-OP
    }

    // ================= ADMIN =================

    @Override
    public List<Course> getCartCoursesForUser(String userId) {
        if (userId == null) return new ArrayList<>();
        try {
            Response<CartResponse> res =
                    api.getCartForUser(userId).execute();
            if (res.isSuccessful() && res.body() != null && res.body().success) {
                return mapList(res.body().data);
            }
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @Override
    public double getTotalPriceForUser(String userId) {
        double total = 0;
        for (Course c : getCartCoursesForUser(userId)) {
            if (c != null) total += c.getPrice();
        }
        return total;
    }
}
