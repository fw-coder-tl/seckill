package io.binghe.seckill.application.cache.service.goods;

import io.binghe.seckill.application.cache.model.SeckillBusinessCache;
import io.binghe.seckill.application.cache.service.common.SeckillCacheService;
import io.binghe.seckill.domain.model.entity.SeckillGoods;

/**
 * 商品详情缓存接口
 */
public interface SeckillGoodsCacheService extends SeckillCacheService {

    /**
     * 获取商品信息
     */
    SeckillBusinessCache<SeckillGoods> getSeckillGoods(Long goodsId, Long version);

    /**
     * 更新缓存
     */
    SeckillBusinessCache<SeckillGoods> tryUpdateSeckillGoodsCacheByLock(Long goodsId);
}
