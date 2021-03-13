package com.muniryenigul.kam.utils;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LockableNestedScrollView extends NestedScrollView {
    private boolean scrollable = true;
    public LockableNestedScrollView(@NonNull Context context) {
        super(context);
    }
    public LockableNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
    public LockableNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) { return scrollable && super.onInterceptTouchEvent(ev); }
    public void setScrollingEnabled(boolean enabled) {
        scrollable = enabled;
    }
}