package com.example.gateway.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
public class RequestParamVerifyFilter implements GlobalFilter, Ordered{

	@Override
	public int getOrder() {
		return -100;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		try {
			ServerHttpRequest request = exchange.getRequest();
			HttpHeaders headers = request.getHeaders();
			if(headers.get("sign") == null && headers.get("timestamp") == null) {
				throw new Exception("参数错误，缺少必要参数!");
			}
			String timestamp = headers.get("timestamp").get(0);
			
			MultiValueMap<String, String> queryParams = request.getQueryParams();
			Set<String> keySet = queryParams.keySet();
			String[] keys = (String[]) keySet.toArray();
			Arrays.sort(keys);
			StringBuffer temp = new StringBuffer();
			for (String key : keys) {
				List<String> list = queryParams.get(key);
				for (String param : list) {
					temp.append("&");
					temp.append(key).append("=");
					if(temp != null) {
						param = param.toLowerCase();
					}
					temp.append(param);
				}
			}
			String sign = headers.get("sign").get(0);
			
			return chain.filter(exchange);
		} catch (Exception e) {
			e.printStackTrace();
			return exchange.getResponse().setComplete();
		}
		
		
	}

}
