package io.binghe.seckill.application.cache.service.activity;

import io.binghe.seckill.application.cache.model.SeckillBusinessCache;
import io.binghe.seckill.application.cache.service.common.SeckillCacheService;
import io.binghe.seckill.domain.model.entity.SeckillActivity;

/**
 * 活动详情缓存秒杀接口
 */
public interface SeckillActivityCacheService extends SeckillCacheService {

    /**
     * 根据商品id获取商品信息
     */
    SeckillBusinessCache<SeckillActivity> getCachedSeckillActivity(Long activityId, Long version);

    /**
     * 跟新缓存数据
     */
    SeckillBusinessCache<SeckillActivity> tryUpdateSeckillActivityCacheByLock(Long activityId,boolean doubleCheck);
}
