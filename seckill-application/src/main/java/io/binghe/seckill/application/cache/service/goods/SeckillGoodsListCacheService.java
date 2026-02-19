package io.binghe.seckill.application.cache.service.goods;

import io.binghe.seckill.application.cache.model.SeckillBusinessCache;
import io.binghe.seckill.application.cache.service.common.SeckillCacheService;
import io.binghe.seckill.domain.model.entity.SeckillGoods;

import java.util.List;

/**
 * 获取商品列表缓存接口
 */
public interface SeckillGoodsListCacheService extends SeckillCacheService {

    /**
     * 获取缓存中的商品列表
     */
    SeckillBusinessCache<List<SeckillGoods>> getCachedGoodsList(Long activityId, Long version);


    /**
     * 更新缓存数据
     */
    SeckillBusinessCache<List<SeckillGoods>> tryUpdateSeckillGoodsCacheByLock(Long activityId);
}
