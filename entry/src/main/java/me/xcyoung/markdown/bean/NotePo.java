package me.xcyoung.markdown.bean;

import java.util.Objects;

public class NotePo {
    private String url;
    private String localPath;
    private String addType;
    private String displayName;

    public NotePo(String url, String localPath, String addType, String displayName) {
        this.url = url;
        this.localPath = localPath;
        this.addType = addType;
        this.displayName = displayName;
    }

    public NotePo(String url, String addType, String displayName) {
        this.url = url;
        this.addType = addType;
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAddType() {
        return addType;
    }

    public void setAddType(String addType) {
        this.addType = addType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotePo notePo = (NotePo) o;
        return Objects.equals(url, notePo.url) &&
                Objects.equals(addType, notePo.addType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, addType, displayName);
    }

    public interface NoteAddType {
        String LOCAL = "local";
        String REMOTE = "remote";
    }
}
