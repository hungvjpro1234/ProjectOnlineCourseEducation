// app/src/main/java/com/example/projectonlinecourseeducation/feature/student/model/Course.java
package com.example.projectonlinecourseeducation.core.model;

public class Course {
    private final String id;
    private final String title;
    private final String teacher;
    private final String imageUrl;
    private final String category; // Java / C / C++ / Python ...
    private final int lectures;
    private final int students;
    private final double rating; // 0..5
    private final double price;

    public Course(String id, String title, String teacher, String imageUrl,
                  String category, int lectures, int students, double rating, double price) {
        this.id = id; this.title = title; this.teacher = teacher; this.imageUrl = imageUrl;
        this.category = category; this.lectures = lectures; this.students = students;
        this.rating = rating; this.price = price;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getTeacher() { return teacher; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public int getLectures() { return lectures; }
    public int getStudents() { return students; }
    public double getRating() { return rating; }
    public double getPrice() { return price; }
}
