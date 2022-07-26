package com.adrobz.intelliconchatsample;

import com.google.gson.annotations.SerializedName;

public class AttachmentResponse {

    @SerializedName("file")
    public String file;
    @SerializedName("url")
    public String url;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
