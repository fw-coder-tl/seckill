package io.binghe.seckill.dubbo.interfaces.goods;

import io.binghe.seckill.common.model.dto.SeckillGoodsDTO;
import org.dromara.hmily.annotation.Hmily;

public interface SeckillGoodsDubboService {

    /**
     * 根据id和版本号获取商品详情
     */
    SeckillGoodsDTO getSeckillGoods(Long id, Long version);

    /**
     * 扣减数据库库存
     */
    boolean updateDbAvailableStock(Integer count, Long id);

    /**
     * 扣减商品库存
     */
    @Hmily
    boolean updateAvailableStock(Integer count, Long id, Long txNo);
}
