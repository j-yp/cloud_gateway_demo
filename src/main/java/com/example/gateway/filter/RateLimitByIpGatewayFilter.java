package com.example.gateway.filter;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import reactor.core.publisher.Mono;
/**
 * 令牌桶过滤器，根据ip限制访问
 * @author wisdom
 *
 */
public class RateLimitByIpGatewayFilter implements GatewayFilter, Ordered {
	private Logger log = LoggerFactory.getLogger(RateLimitByIpGatewayFilter.class);
	//令牌桶容量
	int capacity;
	//令牌桶每次加入令牌数量
	int refillTokens;
	//令牌桶加入令牌频率
	Duration refillDuration;

	public RateLimitByIpGatewayFilter() {
		super();
	}

	public RateLimitByIpGatewayFilter(int capacity, int refillTokens, Duration refillDuration) {
		super();
		this.capacity = capacity;
		this.refillTokens = refillTokens;
		this.refillDuration = refillDuration;
	}

	private static final Map<String, Bucket> CACHE = new ConcurrentHashMap<>();

	private Bucket createNewBucket() {
		Refill refill = Refill.greedy(refillTokens, refillDuration);
		Bandwidth limit = Bandwidth.classic(capacity, refill);
		return Bucket4j.builder().addLimit(limit).build();
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
		Bucket bucket = CACHE.computeIfAbsent(ip, k -> createNewBucket());
		log.info("IP: " + ip + "，TokenBucket Available Tokens: " + bucket.getAvailableTokens());
		if (bucket.tryConsume(1)) {
			return chain.filter(exchange);
		} else {
			exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
			return exchange.getResponse().setComplete();
		}
	}

	@Override
	public int getOrder() {
		return -1000;
	}

}
