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
 *
 * NOTE: this implementation stores quizzes keyed by lessonId (one quiz per lesson),
 * following the pattern used in LessonFakeApiService (lessonMap keyed by lessonId).
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
            "]";

    // ------------------ In-memory storage ------------------
    // Keyed by lessonId -> Quiz (one quiz per lesson, like lessonMap pattern)
    private final Map<String, Quiz> quizMap = new LinkedHashMap<>();

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
        return quizMap.get(lessonId);
    }

    @Override
    public synchronized Quiz createQuiz(Quiz quiz) {
        if (!validateQuizStructure(quiz)) {
            return null;
        }
        if (quiz.getLessonId() == null || quiz.getLessonId().trim().isEmpty()) return null;

        String id = quiz.getId();
        if (id == null || id.trim().isEmpty()) id = generateId("quiz");

        Quiz created = new Quiz(id, quiz.getLessonId(), quiz.getTitle(), quiz.getQuestions());

        // follow lesson-like behavior: keep first quiz if exists; otherwise put
        quizMap.putIfAbsent(created.getLessonId(), created);
        notifyQuizChanged(created.getLessonId());
        return quizMap.get(created.getLessonId());
    }

    @Override
    public synchronized Quiz updateQuiz(String quizId, Quiz updated) {
        if (quizId == null || updated == null) return null;

        // find existing quiz by id across quizMap values
        Quiz existing = null;
        String existingLessonId = null;
        for (Map.Entry<String, Quiz> e : quizMap.entrySet()) {
            if (quizId.equals(e.getValue().getId())) {
                existing = e.getValue();
                existingLessonId = e.getKey();
                break;
            }
        }
        if (existing == null) return null;

        if (!validateQuizStructure(updated)) {
            return null;
        }

        // lessonId should remain same (treat lesson as parent)
        String lessonId = existing.getLessonId();
        Quiz newQuiz = new Quiz(existing.getId(), lessonId, updated.getTitle(), updated.getQuestions());

        // replace in map keyed by lessonId
        quizMap.put(lessonId, newQuiz);
        notifyQuizChanged(lessonId);
        return newQuiz;
    }

    @Override
    public synchronized boolean deleteQuiz(String quizId) {
        if (quizId == null) return false;
        String foundLessonId = null;
        for (Map.Entry<String, Quiz> e : quizMap.entrySet()) {
            if (quizId.equals(e.getValue().getId())) {
                foundLessonId = e.getKey();
                break;
            }
        }
        if (foundLessonId != null) {
            quizMap.remove(foundLessonId);
            notifyQuizChanged(foundLessonId);
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

        // notify attempt listeners about new submission
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
            Set<String> seenLessons = new HashSet<>();
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
                    // skip quizzes without lessonId to avoid mapping null key
                    if (lessonId == null || lessonId.trim().isEmpty()) continue;

                    // If we have seen this lessonId earlier in the seed, SKIP this duplicate entirely.
                    // Keep the first occurrence deterministic (lesson-like behavior).
                    if (seenLessons.contains(lessonId)) {
                        System.err.println("Warning: Duplicate lessonId in seed JSON: " + lessonId + " (seed index " + i + "). Skipping this duplicate.");
                        continue; // <-- do not save duplicate
                    } else {
                        seenLessons.add(lessonId);
                    }

                    // Save keyed by lessonId (keep first if duplicates)
                    quizMap.putIfAbsent(lessonId, q);
                    notifyQuizChanged(lessonId);
                } else {
                    System.err.println("Skipping invalid quiz for lessonId=" + lessonId + " at seed index " + i);
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
