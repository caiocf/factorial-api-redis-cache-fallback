package br.com.mkcf.config.redis;

import org.springframework.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class SafeCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(SafeCache.class);

    private final Cache primary;
    private final Cache fallback;

    public SafeCache(Cache primary, Cache fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public String getName() {
        return primary.getName();
    }

    @Override
    public Object getNativeCache() {
        return primary.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        try {
            return primary.get(key);
        } catch (Exception e) {
            log.warn("[SafeCache] Falha ao ler chave '{}' do Redis. Usando fallback. Erro: {}", key, e.getMessage());
            return fallback.get(key);
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        try {
            return primary.get(key, type);
        } catch (Exception e) {
            log.warn("[SafeCache] Falha ao ler (com tipo) chave '{}' do Redis. Usando fallback. Erro: {}", key, e.getMessage());
            return fallback.get(key, type);
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            return primary.get(key, valueLoader);
        } catch (Exception e) {
            log.warn("[SafeCache] Falha ao carregar chave '{}' do Redis com Callable. Usando fallback. Erro: {}", key, e.getMessage());
            return fallback.get(key, valueLoader);
        }
    }

    @Override
    public void put(Object key, Object value) {
        try {
            primary.put(key, value);
        } catch (Exception e) {
            log.warn("[SafeCache] Falha ao salvar chave '{}' no Redis. Usando fallback. Erro: {}", key, e.getMessage());
            fallback.put(key, value);
        }
    }

    @Override
    public void evict(Object key) {
        try {
            primary.evict(key);
        } catch (Exception e) {
            log.warn("[SafeCache] Falha ao remover chave '{}' do Redis. Usando fallback. Erro: {}", key, e.getMessage());
            fallback.evict(key);
        }
    }

    @Override
    public void clear() {
        try {
            primary.clear();
        } catch (Exception e) {
            log.warn("[SafeCache] Falha ao limpar cache no Redis. Usando fallback. Erro: {}", e.getMessage());
            fallback.clear();
        }
    }
}
