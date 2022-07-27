package com.example.neteasecloudmusictext.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.neteasecloudmusictext.R;
import com.example.neteasecloudmusictext.databinding.WidgetSearchBinding;

public class EditTextSearchWidget extends RelativeLayout {
    private EditText editText;
    private ImageView imageView;

    public EditTextSearchWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_search,this);
        editText =findViewById(R.id.et_search);
        imageView =findViewById(R.id.iv_clear);
    }

    public void setOnclickListener(OnClickListener listener) {
        imageView.setOnClickListener(listener);
    }
    public String getEditText() {
        return editText.getText().toString().trim();
    }

    public void removeEditText() {
        editText.setText("");
    }

    public void setText(String text) {
        editText.setText(text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
