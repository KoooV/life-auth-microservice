package com.kov.lifeauthmicroservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class WebConfig {

    //Этот фильтр читает заголовки Forwarded/X-Forwarded-For и корректно подставляет request.getRemoteAddr().
    //Работает только если запросы идут через доверенный reverse-proxy (Ingress/Nginx).
    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter(){
        return new ForwardedHeaderFilter();
    }
}
