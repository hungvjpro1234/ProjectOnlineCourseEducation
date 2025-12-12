package com.example.projectonlinecourseeducation.feature.teacher.quiz;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView trong QuizDialogFragment.
 * Mỗi item chứa: question EditText, 4 options (RadioButton + EditText).
 */
public class QuestionItemAdapter extends RecyclerView.Adapter<QuestionItemAdapter.VH> {

    public static class QuestionModel {
        public String id; // optional (may be null for new)
        public String text = "";
        public List<String> options = new ArrayList<>();
        public int correctIndex = -1;

        public QuestionModel() {
            // ensure 4 options
            for (int i = 0; i < 4; i++) options.add("");
        }
    }

    private final List<QuestionModel> items = new ArrayList<>();

    public QuestionItemAdapter(int count) {
        for (int i = 0; i < count; i++) items.add(new QuestionModel());
    }

    public void setData(List<QuestionModel> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public List<QuestionModel> getData() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_question, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        QuestionModel m = items.get(pos);
        h.tvIndex.setText("Câu " + (pos + 1));
        h.etQuestion.setText(m.text);

        // Ensure options container has 4 rows
        h.optionsContainer.removeAllViews();
        for (int i = 0; i < 4; i++) {
            View row = LayoutInflater.from(h.itemView.getContext()).inflate(R.layout._internal_option_row, h.optionsContainer, false);
            RadioButton rb = row.findViewById(R.id.rbOption);
            EditText etOpt = row.findViewById(R.id.etOptionText);
            rb.setId(View.generateViewId());
            rb.setChecked(m.correctIndex == i);
            String optText = i < m.options.size() ? m.options.get(i) : "";
            etOpt.setText(optText);
            int finalI = i;
            rb.setOnClickListener(v -> {
                m.correctIndex = finalI;
                // notify to refresh others' radio
                notifyItemRangeChanged(0, items.size(), "radio-change");
            });
            // keep changes in model when editing text
            etOpt.addTextChangedListener(new SimpleTextWatcher(s -> {
                m.options.set(finalI, s);
            }));
            h.optionsContainer.addView(row);
        }

        // question text watcher
        h.etQuestion.addTextChangedListener(new SimpleTextWatcher(s -> m.text = s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIndex;
        EditText etQuestion;
        LinearLayout optionsContainer;
        VH(@NonNull View v) {
            super(v);
            tvIndex = v.findViewById(R.id.tvQuestionIndex);
            etQuestion = v.findViewById(R.id.etQuestionText);
            optionsContainer = v.findViewById(R.id.optionsContainer);
        }
    }

    // Validation helper
    public boolean validateAll(StringBuilder errorOut) {
        for (int i = 0; i < items.size(); i++) {
            QuestionModel m = items.get(i);
            if (TextUtils.isEmpty(m.text)) {
                errorOut.append("Câu ").append(i + 1).append(" chưa có nội dung");
                return false;
            }
            for (int j = 0; j < 4; j++) {
                if (m.options.size() <= j || TextUtils.isEmpty(m.options.get(j))) {
                    errorOut.append("Câu ").append(i + 1).append(" option ").append(j + 1).append(" rỗng");
                    return false;
                }
            }
            if (m.correctIndex < 0 || m.correctIndex > 3) {
                errorOut.append("Chưa chọn đáp án đúng cho câu ").append(i + 1);
                return false;
            }
        }
        return true;
    }
}
