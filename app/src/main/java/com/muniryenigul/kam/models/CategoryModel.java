package com.muniryenigul.kam.models;

import android.graphics.drawable.Drawable;

public class CategoryModel {
    private final String categoryName;
    private Drawable drawable;

    public String getCategoryName() {
        return categoryName;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public CategoryModel(String categoryName, Drawable drawable, String order) {
        this.categoryName = categoryName;
        this.drawable = drawable;
    }
}
