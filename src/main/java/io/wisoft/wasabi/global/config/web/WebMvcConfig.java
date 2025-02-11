package io.wisoft.wasabi.global.config.web;

import io.wisoft.wasabi.global.config.web.resolver.AnyoneResolver;
import io.wisoft.wasabi.global.config.web.resolver.MemberIdResolver;
import io.wisoft.wasabi.global.config.web.filter.LogFilter;
import io.wisoft.wasabi.global.config.web.filter.SessionFilter;
import io.wisoft.wasabi.global.config.web.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@EnableAspectJAutoProxy
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;
    private final MemberIdResolver memberIdResolver;
    private final AnyoneResolver anyoneResolver;

    @Value("${spring.cors.allowed-mapping}")
    private String corsMapping;

    @Value("${spring.cors.allowed-origins}")
    private String[] corsOrigins;

    @Value("${spring.cors.allowed-headers}")
    private String[] corsHeaders;

    @Value("${spring.cors.allowed-methods}")
    private String[] corsMethods;

    public WebMvcConfig(final AdminInterceptor adminInterceptor,
                        final MemberIdResolver memberIdResolver,
                        final AnyoneResolver anyoneResolver) {
        this.adminInterceptor = adminInterceptor;
        this.memberIdResolver = memberIdResolver;
        this.anyoneResolver = anyoneResolver;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberIdResolver);
        resolvers.add(anyoneResolver);
    }

    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        return new FilterRegistrationBean<>(new LogFilter());
    }

    @Bean
    public FilterRegistrationBean<SessionFilter> sessionFilter() {
        return new FilterRegistrationBean<>(new SessionFilter());
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping(corsMapping)
                .allowedOriginPatterns(corsOrigins)
                .allowedHeaders(corsHeaders)
                .allowedMethods(corsMethods)
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }
}
