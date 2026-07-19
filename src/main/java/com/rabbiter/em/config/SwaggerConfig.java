package com.rabbiter.em.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 移除@EnableSwagger2（SpringDoc无需此注解）
public class SwaggerConfig {

    @Bean
    public OpenAPI createRestApi() {
        // 配置联系人信息
        Contact contact = new Contact()
                .name("开发者") // 联系人姓名
                .email("dev@example.com") // 联系人邮箱
                .url("https://example.com"); // 联系人网址

        // 配置API基本信息
        Info info = new Info()
                .title("SpringBoot整合OpenAPI 3.0") // 文档标题
                .description("电子商城系统API接口文档（详细信息）") // 文档描述
                .version("1.0") // 版本号

                .contact(contact); // 关联联系人信息

        // 返回OpenAPI配置
        return new OpenAPI().info(info);
    }
}
