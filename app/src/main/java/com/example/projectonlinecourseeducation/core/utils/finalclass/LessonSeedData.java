// core/utils/OnlyFakeApiService/LessonSeedData.java
package com.example.projectonlinecourseeducation.core.utils.finalclass;

public final class LessonSeedData {

    private LessonSeedData() {}

    public static final String LESSONS_JSON = "[\n" +

            // ===== c1: Java Cơ Bản =====
            "  {\"id\":\"c1_l1\",\"courseId\":\"c1\",\"order\":1,\"title\":\"Giới thiệu Java & cài đặt môi trường\",\n" +
            "   \"description\":\"Tổng quan Java và hướng dẫn cài đặt JDK, IDE.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c1_l2\",\"courseId\":\"c1\",\"order\":2,\"title\":\"Biến, kiểu dữ liệu & toán tử\",\n" +
            "   \"description\":\"Các kiểu dữ liệu và toán tử cơ bản trong Java.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c1_l3\",\"courseId\":\"c1\",\"order\":3,\"title\":\"Cấu trúc điều khiển\",\n" +
            "   \"description\":\"if, switch, for, while trong Java.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c1_l4\",\"courseId\":\"c1\",\"order\":4,\"title\":\"Mảng & Collection\",\n" +
            "   \"description\":\"Array, ArrayList, HashMap.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c1_l5\",\"courseId\":\"c1\",\"order\":5,\"title\":\"Nhập môn OOP\",\n" +
            "   \"description\":\"Class, object và các khái niệm OOP.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +

            // ===== c2: Spring Boot =====
            "  {\"id\":\"c2_l1\",\"courseId\":\"c2\",\"order\":1,\"title\":\"Giới thiệu Spring Boot\",\n" +
            "   \"description\":\"Tổng quan Spring Boot và tạo project.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c2_l2\",\"courseId\":\"c2\",\"order\":2,\"title\":\"REST Controller\",\n" +
            "   \"description\":\"Xây dựng REST API với Spring Boot.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c2_l3\",\"courseId\":\"c2\",\"order\":3,\"title\":\"JPA & Entity\",\n" +
            "   \"description\":\"Mapping entity và làm việc với database.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c2_l4\",\"courseId\":\"c2\",\"order\":4,\"title\":\"Service & Repository\",\n" +
            "   \"description\":\"Tách business logic theo layer.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c2_l5\",\"courseId\":\"c2\",\"order\":5,\"title\":\"Spring Security cơ bản\",\n" +
            "   \"description\":\"Authentication và authorization.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +

            // ===== c3: JavaScript =====
            "  {\"id\":\"c3_l1\",\"courseId\":\"c3\",\"order\":1,\"title\":\"Giới thiệu JavaScript\",\n" +
            "   \"description\":\"JavaScript là gì và cách chạy JS.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c3_l2\",\"courseId\":\"c3\",\"order\":2,\"title\":\"Biến & kiểu dữ liệu\",\n" +
            "   \"description\":\"var, let, const và kiểu dữ liệu JS.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c3_l3\",\"courseId\":\"c3\",\"order\":3,\"title\":\"DOM cơ bản\",\n" +
            "   \"description\":\"Thao tác DOM và event.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c3_l4\",\"courseId\":\"c3\",\"order\":4,\"title\":\"Async / Await\",\n" +
            "   \"description\":\"Xử lý bất đồng bộ trong JS.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c3_l5\",\"courseId\":\"c3\",\"order\":5,\"title\":\"Mini Project\",\n" +
            "   \"description\":\"Xây dựng ứng dụng JS đơn giản.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +

            // ===== c4: Python =====
            "  {\"id\":\"c4_l1\",\"courseId\":\"c4\",\"order\":1,\"title\":\"Giới thiệu Python\",\n" +
            "   \"description\":\"Python và môi trường làm việc.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c4_l2\",\"courseId\":\"c4\",\"order\":2,\"title\":\"Biến & kiểu dữ liệu\",\n" +
            "   \"description\":\"Kiểu dữ liệu cơ bản trong Python.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c4_l3\",\"courseId\":\"c4\",\"order\":3,\"title\":\"Control Flow\",\n" +
            "   \"description\":\"if, loop trong Python.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c4_l4\",\"courseId\":\"c4\",\"order\":4,\"title\":\"List & Dictionary\",\n" +
            "   \"description\":\"Cấu trúc dữ liệu quan trọng.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c4_l5\",\"courseId\":\"c4\",\"order\":5,\"title\":\"Xử lý dữ liệu\",\n" +
            "   \"description\":\"Làm việc với dữ liệu trong Python.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c4_l6\",\"courseId\":\"c4\",\"order\":6,\"title\":\"Ứng dụng thực tế\",\n" +
            "   \"description\":\"Áp dụng Python vào phân tích dữ liệu.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +

            // ===== c5: HTML & CSS =====
            "  {\"id\":\"c5_l1\",\"courseId\":\"c5\",\"order\":1,\"title\":\"HTML cơ bản\",\n" +
            "   \"description\":\"Cấu trúc trang HTML.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c5_l2\",\"courseId\":\"c5\",\"order\":2,\"title\":\"CSS cơ bản\",\n" +
            "   \"description\":\"Style và layout.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c5_l3\",\"courseId\":\"c5\",\"order\":3,\"title\":\"Flexbox\",\n" +
            "   \"description\":\"Layout linh hoạt với Flexbox.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c5_l4\",\"courseId\":\"c5\",\"order\":4,\"title\":\"Trang web hoàn chỉnh\",\n" +
            "   \"description\":\"Hoàn thiện website đơn giản.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +

            // ===== c6: SQL =====
            "  {\"id\":\"c6_l1\",\"courseId\":\"c6\",\"order\":1,\"title\":\"Giới thiệu SQL\",\n" +
            "   \"description\":\"SQL và database cơ bản.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c6_l2\",\"courseId\":\"c6\",\"order\":2,\"title\":\"SELECT & WHERE\",\n" +
            "   \"description\":\"Truy vấn dữ liệu.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c6_l3\",\"courseId\":\"c6\",\"order\":3,\"title\":\"JOIN\",\n" +
            "   \"description\":\"Kết hợp bảng dữ liệu.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c6_l4\",\"courseId\":\"c6\",\"order\":4,\"title\":\"GROUP BY\",\n" +
            "   \"description\":\"Tổng hợp dữ liệu.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"},\n" +
            "  {\"id\":\"c6_l5\",\"courseId\":\"c6\",\"order\":5,\"title\":\"Ứng dụng SQL\",\n" +
            "   \"description\":\"SQL cho backend developer.\",\"videoUrl\":\"tCDvOQI3pco\",\"duration\":\"00:10\"}\n" +
            "]";
}
