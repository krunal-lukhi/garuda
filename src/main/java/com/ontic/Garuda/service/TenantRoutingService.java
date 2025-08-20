package com.ontic.Garuda.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.ontic.Garuda.model.PlanetConfig;
import com.ontic.Garuda.model.ResolvedURI;
import com.ontic.Garuda.model.TenantPlanetMapping;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Service
public class TenantRoutingService {
    private final ReactiveMongoTemplate mongoTemplate;

    private final LoadingCache<String, Mono<TenantPlanetMapping>> tenantPlanetMappingCache =
            Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(5))
                    .build(new CacheLoader<>() {
                        @Override
                        public @Nullable Mono<TenantPlanetMapping> load(String tenantId) throws Exception {
                            Query query = Query.query(Criteria.where(TenantPlanetMapping.FM.TENANT_ID).is(tenantId));
                            return mongoTemplate.findOne(query, TenantPlanetMapping.class);
                        }
                    });

    private final LoadingCache<String, Mono<PlanetConfig>> planetConfigCache =
            Caffeine.newBuilder().maximumSize(1_000).expireAfterWrite(Duration.ofMinutes(5))
                    .build(new CacheLoader<>() {
                        @Override
                        public @Nullable Mono<PlanetConfig> load(String nebulaId) throws Exception {
                            Query query = Query.query(Criteria.where(PlanetConfig.FM.ID).is(nebulaId));
                            return mongoTemplate.findOne(query, PlanetConfig.class);
                        }
                    });

    public TenantRoutingService(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<ResolvedURI> resolveTarget(String tenantId, URI incoming) {
        final String path = incoming.getRawPath();
        final String query = incoming.getRawQuery();

        return tenantPlanetMappingCache.get(tenantId)
                .switchIfEmpty(reactor.core.publisher.Mono.error(new IllegalArgumentException("Unknown tenant: " + tenantId)))
                .flatMap(tc -> planetConfigCache.get(tc.getPlanetId())
                        .switchIfEmpty(reactor.core.publisher.Mono.error(new IllegalStateException("Missing cluster: " + tc.getPlanetId())))
                        .map(cfg -> buildTarget(cfg, path, query)));
    }

    private ResolvedURI buildTarget(PlanetConfig planetConfig, String path, String query) {
        String base = normalizeBase(planetConfig.getBaseUri());
        var b = org.springframework.web.util.UriComponentsBuilder.fromUriString(base)
                .path(path);
        if (query != null) b.query(query);
        return new ResolvedURI(URI.create(b.build(true).toUriString()), planetConfig.getId());
    }


    private String normalizeBase(String baseUri) {
        var u = java.net.URI.create(baseUri);
        if (u.getScheme() == null || u.getHost() == null) {
            throw new IllegalStateException("baseUri must be absolute (scheme + host): " + baseUri);
        }
        return org.springframework.web.util.UriComponentsBuilder.newInstance()
                .scheme(u.getScheme()).host(u.getHost()).port(u.getPort()).path(u.getPath())
                .build().toUriString().replaceAll("/+$", "");
    }
}
