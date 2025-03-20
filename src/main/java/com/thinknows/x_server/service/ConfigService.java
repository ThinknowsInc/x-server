package com.thinknows.x_server.service;

import com.thinknows.x_server.model.response.AppConfigResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    @Autowired
    private LogService logService;
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    @Value("${app.force-update:false}")
    private boolean forceUpdate;
    
    @Value("${app.update-url:}")
    private String updateUrl;
    
    /**
     * 获取应用配置
     * @param clientVersion 客户端版本
     * @return 应用配置
     */
    public AppConfigResponse getAppConfig(String clientVersion) {
        AppConfigResponse config = new AppConfigResponse();
        
        // 设置日志上传配置
        config.setEnableLogUpload(logService.isEnableLogUpload());
        config.setLogUploadInterval(logService.getLogUploadInterval());
        config.setLogUploadEndpoint("/api/v1/logs/upload");
        config.setLogRetentionDays(logService.getLogRetentionDays());
        
        // 设置应用版本信息
        config.setAppVersion(appVersion);
        
        // 检查是否需要更新
        if (clientVersion != null && !clientVersion.equals(appVersion)) {
            config.setForceUpdate(forceUpdate);
            config.setUpdateUrl(updateUrl);
        } else {
            config.setForceUpdate(false);
        }
        
        return config;
    }
}
