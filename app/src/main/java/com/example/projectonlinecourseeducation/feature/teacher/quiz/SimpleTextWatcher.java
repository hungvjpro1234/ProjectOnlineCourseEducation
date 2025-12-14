package com.example.projectonlinecourseeducation.feature.teacher.quiz;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {
    public interface Callback { void onChanged(String s); }
    private final Callback callback;
    public SimpleTextWatcher(Callback cb) { this.callback = cb; }
    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
    @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
    @Override public void afterTextChanged(Editable s) { callback.onChanged(s == null ? "" : s.toString()); }
}
