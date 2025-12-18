package com.example.projectonlinecourseeducation.core.utils.datahelper;

public final class LessonQuizHelper {

    private LessonQuizHelper() {}

    // ===== c1 =====
    private static final String QUIZZES_C1 = "[\n" +
            "  {\"id\":null,\"lessonId\":\"c1_l1\",\"title\":\"Quiz: Giới thiệu Java\",\n" +
            "   \"questions\":[\n" +
            "     {\"id\":\"c1_l1_q1\",\"text\":\"Java là gì?\",\"options\":[\"Ngôn ngữ lập trình\",\"Hệ điều hành\",\"Trình duyệt\",\"Cơ sở dữ liệu\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q2\",\"text\":\"Java chạy trên nền tảng nào?\",\"options\":[\"JVM\",\"CPU\",\"BIOS\",\"OS\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q3\",\"text\":\"Java do ai phát triển?\",\"options\":[\"Sun Microsystems\",\"Microsoft\",\"Google\",\"IBM\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q4\",\"text\":\"File Java có đuôi là gì?\",\"options\":[\".java\",\".class\",\".js\",\".jar\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q5\",\"text\":\"Java là ngôn ngữ gì?\",\"options\":[\"Hướng đối tượng\",\"Thủ tục\",\"Assembly\",\"Markup\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q6\",\"text\":\"Java có chạy đa nền tảng không?\",\"options\":[\"Có\",\"Không\",\"Chỉ Windows\",\"Chỉ Linux\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q7\",\"text\":\"JDK dùng để làm gì?\",\"options\":[\"Phát triển Java\",\"Chạy game\",\"Thiết kế UI\",\"Quản lý DB\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q8\",\"text\":\"JRE là gì?\",\"options\":[\"Môi trường chạy Java\",\"IDE\",\"Compiler\",\"Framework\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q9\",\"text\":\"Java có hỗ trợ OOP không?\",\"options\":[\"Có\",\"Không\",\"Một phần\",\"Không rõ\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l1_q10\",\"text\":\"Java phổ biến trong lĩnh vực nào?\",\"options\":[\"Backend\",\"Thiết kế đồ họa\",\"Phần cứng\",\"Game console\"],\"correctIndex\":0}\n" +
            "   ]},\n" +

            "  {\"id\":null,\"lessonId\":\"c1_l2\",\"title\":\"Quiz: Biến & kiểu dữ liệu\",\n" +
            "   \"questions\":[\n" +
            "     {\"id\":\"c1_l2_q1\",\"text\":\"Biến dùng để làm gì?\",\"options\":[\"Lưu dữ liệu\",\"In màn hình\",\"Kết nối DB\",\"Chạy chương trình\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q2\",\"text\":\"int là kiểu gì?\",\"options\":[\"Số nguyên\",\"Chuỗi\",\"Boolean\",\"Đối tượng\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q3\",\"text\":\"double dùng để?\",\"options\":[\"Số thực\",\"Số nguyên\",\"Chuỗi\",\"Ký tự\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q4\",\"text\":\"String là kiểu gì?\",\"options\":[\"Chuỗi\",\"Số\",\"Boolean\",\"Mảng\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q5\",\"text\":\"boolean có mấy giá trị?\",\"options\":[\"2\",\"1\",\"3\",\"4\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q6\",\"text\":\"char lưu trữ gì?\",\"options\":[\"Ký tự\",\"Chuỗi\",\"Số\",\"Object\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q7\",\"text\":\"Biến phải được làm gì trước khi dùng?\",\"options\":[\"Khai báo\",\"Import\",\"Compile\",\"Deploy\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q8\",\"text\":\"final dùng để?\",\"options\":[\"Không thay đổi\",\"Lặp\",\"Rẽ nhánh\",\"Import\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q9\",\"text\":\"Kiểu dữ liệu nguyên thủy là?\",\"options\":[\"int\",\"String\",\"ArrayList\",\"Object\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l2_q10\",\"text\":\"String có phải primitive không?\",\"options\":[\"Không\",\"Có\",\"Tùy JVM\",\"Không rõ\"],\"correctIndex\":0}\n" +
            "   ]},\n" +

            "  {\"id\":null,\"lessonId\":\"c1_l3\",\"title\":\"Quiz: Cấu trúc điều khiển\",\n" +
            "   \"questions\":[\n" +
            "     {\"id\":\"c1_l3_q1\",\"text\":\"if dùng để làm gì?\",\"options\":[\"Rẽ nhánh\",\"Lặp\",\"Khai báo\",\"Import\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q2\",\"text\":\"for dùng để?\",\"options\":[\"Lặp\",\"Rẽ nhánh\",\"Khai báo\",\"Dừng\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q3\",\"text\":\"while là gì?\",\"options\":[\"Vòng lặp\",\"Điều kiện\",\"Hàm\",\"Class\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q4\",\"text\":\"switch dùng để?\",\"options\":[\"Rẽ nhánh nhiều\",\"Lặp\",\"Khai báo\",\"Import\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q5\",\"text\":\"break dùng để?\",\"options\":[\"Thoát vòng lặp\",\"Tiếp tục\",\"Khai báo\",\"Import\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q6\",\"text\":\"continue dùng để?\",\"options\":[\"Bỏ qua vòng hiện tại\",\"Thoát\",\"Kết thúc\",\"Dừng chương trình\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q7\",\"text\":\"if-else là gì?\",\"options\":[\"Rẽ nhánh\",\"Lặp\",\"Class\",\"Interface\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q8\",\"text\":\"for-each dùng cho?\",\"options\":[\"Collection\",\"Điều kiện\",\"Exception\",\"Thread\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q9\",\"text\":\"Điều kiện if trả về kiểu gì?\",\"options\":[\"boolean\",\"int\",\"String\",\"double\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l3_q10\",\"text\":\"Vòng lặp vô hạn là gì?\",\"options\":[\"Không có điều kiện dừng\",\"Lặp 1 lần\",\"Lặp 2 lần\",\"Lặp có điều kiện\"],\"correctIndex\":0}\n" +
            "   ]},\n" +

