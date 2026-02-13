package io.binghe.seckill.infrastructure.cache.local.guava;

import com.google.common.cache.Cache;
import io.binghe.seckill.infrastructure.cache.local.LocalCacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 基于Guava实现的本地缓存
 */
@Service
@ConditionalOnProperty(name = "local.cache.type", havingValue = "guava")
public class GuavaLocalCacheService<K, V> implements LocalCacheService<K, V> {
    //本地缓存，基于Guava实现
    private final Cache<K, V> cache = LocalCacheFactory.getLocalCache();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V getIfPresent(Object key) {
        return cache.getIfPresent(key);
    }
}
