package com.thinknows.x_server.model.request;

import java.util.List;

public class CreatePostRequest {
    private String title;
    private String content;
    private String category;
    private List<String> tags;
    
    public CreatePostRequest() {
    }
    
    public CreatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    public CreatePostRequest(String title, String content, String category, List<String> tags) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