            "  {\"id\":null,\"lessonId\":\"c1_l4\",\"title\":\"Quiz: Collection\",\n" +
            "   \"questions\":[\n" +
            "     {\"id\":\"c1_l4_q1\",\"text\":\"ArrayList dùng để?\",\"options\":[\"Lưu danh sách\",\"Kết nối API\",\"Tạo file\",\"In log\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q2\",\"text\":\"List có cho phép trùng phần tử không?\",\"options\":[\"Có\",\"Không\",\"Tùy JVM\",\"Không rõ\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q3\",\"text\":\"Set có đặc điểm gì?\",\"options\":[\"Không trùng\",\"Có thứ tự\",\"Có index\",\"Có key\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q4\",\"text\":\"Map lưu dữ liệu dạng gì?\",\"options\":[\"Key-Value\",\"List\",\"Array\",\"Tree\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q5\",\"text\":\"HashMap có đảm bảo thứ tự không?\",\"options\":[\"Không\",\"Có\",\"Luôn luôn\",\"Tùy dữ liệu\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q6\",\"text\":\"ArrayList thuộc package nào?\",\"options\":[\"java.util\",\"java.io\",\"java.lang\",\"java.sql\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q7\",\"text\":\"LinkedList khác ArrayList ở điểm nào?\",\"options\":[\"Cấu trúc lưu trữ\",\"Cú pháp\",\"Package\",\"Tên\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q8\",\"text\":\"Collection là gì?\",\"options\":[\"Framework\",\"Class\",\"Method\",\"Biến\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q9\",\"text\":\"Iterator dùng để?\",\"options\":[\"Duyệt collection\",\"Sắp xếp\",\"Lưu\",\"Xóa file\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l4_q10\",\"text\":\"List có truy cập theo index không?\",\"options\":[\"Có\",\"Không\",\"Tùy loại\",\"Không rõ\"],\"correctIndex\":0}\n" +
            "   ]},\n" +

            "  {\"id\":null,\"lessonId\":\"c1_l5\",\"title\":\"Quiz: OOP\",\n" +
            "   \"questions\":[\n" +
            "     {\"id\":\"c1_l5_q1\",\"text\":\"OOP là gì?\",\"options\":[\"Lập trình hướng đối tượng\",\"Lập trình hàm\",\"Lập trình script\",\"Lập trình song song\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q2\",\"text\":\"Class là gì?\",\"options\":[\"Khuôn mẫu\",\"Đối tượng\",\"Biến\",\"Hàm\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q3\",\"text\":\"Object là gì?\",\"options\":[\"Thể hiện của class\",\"Hàm\",\"Biến\",\"Package\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q4\",\"text\":\"Encapsulation là gì?\",\"options\":[\"Đóng gói\",\"Kế thừa\",\"Đa hình\",\"Trừu tượng\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q5\",\"text\":\"Inheritance là gì?\",\"options\":[\"Kế thừa\",\"Đóng gói\",\"Interface\",\"Override\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q6\",\"text\":\"Polymorphism là gì?\",\"options\":[\"Đa hình\",\"Kế thừa\",\"Đóng gói\",\"Override\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q7\",\"text\":\"Abstract class dùng để?\",\"options\":[\"Làm lớp cha\",\"Tạo object\",\"Lưu dữ liệu\",\"Chạy main\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q8\",\"text\":\"Interface có chứa gì?\",\"options\":[\"Method abstract\",\"Biến thường\",\"Constructor\",\"Code logic\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q9\",\"text\":\"Override dùng để?\",\"options\":[\"Ghi đè method\",\"Tạo class\",\"Tạo biến\",\"Import\"],\"correctIndex\":0},\n" +
            "     {\"id\":\"c1_l5_q10\",\"text\":\"new dùng để?\",\"options\":[\"Tạo object\",\"Khai báo class\",\"Import\",\"Compile\"],\"correctIndex\":0}\n" +
            "   ]},\n" ;

