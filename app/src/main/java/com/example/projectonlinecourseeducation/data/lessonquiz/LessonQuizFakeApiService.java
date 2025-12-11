// file: data/lessonquiz/LessonQuizFakeApiService.java
package com.example.projectonlinecourseeducation.data.lessonquiz;

import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizAttempt;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lessonprogress.LessonProgressApi;
import com.example.projectonlinecourseeducation.core.model.lesson.LessonProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fake in-memory implementation của LessonQuizApi
 *
 * - Seed từ JSON, parse bằng org.json
 * - ENFORCE: mỗi QuizQuestion phải có đúng 4 options; correctOptionIndex phải trong [0..3].
 * - ENFORCE: mỗi Quiz có chính xác EXPECTED_QUESTION_COUNT câu (mặc định 10).
 * - submitQuizAttempt: kiểm tra quiz tồn tại và quiz hợp lệ, kiểm tra LessonProgress.isCompleted(),
 *   chấm điểm, lưu attempt, trả QuizAttempt.
 * - Khi attempt lưu thành công -> notify attempt listeners (onAttemptSubmitted).
 */
public class LessonQuizFakeApiService implements LessonQuizApi {

    // Singleton
    private static LessonQuizFakeApiService instance;
    public static LessonQuizFakeApiService getInstance() {
        if (instance == null) instance = new LessonQuizFakeApiService();
        return instance;
    }

