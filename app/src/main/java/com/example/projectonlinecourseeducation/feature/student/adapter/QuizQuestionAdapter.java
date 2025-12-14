// File: QuizQuestionAdapter.java
package com.example.projectonlinecourseeducation.feature.student.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectonlinecourseeducation.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter hiển thị 1 câu hỏi + 4 options (radio).
 *
 * - Giữ selection state local (questionId -> chosenIndex)
 * - revealWrongAnswers(...) sẽ lock radio và highlight wrong/ correct
 *
 * NOTE: adapter dùng reflection để đọc thuộc tính của QuizQuestion model
 * nhằm tương thích với nhiều tên method khác nhau (getText/getQuestion/getTitle, getOptions/getChoices, ...)
 */
public class QuizQuestionAdapter extends RecyclerView.Adapter<QuizQuestionAdapter.VH> {

    public interface OnAnswerSelected {
        void onSelected(String questionId, int chosenIndex);
    }

    private final OnAnswerSelected listener;
    private final List<Object> items = new ArrayList<>(); // hold quiz question objects (unknown compile-time type)

    // keeps selection state local: questionId -> chosenIndex
    private final Map<String, Integer> selected = new HashMap<>();

    // reveal info: questionId -> userChosenIndex (from attempt). When reveal active, UI will lock controls.
    private final Map<String, Integer> revealMap = new HashMap<>();

    public QuizQuestionAdapter(OnAnswerSelected l) {
        this.listener = l;
    }

    /**
     * Accept list of QuizQuestion objects but typed as Object to avoid compile-time dependency on specific POJO names.
     */
    public void submitList(List<?> qs) {
        items.clear();
        if (qs != null) items.addAll(qs);
        selected.clear();
        revealMap.clear();
        notifyDataSetChanged();
    }

    public void clearReveal() {
        revealMap.clear();
        // re-enable selection UI
        notifyDataSetChanged();
    }

    /**
     * Reveal only wrong answers and correct answers:
     * - For each question, if user's answer != correctIndex -> mark user choice as wrong (red) and mark correct as green.
     * - If user's answer == correctIndex -> mark it green.
     *
     * givenAnswers: map questionId -> chosenIndex (can be -1 for unanswered)
     */
    public void revealWrongAnswers(Map<String, Integer> givenAnswers) {
        revealMap.clear();
        if (givenAnswers != null) revealMap.putAll(givenAnswers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_quiz_question, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Object q = items.get(position);
        holder.bind(q, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView tvQ;
        RadioGroup radioGroup;
        RadioButton rb0, rb1, rb2, rb3;
        Context ctx;

        VH(@NonNull View itemView) {
            super(itemView);
            ctx = itemView.getContext();
            tvQ = itemView.findViewById(R.id.tvQuestionText);
            radioGroup = itemView.findViewById(R.id.rgOptions);
            rb0 = itemView.findViewById(R.id.rb0);
            rb1 = itemView.findViewById(R.id.rb1);
            rb2 = itemView.findViewById(R.id.rb2);
            rb3 = itemView.findViewById(R.id.rb3);
        }

        void bind(Object qObj, int pos) {
            // get question id/text/options/correctIndex via reflection-safe helpers
            String qId = safeGetQuestionId(qObj);
            String qText = safeGetQuestionText(qObj);
            List<String> opts = safeGetOptions(qObj);
            Integer correctIdx = safeGetCorrectIndex(qObj);

            // fallback safe values
            if (qText == null) qText = "(Không có nội dung câu hỏi)";
            if (opts == null) opts = new ArrayList<>();
            if (correctIdx == null) correctIdx = -1;

            tvQ.setText((pos + 1) + ". " + qText);

            // reset styles
            resetOptionStyle();

            rb0.setText(opts.size() > 0 ? opts.get(0) : "");
            rb1.setText(opts.size() > 1 ? opts.get(1) : "");
            rb2.setText(opts.size() > 2 ? opts.get(2) : "");
            rb3.setText(opts.size() > 3 ? opts.get(3) : "");

            // Remove listener to avoid recursion when programmatically checking
            radioGroup.setOnCheckedChangeListener(null);

            // restore selection if any
            Integer sel = qId != null ? selected.get(qId) : null;
            if (sel != null) {
                int rbId = rbIdAt(sel);
                if (rbId != -1) radioGroup.check(rbId);
            } else {
                radioGroup.clearCheck();
            }

            // If reveal active for this question: lock controls and highlight
            if (qId != null && revealMap.containsKey(qId)) {
                Integer userSel = revealMap.get(qId); // may be null
                int correct = correctIdx;

                // disable all options
                setRadioEnabled(false);

                // highlight correct option (green)
                markAsCorrect(correct);

                // if user selected wrong, highlight it red
                if (userSel != null && userSel != -1 && userSel != correct) {
                    markAsWrong(userSel);
                }

                // if user selected correct, it's already marked correct
            } else {
                // normal mode: allow selection
                setRadioEnabled(true);

                // Attach listener
                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    int idx = indexOfRb(checkedId);
                    if (qId != null) {
                        if (idx >= 0) {
                            selected.put(qId, idx);
                        } else {
                            selected.remove(qId);
                        }
                        if (listener != null) listener.onSelected(qId, idx);
                    }
                });
            }
        }

        private int rbIdAt(int idx) {
            switch (idx) {
                case 0: return R.id.rb0;
                case 1: return R.id.rb1;
                case 2: return R.id.rb2;
                case 3: return R.id.rb3;
                default: return -1;
            }
        }

        private int indexOfRb(int id) {
            if (id == R.id.rb0) return 0;
            if (id == R.id.rb1) return 1;
            if (id == R.id.rb2) return 2;
            if (id == R.id.rb3) return 3;
            return -1;
        }

        private void setRadioEnabled(boolean enabled) {
            rb0.setEnabled(enabled);
            rb1.setEnabled(enabled);
            rb2.setEnabled(enabled);
            rb3.setEnabled(enabled);
        }

        private void resetOptionStyle() {
            // default text color and style
            int defaultTextColor;
            try {
                defaultTextColor = ContextCompat.getColor(ctx, R.color.text_primary);
            } catch (Exception e) {
                defaultTextColor = ContextCompat.getColor(ctx, android.R.color.black);
            }

            rb0.setTextColor(defaultTextColor);
            rb1.setTextColor(defaultTextColor);
            rb2.setTextColor(defaultTextColor);
            rb3.setTextColor(defaultTextColor);
            rb0.setTypeface(Typeface.DEFAULT);
            rb1.setTypeface(Typeface.DEFAULT);
            rb2.setTypeface(Typeface.DEFAULT);
            rb3.setTypeface(Typeface.DEFAULT);

            // remove compound drawables (if any)
            try {
                rb0.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                rb1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                rb2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                rb3.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } catch (Exception ignored) {}
        }

        private void markAsCorrect(int idx) {
            if (idx < 0) return;
            int color;
            try {
                color = ContextCompat.getColor(ctx, R.color.colorSecondary);
            } catch (Exception e) {
                color = ContextCompat.getColor(ctx, android.R.color.holo_blue_dark);
            }
            int rbId = rbIdAt(idx);
            if (rbId == -1) return;
            RadioButton rb = itemView.findViewById(rbId);
            rb.setTextColor(color);
            rb.setTypeface(Typeface.DEFAULT_BOLD);
            try {
                rb.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_check,0);
            } catch (Exception ignored) {}
        }

