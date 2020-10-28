package com.muniryenigul.kam.models;

import android.graphics.drawable.Drawable;

public class CategoryModel {
    private String categoryName;
    private String order;
    private Drawable drawable;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
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
        this.order = order;
    }
}
