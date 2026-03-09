package io.binghe.seckill.dubbo.interfaces.goods;

import io.binghe.seckill.common.model.dto.SeckillGoodsDTO;
public interface SeckillGoodsDubboService {

    /**
     * 根据id和版本号获取商品详情
     */
    SeckillGoodsDTO getSeckillGoods(Long id, Long version);

    /**
     * 扣减数据库库存
     */
    boolean updateDbAvailableStock(Integer count, Long id);
}