    // ===== c2 =====
    private static final String QUIZZES_C2 =
            "  {\"id\":null,\"lessonId\":\"c2_l1\",\"title\":\"Quiz: Spring Boot\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c2_l1_q1\",\"text\":\"Spring Boot dùng để?\",\"options\":[\"Xây dựng backend\",\"Thiết kế UI\",\"Chỉnh ảnh\",\"Soạn thảo văn bản\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q2\",\"text\":\"Spring Boot dựa trên framework nào?\",\"options\":[\"Spring\",\"Hibernate\",\"Struts\",\"JSF\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q3\",\"text\":\"Spring Boot giúp gì cho developer?\",\"options\":[\"Cấu hình nhanh\",\"Tốn thời gian\",\"Viết SQL\",\"Thiết kế UI\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q4\",\"text\":\"File cấu hình chính của Spring Boot là?\",\"options\":[\"application.properties\",\"pom.xml\",\"index.html\",\"log.txt\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q5\",\"text\":\"Spring Boot có embedded server không?\",\"options\":[\"Có\",\"Không\",\"Tùy DB\",\"Tùy IDE\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q6\",\"text\":\"Embedded server mặc định là?\",\"options\":[\"Tomcat\",\"JBoss\",\"GlassFish\",\"WebLogic\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q7\",\"text\":\"Annotation khởi chạy Spring Boot?\",\"options\":[\"@SpringBootApplication\",\"@EnableBoot\",\"@BootApp\",\"@Main\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q8\",\"text\":\"Spring Boot phù hợp cho loại ứng dụng nào?\",\"options\":[\"Microservice\",\"Game\",\"Desktop\",\"Firmware\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q9\",\"text\":\"Spring Boot có cần XML nhiều không?\",\"options\":[\"Không\",\"Có\",\"Bắt buộc\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l1_q10\",\"text\":\"Spring Boot giúp triển khai nhanh không?\",\"options\":[\"Có\",\"Không\",\"Tùy server\",\"Tùy DB\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c2_l2\",\"title\":\"Quiz: REST Controller\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c2_l2_q1\",\"text\":\"REST API dùng để?\",\"options\":[\"Giao tiếp client-server\",\"Thiết kế giao diện\",\"Lưu file\",\"Test phần cứng\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q2\",\"text\":\"Annotation tạo REST controller?\",\"options\":[\"@RestController\",\"@Controller\",\"@Service\",\"@Component\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q3\",\"text\":\"HTTP GET dùng để?\",\"options\":[\"Lấy dữ liệu\",\"Thêm dữ liệu\",\"Xóa dữ liệu\",\"Cập nhật dữ liệu\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q4\",\"text\":\"HTTP POST dùng để?\",\"options\":[\"Tạo mới\",\"Lấy dữ liệu\",\"Xóa\",\"Cập nhật\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q5\",\"text\":\"HTTP PUT dùng để?\",\"options\":[\"Cập nhật\",\"Xóa\",\"Lấy\",\"Tạo\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q6\",\"text\":\"HTTP DELETE dùng để?\",\"options\":[\"Xóa dữ liệu\",\"Tạo mới\",\"Lấy\",\"Cập nhật\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q7\",\"text\":\"@RequestMapping dùng để?\",\"options\":[\"Mapping URL\",\"Validate\",\"Security\",\"Transaction\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q8\",\"text\":\"REST thường trả về dạng nào?\",\"options\":[\"JSON\",\"XML\",\"TXT\",\"DOC\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q9\",\"text\":\"@PathVariable dùng để?\",\"options\":[\"Nhận tham số URL\",\"Validate\",\"Parse JSON\",\"Upload file\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l2_q10\",\"text\":\"@RequestBody dùng để?\",\"options\":[\"Nhận body\",\"Nhận header\",\"Nhận path\",\"Nhận cookie\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c2_l3\",\"title\":\"Quiz: JPA\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c2_l3_q1\",\"text\":\"JPA dùng để?\",\"options\":[\"Làm việc với DB\",\"Gửi mail\",\"Cache dữ liệu\",\"Test API\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q2\",\"text\":\"JPA là viết tắt của?\",\"options\":[\"Java Persistence API\",\"Java Process API\",\"Java Primary API\",\"Java Package API\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q3\",\"text\":\"Entity được đánh dấu bằng annotation nào?\",\"options\":[\"@Entity\",\"@Table\",\"@Column\",\"@Id\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q4\",\"text\":\"@Id dùng để?\",\"options\":[\"Khóa chính\",\"Khóa ngoại\",\"Index\",\"Unique\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q5\",\"text\":\"Repository dùng để?\",\"options\":[\"CRUD DB\",\"Validate\",\"Security\",\"Log\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q6\",\"text\":\"JpaRepository thuộc package nào?\",\"options\":[\"org.springframework.data.jpa\",\"java.util\",\"javax.sql\",\"hibernate\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q7\",\"text\":\"ORM là gì?\",\"options\":[\"Ánh xạ object-table\",\"Giao tiếp mạng\",\"Cache\",\"Security\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q8\",\"text\":\"JPA thường dùng với DB nào?\",\"options\":[\"Quan hệ\",\"NoSQL\",\"File\",\"Cache\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q9\",\"text\":\"Hibernate là gì?\",\"options\":[\"JPA implementation\",\"Web server\",\"IDE\",\"Framework UI\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l3_q10\",\"text\":\"@GeneratedValue dùng để?\",\"options\":[\"Tự tăng ID\",\"Validate\",\"Map JSON\",\"Encrypt\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c2_l4\",\"title\":\"Quiz: Service Layer\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c2_l4_q1\",\"text\":\"Service dùng để?\",\"options\":[\"Xử lý business logic\",\"Render UI\",\"Gọi API\",\"Cấu hình server\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q2\",\"text\":\"Annotation đánh dấu Service?\",\"options\":[\"@Service\",\"@Component\",\"@Repository\",\"@Controller\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q3\",\"text\":\"Service nằm giữa layer nào?\",\"options\":[\"Controller và Repository\",\"UI và DB\",\"Client và Server\",\"DB và File\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q4\",\"text\":\"Service có nên chứa logic phức tạp không?\",\"options\":[\"Có\",\"Không\",\"Tùy DB\",\"Tùy UI\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q5\",\"text\":\"Controller có nên gọi DB trực tiếp không?\",\"options\":[\"Không\",\"Có\",\"Luôn luôn\",\"Tùy dự án\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q6\",\"text\":\"Service giúp gì cho code?\",\"options\":[\"Dễ bảo trì\",\"Khó đọc\",\"Chậm\",\"Rối\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q7\",\"text\":\"@Transactional thường đặt ở đâu?\",\"options\":[\"Service\",\"Controller\",\"Entity\",\"Config\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q8\",\"text\":\"Service có thể gọi nhiều repository không?\",\"options\":[\"Có\",\"Không\",\"Bắt buộc 1\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q9\",\"text\":\"Service có trả DTO không?\",\"options\":[\"Có\",\"Không\",\"Không nên\",\"Bắt buộc\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l4_q10\",\"text\":\"Service thuộc layer nào?\",\"options\":[\"Business\",\"Presentation\",\"Database\",\"Infrastructure\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c2_l5\",\"title\":\"Quiz: Security\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c2_l5_q1\",\"text\":\"Security dùng để?\",\"options\":[\"Bảo mật\",\"Trang trí\",\"Logging\",\"Debug\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q2\",\"text\":\"Spring Security dùng để?\",\"options\":[\"Xác thực & phân quyền\",\"Cache\",\"ORM\",\"UI\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q3\",\"text\":\"Authentication là gì?\",\"options\":[\"Xác thực\",\"Phân quyền\",\"Mã hóa\",\"Log\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q4\",\"text\":\"Authorization là gì?\",\"options\":[\"Phân quyền\",\"Xác thực\",\"Cache\",\"Encrypt\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q5\",\"text\":\"JWT là gì?\",\"options\":[\"Token\",\"Session\",\"Cookie\",\"Header\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q6\",\"text\":\"Token thường gửi qua đâu?\",\"options\":[\"Header\",\"Body\",\"Path\",\"Cookie\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q7\",\"text\":\"BCrypt dùng để?\",\"options\":[\"Mã hóa mật khẩu\",\"Gửi mail\",\"Cache\",\"Validate\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q8\",\"text\":\"Role dùng để?\",\"options\":[\"Phân quyền\",\"Xác thực\",\"Log\",\"Cache\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q9\",\"text\":\"Spring Security có filter không?\",\"options\":[\"Có\",\"Không\",\"Tùy DB\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c2_l5_q10\",\"text\":\"Security giúp hệ thống?\",\"options\":[\"An toàn\",\"Chậm\",\"Rối\",\"Khó dùng\"],\"correctIndex\":0}\n" +
                    "   ]},\n";

