package io.binghe.seckill.application.cache.service.activity;

import io.binghe.seckill.application.cache.model.SeckillBusinessCache;
import io.binghe.seckill.application.cache.service.common.SeckillCacheService;
import io.binghe.seckill.domain.model.entity.SeckillActivity;

import java.util.List;

/**
 * 加有缓存的秒杀活动接口服务
 */
public interface SeckillActivityListCacheService extends SeckillCacheService {

    /**
     * 增加二级缓存根据状态获取活动列表
     */
    SeckillBusinessCache<List<SeckillActivity>> getCachedActivities(Integer status, Long version);

    /**
     * 更新缓存数据
     */
    SeckillBusinessCache<List<SeckillActivity>> tryUpdateSeckillActivityCacheByLock(Integer status, boolean doubleCheck);
}
