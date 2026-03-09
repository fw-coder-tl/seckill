package io.binghe.seckill.application.cache.service;

import io.binghe.seckill.common.cache.model.SeckillBusinessCache;
import io.binghe.seckill.common.cache.service.SeckillCacheService;
import io.binghe.seckill.goods.domain.model.entity.SeckillGoods;
public interface SeckillGoodsCacheService extends SeckillCacheService {

    /**
     * 获取商品信息
     */
    SeckillBusinessCache<SeckillGoods> getSeckillGoods(Long goodsId, Long version);

    /**
     * 更新缓存
     */
    SeckillBusinessCache<SeckillGoods> tryUpdateSeckillGoodsCacheByLock(Long goodsId, boolean doubleCheck);
}
