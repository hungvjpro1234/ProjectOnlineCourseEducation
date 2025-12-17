// core/utils/OnlyFakeApiService/LessonCommentSeedData.java
package com.example.projectonlinecourseeducation.core.utils.OnlyApiService;

public final class LessonCommentSeedData {

    private LessonCommentSeedData() {}

    public static final String COMMENTS_JSON = "[\n" +

            // ===== c1 =====
            "  {\"lessonId\":\"c1_l1\",\"userId\":\"student1\",\"userName\":\"Student One\",\n" +
            "   \"content\":\"Phần giới thiệu dễ hiểu, em làm theo được ngay.\",\"createdAtOffsetMs\":-259200000},\n" +
            "  {\"lessonId\":\"c1_l2\",\"userId\":\"student2\",\"userName\":\"Student Two\",\n" +
            "   \"content\":\"Em cần xem lại phần kiểu dữ liệu một chút.\",\"createdAtOffsetMs\":-172800000},\n" +
            "  {\"lessonId\":\"c1_l3\",\"userId\":\"student3\",\"userName\":\"Student Three\",\n" +
            "   \"content\":\"Ví dụ if else rất dễ hiểu ạ.\",\"createdAtOffsetMs\":-86400000},\n" +
            "  {\"lessonId\":\"c1_l4\",\"userId\":\"student4\",\"userName\":\"Student Four\",\n" +
            "   \"content\":\"Collection giải thích rõ ràng.\",\"createdAtOffsetMs\":-43200000},\n" +
            "  {\"lessonId\":\"c1_l5\",\"userId\":\"student5\",\"userName\":\"Student Five\",\n" +
            "   \"content\":\"OOP là phần em thích nhất.\",\"createdAtOffsetMs\":-21600000},\n" +

            // ===== c2 =====
            "  {\"lessonId\":\"c2_l1\",\"userId\":\"student6\",\"userName\":\"Student Six\",\n" +
            "   \"content\":\"Giới thiệu Spring Boot rất dễ tiếp cận.\",\"createdAtOffsetMs\":-259200000},\n" +
            "  {\"lessonId\":\"c2_l2\",\"userId\":\"student1\",\"userName\":\"Student One\",\n" +
            "   \"content\":\"REST Controller trình bày rõ ràng.\",\"createdAtOffsetMs\":-172800000},\n" +
            "  {\"lessonId\":\"c2_l3\",\"userId\":\"student2\",\"userName\":\"Student Two\",\n" +
            "   \"content\":\"Entity mapping rất dễ hiểu.\",\"createdAtOffsetMs\":-86400000},\n" +
            "  {\"lessonId\":\"c2_l4\",\"userId\":\"student3\",\"userName\":\"Student Three\",\n" +
            "   \"content\":\"Service layer giúp code gọn hơn nhiều.\",\"createdAtOffsetMs\":-43200000},\n" +
            "  {\"lessonId\":\"c2_l5\",\"userId\":\"student4\",\"userName\":\"Student Four\",\n" +
            "   \"content\":\"Security cơ bản vừa đủ cho người mới.\",\"createdAtOffsetMs\":-21600000},\n" +

            // ===== c3 =====
            "  {\"lessonId\":\"c3_l1\",\"userId\":\"student5\",\"userName\":\"Student Five\",\n" +
            "   \"content\":\"Giải thích JavaScript rất dễ hiểu.\",\"createdAtOffsetMs\":-259200000},\n" +
            "  {\"lessonId\":\"c3_l2\",\"userId\":\"student6\",\"userName\":\"Student Six\",\n" +
            "   \"content\":\"Phần let và const rất rõ ràng.\",\"createdAtOffsetMs\":-172800000},\n" +
            "  {\"lessonId\":\"c3_l3\",\"userId\":\"student1\",\"userName\":\"Student One\",\n" +
            "   \"content\":\"DOM minh họa trực quan.\",\"createdAtOffsetMs\":-86400000},\n" +
            "  {\"lessonId\":\"c3_l4\",\"userId\":\"student2\",\"userName\":\"Student Two\",\n" +
            "   \"content\":\"Async/await giải thích dễ hiểu.\",\"createdAtOffsetMs\":-43200000},\n" +
            "  {\"lessonId\":\"c3_l5\",\"userId\":\"student3\",\"userName\":\"Student Three\",\n" +
            "   \"content\":\"Mini project rất thực tế.\",\"createdAtOffsetMs\":-21600000},\n" +