    // ===== c3 =====
    private static final String QUIZZES_C3 =
            "  {\"id\":null,\"lessonId\":\"c3_l1\",\"title\":\"Quiz: JavaScript\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c3_l1_q1\",\"text\":\"JavaScript dùng để?\",\"options\":[\"Lập trình web\",\"Soạn thảo\",\"Vẽ ảnh\",\"Tạo DB\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q2\",\"text\":\"JavaScript chạy ở đâu?\",\"options\":[\"Trình duyệt\",\"Database\",\"Server vật lý\",\"Hệ điều hành\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q3\",\"text\":\"JavaScript là ngôn ngữ gì?\",\"options\":[\"Thông dịch\",\"Biên dịch\",\"Assembly\",\"Markup\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q4\",\"text\":\"JS có thể chạy backend không?\",\"options\":[\"Có\",\"Không\",\"Chỉ frontend\",\"Chỉ mobile\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q5\",\"text\":\"Node.js dùng để?\",\"options\":[\"Chạy JS backend\",\"Viết CSS\",\"Thiết kế UI\",\"Soạn thảo HTML\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q6\",\"text\":\"JavaScript có hỗ trợ OOP không?\",\"options\":[\"Có\",\"Không\",\"Một phần\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q7\",\"text\":\"JS thường dùng kết hợp với?\",\"options\":[\"HTML & CSS\",\"Java\",\"Python\",\"SQL\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q8\",\"text\":\"JavaScript có chạy đa nền tảng không?\",\"options\":[\"Có\",\"Không\",\"Chỉ Windows\",\"Chỉ Linux\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q9\",\"text\":\"File JavaScript có đuôi là?\",\"options\":[\".js\",\".java\",\".html\",\".css\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l1_q10\",\"text\":\"JavaScript phổ biến nhất trong lĩnh vực nào?\",\"options\":[\"Web\",\"AI\",\"IoT\",\"Embedded\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c3_l2\",\"title\":\"Quiz: Biến JS\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c3_l2_q1\",\"text\":\"let dùng để?\",\"options\":[\"Khai báo biến\",\"Gọi API\",\"Import file\",\"In log\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q2\",\"text\":\"const dùng để?\",\"options\":[\"Biến không đổi\",\"Biến toàn cục\",\"Hàm\",\"Class\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q3\",\"text\":\"var có phạm vi gì?\",\"options\":[\"Function scope\",\"Block scope\",\"Class scope\",\"Global only\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q4\",\"text\":\"let có phạm vi gì?\",\"options\":[\"Block scope\",\"Function scope\",\"Global\",\"Class\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q5\",\"text\":\"Có thể gán lại giá trị cho const không?\",\"options\":[\"Không\",\"Có\",\"Tùy trình duyệt\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q6\",\"text\":\"Kiểu dữ liệu của JS là?\",\"options\":[\"Dynamic\",\"Static\",\"Primitive only\",\"Object only\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q7\",\"text\":\"typeof dùng để?\",\"options\":[\"Kiểm tra kiểu\",\"Khai báo biến\",\"So sánh\",\"Ép kiểu\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q8\",\"text\":\"Giá trị undefined là gì?\",\"options\":[\"Chưa gán\",\"Null\",\"0\",\"False\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q9\",\"text\":\"null nghĩa là?\",\"options\":[\"Không có giá trị\",\"Chưa khai báo\",\"0\",\"False\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l2_q10\",\"text\":\"JS có kiểu int riêng không?\",\"options\":[\"Không\",\"Có\",\"Tùy phiên bản\",\"Không rõ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c3_l3\",\"title\":\"Quiz: DOM\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c3_l3_q1\",\"text\":\"DOM là gì?\",\"options\":[\"Cấu trúc HTML\",\"Server\",\"Database\",\"Framework\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q2\",\"text\":\"DOM viết tắt của?\",\"options\":[\"Document Object Model\",\"Data Object Model\",\"Document Order Model\",\"Digital Object Model\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q3\",\"text\":\"DOM cho phép làm gì?\",\"options\":[\"Thao tác HTML\",\"Kết nối DB\",\"Gửi mail\",\"Compile code\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q4\",\"text\":\"getElementById dùng để?\",\"options\":[\"Lấy phần tử\",\"Xóa phần tử\",\"Thêm phần tử\",\"Ẩn phần tử\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q5\",\"text\":\"querySelector trả về gì?\",\"options\":[\"Phần tử đầu tiên\",\"Danh sách\",\"Boolean\",\"String\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q6\",\"text\":\"innerHTML dùng để?\",\"options\":[\"Thay đổi nội dung\",\"Đổi style\",\"Ẩn\",\"Xóa\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q7\",\"text\":\"DOM thuộc về phía nào?\",\"options\":[\"Client\",\"Server\",\"Database\",\"API\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q8\",\"text\":\"addEventListener dùng để?\",\"options\":[\"Bắt sự kiện\",\"Render\",\"Gửi request\",\"Lưu file\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q9\",\"text\":\"Click là loại gì?\",\"options\":[\"Event\",\"Method\",\"Class\",\"Variable\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l3_q10\",\"text\":\"DOM có thể thao tác CSS không?\",\"options\":[\"Có\",\"Không\",\"Tùy browser\",\"Không rõ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c3_l4\",\"title\":\"Quiz: Async\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c3_l4_q1\",\"text\":\"Async dùng để?\",\"options\":[\"Xử lý bất đồng bộ\",\"Tạo UI\",\"Style CSS\",\"Lưu file\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q2\",\"text\":\"Promise dùng để?\",\"options\":[\"Xử lý async\",\"Khai báo biến\",\"DOM\",\"Style\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q3\",\"text\":\"Promise có mấy trạng thái?\",\"options\":[\"3\",\"2\",\"4\",\"5\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q4\",\"text\":\"async/await giúp gì?\",\"options\":[\"Code dễ đọc\",\"Chạy nhanh hơn\",\"Giảm RAM\",\"Bỏ callback\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q5\",\"text\":\"then() dùng khi nào?\",\"options\":[\"Promise thành công\",\"Promise lỗi\",\"Khai báo\",\"Hủy\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q6\",\"text\":\"catch() dùng để?\",\"options\":[\"Bắt lỗi\",\"Thành công\",\"Delay\",\"Loop\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q7\",\"text\":\"fetch dùng để?\",\"options\":[\"Gọi API\",\"Thao tác DOM\",\"Ghi file\",\"Style\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q8\",\"text\":\"Async có chặn luồng không?\",\"options\":[\"Không\",\"Có\",\"Tùy hàm\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q9\",\"text\":\"Callback hell là gì?\",\"options\":[\"Lồng callback\",\"Lỗi cú pháp\",\"Promise lỗi\",\"DOM lỗi\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l4_q10\",\"text\":\"Async thường dùng trong?\",\"options\":[\"API\",\"CSS\",\"HTML\",\"Image\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c3_l5\",\"title\":\"Quiz: Mini Project\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c3_l5_q1\",\"text\":\"Mini project giúp?\",\"options\":[\"Thực hành\",\"Lý thuyết\",\"Cài đặt\",\"Cấu hình\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q2\",\"text\":\"Mini project thường dùng để?\",\"options\":[\"Củng cố kiến thức\",\"Trang trí\",\"Test DB\",\"Deploy server\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q3\",\"text\":\"Mini project có nên làm cá nhân không?\",\"options\":[\"Có\",\"Không\",\"Bắt buộc nhóm\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q4\",\"text\":\"Mini project thường dùng công nghệ nào?\",\"options\":[\"HTML CSS JS\",\"Java Swing\",\"C++\",\"Assembly\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q5\",\"text\":\"Mini project giúp đánh giá điều gì?\",\"options\":[\"Kỹ năng\",\"Lý thuyết\",\"Cấu hình\",\"Học thuộc\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q6\",\"text\":\"Mini project có deadline không?\",\"options\":[\"Có\",\"Không\",\"Tùy giáo viên\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q7\",\"text\":\"Mini project có cần UI không?\",\"options\":[\"Có\",\"Không\",\"Tùy đề\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q8\",\"text\":\"Mini project nên bắt đầu từ đâu?\",\"options\":[\"Phân tích yêu cầu\",\"Viết code ngay\",\"Deploy\",\"Test\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q9\",\"text\":\"Mini project có cần dùng async không?\",\"options\":[\"Có thể\",\"Không\",\"Bắt buộc\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c3_l5_q10\",\"text\":\"Mini project giúp chuẩn bị cho?\",\"options\":[\"Dự án lớn\",\"Lý thuyết\",\"Thi cử\",\"Cài máy\"],\"correctIndex\":0}\n" +
                    "   ]},\n";

