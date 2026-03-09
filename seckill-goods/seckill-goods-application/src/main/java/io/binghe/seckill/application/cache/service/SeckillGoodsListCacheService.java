package io.binghe.seckill.application.cache.service;

import io.binghe.seckill.common.cache.model.SeckillBusinessCache;
import io.binghe.seckill.common.cache.service.SeckillCacheService;
import io.binghe.seckill.goods.domain.model.entity.SeckillGoods;

import java.util.List;
public interface SeckillGoodsListCacheService extends SeckillCacheService {

    /**
     * 获取缓存中的商品列表
     */
    SeckillBusinessCache<List<SeckillGoods>> getCachedGoodsList(Long activityId, Long version);


    /**
     * 更新缓存数据
     */
    SeckillBusinessCache<List<SeckillGoods>> tryUpdateSeckillGoodsCacheByLock(Long activityId, boolean doubleCheck);
}
