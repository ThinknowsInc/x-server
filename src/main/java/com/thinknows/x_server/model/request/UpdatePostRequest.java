package com.thinknows.x_server.model.request;

import java.util.List;

public class UpdatePostRequest {
    private String title;
    private String content;
    private String category;
    private List<String> tags;
    
    public UpdatePostRequest() {
    }
    
    public UpdatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    public UpdatePostRequest(String title, String content, String category, List<String> tags) {
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
