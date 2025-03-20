package com.thinknows.x_server.model.response;

import java.time.LocalDateTime;

public class AppConfigResponse {
    private boolean enableLogUpload;
    private int logUploadInterval; // 单位：分钟
    private String logUploadEndpoint;
    private int logRetentionDays;
    private LocalDateTime serverTime;
    private String appVersion;
    private boolean forceUpdate;
    private String updateUrl;

    public AppConfigResponse() {
        this.serverTime = LocalDateTime.now();
    }

    public boolean isEnableLogUpload() {
        return enableLogUpload;
    }

    public void setEnableLogUpload(boolean enableLogUpload) {
        this.enableLogUpload = enableLogUpload;
    }

    public int getLogUploadInterval() {
        return logUploadInterval;
    }

    public void setLogUploadInterval(int logUploadInterval) {
        this.logUploadInterval = logUploadInterval;
    }

    public String getLogUploadEndpoint() {
        return logUploadEndpoint;
    }

    public void setLogUploadEndpoint(String logUploadEndpoint) {
        this.logUploadEndpoint = logUploadEndpoint;
    }

    public int getLogRetentionDays() {
        return logRetentionDays;
    }

    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }
}
