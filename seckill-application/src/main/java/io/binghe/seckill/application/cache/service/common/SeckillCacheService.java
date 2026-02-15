package io.binghe.seckill.application.cache.service.common;

/**
 * 通用缓存接口
 */
public interface SeckillCacheService {

    String buildCacheKey(Object key);
}
