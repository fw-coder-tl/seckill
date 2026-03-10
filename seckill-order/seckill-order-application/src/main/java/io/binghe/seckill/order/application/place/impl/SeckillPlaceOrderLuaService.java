package io.binghe.seckill.order.application.place.impl;

import io.binghe.seckill.common.constants.SeckillConstants;
import io.binghe.seckill.common.exception.ErrorCode;
import io.binghe.seckill.common.exception.SeckillException;
import io.binghe.seckill.common.model.dto.SeckillGoodsDTO;
import io.binghe.seckill.order.application.command.SeckillOrderCommand;
import io.binghe.seckill.order.application.place.SeckillPlaceOrderService;
import io.binghe.seckill.order.domain.model.entity.SeckillOrder;
import org.dromara.hmily.annotation.HmilyTCC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "place.order.type", havingValue = "lua")
public class SeckillPlaceOrderLuaService extends SeckillPlaceOrderBaseService implements SeckillPlaceOrderService {
    private final Logger logger = LoggerFactory.getLogger(SeckillPlaceOrderLuaService.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTCC(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand, Long txNo) {
        //幂等处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)) {
            logger.warn("placeOrder|基于Lua脚本防止超卖的方法已经执行过Try方法,txNo:{}", txNo);
            return txNo;
        }
        //悬挂处理
        if (distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CONFIRM_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)
                || distributedCacheService.isMemberSet(SeckillConstants.getKey(SeckillConstants.ORDER_CANCEL_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo)) {
            logger.warn("placeOrder|基于Lua脚本防止超卖的方法已经执行过Condirm方法或者Cancel方法,txNo:{}", txNo);
            return txNo;
        }
        SeckillGoodsDTO seckillGoods = seckillGoodsDubboService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());
        //检测商品
        this.checkSeckillGoods(seckillOrderCommand, seckillGoods);
        //获取商品限购信息
        Object limitObj = distributedCacheService.getObject(SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_LIMIT_KEY_PREFIX, String.valueOf(seckillOrderCommand.getGoodsId())));
        //如果从Redis获取到的限购信息为null，则说明商品已经下线
        if (limitObj == null) {
            throw new SeckillException(ErrorCode.GOODS_OFFLINE);
        }

        if (Integer.parseInt(String.valueOf(limitObj)) < seckillOrderCommand.getQuantity()) {
            throw new SeckillException(ErrorCode.BEYOND_LIMIT_NUM);
        }
        String key = SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, String.valueOf(seckillOrderCommand.getGoodsId()));
        Long result = distributedCacheService.decrementByLua(key, seckillOrderCommand.getQuantity());
        //是否保存try日志
        boolean isSaveTryLog = false;
        try {
            this.checkResult(result);
            SeckillOrder seckillOrder = this.buildSeckillOrder(userId, seckillOrderCommand, seckillGoods);
            //巧妙的使用事务编号作为订单id，避免过多资源浪费，也可以使用其他方式生成订单id
            seckillOrder.setId(txNo);
            seckillOrderDomainService.saveSeckillOrder(seckillOrder);
            //保存try日志
            distributedCacheService.addSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            isSaveTryLog = true;
            seckillGoodsDubboService.updateAvailableStock(seckillOrderCommand.getQuantity(), seckillOrderCommand.getGoodsId(), txNo);
//            int i = 1 / 0;
            return seckillOrder.getId();
        } catch (Exception e) {
            if (isSaveTryLog) {
                distributedCacheService.removeSet(SeckillConstants.getKey(SeckillConstants.ORDER_TRY_KEY_PREFIX, SeckillConstants.ORDER_KEY), txNo);
            }
            //将内存中的库存增加回去
            distributedCacheService.incrementByLua(key, seckillOrderCommand.getQuantity());
            if (e instanceof SeckillException) {
                throw e;
            } else {
                throw new SeckillException(ErrorCode.ORDER_FAILED);
            }
        }
    }

    private void checkResult(Long result) {
        if (result == SeckillConstants.LUA_RESULT_GOODS_STOCK_NOT_EXISTS) {
            throw new SeckillException(ErrorCode.STOCK_IS_NULL);
        }
        if (result == SeckillConstants.LUA_RESULT_GOODS_STOCK_PARAMS_LT_ZERO) {
            throw new SeckillException(ErrorCode.PARAMS_INVALID);
        }
        if (result == SeckillConstants.LUA_RESULT_GOODS_STOCK_LT_ZERO) {
            throw new SeckillException(ErrorCode.STOCK_LT_ZERO);
        }
    }
}