    // ===== c4 =====
    private static final String QUIZZES_C4 =
            "  {\"id\":null,\"lessonId\":\"c4_l1\",\"title\":\"Quiz: Python\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c4_l1_q1\",\"text\":\"Python là gì?\",\"options\":[\"Ngôn ngữ lập trình\",\"Trình duyệt\",\"IDE\",\"Database\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q2\",\"text\":\"Python là ngôn ngữ gì?\",\"options\":[\"Thông dịch\",\"Biên dịch\",\"Assembly\",\"Markup\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q3\",\"text\":\"Python có dễ học không?\",\"options\":[\"Có\",\"Không\",\"Khó\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q4\",\"text\":\"File Python có đuôi là?\",\"options\":[\".py\",\".java\",\".js\",\".exe\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q5\",\"text\":\"Python chạy đa nền tảng không?\",\"options\":[\"Có\",\"Không\",\"Chỉ Windows\",\"Chỉ Linux\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q6\",\"text\":\"Python phổ biến trong lĩnh vực nào?\",\"options\":[\"Data & AI\",\"Game console\",\"Firmware\",\"Assembly\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q7\",\"text\":\"Python có hỗ trợ OOP không?\",\"options\":[\"Có\",\"Không\",\"Một phần\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q8\",\"text\":\"print() dùng để?\",\"options\":[\"In ra màn hình\",\"Lưu file\",\"Gọi API\",\"Kết nối DB\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q9\",\"text\":\"Python có cần dấu ; không?\",\"options\":[\"Không\",\"Có\",\"Tùy IDE\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l1_q10\",\"text\":\"Python phù hợp cho người mới không?\",\"options\":[\"Có\",\"Không\",\"Khó\",\"Không rõ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c4_l2\",\"title\":\"Quiz: Kiểu dữ liệu Python\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c4_l2_q1\",\"text\":\"List dùng để?\",\"options\":[\"Lưu nhiều giá trị\",\"Kết nối DB\",\"Gửi mail\",\"In log\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q2\",\"text\":\"Tuple có thay đổi được không?\",\"options\":[\"Không\",\"Có\",\"Tùy IDE\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q3\",\"text\":\"Set có đặc điểm gì?\",\"options\":[\"Không trùng\",\"Có thứ tự\",\"Có index\",\"Có key\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q4\",\"text\":\"Dict lưu dữ liệu dạng gì?\",\"options\":[\"Key-Value\",\"List\",\"Array\",\"Tuple\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q5\",\"text\":\"int là kiểu gì?\",\"options\":[\"Số nguyên\",\"Số thực\",\"Chuỗi\",\"Boolean\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q6\",\"text\":\"float dùng để?\",\"options\":[\"Số thực\",\"Số nguyên\",\"Chuỗi\",\"List\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q7\",\"text\":\"str là kiểu gì?\",\"options\":[\"Chuỗi\",\"Số\",\"Boolean\",\"Object\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q8\",\"text\":\"bool có mấy giá trị?\",\"options\":[\"2\",\"1\",\"3\",\"4\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q9\",\"text\":\"None nghĩa là gì?\",\"options\":[\"Không có giá trị\",\"0\",\"False\",\"Rỗng\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l2_q10\",\"text\":\"Python là kiểu dữ liệu gì?\",\"options\":[\"Dynamic\",\"Static\",\"Primitive\",\"Strong only\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c4_l3\",\"title\":\"Quiz: Control Flow\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c4_l3_q1\",\"text\":\"if dùng để?\",\"options\":[\"Rẽ nhánh\",\"Lặp\",\"Import\",\"Khai báo\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q2\",\"text\":\"for dùng để?\",\"options\":[\"Lặp\",\"Rẽ nhánh\",\"Import\",\"Khai báo\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q3\",\"text\":\"while là gì?\",\"options\":[\"Vòng lặp\",\"Điều kiện\",\"Hàm\",\"Class\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q4\",\"text\":\"break dùng để?\",\"options\":[\"Thoát vòng lặp\",\"Tiếp tục\",\"Khai báo\",\"Import\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q5\",\"text\":\"continue dùng để?\",\"options\":[\"Bỏ qua vòng hiện tại\",\"Thoát\",\"Dừng chương trình\",\"Import\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q6\",\"text\":\"elif là gì?\",\"options\":[\"else if\",\"Vòng lặp\",\"Hàm\",\"Class\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q7\",\"text\":\"range() dùng để?\",\"options\":[\"Tạo dãy số\",\"So sánh\",\"Ép kiểu\",\"In\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q8\",\"text\":\"for-each trong Python là?\",\"options\":[\"for item in list\",\"foreach()\",\"loop()\",\"each()\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q9\",\"text\":\"Điều kiện if trả về kiểu gì?\",\"options\":[\"Boolean\",\"Int\",\"String\",\"Float\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l3_q10\",\"text\":\"Vòng lặp vô hạn là gì?\",\"options\":[\"Không có điều kiện dừng\",\"Lặp 1 lần\",\"Lặp 2 lần\",\"Lặp có điều kiện\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c4_l4\",\"title\":\"Quiz: Dictionary\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c4_l4_q1\",\"text\":\"Dict lưu theo?\",\"options\":[\"Key-Value\",\"Index\",\"File\",\"Row\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q2\",\"text\":\"Key trong dict có trùng được không?\",\"options\":[\"Không\",\"Có\",\"Tùy Python\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q3\",\"text\":\"Value trong dict có trùng được không?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tùy IDE\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q4\",\"text\":\"Cú pháp tạo dict là?\",\"options\":[\"{}\",\"[]\",\"()\",\"<>\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q5\",\"text\":\"dict.get() dùng để?\",\"options\":[\"Lấy giá trị\",\"Xóa\",\"Thêm\",\"Update\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q6\",\"text\":\"dict.keys() trả về?\",\"options\":[\"Danh sách key\",\"Danh sách value\",\"Cặp key-value\",\"Boolean\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q7\",\"text\":\"dict.values() trả về?\",\"options\":[\"Danh sách value\",\"Danh sách key\",\"Index\",\"Boolean\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q8\",\"text\":\"dict.items() trả về?\",\"options\":[\"Key-Value\",\"Key\",\"Value\",\"Index\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q9\",\"text\":\"Dict có thứ tự không (Python 3.7+)?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tùy\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l4_q10\",\"text\":\"Dict thường dùng cho?\",\"options\":[\"Dữ liệu ánh xạ\",\"Danh sách\",\"Số\",\"Chuỗi\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c4_l5\",\"title\":\"Quiz: Data\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c4_l5_q1\",\"text\":\"Python dùng cho data?\",\"options\":[\"Đúng\",\"Sai\",\"Không biết\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q2\",\"text\":\"Thư viện xử lý dữ liệu phổ biến?\",\"options\":[\"Pandas\",\"Flask\",\"Django\",\"Tkinter\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q3\",\"text\":\"NumPy dùng để?\",\"options\":[\"Tính toán số\",\"Vẽ UI\",\"Gửi mail\",\"Web\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q4\",\"text\":\"Data analysis là gì?\",\"options\":[\"Phân tích dữ liệu\",\"Thiết kế UI\",\"Deploy\",\"Test\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q5\",\"text\":\"DataFrame thuộc thư viện nào?\",\"options\":[\"Pandas\",\"NumPy\",\"Matplotlib\",\"Seaborn\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q6\",\"text\":\"CSV là gì?\",\"options\":[\"File dữ liệu\",\"Image\",\"Executable\",\"Binary\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q7\",\"text\":\"Python có dùng cho ML không?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q8\",\"text\":\"Matplotlib dùng để?\",\"options\":[\"Vẽ biểu đồ\",\"Kết nối DB\",\"API\",\"UI\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q9\",\"text\":\"Seaborn dựa trên thư viện nào?\",\"options\":[\"Matplotlib\",\"Pandas\",\"NumPy\",\"TensorFlow\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l5_q10\",\"text\":\"Python phổ biến trong data science không?\",\"options\":[\"Có\",\"Không\",\"Ít\",\"Không rõ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c4_l6\",\"title\":\"Quiz: Ứng dụng\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c4_l6_q1\",\"text\":\"Ứng dụng Python?\",\"options\":[\"Phân tích dữ liệu\",\"Chơi game\",\"Soạn thảo\",\"Vẽ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q2\",\"text\":\"Python dùng làm web với framework nào?\",\"options\":[\"Django\",\"Spring\",\"Laravel\",\"Express\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q3\",\"text\":\"Flask là gì?\",\"options\":[\"Web framework\",\"IDE\",\"DB\",\"Tool\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q4\",\"text\":\"Python dùng cho AI không?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q5\",\"text\":\"Python dùng cho automation không?\",\"options\":[\"Có\",\"Không\",\"Tuỳ\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q6\",\"text\":\"Python có dùng cho game không?\",\"options\":[\"Có\",\"Không\",\"Ít\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q7\",\"text\":\"Thư viện AI phổ biến?\",\"options\":[\"TensorFlow\",\"Bootstrap\",\"React\",\"Vue\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q8\",\"text\":\"Python dùng cho scripting không?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q9\",\"text\":\"Python dùng cho desktop app với?\",\"options\":[\"Tkinter\",\"Spring\",\"Swing\",\"Qt Java\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c4_l6_q10\",\"text\":\"Python phù hợp nhất cho?\",\"options\":[\"Đa mục đích\",\"Chỉ web\",\"Chỉ game\",\"Chỉ mobile\"],\"correctIndex\":0}\n" +
                    "   ]},\n";

