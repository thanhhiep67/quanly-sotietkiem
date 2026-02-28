package vn.edu.taydo.quanly_sotietkiem.config;

import java.util.Arrays;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/**") // kiểm tra tất cả endpoint
                .excludePathPatterns(
                        "/login",
                        "/register"
                );
    }

}

