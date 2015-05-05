package es.us.lsi.fogallego.reviewsdownloader;

import java.util.List;

public class CategorySource {
    private String category;
    private List<String> urls;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