    // ===== c5 =====
    private static final String QUIZZES_C5 =
            "  {\"id\":null,\"lessonId\":\"c5_l1\",\"title\":\"Quiz: HTML\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c5_l1_q1\",\"text\":\"HTML dùng để?\",\"options\":[\"Tạo cấu trúc web\",\"Style\",\"Logic\",\"DB\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q2\",\"text\":\"HTML là viết tắt của?\",\"options\":[\"HyperText Markup Language\",\"HighText Machine Language\",\"Hyper Tool Markup Language\",\"Home Tool Markup Language\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q3\",\"text\":\"HTML có phải ngôn ngữ lập trình không?\",\"options\":[\"Không\",\"Có\",\"Một phần\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q4\",\"text\":\"Thẻ nào tạo đoạn văn?\",\"options\":[\"<p>\",\"<div>\",\"<span>\",\"<h1>\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q5\",\"text\":\"Thẻ nào tạo liên kết?\",\"options\":[\"<a>\",\"<link>\",\"<href>\",\"<url>\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q6\",\"text\":\"Thuộc tính src dùng cho?\",\"options\":[\"Ảnh / media\",\"Style\",\"Text\",\"Script\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q7\",\"text\":\"HTML có bao nhiêu thẻ h1-h6?\",\"options\":[\"6\",\"3\",\"5\",\"8\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q8\",\"text\":\"Thẻ img dùng để?\",\"options\":[\"Hiển thị ảnh\",\"Hiển thị text\",\"Layout\",\"Script\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q9\",\"text\":\"HTML có thể nhúng CSS không?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l1_q10\",\"text\":\"HTML là phần nào của web?\",\"options\":[\"Cấu trúc\",\"Giao diện\",\"Logic\",\"Database\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c5_l2\",\"title\":\"Quiz: CSS\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c5_l2_q1\",\"text\":\"CSS dùng để?\",\"options\":[\"Trang trí web\",\"Logic\",\"DB\",\"API\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q2\",\"text\":\"CSS là viết tắt của?\",\"options\":[\"Cascading Style Sheets\",\"Computer Style Sheets\",\"Creative Style Sheets\",\"Colorful Style Sheets\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q3\",\"text\":\"CSS dùng để làm gì?\",\"options\":[\"Tạo giao diện\",\"Xử lý logic\",\"Kết nối DB\",\"Gửi mail\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q4\",\"text\":\"Thuộc tính nào đổi màu chữ?\",\"options\":[\"color\",\"background\",\"font-size\",\"border\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q5\",\"text\":\"background-color dùng để?\",\"options\":[\"Màu nền\",\"Màu chữ\",\"Viền\",\"Khoảng cách\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q6\",\"text\":\"margin dùng để?\",\"options\":[\"Khoảng cách ngoài\",\"Khoảng cách trong\",\"Viền\",\"Căn giữa\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q7\",\"text\":\"padding dùng để?\",\"options\":[\"Khoảng cách trong\",\"Khoảng cách ngoài\",\"Viền\",\"Căn lề\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q8\",\"text\":\"display:none dùng để?\",\"options\":[\"Ẩn phần tử\",\"Xóa\",\"Disable\",\"Fade\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q9\",\"text\":\"CSS có thể viết inline không?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l2_q10\",\"text\":\"CSS là phần nào của web?\",\"options\":[\"Giao diện\",\"Cấu trúc\",\"Logic\",\"DB\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c5_l3\",\"title\":\"Quiz: Flexbox\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c5_l3_q1\",\"text\":\"Flexbox dùng để?\",\"options\":[\"Layout\",\"Logic\",\"API\",\"DB\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q2\",\"text\":\"display nào kích hoạt flexbox?\",\"options\":[\"flex\",\"block\",\"inline\",\"grid\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q3\",\"text\":\"flex-direction dùng để?\",\"options\":[\"Hướng sắp xếp\",\"Khoảng cách\",\"Căn giữa\",\"Ẩn\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q4\",\"text\":\"justify-content dùng để?\",\"options\":[\"Căn trục chính\",\"Căn trục phụ\",\"Đổi chiều\",\"Ẩn\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q5\",\"text\":\"align-items dùng để?\",\"options\":[\"Căn trục phụ\",\"Căn trục chính\",\"Đổi hướng\",\"Ẩn\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q6\",\"text\":\"flex-wrap dùng để?\",\"options\":[\"Xuống dòng\",\"Căn giữa\",\"Ẩn\",\"Đổi chiều\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q7\",\"text\":\"gap dùng để?\",\"options\":[\"Khoảng cách\",\"Màu\",\"Căn lề\",\"Ẩn\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q8\",\"text\":\"Flexbox layout theo chiều nào?\",\"options\":[\"1 chiều\",\"2 chiều\",\"3 chiều\",\"Không chiều\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q9\",\"text\":\"Item trong flexbox gọi là?\",\"options\":[\"Flex item\",\"Flex child\",\"Box\",\"Node\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l3_q10\",\"text\":\"Flexbox phù hợp cho?\",\"options\":[\"Layout nhỏ\",\"Layout phức tạp\",\"Logic\",\"DB\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c5_l4\",\"title\":\"Quiz: Website\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c5_l4_q1\",\"text\":\"Hoàn thiện web để?\",\"options\":[\"Sử dụng\",\"Test\",\"Xoá\",\"Debug\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q2\",\"text\":\"Website cần những thành phần nào?\",\"options\":[\"HTML CSS JS\",\"Java SQL\",\"Python DB\",\"C++\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q3\",\"text\":\"Responsive là gì?\",\"options\":[\"Tương thích màn hình\",\"Nhanh\",\"Bảo mật\",\"Debug\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q4\",\"text\":\"Media query dùng để?\",\"options\":[\"Responsive\",\"Animation\",\"Logic\",\"DB\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q5\",\"text\":\"Website cần test trên?\",\"options\":[\"Nhiều trình duyệt\",\"1 trình duyệt\",\"Không cần\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q6\",\"text\":\"SEO dùng để?\",\"options\":[\"Tối ưu tìm kiếm\",\"Trang trí\",\"Logic\",\"DB\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q7\",\"text\":\"Performance là gì?\",\"options\":[\"Tốc độ\",\"Bảo mật\",\"Giao diện\",\"Logic\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q8\",\"text\":\"Deploy web nghĩa là?\",\"options\":[\"Đưa lên server\",\"Xóa\",\"Test\",\"Debug\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q9\",\"text\":\"Hosting là gì?\",\"options\":[\"Nơi lưu web\",\"Framework\",\"IDE\",\"Tool\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c5_l4_q10\",\"text\":\"Website hoàn chỉnh cần?\",\"options\":[\"Hoạt động ổn định\",\"Code đẹp\",\"Nhiều màu\",\"Ít file\"],\"correctIndex\":0}\n" +
                    "   ]},\n";