        private void markAsWrong(int idx) {
            if (idx < 0) return;
            int color;
            try {
                color = ContextCompat.getColor(ctx, R.color.colorError);
            } catch (Exception e) {
                color = ContextCompat.getColor(ctx, android.R.color.holo_red_dark);
            }
            int rbId = rbIdAt(idx);
            if (rbId == -1) return;
            RadioButton rb = itemView.findViewById(rbId);
            rb.setTextColor(color);
            rb.setTypeface(Typeface.DEFAULT_BOLD);
            try {
                rb.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_close,0);
            } catch (Exception ignored) {}
        }
    }

    // ---------------------------
    // Reflection helpers
    // ---------------------------

    private String safeGetQuestionId(Object qObj) {
        if (qObj == null) return null;
        // try common method names for id
        String[] names = {"getId", "getID", "id", "getQuestionId", "getQid"};
        for (String n : names) {
            Object v = invokeMethodNullable(qObj, n);
            if (v instanceof String) return (String) v;
            if (v != null) return v.toString();
        }
        return null;
    }

    private String safeGetQuestionText(Object qObj) {
        if (qObj == null) return null;
        String[] names = {"getText", "getQuestionText", "getQuestion", "getTitle", "getPrompt"};
        for (String n : names) {
            Object v = invokeMethodNullable(qObj, n);
            if (v instanceof String) return (String) v;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> safeGetOptions(Object qObj) {
        if (qObj == null) return null;
        // try methods returning List<String> or String[]
        String[] names = {"getOptions", "getChoices", "getAnswers", "getOptionList", "getOptionsList"};
        for (String n : names) {
            Object v = invokeMethodNullable(qObj, n);
            if (v instanceof List) {
                try {
                    return (List<String>) v;
                } catch (ClassCastException ignored) {}
            } else if (v instanceof String[]) {
                String[] arr = (String[]) v;
                List<String> out = new ArrayList<>();
                for (String s : arr) out.add(s);
                return out;
            }
        }
        // fallback: try getOption0..getOption3
        List<String> fallback = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Object v = invokeMethodNullable(qObj, "getOption" + i);
            if (v instanceof String) fallback.add((String) v);
        }
        return fallback.isEmpty() ? null : fallback;
    }

    private Integer safeGetCorrectIndex(Object qObj) {
        if (qObj == null) return null;
        String[] names = {"getCorrectOptionIndex", "getCorrectIndex", "getCorrect", "getAnswerIndex"};
        for (String n : names) {
            Object v = invokeMethodNullable(qObj, n);
            if (v instanceof Integer) return (Integer) v;
            if (v instanceof Number) return ((Number) v).intValue();
        }
        return null;
    }

    /**
     * Try to invoke a no-arg method by name, return null on any error.
     */
    private Object invokeMethodNullable(Object target, String methodName) {
        if (target == null || methodName == null) return null;
        try {
            Method m = target.getClass().getMethod(methodName);
            return m.invoke(target);
        } catch (NoSuchMethodException ignored) {
            // try with "isX" boolean style? skip for now
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
        return null;
    }
}
