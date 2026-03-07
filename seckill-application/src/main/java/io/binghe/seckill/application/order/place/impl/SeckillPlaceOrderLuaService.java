package io.binghe.seckill.application.order.place.impl;

import io.binghe.seckill.application.command.SeckillOrderCommand;
import io.binghe.seckill.application.order.place.SeckillPlaceOrderService;
import io.binghe.seckill.application.service.SeckillGoodsService;
import io.binghe.seckill.domain.constants.SeckillConstants;
import io.binghe.seckill.domain.model.dto.SeckillGoodsDTO;
import io.binghe.seckill.domain.model.entity.SeckillOrder;
import io.binghe.seckill.domain.service.SeckillOrderDomainService;
import io.binghe.seckill.infrastructure.cache.distribute.DistributedCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "place.order.type", havingValue = "lua")
public class SeckillPlaceOrderLuaService implements SeckillPlaceOrderService {

    @Autowired
    private SeckillOrderDomainService seckillOrderDomainService;
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private DistributedCacheService distributedCacheService;

    @Override
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        boolean decrementStock = false;
        SeckillGoodsDTO seckillGoods = seckillGoodsService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());
        //检测商品
        this.checkSeckillGoods(seckillOrderCommand, seckillGoods);
        String key = SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, String.valueOf(seckillOrderCommand.getGoodsId()));
        Long result = distributedCacheService.decrementByLua(key, seckillOrderCommand.getQuantity());
        distributedCacheService.checkResult(result);
        decrementStock = true;
        try{
            SeckillOrder seckillOrder = this.buildSeckillOrder(userId, seckillOrderCommand, seckillGoods);
            seckillOrderDomainService.saveSeckillOrder(seckillOrder);
            seckillGoodsService.updateDbAvailableStock(seckillOrderCommand.getQuantity(), seckillOrderCommand.getGoodsId());
            return seckillOrder.getId();
        }catch (Exception e){
            //将内存中的库存增加回去
            if (decrementStock){
                distributedCacheService.incrementByLua(key, seckillOrderCommand.getQuantity());
            }
            throw e;
        }
    }
}
