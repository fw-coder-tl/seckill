package io.binghe.seckill.application.service;

/**
 * redis服务接口
 */
public interface RedisService {
    /**
     * 设置缓存
     */
    void set(String key,Object value);
}