    // ------------------ Seed JSON (dev) ------------------
    private static final String QUIZZES_JSON = "[\n" +
            "  {\n" +
            "    \"id\":null,\n" +
            "    \"lessonId\":\"c1_l1\",\n" +
            "    \"title\":\"Quiz: Kiểm tra nhanh Giới thiệu Java\",\n" +
            "    \"questions\": [\n" +
            "      {\"id\":\"c1_l1_q1\",\"text\":\"Câu 1: Java là gì?\",\"options\":[\"Ngôn ngữ lập trình hướng đối tượng\",\"Một hệ điều hành\",\"Một cơ sở dữ liệu\",\"Một công cụ thiết kế\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q2\",\"text\":\"Câu 2: JDK viết tắt của?\",\"options\":[\"Java Development Kit\",\"Java Deployment Kit\",\"Joint Development Kit\",\"Java Debugging Kit\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q3\",\"text\":\"Câu 3: Dấu để kết thúc câu lệnh trong Java?\",\"options\":[\";\",\":\",\".\",\",\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q4\",\"text\":\"Câu 4: Từ khóa để kế thừa lớp trong Java?\",\"options\":[\"extends\",\"implements\",\"inherits\",\"super\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q5\",\"text\":\"Câu 5: Phương thức main có chữ ký nào đúng?\",\"options\":[\"public static void main(String[] args)\",\"private void main()\",\"public int main(String[] args)\",\"static main(String args)\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q6\",\"text\":\"Câu 6: IDE phổ biến cho Java là?\",\"options\":[\"IntelliJ IDEA\",\"Photoshop\",\"Excel\",\"PowerPoint\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q7\",\"text\":\"Câu 7: Biến để chạy chương trình mẫu 'Hello World' nên nằm trong phương thức nào?\",\"options\":[\"main\",\"class\",\"package\",\"interface\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q8\",\"text\":\"Câu 8: Định dạng đúng của gói (package) trong Java thường là?\",\"options\":[\"com.example.myapp\",\"com-example-myapp\",\"com example myapp\",\"com:example:myapp\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q9\",\"text\":\"Câu 9: Để in ra console dùng phương thức nào?\",\"options\":[\"System.out.println\",\"console.log\",\"print\",\"echo\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l1_q10\",\"text\":\"Câu 10: File .java sau khi biên dịch sẽ tạo file nào?\",\"options\":[\".class\",\".exe\",\".py\",\".jar\"],\"correctIndex\":0}\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":null,\n" +
            "    \"lessonId\":\"c1_l2\",\n" +
            "    \"title\":\"Quiz: Biến, kiểu dữ liệu & toán tử\",\n" +
            "    \"questions\": [\n" +
            "      {\"id\":\"c1_l2_q1\",\"text\":\"Câu 1: Kiểu dữ liệu để lưu số nguyên trong Java?\",\"options\":[\"int\",\"String\",\"boolean\",\"double\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q2\",\"text\":\"Câu 2: Từ khóa khai báo hằng số (constant) trong Java?\",\"options\":[\"final\",\"const\",\"static\",\"immutable\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q3\",\"text\":\"Câu 3: Kết quả phép 5 / 2 (int / int) trong Java là?\",\"options\":[\"2\",\"2.5\",\"3\",\"2.0\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q4\",\"text\":\"Câu 4: Kiểu dữ liệu đúng cho true/false?\",\"options\":[\"boolean\",\"int\",\"char\",\"String\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q5\",\"text\":\"Câu 5: Toán tử so sánh để kiểm tra bằng bằng giá trị primitive?\",\"options\":[\"==\",\"equals\",\"compareTo\",\"is\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q6\",\"text\":\"Câu 6: Ký tự duy nhất trong Java được đặt trong?\",\"options\":[\"' ' (single quotes)\",\"\\\" \\\" (double quotes)\",\"backticks\",\"<>\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q7\",\"text\":\"Câu 7: Kết quả biểu thức (true && false) là?\",\"options\":[\"false\",\"true\",\"null\",\"error\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q8\",\"text\":\"Câu 8: Toán tử dùng cho phép cộng chuỗi (String) trong Java?\",\"options\":[\"+\",\"concat\",\"append\",\"&\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q9\",\"text\":\"Câu 9: Kiểu dữ liệu phù hợp để lưu số thập phân chính xác đơn?\",\"options\":[\"float\",\"int\",\"long\",\"char\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l2_q10\",\"text\":\"Câu 10: Kiểm tra so sánh chuỗi nên dùng phương thức nào?\",\"options\":[\"equals\",\"==\",\"compare\",\"match\"],\"correctIndex\":0}\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":null,\n" +
            "    \"lessonId\":\"c1_l3\",\n" +
            "    \"title\":\"Quiz: Cấu trúc điều khiển (if, switch, loop)\",\n" +
            "    \"questions\": [\n" +
            "      {\"id\":\"c1_l3_q1\",\"text\":\"Câu 1: Câu lệnh dùng cho lựa chọn nhánh hai hướng?\",\"options\":[\"if-else\",\"switch\",\"for\",\"while\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q2\",\"text\":\"Câu 2: Để lặp biết trước số lần dùng?\",\"options\":[\"for\",\"while\",\"do-while\",\"if\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q3\",\"text\":\"Câu 3: Trong switch, để so sánh nhiều case thì dùng?\",\"options\":[\"case\",\"if\",\"loop\",\"break\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q4\",\"text\":\"Câu 4: Lệnh nào thoát khỏi vòng lặp hiện tại?\",\"options\":[\"break\",\"continue\",\"return\",\"exit\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q5\",\"text\":\"Câu 5: Vòng lặp thực hiện ít nhất 1 lần phù hợp là?\",\"options\":[\"do-while\",\"while\",\"for\",\"foreach\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q6\",\"text\":\"Câu 6: 'continue' làm gì trong vòng lặp?\",\"options\":[\"bỏ qua lần lặp hiện tại\",\"thoát vòng lặp\",\"dừng chương trình\",\"không làm gì\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q7\",\"text\":\"Câu 7: Biểu thức điều kiện phải trả về kiểu nào trong if(...) ?\",\"options\":[\"boolean\",\"int\",\"String\",\"Object\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q8\",\"text\":\"Câu 8: Khi dùng nested loops thì complex tăng theo?\",\"options\":[\"bậc của số vòng lặp\",\"giảm\",\"không đổi\",\"sai\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q9\",\"text\":\"Câu 9: switch có thể dùng với kiểu nào?\",\"options\":[\"int, String, enum\",\"double\",\"float\",\"Object bất kỳ\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l3_q10\",\"text\":\"Câu 10: Lệnh nào dùng để thoát khỏi hàm?\",\"options\":[\"return\",\"break\",\"continue\",\"exit\"],\"correctIndex\":0}\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":null,\n" +
            "    \"lessonId\":\"c1_l4\",\n" +
            "    \"title\":\"Quiz: Mảng & Collection cơ bản\",\n" +
            "    \"questions\": [\n" +
            "      {\"id\":\"c1_l4_q1\",\"text\":\"Câu 1: Cách khai báo mảng int 5 phần tử?\",\"options\":[\"int[] a = new int[5];\",\"int a = new int(5);\",\"array<int> a = new array<>();\",\"int a[] = 5;\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q2\",\"text\":\"Câu 2: Lớp nào dùng để lưu danh sách có thể thay đổi kích thước?\",\"options\":[\"ArrayList\",\"HashMap\",\"LinkedList\",\"TreeSet\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q3\",\"text\":\"Câu 3: Map lưu trữ dữ liệu theo cặp?\",\"options\":[\"key-value\",\"index-value\",\"node-edge\",\"pair-list\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q4\",\"text\":\"Câu 4: Để lặp qua collection dùng?\",\"options\":[\"for-each\",\"switch\",\"if\",\"do-while\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q5\",\"text\":\"Câu 5: Để sắp xếp ArrayList có thể dùng?\",\"options\":[\"Collections.sort(list)\",\"list.sort()\",\"list.orderBy()\",\"list.arrange()\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q6\",\"text\":\"Câu 6: HashMap cho phép keys là?\",\"options\":[\"objects with proper hashCode/equals\",\"primitive only\",\"null impossible\",\"only String\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q7\",\"text\":\"Câu 7: LinkedList khác ArrayList ở điểm nào?\",\"options\":[\"chèn/xóa giữa nhanh hơn\",\"truy xuất ngẫu nhiên nhanh hơn\",\"luôn nhỏ hơn\",\"không dùng được\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q8\",\"text\":\"Câu 8: Set khác List là?\",\"options\":[\"không cho phép phần tử trùng lặp\",\"luôn có thứ tự\",\"cho phép index\",\"lưu theo cặp\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q9\",\"text\":\"Câu 9: Lấy kích thước collection dùng phương thức?\",\"options\":[\"size()\",\"length()\",\"count()\",\"getSize()\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l4_q10\",\"text\":\"Câu 10: Để chuyển List sang mảng dùng?\",\"options\":[\"list.toArray()\",\"list.asArray()\",\"list.asList()\",\"list.copy()\"],\"correctIndex\":0}\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\":null,\n" +
            "    \"lessonId\":\"c1_l5\",\n" +
            "    \"title\":\"Quiz: Giới thiệu lập trình hướng đối tượng\",\n" +
            "    \"questions\": [\n" +
            "      {\"id\":\"c1_l5_q1\",\"text\":\"Câu 1: OOP gồm những tính chất nào?\",\"options\":[\"Encapsulation, Inheritance, Polymorphism, Abstraction\",\"Loop, Condition, Variable, Function\",\"Compile, Run, Debug, Test\",\"Class, Package, Module, Thread\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q2\",\"text\":\"Câu 2: Từ khóa để kế thừa trong Java là?\",\"options\":[\"extends\",\"implements\",\"inherits\",\"uses\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q3\",\"text\":\"Câu 3: Interface khác abstract class ở chỗ?\",\"options\":[\"không có trạng thái (field) mặc định trước Java 8\",\"không thể có method\",\"luôn có constructor\",\"luôn private\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q4\",\"text\":\"Câu 4: Đa hình (polymorphism) cho phép gì?\",\"options\":[\"gọi method của subclass qua tham chiếu parent\",\"tạo nhiều biến cùng tên\",\"lưu nhiều kiểu dữ liệu cùng biến\",\"gộp hai class\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q5\",\"text\":\"Câu 5: Từ khóa để gọi constructor lớp cha?\",\"options\":[\"super\",\"parent\",\"this\",\"base\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q6\",\"text\":\"Câu 6: Đóng gói (encapsulation) làm gì?\",\"options\":[\"ẩn chi tiết hiện thực bằng private + getter/setter\",\"tăng tốc độ chạy\",\"giảm bộ nhớ\",\"tự động test\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q7\",\"text\":\"Câu 7: Ghi đè phương thức gọi là?\",\"options\":[\"override\",\"overload\",\"overwrite\",\"overrun\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q8\",\"text\":\"Câu 8: Nếu muốn nhiều implement khác nhau cho cùng method signature gọi là?\",\"options\":[\"polymorphism\",\"inheritance\",\"encapsulation\",\"abstraction\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q9\",\"text\":\"Câu 9: Abstract class có thể có method nào?\",\"options\":[\"cả abstract lẫn concrete method\",\"chỉ abstract\",\"chỉ concrete\",\"không có method\"],\"correctIndex\":0},\n" +
            "      {\"id\":\"c1_l5_q10\",\"text\":\"Câu 10: Tạo object từ class bằng từ khóa?\",\"options\":[\"new\",\"create\",\"init\",\"make\"],\"correctIndex\":0}\n" +
            "    ]\n" +
            "  }\n" +
            "]";

