package com.thinknows.x_server.controller;

import com.thinknows.x_server.model.request.LogUploadRequest;
import com.thinknows.x_server.model.response.ApiResponse;
import com.thinknows.x_server.model.response.AppConfigResponse;
import com.thinknows.x_server.service.ConfigService;
import com.thinknows.x_server.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class AppController {

    @Autowired
    private ConfigService configService;
    
    @Autowired
    private LogService logService;
    
    /**
     * 获取应用配置
     * @param clientVersion 客户端版本
     * @return 应用配置
     */
    @GetMapping("/app-config")
    public ResponseEntity<ApiResponse<AppConfigResponse>> getAppConfig(
            @RequestHeader(value = "X-App-Version", required = false) String clientVersion) {
        
        AppConfigResponse config = configService.getAppConfig(clientVersion);
        return ResponseEntity.ok(new ApiResponse<>(200, "Config retrieved successfully", config));
    }
    
    /**
     * 上传日志文件
     * @param file 压缩的日志文件
     * @param request 日志上传请求信息
     * @return 上传结果
     */
    @PostMapping(value = "/logs/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadLog(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute LogUploadRequest request) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(400, "Log file is empty", null));
            }
            
            String savedPath = logService.saveLogFile(
                    file, 
                    request.getDeviceId(), 
                    request.getUserId(),
                    request.getLogType());
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Log uploaded successfully", savedPath));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(500, "Failed to upload log: " + e.getMessage(), null));
        }
    }
}
