package com.example.projectonlinecourseeducation.feature.teacher.quiz;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.Quiz;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.lessonquiz.LessonQuizApi;

import java.util.ArrayList;
import java.util.List;

/**
 * DialogFragment dùng RecyclerView để edit/create Quiz (10 câu).
 * Arg: ARG_LESSON_ID (String) bắt buộc.
 */
public class QuizDialogFragment extends DialogFragment {

    public static final String ARG_LESSON_ID = "arg_lesson_id";
    private EditText etTitle;
    private RecyclerView rvQuestions;
    private Button btnCancel, btnSave;
    private QuestionItemAdapter adapter;
    private String lessonId;
    private Quiz existingQuiz;

    public static QuizDialogFragment newInstance(String lessonId) {
        QuizDialogFragment f = new QuizDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_LESSON_ID, lessonId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        etTitle = v.findViewById(R.id.etQuizTitle);
        rvQuestions = v.findViewById(R.id.rvQuestions);
        btnCancel = v.findViewById(R.id.btnCancelQuiz);
        btnSave = v.findViewById(R.id.btnSaveQuiz);

        lessonId = getArguments() != null ? getArguments().getString(ARG_LESSON_ID) : null;
        if (lessonId == null) {
            dismiss();
            return;
        }

        adapter = new QuestionItemAdapter(10);
        rvQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQuestions.setAdapter(adapter);

        loadExistingQuiz();

        btnCancel.setOnClickListener(x -> dismiss());
        btnSave.setOnClickListener(x -> onSaveClicked());
    }

    private void loadExistingQuiz() {
        LessonQuizApi api = ApiProvider.getLessonQuizApi();
        try {
            existingQuiz = api.getQuizForLesson(lessonId);
        } catch (Throwable ignored) {}
        if (existingQuiz != null) {
            etTitle.setText(existingQuiz.getTitle());
            List<QuestionItemAdapter.QuestionModel> list = new ArrayList<>();
            if (existingQuiz.getQuestions() != null) {
                for (QuizQuestion q : existingQuiz.getQuestions()) {
                    QuestionItemAdapter.QuestionModel m = new QuestionItemAdapter.QuestionModel();
                    m.id = q.getId();
                    m.text = q.getQuestion();
                    m.options = new ArrayList<>(q.getOptions());
                    m.correctIndex = q.getCorrectOptionIndex();
                    // ensure 4 options
                    while (m.options.size() < 4) m.options.add("");
                    list.add(m);
                }
            }
            // If some missing (rare) pad to 10
            while (list.size() < 10) list.add(new QuestionItemAdapter.QuestionModel());
            adapter.setData(list);
        } else {
            // default empty; adapter was already created with 10 blanks
        }
    }

    private void onSaveClicked() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Tiêu đề quiz không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder err = new StringBuilder();
        if (!adapter.validateAll(err)) {
            Toast.makeText(requireContext(), err.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        // Build Quiz object
        List<QuestionItemAdapter.QuestionModel> models = adapter.getData();
        List<QuizQuestion> qlist = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            QuestionItemAdapter.QuestionModel m = models.get(i);
            String qid = m.id;
            if (qid == null || qid.trim().isEmpty()) {
                qid = lessonId + "_q" + (i + 1);
            }
            QuizQuestion qq = new QuizQuestion(qid, m.text, m.options, m.correctIndex);
            qlist.add(qq);
        }

        Quiz qObj = new Quiz(existingQuiz == null ? null : existingQuiz.getId(), lessonId, title, qlist);

        LessonQuizApi api = ApiProvider.getLessonQuizApi();
        Quiz saved = null;
        try {
            if (existingQuiz == null) saved = api.createQuiz(qObj);
            else saved = api.updateQuiz(existingQuiz.getId(), qObj);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (saved == null) {
            Toast.makeText(requireContext(), "Lưu quiz thất bại (kiểm tra cấu trúc).", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Lưu quiz thành công", Toast.LENGTH_SHORT).show();
            // Optionally notify parent activity to refresh item display
            dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
    }
}
