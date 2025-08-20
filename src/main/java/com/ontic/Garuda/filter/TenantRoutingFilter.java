package com.ontic.Garuda.filter;

import com.ontic.Garuda.commons.ReferenceConstants;
import com.ontic.Garuda.service.TenantRoutingService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
public class TenantRoutingFilter implements GlobalFilter {

    private final TenantRoutingService routingService;

    public TenantRoutingFilter(TenantRoutingService routingService) {
        this.routingService = routingService;
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {
                    Object p = auth.getPrincipal();
                    if (!(p instanceof Jwt jwt))
                        return Mono.error(new AccessDeniedException("JWT required"));
                    String tenantId = jwt.getClaimAsString(ReferenceConstants.TENANT_ID_CLAIM);
                    if (tenantId == null || tenantId.isBlank())
                        return Mono.error(new AccessDeniedException("Missing tenant claim"));

                    URI incoming = exchange.getRequest().getURI();
                    return routingService.resolveTarget(tenantId, incoming)
                            .flatMap(res -> {
                                var req = exchange.getRequest().mutate()
                                        .header("X-Tenant-Id", tenantId)
                                        .header("X-Cluster-Name", res.getPlanetId())
                                        .build();
                                exchange.getAttributes().put(
                                        ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR,
                                        res.getTargetUri());
                                return chain.filter(exchange.mutate().request(req).build());
                            });
                })
                .onErrorResume(ex -> {
                    var status = (ex instanceof AccessDeniedException) ? HttpStatus.FORBIDDEN
                            : (ex instanceof IllegalArgumentException) ? HttpStatus.UNAUTHORIZED
                            : HttpStatus.BAD_GATEWAY;
                    exchange.getResponse().setStatusCode(status);
                    byte[] body = ("{\"error\":\"" + ex.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8);
                    return exchange.getResponse().writeWith(
                            Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
                });
    }

}