    // ===== c6 =====
    private static final String QUIZZES_C6 =
            "  {\"id\":null,\"lessonId\":\"c6_l1\",\"title\":\"Quiz: SQL\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c6_l1_q1\",\"text\":\"SQL dùng để?\",\"options\":[\"Truy vấn dữ liệu\",\"Thiết kế UI\",\"Viết game\",\"Vẽ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q2\",\"text\":\"SQL là viết tắt của?\",\"options\":[\"Structured Query Language\",\"Simple Query Language\",\"Sequential Query Language\",\"Standard Query Language\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q3\",\"text\":\"SQL dùng với loại DB nào?\",\"options\":[\"Quan hệ\",\"NoSQL\",\"File\",\"Cache\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q4\",\"text\":\"Bảng trong DB gọi là?\",\"options\":[\"Table\",\"Row\",\"Column\",\"Index\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q5\",\"text\":\"Dòng dữ liệu trong bảng là?\",\"options\":[\"Row\",\"Column\",\"Table\",\"Key\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q6\",\"text\":\"Cột trong bảng gọi là?\",\"options\":[\"Column\",\"Row\",\"Index\",\"Schema\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q7\",\"text\":\"SQL có phân biệt hoa thường không?\",\"options\":[\"Không\",\"Có\",\"Tuỳ DB\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q8\",\"text\":\"SQL thường dùng trong layer nào?\",\"options\":[\"Database\",\"UI\",\"Frontend\",\"Client\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q9\",\"text\":\"SQL có phải ngôn ngữ lập trình không?\",\"options\":[\"Không\",\"Có\",\"Một phần\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l1_q10\",\"text\":\"SQL quan trọng với backend không?\",\"options\":[\"Có\",\"Không\",\"Ít\",\"Không rõ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c6_l2\",\"title\":\"Quiz: SELECT\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c6_l2_q1\",\"text\":\"SELECT dùng để?\",\"options\":[\"Lấy dữ liệu\",\"Xoá\",\"Cập nhật\",\"Tạo bảng\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q2\",\"text\":\"Cú pháp SELECT cơ bản là?\",\"options\":[\"SELECT * FROM table\",\"GET * FROM table\",\"FETCH table\",\"READ table\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q3\",\"text\":\"WHERE dùng để?\",\"options\":[\"Lọc dữ liệu\",\"Sắp xếp\",\"Nhóm\",\"Gộp\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q4\",\"text\":\"ORDER BY dùng để?\",\"options\":[\"Sắp xếp\",\"Lọc\",\"Nhóm\",\"Xoá\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q5\",\"text\":\"ASC nghĩa là?\",\"options\":[\"Tăng dần\",\"Giảm dần\",\"Ngẫu nhiên\",\"Không đổi\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q6\",\"text\":\"DESC nghĩa là?\",\"options\":[\"Giảm dần\",\"Tăng dần\",\"Ngẫu nhiên\",\"Không đổi\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q7\",\"text\":\"LIMIT dùng để?\",\"options\":[\"Giới hạn dòng\",\"Giới hạn cột\",\"Xoá\",\"Gộp\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q8\",\"text\":\"DISTINCT dùng để?\",\"options\":[\"Loại trùng\",\"Sắp xếp\",\"Nhóm\",\"Đếm\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q9\",\"text\":\"SELECT có thể lấy nhiều cột không?\",\"options\":[\"Có\",\"Không\",\"Tuỳ DB\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l2_q10\",\"text\":\"SELECT có lấy được tất cả cột không?\",\"options\":[\"Có\",\"Không\",\"Không rõ\",\"Tuỳ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c6_l3\",\"title\":\"Quiz: JOIN\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c6_l3_q1\",\"text\":\"JOIN dùng để?\",\"options\":[\"Kết hợp bảng\",\"Xoá DB\",\"Backup\",\"Index\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q2\",\"text\":\"INNER JOIN trả về gì?\",\"options\":[\"Dòng khớp\",\"Tất cả\",\"Chỉ trái\",\"Chỉ phải\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q3\",\"text\":\"LEFT JOIN trả về gì?\",\"options\":[\"Tất cả bảng trái\",\"Chỉ bảng phải\",\"Chỉ khớp\",\"Không gì\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q4\",\"text\":\"RIGHT JOIN trả về gì?\",\"options\":[\"Tất cả bảng phải\",\"Chỉ bảng trái\",\"Chỉ khớp\",\"Không gì\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q5\",\"text\":\"FULL JOIN trả về?\",\"options\":[\"Tất cả dữ liệu\",\"Chỉ khớp\",\"Trái\",\"Phải\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q6\",\"text\":\"JOIN dựa trên cái gì?\",\"options\":[\"Khóa\",\"Index\",\"Tên bảng\",\"Thứ tự\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q7\",\"text\":\"JOIN thường dùng trong?\",\"options\":[\"Truy vấn nhiều bảng\",\"Insert\",\"Update\",\"Delete\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q8\",\"text\":\"JOIN có làm chậm query không?\",\"options\":[\"Có thể\",\"Không\",\"Luôn luôn\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q9\",\"text\":\"JOIN dùng nhiều có cần index không?\",\"options\":[\"Nên có\",\"Không cần\",\"Không rõ\",\"Tuỳ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l3_q10\",\"text\":\"JOIN là kiến thức quan trọng không?\",\"options\":[\"Có\",\"Không\",\"Ít\",\"Tuỳ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c6_l4\",\"title\":\"Quiz: GROUP BY\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c6_l4_q1\",\"text\":\"GROUP BY dùng để?\",\"options\":[\"Nhóm dữ liệu\",\"Xoá\",\"Insert\",\"Update\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q2\",\"text\":\"GROUP BY thường đi với?\",\"options\":[\"Hàm tổng hợp\",\"JOIN\",\"LIMIT\",\"DISTINCT\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q3\",\"text\":\"COUNT dùng để?\",\"options\":[\"Đếm\",\"Cộng\",\"Trung bình\",\"Lớn nhất\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q4\",\"text\":\"SUM dùng để?\",\"options\":[\"Cộng\",\"Đếm\",\"Trung bình\",\"So sánh\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q5\",\"text\":\"AVG dùng để?\",\"options\":[\"Trung bình\",\"Cộng\",\"Đếm\",\"So sánh\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q6\",\"text\":\"MAX dùng để?\",\"options\":[\"Giá trị lớn nhất\",\"Nhỏ nhất\",\"Đếm\",\"So sánh\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q7\",\"text\":\"MIN dùng để?\",\"options\":[\"Giá trị nhỏ nhất\",\"Lớn nhất\",\"Đếm\",\"So sánh\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q8\",\"text\":\"HAVING dùng để?\",\"options\":[\"Lọc sau group\",\"Lọc trước\",\"Join\",\"Sort\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q9\",\"text\":\"WHERE và HAVING khác nhau ở đâu?\",\"options\":[\"Thứ tự lọc\",\"Cú pháp\",\"Hiệu năng\",\"Không khác\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l4_q10\",\"text\":\"GROUP BY có bắt buộc không?\",\"options\":[\"Không\",\"Có\",\"Tuỳ\",\"Không rõ\"],\"correctIndex\":0}\n" +
                    "   ]},\n" +

