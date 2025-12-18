// core/utils/OnlyFakeApiService/CourseSeedData.java
package com.example.projectonlinecourseeducation.core.utils.datahelper;

public final class CourseHelper {

    private CourseHelper() {}

    public static final String COURSES_JSON = "[\n" +
            "  {\n" +
            "    \"id\":\"c1\",\n" +
            "    \"title\":\"Java Cơ Bản\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://s3-hfx03.fptcloud.com/codelearnstorage/files/thumbnails/java-co-ban_18f86065cf1d4640ae1ee476da5acf49.png\",\n" +
            "    \"category\":\"Java, Backend\",\n" +
            "    \"lectures\":5,\n" +
            "    \"students\":1200,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":199000,\n" +
            "    \"description\":\"Khóa học Java cơ bản cho người mới bắt đầu, đi từ cú pháp đến OOP và thực hành.\",\n" +
            "    \"createdAt\":\"03/2024\",\n" +
            "    \"ratingCount\":3,\n" +
            "    \"totalDurationMinutes\":106,\n" +
            "    \"skills\":[\"Java syntax\", \"OOP\", \"Collection\"],\n" +
            "    \"requirements\":[\"Biết máy tính cơ bản\", \"Cài JDK\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
            "  },\n" +

            "  {\n" +
            "    \"id\":\"c2\",\n" +
            "    \"title\":\"Java Web với Spring Boot\",\n" +
            "    \"teacher\":\"Nguyễn A\",\n" +
            "    \"imageUrl\":\"https://static.unica.vn/upload/images/2019/04/hoc-fullstack-java-web-voi-spring-boot_1555642764.jpg\",\n" +
            "    \"category\":\"Java, Backend\",\n" +
            "    \"lectures\":5,\n" +
            "    \"students\":1500,\n" +
            "    \"rating\":4.8,\n" +
            "    \"price\":299000,\n" +
            "    \"description\":\"Xây dựng REST API backend với Spring Boot và JPA.\",\n" +
            "    \"createdAt\":\"05/2024\",\n" +
            "    \"ratingCount\":3,\n" +
            "    \"totalDurationMinutes\":112,\n" +
            "    \"skills\":[\"Spring Boot\", \"REST API\", \"JPA\"],\n" +
            "    \"requirements\":[\"Java cơ bản\", \"OOP\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
            "  },\n" +

            "  {\n" +
            "    \"id\":\"c3\",\n" +
            "    \"title\":\"JavaScript Cơ Bản Đến Nâng Cao\",\n" +
            "    \"teacher\":\"Teacher Assistant\",\n" +
            "    \"imageUrl\":\"https://online.unicode.vn/storage/images/kh%C3%B3a%20h%E1%BB%8Dc%20javascript.png\",\n" +
            "    \"category\":\"JavaScript, Frontend\",\n" +
            "    \"lectures\":5,\n" +
            "    \"students\":2000,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":199000,\n" +
            "    \"description\":\"Học JavaScript từ cơ bản đến nâng cao, DOM và async.\",\n" +
            "    \"createdAt\":\"01/2024\",\n" +
            "    \"ratingCount\":3,\n" +
            "    \"totalDurationMinutes\":109,\n" +
            "    \"skills\":[\"JavaScript\", \"DOM\", \"Async/Await\"],\n" +
            "    \"requirements\":[\"HTML cơ bản\", \"CSS cơ bản\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
            "  },\n" +

            "  {\n" +
            "    \"id\":\"c4\",\n" +
            "    \"title\":\"Python cho Phân Tích Dữ Liệu\",\n" +
            "    \"teacher\":\"Teacher Three\",\n" +
            "    \"imageUrl\":\"https://zendvn.com/images/coursesOffline/V6Lh4RvG1u.png\",\n" +
            "    \"category\":\"Python, Data / AI\",\n" +
            "    \"lectures\":6,\n" +
            "    \"students\":900,\n" +
            "    \"rating\":4.7,\n" +
            "    \"price\":249000,\n" +
            "    \"description\":\"Sử dụng Python để xử lý và phân tích dữ liệu thực tế.\",\n" +
            "    \"createdAt\":\"06/2024\",\n" +
            "    \"ratingCount\":2,\n" +
            "    \"totalDurationMinutes\":130,\n" +
            "    \"skills\":[\"Python\", \"Data analysis\"],\n" +
            "    \"requirements\":[\"Biết lập trình cơ bản\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
            "  },\n" +

            "  {\n" +
            "    \"id\":\"c5\",\n" +
            "    \"title\":\"HTML & CSS Cho Người Mới\",\n" +
            "    \"teacher\":\"Teacher Four\",\n" +
            "    \"imageUrl\":\"https://s3-hfx03.fptcloud.com/codelearnstorage/files/thumbnails/html-css-cho-nguoi-moi-bat-dau_3867537473444f328a4de0fa0231a6ea.jpg\",\n" +
            "    \"category\":\"HTML, CSS, Frontend\",\n" +
            "    \"lectures\":4,\n" +
            "    \"students\":1800,\n" +
            "    \"rating\":4.5,\n" +
            "    \"price\":149000,\n" +
            "    \"description\":\"Xây dựng giao diện web từ HTML và CSS.\",\n" +
            "    \"createdAt\":\"02/2024\",\n" +
            "    \"ratingCount\":4,\n" +
            "    \"totalDurationMinutes\":95,\n" +
            "    \"skills\":[\"HTML\", \"CSS\"],\n" +
            "    \"requirements\":[\"Không yêu cầu\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
            "  },\n" +

            "  {\n" +
            "    \"id\":\"c6\",\n" +
            "    \"title\":\"SQL Cơ Bản Cho Backend\",\n" +
            "    \"teacher\":\"Teacher Five\",\n" +
            "    \"imageUrl\":\"https://cdn-main.28tech.com.vn/media/anh-khoa-hoc/anh_dai_dien_khoa_hoc/new/c%C6%A1%20s%E1%BB%9F%20d%E1%BB%AF%20li%E1%BB%87u%20v%C3%A0%20sql.png\",\n" +
            "    \"category\":\"SQL, Backend\",\n" +
            "    \"lectures\":5,\n" +
            "    \"students\":1100,\n" +
            "    \"rating\":4.6,\n" +
            "    \"price\":179000,\n" +
            "    \"description\":\"Học SQL từ cơ bản đến truy vấn dữ liệu hiệu quả.\",\n" +
            "    \"createdAt\":\"04/2024\",\n" +
            "    \"ratingCount\":3,\n" +
            "    \"totalDurationMinutes\":102,\n" +
            "    \"skills\":[\"SQL\", \"Query\", \"Join\"],\n" +
            "    \"requirements\":[\"Biết máy tính cơ bản\"],\n" +
            "    \"isInitialApproved\":true,\n" +
            "    \"isEditApproved\":true\n" +
            "  }\n" +
            "]";
}
