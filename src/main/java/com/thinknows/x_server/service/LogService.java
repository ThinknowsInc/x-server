package com.thinknows.x_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

@Service
public class LogService {

    @Value("${app.log.storage.path:./logs}")
    private String logStoragePath;

    @Value("${app.log.enable-upload:true}")
    private boolean enableLogUpload;

    @Value("${app.log.upload-interval:60}")
    private int logUploadInterval;

    @Value("${app.log.retention-days:30}")
    private int logRetentionDays;

    /**
     * 保存上传的日志文件
     * @param file 压缩的日志文件
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param logType 日志类型
     * @return 保存的文件路径
     */
    public String saveLogFile(MultipartFile file, String deviceId, String userId, String logType) throws IOException {
        // 创建存储目录
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String userFolder = userId != null ? userId : "anonymous";
        String deviceFolder = deviceId != null ? deviceId : "unknown-device";
        
        Path storagePath = Paths.get(logStoragePath, dateFolder, userFolder, deviceFolder);
        Files.createDirectories(storagePath);
        
        // 生成唯一文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String fileName = String.format("%s_%s_%s.log", timestamp, logType, uniqueId);
        
        Path targetPath = storagePath.resolve(fileName);
        
        // 解压并保存文件
        if (file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".gz")) {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(file.getInputStream());
                 FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } else {
            // 如果不是gzip格式，直接保存
            file.transferTo(targetPath);
        }
        
        // 清理过期日志
        cleanupOldLogs();
        
        return targetPath.toString();
    }
    
    /**
     * 清理过期日志
     */
    private void cleanupOldLogs() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(logRetentionDays);
            String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            File logDir = new File(logStoragePath);
            File[] dateDirs = logDir.listFiles();
            
            if (dateDirs != null) {
                for (File dateDir : dateDirs) {
                    if (dateDir.isDirectory() && dateDir.getName().compareTo(cutoffDateStr) < 0) {
                        deleteDirectory(dateDir);
                    }
                }
            }
        } catch (Exception e) {
            // 记录错误但不影响主流程
            System.err.println("Error cleaning up old logs: " + e.getMessage());
        }
    }
    
    /**
     * 递归删除目录
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    /**
     * 获取日志上传配置
     */
    public boolean isEnableLogUpload() {
        return enableLogUpload;
    }
    
    public int getLogUploadInterval() {
        return logUploadInterval;
    }
    
    public int getLogRetentionDays() {
        return logRetentionDays;
    }
}