                    "  {\"id\":null,\"lessonId\":\"c6_l5\",\"title\":\"Quiz: Ứng dụng SQL\",\n" +
                    "   \"questions\":[\n" +
                    "     {\"id\":\"c6_l5_q1\",\"text\":\"SQL cần cho backend?\",\"options\":[\"Có\",\"Không\",\"Tuỳ\",\"Ít\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q2\",\"text\":\"Backend thường dùng SQL để?\",\"options\":[\"Lưu & truy vấn\",\"Vẽ UI\",\"Test\",\"Debug\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q3\",\"text\":\"ORM có thay thế SQL hoàn toàn không?\",\"options\":[\"Không\",\"Có\",\"Tuỳ\",\"Không rõ\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q4\",\"text\":\"SQL Injection là gì?\",\"options\":[\"Lỗ hổng bảo mật\",\"Framework\",\"Tool\",\"Index\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q5\",\"text\":\"PreparedStatement giúp gì?\",\"options\":[\"Chống injection\",\"Nhanh hơn\",\"Dễ viết\",\"Ít code\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q6\",\"text\":\"Transaction dùng để?\",\"options\":[\"Đảm bảo toàn vẹn\",\"Tăng tốc\",\"Cache\",\"Log\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q7\",\"text\":\"ACID là gì?\",\"options\":[\"Nguyên tắc transaction\",\"Chuẩn SQL\",\"Framework\",\"Index\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q8\",\"text\":\"Index giúp gì?\",\"options\":[\"Tăng tốc truy vấn\",\"Trang trí\",\"Bảo mật\",\"Backup\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q9\",\"text\":\"Backup DB để?\",\"options\":[\"Phục hồi dữ liệu\",\"Trang trí\",\"Debug\",\"Test\"],\"correctIndex\":0},\n" +
                    "     {\"id\":\"c6_l5_q10\",\"text\":\"SQL là kỹ năng bắt buộc backend?\",\"options\":[\"Có\",\"Không\",\"Tuỳ\",\"Ít\"],\"correctIndex\":0}\n" +
                    "   ]}\n" +
                    "]";

    // ===== FINAL JSON =====
    public static final String QUIZZES_JSON =
            QUIZZES_C1 +
                    QUIZZES_C2 +
                    QUIZZES_C3 +
                    QUIZZES_C4 +
                    QUIZZES_C5 +
                    QUIZZES_C6;
}

