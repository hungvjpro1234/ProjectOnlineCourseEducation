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

import java.util.ArrayList;
import java.util.List;

/**
 * DialogFragment dùng để soạn Quiz cho lesson chưa được persist (create flow).
 *
 * Usage:
 *   QuizDraftDialogFragment f = QuizDraftDialogFragment.newInstanceForPosition(position, lessonKey);
 *   f.setListener(listener); // listener receives Quiz draft
 *   f.show(getSupportFragmentManager(), "quiz_draft_" + lessonKey);
 *
 * It reuses the same UI as QuizDialogFragment (fragment_quiz_dialog.xml).
 * It does NOT call LessonQuizApi; it returns Quiz via listener.
 */
public class QuizDraftDialogFragment extends DialogFragment {

    private static final String ARG_LESSON_KEY = "arg_lesson_key";
    private static final String ARG_POSITION = "arg_position";

    private EditText etTitle;
    private RecyclerView rvQuestions;
    private Button btnCancel, btnSave;
    private QuestionItemAdapter adapter;
    private String lessonKey; // unique key for local lesson (e.g., "NEW#1#Title")
    private int position = -1;

    public interface QuizDraftListener {
        /**
         * Called when user saved quiz draft in create-flow.
         * @param lessonKey the key identifying the local lesson (useful to persist mapping)
         * @param quiz the Quiz object (may have id==null)
         */
        void onQuizDraftSaved(String lessonKey, Quiz quiz);
    }

    private QuizDraftListener listener;

    public void setListener(QuizDraftListener l) {
        this.listener = l;
    }

    public static QuizDraftDialogFragment newInstanceForPosition(int position, String lessonKey) {
        QuizDraftDialogFragment f = new QuizDraftDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_LESSON_KEY, lessonKey);
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    public QuizDraftDialogFragment() { /* empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_quiz_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        etTitle = v.findViewById(R.id.etQuizTitle);
        rvQuestions = v.findViewById(R.id.rvQuestions);
        btnCancel = v.findViewById(R.id.btnCancelQuiz);
        btnSave = v.findViewById(R.id.btnSaveQuiz);

        lessonKey = getArguments() != null ? getArguments().getString(ARG_LESSON_KEY) : null;
        position = getArguments() != null ? getArguments().getInt(ARG_POSITION, -1) : -1;

        if (lessonKey == null) {
            // Nothing to do
            dismiss();
            return;
        }

        adapter = new QuestionItemAdapter(10);
        rvQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQuestions.setAdapter(adapter);

        // If caller wants to prepopulate an existing draft, they should call setInitialDraft(...) before showing.
        // (We provide a method below to support that.)
        btnCancel.setOnClickListener(x -> dismiss());
        btnSave.setOnClickListener(x -> onSaveClicked());
    }

    /**
     * Allow activity to pre-fill dialog with an existing draft Quiz (if any).
     * Call this after creating fragment and before show().
     */
    public void setInitialDraft(Quiz draft) {
        if (draft == null) return;
        if (etTitle != null) etTitle.setText(draft.getTitle() == null ? "" : draft.getTitle());
        if (adapter != null && draft.getQuestions() != null) {
            List<QuestionItemAdapter.QuestionModel> list = new ArrayList<>();
            for (QuizQuestion q : draft.getQuestions()) {
                QuestionItemAdapter.QuestionModel m = new QuestionItemAdapter.QuestionModel();
                m.id = q.getId();
                // QuizQuestion getters: adapt to your model (some classes have getQuestion or getText)
                // In your project existing QuizQuestion uses names: id, text or question? adapt accordingly.
                // We'll try common getter names; if mismatch, adjust below.
                try {
                    m.text = q.getQuestion();
                } catch (Throwable ignored) {
                    try { m.text = q.getQuestion(); } catch (Throwable ignored2) { m.text = ""; }
                }
                m.options = new ArrayList<>(q.getOptions() == null ? new ArrayList<>() : q.getOptions());
                m.correctIndex = q.getCorrectOptionIndex();
                while (m.options.size() < 4) m.options.add("");
                list.add(m);
            }
            while (list.size() < 10) list.add(new QuestionItemAdapter.QuestionModel());
            adapter.setData(list);
        }
    }

    private void onSaveClicked() {
        String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Tiêu đề quiz không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder err = new StringBuilder();
        if (!adapter.validateAll(err)) {
            Toast.makeText(requireContext(), err.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        List<QuestionItemAdapter.QuestionModel> models = adapter.getData();
        List<QuizQuestion> qlist = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            QuestionItemAdapter.QuestionModel m = models.get(i);
            String qid = m.id;
            if (qid == null || qid.trim().isEmpty()) {
                qid = (lessonKey == null ? "draft" : lessonKey) + "_q" + (i + 1);
            }
            // IMPORTANT: adapt to your QuizQuestion constructor signature.
            QuizQuestion qq = new QuizQuestion(qid, m.text, m.options, m.correctIndex);
            qlist.add(qq);
        }

        Quiz draftQuiz = new Quiz(null, /* lessonId */ null, title, qlist);
        // We intentionally keep lessonId null — activity will attach the correct lesson/course context.

        if (listener != null) {
            listener.onQuizDraftSaved(lessonKey, draftQuiz);
        }
        Toast.makeText(requireContext(), "Đã lưu nháp quiz cục bộ", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            // make it large so editor is comfortable
            d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
    }
}