    // ------------------ In-memory storage ------------------
    // quizId -> Quiz
    private final Map<String, Quiz> quizMap = new HashMap<>();
    // lessonId -> quizId (convention: 1 quiz per lesson)
    private final Map<String, String> lessonToQuiz = new HashMap<>();

    // attemptId -> QuizAttempt
    private final Map<String, QuizAttempt> attemptMap = new LinkedHashMap<>();
    // student+lesson -> list of attemptIds (newest first)
    private final Map<String, List<String>> attemptsIndex = new HashMap<>();

    // listeners
    private final List<LessonQuizApi.QuizUpdateListener> quizListeners = new ArrayList<>();
    private final List<LessonQuizApi.AttemptListener> attemptListeners = new ArrayList<>();

    // id generator
    private final AtomicInteger idCounter = new AtomicInteger(1);

    // Configurable passing threshold and expected question count
    private final int PASSING_COUNT = 8; // 8/10 required
    private final int EXPECTED_QUESTION_COUNT = 10;

    // ------------------ Constructor & seeding ------------------
    private LessonQuizFakeApiService() {
        seedFromJson();
    }

    // ------------------ Public API (implements LessonQuizApi) ------------------

    @Override
    public synchronized Quiz getQuizForLesson(String lessonId) {
        if (lessonId == null) return null;
        String qid = lessonToQuiz.get(lessonId);
        if (qid == null) return null;
        return quizMap.get(qid);
    }

