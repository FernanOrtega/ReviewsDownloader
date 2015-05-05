package es.us.lsi.fogallego.reviewsdownloader;

import java.util.List;

public class Source {
    private String site;
    private String siteUrl;
    private String folderOut;
    private String downloaderClass;
    private List<CategorySource> sources;

    public Source() {
    }

    public String getFolderOut() {
        return folderOut;
    }

    public void setFolderOut(String folderOut) {
        this.folderOut = folderOut;
    }

    public List<CategorySource> getSources() {
        return sources;
    }

    public void setSources(List<CategorySource> sources) {
        this.sources = sources;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getDownloaderClass() {
        return downloaderClass;
    }

    public void setDownloaderClass(String downloaderClass) {
        this.downloaderClass = downloaderClass;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }
}
