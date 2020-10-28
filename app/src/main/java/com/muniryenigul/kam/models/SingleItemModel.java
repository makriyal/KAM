package com.muniryenigul.kam.models;

public class SingleItemModel {
    private String name;
    private String author;
    private String publisher;
    private String cover;
    private String description;
    private String individual;
    private String coverBig, isbn;
    private boolean selected;
    public SingleItemModel(String name, String author, String publisher, String cover, String individual, String coverBig, String isbn, String description) {
        this.name = name; this.author = author; this.publisher = publisher;
        this.cover = cover; this.individual = individual; this.coverBig = coverBig;
        this.isbn = isbn;this.description = description;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAuthor() {
        return author;
    }
    public String getPublisher() {
        return publisher;
    }
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getIndividual() {
        return individual;
    }
    public String getCoverBig() {
        return coverBig;
    }
    public String getIsbn() {
        return isbn;
    }
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}