    @Override
    public synchronized Quiz createQuiz(Quiz quiz) {
        if (!validateQuizStructure(quiz)) {
            return null;
        }
        String id = quiz.getId();
        if (id == null || id.trim().isEmpty()) id = generateId("quiz");
        Quiz created = new Quiz(id, quiz.getLessonId(), quiz.getTitle(), quiz.getQuestions());
        saveQuizInternal(created);
        return created;
    }

    @Override
    public synchronized Quiz updateQuiz(String quizId, Quiz updated) {
        if (quizId == null || updated == null) return null;
        Quiz exist = quizMap.get(quizId);
        if (exist == null) return null;

        if (!validateQuizStructure(updated)) {
            return null;
        }

        // lessonId cannot change
        if (!exist.getLessonId().equals(updated.getLessonId())) {
            updated = new Quiz(quizId, exist.getLessonId(), updated.getTitle(), updated.getQuestions());
        } else {
            updated = new Quiz(quizId, updated.getLessonId(), updated.getTitle(), updated.getQuestions());
        }

        quizMap.put(quizId, updated);
        lessonToQuiz.put(updated.getLessonId(), quizId);
        notifyQuizChanged(updated.getLessonId());
        return updated;
    }

    @Override
    public synchronized boolean deleteQuiz(String quizId) {
        if (quizId == null) return false;
        Quiz removed = quizMap.remove(quizId);
        if (removed != null) {
            lessonToQuiz.remove(removed.getLessonId());
            notifyQuizChanged(removed.getLessonId());
            return true;
        }
        return false;
    }

