package br.com.mkcf.config.redis;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.HashSet;

public class SafeFallbackCacheManager implements CacheManager {

    private final CacheManager primary;
    private final CacheManager fallback;

    public SafeFallbackCacheManager(CacheManager primary, CacheManager fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public Cache getCache(String name) {
        return new SafeCache(primary.getCache(name), fallback.getCache(name));
    }

    @Override
    public Collection<String> getCacheNames() {
        return new HashSet<>(primary.getCacheNames());
    }
}

