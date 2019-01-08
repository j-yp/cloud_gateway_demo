package com.example.gateway;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import com.example.gateway.filter.RateLimitByIpGatewayFilter;
import com.example.gateway.filter.RequestTimeFilter;

@SpringBootApplication
public class SpringCloudGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringCloudGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/customer/**")
                        .filters(f -> f.filter(new RequestTimeFilter())
                                .addResponseHeader("X-Response-Default-Foo", "Default-Bar"))
                        .uri("https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&ch=&tn=baiduerr&bar=&wd=123")
                        .order(0)
                        .id("customer_filter_router")
                )
				.route(r -> r.path("/throttle/customer/**")
						.filters(f -> f.filter(new RateLimitByIpGatewayFilter(10, 1, Duration.ofSeconds(1))))
						.uri("https://www.baidu.com/").order(0).id("throttle_customer_service"))
				.build();

	}

}