    @Override
    public synchronized QuizAttempt submitQuizAttempt(String lessonId, String studentId, Map<String, Integer> answers) {
        if (lessonId == null || studentId == null || answers == null) return null;

        // Validate quiz exists
        Quiz quiz = getQuizForLesson(lessonId);
        if (quiz == null) return null;

        // Validate quiz structure still valid
        if (!validateQuizStructure(quiz)) return null;

        // Validate lesson completed for student
        LessonProgressApi lpApi = ApiProvider.getLessonProgressApi();
        LessonProgress progress = null;
        try {
            progress = lpApi.getLessonProgress(lessonId, studentId);
        } catch (Exception ignored) {}
        if (progress == null || !progress.isCompleted()) {
            // not allowed to take quiz if lesson not completed
            return null;
        }

        List<QuizQuestion> questions = quiz.getQuestions();
        int correct = 0;
        for (QuizQuestion q : questions) {
            Integer chosen = answers.get(q.getId());
            if (chosen != null && chosen.intValue() == q.getCorrectOptionIndex()) {
                correct++;
            }
        }

        int totalQuestions = questions.size();
        int scorePercent = totalQuestions > 0 ? (int) Math.round(((double) correct / totalQuestions) * 100.0) : 0;
        boolean passed = correct >= PASSING_COUNT;

        String attemptId = generateId("attempt");
        QuizAttempt attempt = new QuizAttempt(
                attemptId,
                quiz.getId(),
                lessonId,
                studentId,
                new HashMap<>(answers),
                correct,
                scorePercent,
                passed,
                System.currentTimeMillis()
        );

        // persist
        attemptMap.put(attemptId, attempt);
        String idxKey = indexKey(lessonId, studentId);
        List<String> list = attemptsIndex.computeIfAbsent(idxKey, k -> new ArrayList<>());
        list.add(0, attemptId); // newest first

        // Notify (UI may refresh attempts list)
        notifyQuizChanged(lessonId);

        // NEW: notify attempt listeners about new submission
        notifyAttemptSubmitted(attempt);

        return attempt;
    }

    @Override
    public synchronized List<QuizAttempt> getAttemptsForLesson(String lessonId, String studentId) {
        List<QuizAttempt> result = new ArrayList<>();
        if (lessonId == null) return result;
        String key = indexKey(lessonId, studentId);
        List<String> ids = attemptsIndex.get(key);
        if (ids == null) return result;
        for (String id : ids) {
            QuizAttempt a = attemptMap.get(id);
            if (a != null) result.add(a);
        }
        return result;
    }

    @Override
    public synchronized QuizAttempt getAttemptById(String attemptId) {
        if (attemptId == null) return null;
        return attemptMap.get(attemptId);
    }

    @Override
    public synchronized void addQuizUpdateListener(QuizUpdateListener l) {
        if (l == null) return;
        if (!quizListeners.contains(l)) quizListeners.add(l);
    }

    @Override
    public synchronized void removeQuizUpdateListener(QuizUpdateListener l) {
        quizListeners.remove(l);
    }

    @Override
    public synchronized void addAttemptListener(AttemptListener l) {
        if (l == null) return;
        if (!attemptListeners.contains(l)) attemptListeners.add(l);
    }

    @Override
    public synchronized void removeAttemptListener(AttemptListener l) {
        attemptListeners.remove(l);
    }

