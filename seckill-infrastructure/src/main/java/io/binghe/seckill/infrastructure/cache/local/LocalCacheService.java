package io.binghe.seckill.infrastructure.cache.local;

/**
 * 本地缓存服务接口
 */
public interface LocalCacheService<K, V> {

    void put(K key, V value);

    V getIfPresent(Object key);
}
