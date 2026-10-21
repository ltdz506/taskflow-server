package org.ltdz.taskflow.config;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.auth.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 拦截器 + 开发期 CORS。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                // 登录、Agent 换取 token 无需鉴权；WebSocket 握手在 handler 内单独校验
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/agent/getUserIllustration",
                        "/ws",
                        "/ws/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 开发期允许前端跨域访问；上线时应收紧为具体域名
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