    // ------------------ Utilities / helpers ------------------

    private String generateId(String prefix) {
        return prefix + "_" + idCounter.getAndIncrement();
    }

    private String indexKey(String lessonId, String studentId) {
        return lessonId + "|" + (studentId == null ? "_GLOBAL_" : studentId);
    }

    private void saveQuizInternal(Quiz q) {
        quizMap.put(q.getId(), q);
        lessonToQuiz.put(q.getLessonId(), q.getId());
        notifyQuizChanged(q.getLessonId());
    }

    private void notifyQuizChanged(String lessonId) {
        List<LessonQuizApi.QuizUpdateListener> copy = new ArrayList<>(quizListeners);
        for (LessonQuizApi.QuizUpdateListener l : copy) {
            try {
                l.onQuizChanged(lessonId);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAttemptSubmitted(QuizAttempt attempt) {
        if (attempt == null) return;
        List<LessonQuizApi.AttemptListener> copy = new ArrayList<>(attemptListeners);
        for (LessonQuizApi.AttemptListener l : copy) {
            try {
                l.onAttemptSubmitted(attempt);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Validate quiz structure according to project rule:
     * - quiz non-null, lessonId present
     * - questions != null and size == EXPECTED_QUESTION_COUNT
     * - each question non-null and q.isValidFourOptions() == true
     */
    private boolean validateQuizStructure(Quiz quiz) {
        if (quiz == null) return false;
        if (quiz.getLessonId() == null || quiz.getLessonId().trim().isEmpty()) return false;
        List<QuizQuestion> qs = quiz.getQuestions();
        if (qs == null) return false;
        if (qs.size() != EXPECTED_QUESTION_COUNT) return false;
        for (QuizQuestion q : qs) {
            if (q == null) return false;
            if (!q.isValidFourOptions()) return false;
        }
        return true;
    }

    // ------------------ Dev helpers (seed / admin) ------------------

    private void seedFromJson() {
        try {
            JSONArray arr = new JSONArray(QUIZZES_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String id = o.optString("id", null);
                String lessonId = o.optString("lessonId", null);
                String title = o.optString("title", "");
                List<QuizQuestion> qs = new ArrayList<>();
                JSONArray qArr = o.optJSONArray("questions");
                if (qArr != null) {
                    for (int j = 0; j < qArr.length(); j++) {
                        JSONObject qo = qArr.getJSONObject(j);
                        String qid = qo.optString("id", generateQuestionId(lessonId, j + 1));
                        String text = qo.optString("text", "");
                        List<String> options = new ArrayList<>();
                        JSONArray opts = qo.optJSONArray("options");
                        if (opts != null) {
                            for (int k = 0; k < opts.length(); k++) {
                                options.add(opts.optString(k, ""));
                            }
                        }
                        int correct = qo.optInt("correctIndex", 0);
                        QuizQuestion qq = new QuizQuestion(qid, text, options, correct);
                        qs.add(qq);
                    }
                }
                // If id null or empty, generate one
                if (id == null || id.trim().isEmpty()) id = generateId("quiz");
                Quiz q = new Quiz(id, lessonId, title, qs);
                // Only save if structure valid; otherwise skip (dev)
                if (validateQuizStructure(q)) {
                    saveQuizInternal(q);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String generateQuestionId(String lessonId, int idx) {
        if (lessonId == null) return "q_" + idx;
        return lessonId + "_q" + idx;
    }

    /**
     * Dùng cho testing: reset toàn bộ dữ liệu quiz + attempts
     */
    public synchronized void clearAll() {
        quizMap.clear();
        lessonToQuiz.clear();
        attemptMap.clear();
        attemptsIndex.clear();
        idCounter.set(1);
        notifyQuizChanged(null);
    }

    /**
     * Trả danh sách tất cả quiz (debug / admin)
     */
    public synchronized List<Quiz> listAllQuizzes() {
        return new ArrayList<>(quizMap.values());
    }
}
