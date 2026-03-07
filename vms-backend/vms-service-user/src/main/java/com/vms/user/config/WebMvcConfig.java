package com.vms.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 * 配置静态资源访问路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保上传目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 确保路径以 / 结尾
        String path = uploadPath.endsWith("/") || uploadPath.endsWith("\\")
                ? uploadPath
                : uploadPath + "/";

        // 确保URL前缀以 / 结尾
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";

        // 映射静态资源路径
        // 访问 URL: /uploads/avatar/xxx.jpg -> 物理路径: D:/vms-upload/avatar/xxx.jpg
        registry.addResourceHandler(prefix + "**")
                .addResourceLocations("file:" + path);
    }
}