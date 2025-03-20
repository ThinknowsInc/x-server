package com.thinknows.x_server.model.response;

import java.time.LocalDateTime;

import com.thinknows.x_server.model.DeviceInfo;

public class DeviceSession {
    private String sessionId;
    private DeviceInfo deviceInfo;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;
    private String ipAddress;
    private boolean currentDevice;

    public DeviceSession() {
    }

    public DeviceSession(String sessionId, DeviceInfo deviceInfo, LocalDateTime loginTime, 
                         LocalDateTime lastActivityTime, String ipAddress, boolean currentDevice) {
        this.sessionId = sessionId;
        this.deviceInfo = deviceInfo;
        this.loginTime = loginTime;
        this.lastActivityTime = lastActivityTime;
        this.ipAddress = ipAddress;
        this.currentDevice = currentDevice;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(boolean currentDevice) {
        this.currentDevice = currentDevice;
    }
}