            // ===== c4 =====
            "  {\"lessonId\":\"c4_l1\",\"userId\":\"student4\",\"userName\":\"Student Four\",\n" +
            "   \"content\":\"Python dễ tiếp cận hơn em nghĩ.\",\"createdAtOffsetMs\":-259200000},\n" +
            "  {\"lessonId\":\"c4_l2\",\"userId\":\"student5\",\"userName\":\"Student Five\",\n" +
            "   \"content\":\"Kiểu dữ liệu Python rất rõ ràng.\",\"createdAtOffsetMs\":-172800000},\n" +
            "  {\"lessonId\":\"c4_l3\",\"userId\":\"student6\",\"userName\":\"Student Six\",\n" +
            "   \"content\":\"Control flow giải thích dễ hiểu.\",\"createdAtOffsetMs\":-86400000},\n" +
            "  {\"lessonId\":\"c4_l4\",\"userId\":\"student1\",\"userName\":\"Student One\",\n" +
            "   \"content\":\"List và dict dùng rất tiện.\",\"createdAtOffsetMs\":-43200000},\n" +
            "  {\"lessonId\":\"c4_l5\",\"userId\":\"student2\",\"userName\":\"Student Two\",\n" +
            "   \"content\":\"Phần xử lý dữ liệu khá hay.\",\"createdAtOffsetMs\":-21600000},\n" +
            "  {\"lessonId\":\"c4_l6\",\"userId\":\"student3\",\"userName\":\"Student Three\",\n" +
            "   \"content\":\"Ví dụ thực tế dễ áp dụng.\",\"createdAtOffsetMs\":-10800000},\n" +

            // ===== c5 =====
            "  {\"lessonId\":\"c5_l1\",\"userId\":\"student4\",\"userName\":\"Student Four\",\n" +
            "   \"content\":\"HTML cơ bản trình bày rõ ràng.\",\"createdAtOffsetMs\":-259200000},\n" +
            "  {\"lessonId\":\"c5_l2\",\"userId\":\"student5\",\"userName\":\"Student Five\",\n" +
            "   \"content\":\"CSS giải thích dễ hiểu.\",\"createdAtOffsetMs\":-172800000},\n" +
            "  {\"lessonId\":\"c5_l3\",\"userId\":\"student6\",\"userName\":\"Student Six\",\n" +
            "   \"content\":\"Flexbox rất hữu ích.\",\"createdAtOffsetMs\":-86400000},\n" +
            "  {\"lessonId\":\"c5_l4\",\"userId\":\"student1\",\"userName\":\"Student One\",\n" +
            "   \"content\":\"Làm xong thấy tự tin hơn nhiều.\",\"createdAtOffsetMs\":-43200000},\n" +

            // ===== c6 =====
            "  {\"lessonId\":\"c6_l1\",\"userId\":\"student2\",\"userName\":\"Student Two\",\n" +
            "   \"content\":\"Giới thiệu SQL dễ hiểu.\",\"createdAtOffsetMs\":-259200000},\n" +
            "  {\"lessonId\":\"c6_l2\",\"userId\":\"student3\",\"userName\":\"Student Three\",\n" +
            "   \"content\":\"SELECT và WHERE trình bày rõ ràng.\",\"createdAtOffsetMs\":-172800000},\n" +
            "  {\"lessonId\":\"c6_l3\",\"userId\":\"student4\",\"userName\":\"Student Four\",\n" +
            "   \"content\":\"JOIN giải thích rất trực quan.\",\"createdAtOffsetMs\":-86400000},\n" +
            "  {\"lessonId\":\"c6_l4\",\"userId\":\"student5\",\"userName\":\"Student Five\",\n" +
            "   \"content\":\"GROUP BY dễ áp dụng.\",\"createdAtOffsetMs\":-43200000},\n" +
            "  {\"lessonId\":\"c6_l5\",\"userId\":\"student6\",\"userName\":\"Student Six\",\n" +
            "   \"content\":\"SQL rất cần cho backend.\",\"createdAtOffsetMs\":-21600000}\n" +
            "]";
}
