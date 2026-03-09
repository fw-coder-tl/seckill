package io.binghe.seckill.common.cache.service;
public interface SeckillCacheService {
    /**
     * 构建缓存的key
     */
    String buildCacheKey(Object key);
}
