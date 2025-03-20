package com.thinknows.x_server.model.request;

import com.thinknows.x_server.model.DeviceInfo;

public class TwoFactorVerifyRequest {
    private String twoFactorToken;
    private String code;
    private DeviceInfo deviceInfo;

    public TwoFactorVerifyRequest() {
    }

    public TwoFactorVerifyRequest(String twoFactorToken, String code) {
        this.twoFactorToken = twoFactorToken;
        this.code = code;
    }

    public String getTwoFactorToken() {
        return twoFactorToken;
    }

    public void setTwoFactorToken(String twoFactorToken) {
        this.twoFactorToken = twoFactorToken;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
