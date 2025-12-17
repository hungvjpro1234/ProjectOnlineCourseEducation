package com.example.projectonlinecourseeducation.data.cart.remote;

import com.google.gson.annotations.SerializedName;

public class CartCourseDto {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("price")
    private Double price;

    @SerializedName("teacher")
    private String teacher;

    @SerializedName("rating")
    private Double rating;

    // getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public Double getPrice() { return price; }
    public String getTeacher() { return teacher; }
    public Double getRating() { return rating; }
}
