package com.example.projectonlinecourseeducation.feature.teacher.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;
import com.example.projectonlinecourseeducation.core.model.lesson.quiz.QuizQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách câu hỏi quiz trong màn teacher management.
 * Mỗi item:
 * - tiêu đề câu hỏi
 * - 4 option (static) — highlight đáp án đúng (bold + màu)
 */
public class ManagementLessonQuizAdapter extends RecyclerView.Adapter<ManagementLessonQuizAdapter.Holder> {

    private List<QuizQuestion> questions = new ArrayList<>();

    public ManagementLessonQuizAdapter(List<QuizQuestion> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public void updateQuestions(List<QuizQuestion> qs) {
        this.questions = qs != null ? qs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_quiz_question, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position < 0 || position >= questions.size()) return;
        holder.bind(questions.get(position), position);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final TextView tvQIndex;
        private final TextView tvQText;
        private final TextView tvOpt0;
        private final TextView tvOpt1;
        private final TextView tvOpt2;
        private final TextView tvOpt3;
        private final LinearLayout layoutOptions;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvQIndex = itemView.findViewById(R.id.tvQuizQIndex);
            tvQText = itemView.findViewById(R.id.tvQuizQText);
            tvOpt0 = itemView.findViewById(R.id.tvOption0);
            tvOpt1 = itemView.findViewById(R.id.tvOption1);
            tvOpt2 = itemView.findViewById(R.id.tvOption2);
            tvOpt3 = itemView.findViewById(R.id.tvOption3);
            layoutOptions = itemView.findViewById(R.id.layoutOptions);
        }

        public void bind(QuizQuestion q, int pos) {
            if (q == null) {
                tvQIndex.setText((pos + 1) + ".");
                tvQText.setText("[Câu hỏi trống]");
                tvOpt0.setText("");
                tvOpt1.setText("");
                tvOpt2.setText("");
                tvOpt3.setText("");
                return;
            }

            tvQIndex.setText((pos + 1) + ".");
            tvQText.setText(q.getQuestion() != null ? q.getQuestion() : "");

            List<String> opts = q.getOptions();
            String o0 = opts.size() > 0 ? opts.get(0) : "";
            String o1 = opts.size() > 1 ? opts.get(1) : "";
            String o2 = opts.size() > 2 ? opts.get(2) : "";
            String o3 = opts.size() > 3 ? opts.get(3) : "";

            tvOpt0.setText("A. " + o0);
            tvOpt1.setText("B. " + o1);
            tvOpt2.setText("C. " + o2);
            tvOpt3.setText("D. " + o3);

            // Reset styles
            resetOptionStyle(tvOpt0);
            resetOptionStyle(tvOpt1);
            resetOptionStyle(tvOpt2);
            resetOptionStyle(tvOpt3);

            // Highlight correct option
            int correct = -1;
            try { correct = q.getCorrectOptionIndex(); } catch (Exception ignored) {}
            switch (correct) {
                case 0: highlightCorrect(tvOpt0); break;
                case 1: highlightCorrect(tvOpt1); break;
                case 2: highlightCorrect(tvOpt2); break;
                case 3: highlightCorrect(tvOpt3); break;
                default: break;
            }
        }

        private void resetOptionStyle(TextView t) {
            t.setTypeface(Typeface.DEFAULT);
            t.setTextColor(t.getResources().getColor(R.color.text_primary));
            t.setBackground(null);
            t.setAlpha(1f);
        }

        private void highlightCorrect(TextView t) {
            try {
                t.setTypeface(Typeface.DEFAULT_BOLD);
                t.setTextColor(t.getResources().getColor(R.color.green_success));
            } catch (Exception ignored) {}
        }
    }
}
